package com.example.mydiabetesapp.feature.sync.ui

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mydiabetesapp.R
import com.example.mydiabetesapp.core.AppDatabase
import com.example.mydiabetesapp.feature.glucose.data.GlucoseRepository
import com.example.mydiabetesapp.feature.profile.data.UserProfile
import com.example.mydiabetesapp.feature.sync.viewmodel.SyncViewModel
import com.example.mydiabetesapp.feature.sync.viewmodel.SyncViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.ArrayDeque
import java.util.Calendar
import java.util.UUID

private const val TAG = "SyncFragment"

private val UUID_GLUCOSE_SERVICE     = UUID.fromString("00001808-0000-1000-8000-00805f9b34fb")
private val UUID_GLUCOSE_MEASUREMENT = UUID.fromString("00002a18-0000-1000-8000-00805f9b34fb")
private val UUID_GLUCOSE_CONTEXT     = UUID.fromString("00002a34-0000-1000-8000-00805f9b34fb")
private val UUID_GLUCOSE_RACP        = UUID.fromString("00002a52-0000-1000-8000-00805f9b34fb")
private val UUID_CLIENT_CHAR_CONFIG  = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")



class SyncFragment : Fragment() {

    private lateinit var scanButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var rvDevices: RecyclerView
    private lateinit var tvStatus: TextView

    private val devices = mutableListOf<ScanResult>()
    private lateinit var adapter: DeviceAdapter

    private var pendingBondDevice: BluetoothDevice? = null
    private var gatt: BluetoothGatt? = null
    private val writeQueue: ArrayDeque<() -> Unit> = ArrayDeque()

    private lateinit var viewModel: SyncViewModel

    private val bluetoothAdapter by lazy {
        (requireContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
    }
    private val scanner get() = bluetoothAdapter.bluetoothLeScanner

    private val bondReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context, intent: Intent) {
            if (intent.action != BluetoothDevice.ACTION_BOND_STATE_CHANGED) return
            val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
            val state  = intent.getIntExtra(
                BluetoothDevice.EXTRA_BOND_STATE,
                BluetoothDevice.BOND_NONE
            )
            if (device == pendingBondDevice) {
                when (state) {
                    BluetoothDevice.BOND_BONDED -> {
                        viewModel.updateStatus("Пара создана, подключаемся…")
                        pendingBondDevice = null
                        doConnectGatt(device!!)
                    }
                    BluetoothDevice.BOND_NONE -> {
                        viewModel.updateStatus("Пара не создана")
                        progressBar.visibility = View.GONE
                        pendingBondDevice = null
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        requireContext().registerReceiver(bondReceiver,
            IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        )
    }

    @SuppressLint("MissingPermission")
    override fun onPause() {
        super.onPause()
        requireContext().unregisterReceiver(bondReceiver)
        if (hasConnectPerm()) gatt?.close()
        gatt = null
    }

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { perms ->
        if (perms.values.all { it }) startScan()
        else {
            scanButton.isEnabled = true
            viewModel.updateStatus("Нужны разрешения BLE")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_sync, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val toolbar = view.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)

        toolbar.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_syncFragment_to_nav_home)
        }
        val repo = GlucoseRepository(AppDatabase.getDatabase(requireContext()).glucoseDao())
        viewModel = ViewModelProvider(this, SyncViewModelFactory(repo))[SyncViewModel::class.java]
        viewModel.status.observe(viewLifecycleOwner) { tvStatus.text = "Статус: $it" }

        lifecycleScope.launch {
            val dao = AppDatabase.getDatabase(requireContext()).userProfileDao()
            if (dao.getCount() == 0) dao.insertUserProfile(UserProfile(1, "Я", "", 0, 0f, 0f))
        }

        scanButton  = view.findViewById(R.id.btnScan)
        progressBar = view.findViewById(R.id.progressScan)
        rvDevices   = view.findViewById(R.id.rvDevices)
        tvStatus    = view.findViewById(R.id.tvStatus)

        adapter = DeviceAdapter(devices) { connectDevice(it) }
        rvDevices.layoutManager = LinearLayoutManager(requireContext())
        rvDevices.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )
        rvDevices.adapter = adapter

        scanButton.setOnClickListener { checkPermissionsAndScan() }
        viewModel.updateStatus("Нажмите «Найти»")
    }

