package com.example.mydiabetesapp.ui.journal

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.mydiabetesapp.R
import com.example.mydiabetesapp.databinding.FragmentJournalBinding

class JournalFragment : Fragment(R.layout.fragment_journal) {

    private var _binding: FragmentJournalBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentJournalBinding.bind(view)

        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.analysisBlock.setOnClickListener {
            findNavController().navigate(R.id.action_journalFragment_to_statisticsFragment)
        }
        binding.analysisHba1c.setOnClickListener {
            it.findNavController().navigate(R.id.action_journalFragment_to_hba1cStatisticsFragment)
        }
        binding.analysisPressure.setOnClickListener {
            findNavController().navigate(R.id.action_nav_journal_to_pulseStatisticsFragment)
        }


        binding.analysisWeight.setOnClickListener {
            val nav = findNavController()
            if (nav.currentDestination?.id != R.id.weightStatisticsFragment) {
                nav.navigate(R.id.action_global_weightStatisticsFragment)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
