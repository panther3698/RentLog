package com.example.rentlog.ui.navigation

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Dashboard : Screen("dashboard")
    object AddEditRent : Screen("add_edit_rent/{month}") {
        fun createRoute(month: Int) = "add_edit_rent/$month"
    }
    object Summary : Screen("summary")
    object Settings : Screen("settings")
}