    private fun hasConnectPerm() =
        ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.BLUETOOTH_CONNECT
        ) ==
                PackageManager.PERMISSION_GRANTED

    private fun checkPermissionsAndScan() {
        scanButton.isEnabled = false
        val need = listOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION
        ).filter { ContextCompat.checkSelfPermission(
            requireContext(),
            it
        ) != PackageManager.PERMISSION_GRANTED
        }

        if (need.isEmpty()) startScan() else permissionLauncher.launch(need.toTypedArray())
    }

    @SuppressLint("MissingPermission")
    private fun startScan() {
        viewModel.updateStatus("Сканирую…")
        progressBar.visibility = View.VISIBLE
        devices.clear(); adapter.notifyDataSetChanged()

        val settings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
        try { scanner.startScan(null, settings, bleScanCallback) }
        catch (e: SecurityException) { viewModel.updateStatus("Ошибка сканирования"); finishScan(); return }

        lifecycleScope.launch { delay(10_000); finishScan() }
    }

    @SuppressLint("MissingPermission")
    private fun finishScan() {
        try { scanner.stopScan(bleScanCallback) } catch (_:Throwable) {}
        progressBar.visibility = View.GONE
        scanButton.isEnabled = true
        viewModel.updateStatus(if (devices.isEmpty()) "Ничего не найдено" else "Выберите устройство")
    }

    private val bleScanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(ct: Int, res: ScanResult) {
            val addr = res.device.address
            val name = if (hasConnectPerm()) res.device.name ?: "<Unknown>" else "<no perm>"
            Log.d(TAG, "Found: $addr / $name")
            if (devices.none { it.device.address == addr }) {
                devices += res; adapter.notifyItemInserted(devices.size -1)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun connectDevice(res: ScanResult) {
        val d = res.device
        if (d.bondState == BluetoothDevice.BOND_NONE) {
            pendingBondDevice = d
            viewModel.updateStatus("Пары нет, создаем…")
            d.createBond()
        } else doConnectGatt(d)
    }

    @SuppressLint("MissingPermission")
    private fun doConnectGatt(d: BluetoothDevice) {
        viewModel.updateStatus("Подключаюсь…")
        progressBar.visibility = View.VISIBLE
        gatt?.close()
        gatt = d.connectGatt(requireContext(), false, gattCallback, BluetoothDevice.TRANSPORT_LE)
    }

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(g: BluetoothGatt, st: Int, newSt: Int) =
            requireActivity().runOnUiThread {
                if (newSt == BluetoothProfile.STATE_CONNECTED) {
                    viewModel.updateStatus("Connected, discovering…")
                    g.discoverServices()
                } else if (newSt == BluetoothProfile.STATE_DISCONNECTED) {
                    viewModel.updateStatus("Disconnected")
                    progressBar.visibility = View.GONE
                }
            }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(g: BluetoothGatt, st: Int) {
            writeQueue.clear()
            enqueueEnableNotification(g, UUID_GLUCOSE_MEASUREMENT)
            enqueueEnableNotification(g, UUID_GLUCOSE_CONTEXT)
            enqueueEnableIndicationAndWriteRACP(g)
            writeQueue.pollFirst()?.invoke()
        }

        override fun onDescriptorWrite(g: BluetoothGatt, d: BluetoothGattDescriptor, st: Int) {
            lifecycleScope.launch { delay(100); writeQueue.pollFirst()?.invoke() }
        }

        override fun onCharacteristicChanged(g: BluetoothGatt, chr: BluetoothGattCharacteristic) {
            if (chr.uuid == UUID_GLUCOSE_MEASUREMENT) {
                val m = parseMeasurement(chr.value)
                viewModel.saveMeasurement(m)
                requireActivity().runOnUiThread {
                    viewModel.updateStatus("Измерение: %.2f ммоль/л".format(m.value))
                }
            } else if (chr.uuid == UUID_GLUCOSE_RACP) {
                requireActivity().runOnUiThread {
                    viewModel.updateStatus("Все данные получены")
                    progressBar.visibility = View.GONE
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun enqueueEnableNotification(g: BluetoothGatt, uuid: UUID) {
        writeQueue += {
            g.getService(UUID_GLUCOSE_SERVICE)
                ?.getCharacteristic(uuid)
                ?.also { c ->
                    g.setCharacteristicNotification(c, true)
                    c.getDescriptor(UUID_CLIENT_CHAR_CONFIG)?.apply {
                        value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                        g.writeDescriptor(this)
                    }
                }
        }
    }

    @SuppressLint("MissingPermission")
    private fun enqueueEnableIndicationAndWriteRACP(g: BluetoothGatt) {
        writeQueue += {
            g.getService(UUID_GLUCOSE_SERVICE)
                ?.getCharacteristic(UUID_GLUCOSE_RACP)
                ?.also { c ->
                    g.setCharacteristicNotification(c, true)
                    c.getDescriptor(UUID_CLIENT_CHAR_CONFIG)?.apply {
                        value = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
                        g.writeDescriptor(this)
                    }
                }
        }
        writeQueue += {
            g.getService(UUID_GLUCOSE_SERVICE)
                ?.getCharacteristic(UUID_GLUCOSE_RACP)
                ?.also { c ->
                    c.value = byteArrayOf(0x01, 0x01)
                    g.writeCharacteristic(c)
                }
        }
    }

    private fun u16LE(b: ByteArray, pos: Int): Int =
        (b[pos].toInt() and 0xFF) or ((b[pos + 1].toInt() and 0xFF) shl 8)

    private fun sfloatToFloat(raw: Int): Float {
        val mant = (raw and 0x0FFF).let { if (it and 0x0800 != 0) it or -0x1000 else it }
        val exp  = (raw shr 12).let   { if (it and 0x8    != 0) it or -0x10   else it }
        return (mant * Math.pow(10.0, exp.toDouble())).toFloat()
    }

    private fun parseMeasurement(data: ByteArray): Measurement {
        if (data.size < 10) return Measurement("", "", 0f, "")

        var p = 0
        val flags = data[p++].toInt() and 0xFF

        p += 2

        val year  = u16LE(data, p).also { p += 2 }
        val month = data[p++].toInt() and 0xFF
        val day   = data[p++].toInt() and 0xFF
        val hour  = data[p++].toInt() and 0xFF
        val min   = data[p++].toInt() and 0xFF
        val sec   = data[p++].toInt() and 0xFF

        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, min)
            set(Calendar.SECOND, sec)
            set(Calendar.MILLISECOND, 0)
        }

        if (flags and 0x01 != 0) {
            val rawOffset = ByteBuffer.wrap(data, p, 2)
                .order(ByteOrder.LITTLE_ENDIAN)
                .short
                .toInt()
            p += 2
            cal.add(Calendar.MINUTE, rawOffset)
        }

        var mmol = 0f
        if (flags and 0x02 != 0) {
            val rawSfloat = u16LE(data, p)
            val conc      = sfloatToFloat(rawSfloat)
            p += 3

            mmol = if (flags and 0x04 == 0) {
                (conc * 100_000f) / 18f
            } else {
                conc * 1_000f
            }
        }

        if (flags and 0x08 != 0) p += 2

        val dateStr = "%02d.%02d.%04d".format(
            cal.get(Calendar.DAY_OF_MONTH),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.YEAR)
        )
        val timeStr = "%02d:%02d".format(
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE)
        )

        return Measurement(date = dateStr, time = timeStr, value = mmol, context = "")
    }
    data class Measurement(val date: String, val time: String, val value: Float, val context: String)

    private inner class DeviceAdapter(
        private val items: List<ScanResult>,
        private val onClick: (ScanResult) -> Unit
    ) : RecyclerView.Adapter<DeviceAdapter.VH>() {
        inner class VH(v: View) : RecyclerView.ViewHolder(v) {
            val tvName = v.findViewById<TextView>(R.id.tvDeviceName)
            val tvAddr = v.findViewById<TextView>(R.id.tvDeviceAddr)
            val tvRssi = v.findViewById<TextView>(R.id.tvDeviceRssi)
        }
        @SuppressLint("MissingPermission")
        override fun onBindViewHolder(h: VH, pos: Int) {
            val r = items[pos]
            h.tvName.text = if (hasConnectPerm()) r.device.name ?: "<Unknown>" else "<no perm>"
            h.tvAddr.text = r.device.address
            h.tvRssi.text = "${r.rssi} dBm"
            h.itemView.setOnClickListener { onClick(r) }
        }
        override fun onCreateViewHolder(p: ViewGroup, vt: Int) =
            VH(LayoutInflater.from(p.context).inflate(R.layout.item_ble_device, p, false))
        override fun getItemCount() = items.size
    }
}