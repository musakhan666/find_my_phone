package com.gammaplay.findmyphone.ui.main

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.gammaplay.findmyphone.ui.settings.SettingsScreen
import com.gammaplay.findmyphone.ui.home.HomeScreen
import com.gammaplay.findmyphone.ui.home.HomeViewModel
import com.gammaplay.findmyphone.ui.permission.PermissionsViewModel
import com.gammaplay.findmyphone.ui.tutorial.TutorialScreen
import com.gammaplay.findmyphone.utils.AppStatusManager

@Composable
fun RootNavigationGraph(navController: NavHostController) {
    // Check if the tutorial has been shown before
    val context = LocalContext.current
    val settings = AppStatusManager(context)

    val hasShownTutorial = settings.hasShownTutorial()
    val permission = PermissionsViewModel()
        permission.checkAndRequestPermissions(context)
    // Set the start destination based on the tutorial flag
    val startDestination = if (hasShownTutorial) Graph.HOME else Graph.TUTORIAL



    NavHost(
        navController = navController,
        route = Graph.ROOT,
        startDestination = startDestination
    ) {
        composable(Graph.HOME) {
            val homeViewModel = HomeViewModel(context.applicationContext)
            HomeScreen(navController, homeViewModel)
        }
        composable(Graph.SETTINGS) {
            SettingsScreen(navController)
        }
        composable(Graph.TUTORIAL) {
            TutorialScreen(navController)
        }
    }
}

object Graph {
    const val ROOT = "root_graph"
    const val HOME = "home_graph"
    const val SETTINGS = "settings_graph"
    const val TUTORIAL = "tutorial_graph"
}