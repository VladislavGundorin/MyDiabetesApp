package com.example.mydiabetesapp.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.mydiabetesapp.R
import com.example.mydiabetesapp.databinding.FragmentHomeBinding
import com.google.android.material.snackbar.Snackbar

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val blePermissionLauncher = registerForActivityResult(
        RequestMultiplePermissions()
    ) { perms ->
        val granted = perms.entries.all { it.value }
        if (granted) {
            findNavController().navigate(R.id.action_nav_home_to_syncFragment)
        } else {
            binding.root.snackbar("Нужны разрешения для работы с глюкометром по BLE")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnAddGlucoseEntry.setOnClickListener {
            findNavController().navigate(R.id.action_nav_home_to_addEntryFragment)
        }
        binding.btnAddWeightEntry.setOnClickListener {
            findNavController().navigate(R.id.action_nav_home_to_addWeightFragment)
        }
        binding.btnExport.setOnClickListener {
            findNavController().navigate(R.id.action_nav_home_to_exportFragment)
        }
        binding.btnAddHba1cEntry.setOnClickListener {
            findNavController().navigate(R.id.action_nav_home_to_addHba1cFragment)
        }
        binding.btnNotifications.setOnClickListener {
            findNavController().navigate(R.id.nav_notification)
        }
        binding.btnAddPulse.setOnClickListener {
            findNavController().navigate(R.id.action_nav_home_to_addPressureFragment)
        }

        binding.btnAddSync.setOnClickListener {
            requestBlePermissionsAndSync()
        }
    }

    private fun requestBlePermissionsAndSync() {
        val needed = listOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ).filter {
            ContextCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED
        }

        if (needed.isEmpty()) {
            findNavController().navigate(R.id.action_nav_home_to_syncFragment)
        } else {
            blePermissionLauncher.launch(needed.toTypedArray())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

private fun View.snackbar(text: String) {
    Snackbar.make(this, text, Snackbar.LENGTH_LONG).show()
}
