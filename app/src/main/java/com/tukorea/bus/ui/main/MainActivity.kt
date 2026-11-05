package com.tukorea.bus.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.tukorea.bus.ui.navigation.ShuttleBottomBar
import com.tukorea.bus.ui.navigation.ShuttleNavHost
import com.tukorea.bus.ui.navigation.navigateToTab
import com.tukorea.bus.ui.theme.BusTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            BusTheme {
                val navController = rememberNavController()
                val backStack by navController.currentBackStackEntryAsState()

                Scaffold(
                    containerColor = MaterialTheme.colorScheme.background,
                    bottomBar = {
                        ShuttleBottomBar(
                            currentRoute = backStack?.destination?.route,
                            onSelect = navController::navigateToTab,
                        )
                    },
                ) { padding ->
                    ShuttleNavHost(navController = navController, modifier = Modifier.padding(padding))
                }
            }
        }
    }
}
