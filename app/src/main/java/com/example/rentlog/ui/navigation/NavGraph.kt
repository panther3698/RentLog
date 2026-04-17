package com.example.rentlog.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.rentlog.ui.screens.addedit.AddEditRentScreen
import com.example.rentlog.ui.screens.calculator.HraCalculatorScreen
import com.example.rentlog.ui.screens.dashboard.DashboardScreen
import com.example.rentlog.ui.screens.onboarding.OnboardingScreen
import com.example.rentlog.ui.screens.premium.PremiumScreen
import com.example.rentlog.ui.screens.settings.SettingsScreen
import com.example.rentlog.ui.screens.summary.SummaryScreen
import com.example.rentlog.ui.screens.welcome.WelcomeScreen

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
            route = Screen.Welcome.route,
            enterTransition = { fadeIn(animationSpec = tween(600)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            WelcomeScreen(
                onGetStarted = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.Onboarding.route,
            enterTransition = { fadeIn(animationSpec = tween(400)) },
            exitTransition = { fadeOut(animationSpec = tween(400)) }
        ) {
            OnboardingScreen(
                onNavigateToDashboard = {
                    if (navController.previousBackStackEntry != null) {
                        navController.popBackStack()
                    } else {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                },
                onBack = {
                    if (navController.previousBackStackEntry != null) {
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
                onCalculatorClick = {
                    navController.navigate(Screen.HraCalculator.route)
                }
            )
        }

        composable(
            route = Screen.AddEditRent.route,
            arguments = listOf(navArgument("month") { type = NavType.IntType }),
            enterTransition = {
                slideInVertically(initialOffsetY = { it }, animationSpec = tween(400)) + fadeIn()
            },
            exitTransition = {
                slideOutVertically(targetOffsetY = { it }, animationSpec = tween(400)) + fadeOut()
            }
        ) {
            AddEditRentScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.Summary.route) {
            SummaryScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onEditProfile = { navController.navigate(Screen.Onboarding.route) },
                onGoPremium = { navController.navigate(Screen.Premium.route) }
            )
        }

        composable(
            route = Screen.HraCalculator.route,
            enterTransition = {
                slideInVertically(initialOffsetY = { it }, animationSpec = tween(400)) + fadeIn()
            },
            exitTransition = {
                slideOutVertically(targetOffsetY = { it }, animationSpec = tween(400)) + fadeOut()
            }
        ) {
            HraCalculatorScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.Premium.route) {
            PremiumScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
