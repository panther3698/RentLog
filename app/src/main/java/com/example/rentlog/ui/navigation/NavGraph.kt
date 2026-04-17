package com.example.rentlog.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.rentlog.ui.screens.onboarding.OnboardingScreen
import com.example.rentlog.ui.screens.dashboard.DashboardScreen
import com.example.rentlog.ui.screens.addedit.AddEditScreen
import com.example.rentlog.ui.screens.summary.SummaryScreen
import com.example.rentlog.ui.screens.settings.SettingsScreen

@Composable
fun NavGraph(navController: NavHostController, startDestination: String) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { 
            fadeIn(animationSpec = tween(400)) + 
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(400)) 
        },
        exitTransition = { 
            fadeOut(animationSpec = tween(400)) + 
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(400)) 
        },
        popEnterTransition = { 
            fadeIn(animationSpec = tween(400)) + 
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(400)) 
        },
        popExitTransition = { 
            fadeOut(animationSpec = tween(400)) + 
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(400)) 
        }
    ) {
        composable(
            route = Screen.Onboarding.route,
            arguments = listOf(navArgument("isNew") { type = NavType.BoolType; defaultValue = true }),
            enterTransition = { fadeIn(animationSpec = tween(400)) },
            exitTransition = { fadeOut(animationSpec = tween(400)) }
        ) { backStackEntry ->
            val isNew = backStackEntry.arguments?.getBoolean("isNew") ?: true
            OnboardingScreen(
                onComplete = {
                    if (isNew) {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    } else {
                        navController.popBackStack()
                    }
                }
            )
        }
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onMonthClick = { month ->
                    navController.navigate(Screen.AddEditRent.createRoute(month))
                },
                onSummaryClick = {
                    navController.navigate(Screen.Summary.route)
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                },
                onAddLandlord = {
                    navController.navigate(Screen.Onboarding.createRoute(isNew = false))
                }
            )
        }
        composable(
            route = Screen.AddEditRent.route,
            arguments = listOf(navArgument("month") { type = NavType.StringType }),
            enterTransition = { 
                slideInVertically(initialOffsetY = { it }, animationSpec = tween(400)) + fadeIn() 
            },
            exitTransition = { 
                slideOutVertically(targetOffsetY = { it }, animationSpec = tween(400)) + fadeOut() 
            }
        ) { backStackEntry ->
            val monthStr = backStackEntry.arguments?.getString("month") ?: "1"
            AddEditScreen(
                month = monthStr.toIntOrNull() ?: 1,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.Summary.route) {
            SummaryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
