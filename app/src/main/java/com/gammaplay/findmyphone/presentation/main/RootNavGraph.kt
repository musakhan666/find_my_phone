package com.gammaplay.findmyphone.presentation.main

import SettingsScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gammaplay.findmyphone.AppState
import com.gammaplay.findmyphone.presentation.home.HomeScreen
import com.gammaplay.findmyphone.presentation.home.HomeViewModel
import com.gammaplay.findmyphone.presentation.tutorial.TutorialScreen
import com.gammaplay.findmyphone.utils.AppStatusManager

@Composable
fun RootNavigationGraph() {
    // Check if the tutorial has been shown before
    val context = LocalContext.current
    val settings = AppStatusManager(context)

    val hasShownTutorial = settings.hasShownTutorial()

    // Set the start destination based on the tutorial flag
    val startDestination = if (hasShownTutorial) Graph.HOME else Graph.TUTORIAL
    val appState = rememberAppState()
    val navController = appState.navController
    val homeViewModel: HomeViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        route = Graph.ROOT,
        startDestination = startDestination
    ) {
        AppGraph(appState, homeViewModel)
    }
}

private fun NavGraphBuilder.AppGraph(
    appState: AppState,
    homeViewModel: HomeViewModel
) {

    composable(Graph.HOME) { HomeScreen(openAndPopUp = { route -> appState.clearAndNavigate(route) }, homeViewModel) }
    composable(Graph.SETTINGS) { SettingsScreen(onBackPressed = { appState.popUp() }) }
    composable(Graph.TUTORIAL) { TutorialScreen(openAndPopUp = { route -> appState.clearAndNavigate(route) }) }
}

@Composable
fun rememberAppState(
    navController: NavHostController = rememberNavController(),
) = remember(navController) {
    AppState(navController)
}

object Graph {
    const val ROOT = "root_graph"
    const val HOME = "home_graph"
    const val SETTINGS = "settings_graph"
    const val TUTORIAL = "tutorial_graph"
}