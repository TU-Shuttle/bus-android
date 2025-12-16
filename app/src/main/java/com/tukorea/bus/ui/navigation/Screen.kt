package com.tukorea.bus.ui.navigation

/**
 * 앱 내 네비게이션 화면을 정의하는 sealed class
 */
sealed class Screen(val route: String) {
    data object Map : Screen("map")
    data object Home : Screen("home")
    data object Calendar : Screen("calendar")
    data object Stations : Screen("stations")
    data object QuickRide : Screen("quick_ride")
    data object Notifications : Screen("notifications")
    data object NotificationDetail : Screen("notification_detail/{notificationId}") {
        fun createRoute(notificationId: Int) = "notification_detail/$notificationId"
    }
    data object Settings : Screen("settings")
    data object Ride : Screen("ride")
    data object Temp : Screen("temp")
}

