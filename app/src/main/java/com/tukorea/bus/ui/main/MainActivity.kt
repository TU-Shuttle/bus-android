package com.tukorea.bus.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tukorea.bus.databinding.ActivityMainBinding
import com.tukorea.bus.ui.temp.TempFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Fragment transaction: TempFragment 추가
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(binding.container.id, TempFragment())
                .commit()
        }
    }
}