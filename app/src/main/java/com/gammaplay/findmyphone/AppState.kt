package com.gammaplay.findmyphone

import androidx.compose.runtime.Stable
import androidx.navigation.NavHostController

@Stable
class AppState(
    val navController: NavHostController,
) {

    fun popUp() {
        navController.popBackStack()
    }

    fun navigate(route: String) {
        navController.navigate(route) {
            launchSingleTop = true
        }
    }


    fun navigateAndPopUp(route: String, popUp: String, isInclusive: Boolean = true) {
        navController.navigate(route) {
            launchSingleTop = true
            popUpTo(popUp) {
                inclusive = isInclusive
            }
        }

    }

    fun clearAndNavigate(route: String) {
        navController.navigate(route) {
            launchSingleTop = true
            popUpTo(0) { inclusive = true }
        }
    }
}