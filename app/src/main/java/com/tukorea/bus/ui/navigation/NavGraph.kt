package com.tukorea.bus.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.tukorea.bus.ui.calendar.CalendarScreen
import com.tukorea.bus.ui.home.HomeScreen
import com.tukorea.bus.ui.map.MapScreen
import com.tukorea.bus.ui.notifications.NotificationDetailScreen
import com.tukorea.bus.ui.notifications.NotificationsScreen
import com.tukorea.bus.ui.quickride.QuickRideScreen
import com.tukorea.bus.ui.realtime.RealtimeScreen
import com.tukorea.bus.ui.ride.RideScreen
import com.tukorea.bus.ui.settings.SettingsScreen
import com.tukorea.bus.ui.stations.StationsScreen
import com.tukorea.bus.ui.temp.TempScreen

@Composable
fun BusNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Screen.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateTo = { route ->
                    navController.navigate(route) {
                        popUpTo(Screen.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
        composable(Screen.Calendar.route) {
            CalendarScreen(
                onNavigateTo = { route ->
                    navController.navigate(route) {
                        popUpTo(Screen.Calendar.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
        composable(Screen.Stations.route) {
            StationsScreen(
                onNavigateTo = { route ->
                    navController.navigate(route) {
                        popUpTo(Screen.Stations.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
        composable(Screen.QuickRide.route) {
            QuickRideScreen(
                onNavigateTo = { route ->
                    navController.navigate(route) {
                        popUpTo(Screen.QuickRide.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
        composable(Screen.Notifications.route) {
            NotificationsScreen(
                onNavigateToDetail = { notificationId ->
                    navController.navigate(Screen.NotificationDetail.createRoute(notificationId))
                }
            )
        }
        composable(
            route = Screen.NotificationDetail.route,
            arguments = listOf(
                navArgument("notificationId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val notificationId = backStackEntry.arguments?.getInt("notificationId") ?: return@composable
            NotificationDetailScreen(
                notificationId = notificationId,
                onBackPress = { navController.popBackStack() }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
        composable(Screen.Realtime.route) {
            RealtimeScreen(
                onNavigateTo = { route ->
                    navController.navigate(route) {
                        popUpTo(Screen.Realtime.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
        composable(Screen.Ride.route) {
            RideScreen(
                onNavigateTo = { route ->
                    navController.navigate(route) {
                        popUpTo(Screen.Ride.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
        composable(Screen.Map.route) {
            MapScreen()
        }

        composable(Screen.Temp.route) {
            TempScreen()
        }
    }
}

