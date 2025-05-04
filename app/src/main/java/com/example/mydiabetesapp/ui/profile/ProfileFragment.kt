package com.example.mydiabetesapp.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.mydiabetesapp.MainActivity
import com.example.mydiabetesapp.R
import com.example.mydiabetesapp.data.database.AppDatabase
import com.example.mydiabetesapp.data.database.UserProfile
import com.example.mydiabetesapp.databinding.FragmentProfileBinding
import com.example.mydiabetesapp.drive.DriveServiceHelper
import com.example.mydiabetesapp.repository.ProfileRepository
import com.example.mydiabetesapp.ui.viewmodel.ProfileViewModel
import com.example.mydiabetesapp.ui.viewmodel.ProfileViewModelFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {
    private var _b: FragmentProfileBinding? = null
    private val b get() = _b!!
    private lateinit var vm: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _b = FragmentProfileBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        b.toolbar.setNavigationOnClickListener { requireActivity().onBackPressed() }

        val genders = resources.getStringArray(R.array.gender_categories)
        b.spinnerCategory.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            genders
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        val dao = AppDatabase.getDatabase(requireContext()).userProfileDao()
        vm = ViewModelProvider(
            this,
            ProfileViewModelFactory(ProfileRepository(dao))
        )[ProfileViewModel::class.java]

        viewLifecycleOwner.lifecycleScope.launch {
            vm.profile.collect { p ->
                p?.let {
                    b.etNameInput.setText(it.name)
                    b.etAgeInput.setText(if (it.age == 0) "" else it.age.toString())
                    b.etHeightInput.setText(if (it.height == 0f) "" else it.height.toString())
                    b.etWeigthInput.setText(if (it.weight == 0f) "" else it.weight.toString())
                    b.spinnerCategory.setSelection(
                        genders.indexOf(it.gender).coerceAtLeast(0)
                    )
                }
            }
        }

        b.btnSave.setOnClickListener {
            val prof = gatherProfileFromUi()
            vm.updateProfile(prof)
            Toast.makeText(requireContext(), "Профиль сохранён", Toast.LENGTH_SHORT).show()
        }

        b.btnSignInDrive.setOnClickListener {
            (activity as? MainActivity)?.startGoogleSignIn()
        }

        b.btnExportDrive.setOnClickListener {
            val helper = (activity as? MainActivity)?.getDriveHelper()
            if (helper == null) {
                Toast.makeText(requireContext(),
                    "Сначала войдите в Google", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            lifecycleScope.launch {
                val p = vm.profile.first()
                val csv = buildCsv(p)
                try {
                    val file = helper.exportCsv(csv)
                    requireContext()
                        .getSharedPreferences("prefs", 0)
                        .edit()
                        .putString("lastDriveFileId", file.id)
                        .apply()

                    Toast.makeText(requireContext(),
                        "Экспортировано: ${file.name}\nID=${file.id}",
                        Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(requireContext(),
                        "Ошибка экспорта: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        b.btnImportDrive.setOnClickListener {
            val helper = (activity as? MainActivity)?.getDriveHelper()
            if (helper == null) {
                Toast.makeText(requireContext(),
                    "Сначала войдите в Google", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            lifecycleScope.launch {
                try {
                    val latest = helper.findLatestCsv()
                    if (latest == null) {
                        Toast.makeText(requireContext(),
                            "На вашем Drive нет экспортированных CSV", Toast.LENGTH_SHORT).show()
                        return@launch
                    }

                    val csv = helper.importCsv(latest.id)
                    val lines = csv.lines().filter { it.isNotBlank() }
                    if (lines.size < 2) {
                        Toast.makeText(requireContext(),
                            "CSV пуст или неверного формата", Toast.LENGTH_SHORT).show()
                        return@launch
                    }

                    val cols = lines[1].split(",")
                    if (cols.size < 6) {
                        Toast.makeText(requireContext(),
                            "Неправильный формат CSV", Toast.LENGTH_LONG).show()
                        return@launch
                    }

                    val prof = UserProfile(
                        id     = cols[0].toIntOrNull() ?: 1,
                        name   = cols[1],
                        gender = cols[2],
                        age    = cols[3].toIntOrNull() ?: 0,
                        height = cols[4].toFloatOrNull() ?: 0f,
                        weight = cols[5].toFloatOrNull() ?: 0f
                    )
                    vm.updateProfile(prof)
                    b.etNameInput.setText(prof.name)
                    b.etAgeInput.setText(prof.age.toString())
                    b.etHeightInput.setText(prof.height.toString())
                    b.etWeigthInput.setText(prof.weight.toString())
                    b.spinnerCategory.setSelection(
                        genders.indexOf(prof.gender).coerceAtLeast(0)
                    )

                    Toast.makeText(requireContext(),
                        "Импортировано строк: ${lines.size - 1}",
                        Toast.LENGTH_SHORT).show()

                } catch (e: Exception) {
                    Toast.makeText(requireContext(),
                        "Ошибка импорта: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun gatherProfileFromUi(): UserProfile =
        UserProfile(
            id     = 1,
            name   = b.etNameInput.text.toString(),
            gender = b.spinnerCategory.selectedItem.toString(),
            age    = b.etAgeInput.text.toString().toIntOrNull() ?: 0,
            height = b.etHeightInput.text.toString().toFloatOrNull() ?: 0f,
            weight = b.etWeigthInput.text.toString().toFloatOrNull() ?: 0f
        )

    private fun buildCsv(p: UserProfile?): String =
        buildString {
            append("id,name,gender,age,height,weight\n")
            p?.let {
                append("${it.id},${it.name},${it.gender},${it.age},${it.height},${it.weight}\n")
            }
        }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}
