package com.tukorea.bus.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.tukorea.bus.R
import com.tukorea.bus.ui.home.HomeRoute
import com.tukorea.bus.ui.info.InfoRoute
import com.tukorea.bus.ui.rides.MyRidesRoute
import com.tukorea.bus.ui.theme.busColors
import com.tukorea.bus.ui.timetable.TimetableRoute

enum class Destination(
    val route: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
) {
    HOME("home", R.string.tab_home, Icons.Outlined.Map, Icons.Filled.Map),
    TIMETABLE("timetable", R.string.tab_timetable, Icons.Outlined.Schedule, Icons.Filled.Schedule),
    MY_RIDES("my_rides", R.string.tab_my_rides, Icons.Outlined.ConfirmationNumber, Icons.Filled.ConfirmationNumber),
    INFO("info", R.string.tab_info, Icons.Outlined.Info, Icons.Filled.Info),
}

fun NavHostController.navigateToTab(destination: Destination) {
    navigate(destination.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun ShuttleNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(navController = navController, startDestination = Destination.HOME.route, modifier = modifier) {
        composable(Destination.HOME.route) {
            HomeRoute(onOpenTimetable = { navController.navigateToTab(Destination.TIMETABLE) })
        }
        composable(Destination.TIMETABLE.route) { TimetableRoute() }
        composable(Destination.MY_RIDES.route) { MyRidesRoute() }
        composable(Destination.INFO.route) { InfoRoute() }
    }
}

@Composable
fun ShuttleBottomBar(currentRoute: String?, onSelect: (Destination) -> Unit) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp) {
        Destination.entries.forEach { destination ->
            val selected = currentRoute == destination.route
            NavigationBarItem(
                selected = selected,
                onClick = { onSelect(destination) },
                icon = {
                    Icon(
                        imageVector = if (selected) destination.selectedIcon else destination.icon,
                        contentDescription = null,
                    )
                },
                label = {
                    Text(
                        text = stringResource(destination.labelRes),
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onSurface,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    unselectedIconColor = MaterialTheme.busColors.textTertiary,
                    unselectedTextColor = MaterialTheme.busColors.textTertiary,
                    indicatorColor = Color.Transparent,
                ),
            )
        }
    }
}
