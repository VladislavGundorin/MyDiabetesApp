package com.example.mydiabetesapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.mydiabetesapp.databinding.ActivityMainBinding
import com.example.mydiabetesapp.ui.home.HomeFragment
import com.example.mydiabetesapp.ui.journal.JournalFragment
import com.example.mydiabetesapp.ui.monitoring.MonitoringFragment
import com.example.mydiabetesapp.ui.notification.NotificationFragment
import com.example.mydiabetesapp.ui.profile.ProfileFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> loadFragment(HomeFragment())
                R.id.nav_monitoring -> loadFragment(MonitoringFragment())
                R.id.nav_journal -> loadFragment(JournalFragment())
                R.id.nav_notification -> loadFragment(NotificationFragment())
                R.id.nav_profile -> loadFragment(ProfileFragment())
            }
            true
        }
    }

    private fun loadFragment(fragment: androidx.fragment.app.Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
