package com.example.mydiabetesapp.ui.profile

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.mydiabetesapp.MainActivity
import com.example.mydiabetesapp.R
import com.example.mydiabetesapp.data.database.AppDatabase
import com.example.mydiabetesapp.data.database.GlucoseEntry
import com.example.mydiabetesapp.data.database.Hba1cEntry
import com.example.mydiabetesapp.data.database.UserProfile
import com.example.mydiabetesapp.data.database.WeightEntry
import com.example.mydiabetesapp.databinding.FragmentProfileBinding
import com.example.mydiabetesapp.repository.ProfileRepository
import com.example.mydiabetesapp.ui.viewmodel.ProfileViewModel
import com.example.mydiabetesapp.ui.viewmodel.ProfileViewModelFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {
    private var _b: FragmentProfileBinding? = null
    private val b get() = _b!!

    private lateinit var vm: ProfileViewModel
    private lateinit var genders: List<String>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _b = FragmentProfileBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dao = AppDatabase.getDatabase(requireContext()).userProfileDao()
        vm = ViewModelProvider(
            this,
            ProfileViewModelFactory(ProfileRepository(dao))
        )[ProfileViewModel::class.java]

        genders = resources.getStringArray(R.array.gender_categories).toList()
        b.spinnerCategory.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            genders
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        lifecycleScope.launch {
            vm.profile.first()?.let { bindProfile(it) }
        }

        b.btnSave.setOnClickListener {
            val prof = currentProfileFromInputs()
            lifecycleScope.launch {
                vm.updateProfile(prof)
                Toast.makeText(requireContext(), "Профиль сохранён", Toast.LENGTH_SHORT).show()
            }
        }

        b.btnSignInDrive.setOnClickListener {
            (activity as? MainActivity)?.startGoogleSignIn()
        }

        b.btnExportDrive.setOnClickListener {
            val helper = (activity as? MainActivity)?.getDriveHelper()
            if (helper == null) {
                Toast.makeText(requireContext(), "Сначала войдите в Google", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            lifecycleScope.launch {
                val profileCsv = buildString {
                    append("id,name,gender,age,height,weight\n")
                    vm.profile.first()?.let {
                        append("${it.id},${it.name},${it.gender},${it.age},${it.height},${it.weight}\n")
                    }
                }
                val glucoseCsv = buildString {
                    append("id,userId,date,time,glucose,category\n")
                    AppDatabase.getDatabase(requireContext())
                        .glucoseDao()
                        .getAllEntries()
                        .first()
                        .forEach { e ->
                            append("${e.id},${e.userId},${e.date},${e.time},${e.glucoseLevel},${e.category}\n")
                        }
                }
                val weightCsv = buildString {
                    append("id,userId,date,time,weight\n")
                    AppDatabase.getDatabase(requireContext())
                        .weightDao()
                        .getAllWeightEntries()
                        .first()
                        .forEach { e ->
                            append("${e.id},${e.userId},${e.date},${e.time},${e.weight}\n")
                        }
                }
                val hba1cCsv = buildString {
                    append("id,userId,date,hba1c\n")
                    AppDatabase.getDatabase(requireContext())
                        .hba1cDao()
                        .getAll()
                        .first()
                        .forEach { e ->
                            append("${e.id},${e.userId},${e.date},${e.hba1c}\n")
                        }
                }

                helper.exportAllAsZip(profileCsv, glucoseCsv, weightCsv, hba1cCsv)
                Toast.makeText(requireContext(), "Все данные экспортированы", Toast.LENGTH_LONG).show()
            }
        }
        val prefs   = requireContext().getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)
        val isDark  = prefs.getBoolean(MainActivity.KEY_DARK_MODE, false)
        b.switchTheme.isChecked = isDark
        b.switchTheme.setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean(MainActivity.KEY_DARK_MODE, checked).apply()
            AppCompatDelegate.setDefaultNightMode(
                if (checked) AppCompatDelegate.MODE_NIGHT_YES
                else          AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        b.btnImportDrive.setOnClickListener {
            val helper = (activity as? MainActivity)?.getDriveHelper()
            if (helper == null) {
                Toast.makeText(requireContext(), "Сначала войдите в Google", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            lifecycleScope.launch {
                val backup = helper.findLatestBackup()
                if (backup == null) {
                    Toast.makeText(requireContext(), "Бэкап не найден на Drive", Toast.LENGTH_LONG).show()
                    return@launch
                }
                val map = helper.importAllFromZip(backup.id)

                map["profile.csv"]?.lines()?.getOrNull(1)?.split(",")?.let { f ->
                    val imported = UserProfile(
                        id     = f[0].toInt(),
                        name   = f[1],
                        gender = f[2],
                        age    = f[3].toInt(),
                        height = f[4].toFloat(),
                        weight = f[5].toFloat()
                    )
                    vm.insertProfile(imported)
                    bindProfile(imported)
                }
                map["glucose.csv"]?.lines()?.drop(1)?.forEach { line ->
                    if (line.isBlank()) return@forEach
                    val f = line.split(",")
                    AppDatabase.getDatabase(requireContext()).glucoseDao().insertEntry(
                        GlucoseEntry(
                            id           = f[0].toInt(),
                            userId       = f[1].toInt(),
                            date         = f[2],
                            time         = f[3],
                            glucoseLevel = f[4].toFloat(),
                            category     = f[5]
                        )
                    )
                }
                map["weight.csv"]?.lines()?.drop(1)?.forEach { line ->
                    if (line.isBlank()) return@forEach
                    val f = line.split(",")
                    AppDatabase.getDatabase(requireContext()).weightDao().insertWeightEntry(
                        WeightEntry(
                            id     = f[0].toInt(),
                            userId = f[1].toInt(),
                            date   = f[2],
                            time   = f[3],
                            weight = f[4].toFloat()
                        )
                    )
                }
                map["hba1c.csv"]?.lines()?.drop(1)?.forEach { line ->
                    if (line.isBlank()) return@forEach
                    val f = line.split(",")
                    AppDatabase.getDatabase(requireContext()).hba1cDao().insert(
                        Hba1cEntry(
                            id      = f[0].toInt(),
                            userId  = f[1].toInt(),
                            date    = f[2],
                            hba1c   = f[3].toFloat()
                        )
                    )
                }

                Toast.makeText(requireContext(), "Все данные импортированы", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun currentProfileFromInputs(): UserProfile {
        return UserProfile(
            id     = 1,
            name   = b.etNameInput.text.toString(),
            gender = b.spinnerCategory.selectedItem.toString(),
            age    = b.etAgeInput.text.toString().toIntOrNull() ?: 0,
            height = b.etHeightInput.text.toString().toFloatOrNull() ?: 0f,
            weight = b.etWeigthInput.text.toString().toFloatOrNull() ?: 0f
        )
    }

    private fun bindProfile(p: UserProfile) {
        b.etNameInput.setText(p.name)
        b.etAgeInput.setText(if (p.age == 0) "" else p.age.toString())
        b.etHeightInput.setText(if (p.height == 0f) "" else p.height.toString())
        b.etWeigthInput.setText(if (p.weight == 0f) "" else p.weight.toString())
        b.spinnerCategory.setSelection(genders.indexOf(p.gender).coerceAtLeast(0))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}
