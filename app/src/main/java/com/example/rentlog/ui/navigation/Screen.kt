package com.devchiradhi.rentlog.ui.navigation

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object Onboarding : Screen("onboarding")
    object Dashboard : Screen("dashboard")
    object AddEditRent : Screen("add_edit_rent/{month}/{fiscalYear}") {
        fun createRoute(month: Int, fiscalYear: Int) = "add_edit_rent/$month/$fiscalYear"
    }
    object Summary : Screen("summary/{fiscalYear}") {
        fun createRoute(fiscalYear: Int) = "summary/$fiscalYear"
    }
    object Settings : Screen("settings")
    object HraCalculator : Screen("hra_calculator")
    object Premium : Screen("premium")
}
