package com.idloquy.landmark.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable

@Serializable
object Home

@Serializable
object History

@Serializable
data class Mark(val id: Int)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Home,
        enterTransition = { slideInHorizontally(animationSpec = tween(300)) { it } },
        exitTransition = { slideOutHorizontally(animationSpec = tween(300)) { -it } },
        popEnterTransition = { slideInHorizontally(animationSpec = tween(300)) { -it } },
        popExitTransition = { slideOutHorizontally(animationSpec = tween(300)) { it } },
    ) {
        composable<Home> {
            HomeScreen(
                onHistory = {
                    navController.navigate(History)
                })
        }
        composable<History> {
            HistoryScreen(onBack = {
                navController.popBackStack()
            }, onViewMark = { id ->
                navController.navigate(Mark(id))
            })
        }
        composable<Mark> { backStackEntry ->
            val mark: Mark = backStackEntry.toRoute()
            MarkScreen(
                markId = mark.id,
                onBack = {
                    navController.popBackStack()
                },
            )
        }
    }
}
