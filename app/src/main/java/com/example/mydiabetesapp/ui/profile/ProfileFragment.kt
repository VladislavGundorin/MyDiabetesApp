package com.example.mydiabetesapp.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.ViewModelProvider
import com.example.mydiabetesapp.R
import com.example.mydiabetesapp.data.database.AppDatabase
import com.example.mydiabetesapp.data.database.UserProfile
import com.example.mydiabetesapp.databinding.FragmentProfileBinding
import com.example.mydiabetesapp.repository.ProfileRepository
import com.example.mydiabetesapp.ui.viewmodel.ProfileViewModel
import com.example.mydiabetesapp.ui.viewmodel.ProfileViewModelFactory
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.toolbar.setNavigationOnClickListener { requireActivity().onBackPressed() }

        val genderArray = resources.getStringArray(R.array.gender_categories)
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, genderArray)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = spinnerAdapter

        val db = AppDatabase.getDatabase(requireContext())
        val repository = ProfileRepository(db.userProfileDao())
        viewModel = ViewModelProvider(this, ProfileViewModelFactory(repository))
            .get(ProfileViewModel::class.java)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.profile.collect { profile ->
                if (profile != null) {
                    binding.etNameInput.setText(profile.name)
                    binding.etAgeInput.setText(if (profile.age == 0) "" else profile.age.toString())
                    binding.etHeightInput.setText(if (profile.height == 0f) "" else profile.height.toString())
                    binding.etWeigthInput.setText(if (profile.weight == 0f) "" else profile.weight.toString())
                    val index = genderArray.indexOf(profile.gender)
                    binding.spinnerCategory.setSelection(if (index != -1) index else 0)
                } else {
                    binding.etNameInput.setText("")
                    binding.etAgeInput.setText("")
                    binding.etHeightInput.setText("")
                    binding.etWeigthInput.setText("")
                    binding.spinnerCategory.setSelection(0)
                }
            }
        }

        binding.btnSave.setOnClickListener {
            val name = binding.etNameInput.text.toString().trim()
            val gender = binding.spinnerCategory.selectedItem?.toString() ?: ""
            val ageText = binding.etAgeInput.text.toString().trim()
            val heightText = binding.etHeightInput.text.toString().trim()
            val weightText = binding.etWeigthInput.text.toString().trim()

            if (name.isNotEmpty() && gender != "Выберите пол" &&
                ageText.isNotEmpty() && heightText.isNotEmpty() && weightText.isNotEmpty()
            ) {
                val age = ageText.toIntOrNull() ?: 0
                val height = heightText.toFloatOrNull() ?: 0f
                val weight = weightText.toFloatOrNull() ?: 0f

                val profile = UserProfile(
                    id = 1,
                    name = name,
                    gender = gender,
                    age = age,
                    height = height,
                    weight = weight
                )
                viewModel.updateProfile(profile)
                Toast.makeText(requireContext(), "Профиль сохранен", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Заполните все поля", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
