package com.example.financetracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.financetracker.data.RecordType
import com.example.financetracker.ui.entry.EntryScreen
import com.example.financetracker.ui.home.HomeScreen
import com.example.financetracker.ui.settings.SettingsScreen

object FinanceDestinations {
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val ENTRY = "entry"
    const val RECORD_ID_ARG = "recordId"
    const val RECORD_TYPE_ARG = "recordType"
    const val NEW_RECORD_ID = -1
    const val ENTRY_ROUTE =
        "$ENTRY?$RECORD_ID_ARG={$RECORD_ID_ARG}&$RECORD_TYPE_ARG={$RECORD_TYPE_ARG}"
}

@Composable
fun FinanceNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = FinanceDestinations.HOME,
        modifier = modifier
    ) {
        composable(route = FinanceDestinations.HOME) {
            HomeScreen(
                navigateToAddRecord = { type ->
                    navController.navigate("${FinanceDestinations.ENTRY}?${FinanceDestinations.RECORD_TYPE_ARG}=${type.name}")
                },
                navigateToEditRecord = { id ->
                    navController.navigate("${FinanceDestinations.ENTRY}?${FinanceDestinations.RECORD_ID_ARG}=$id")
                },
                navigateToSettings = {
                    navController.navigate(FinanceDestinations.SETTINGS)
                }
            )
        }
        composable(
            route = FinanceDestinations.ENTRY_ROUTE,
            arguments = listOf(
                navArgument(FinanceDestinations.RECORD_ID_ARG) {
                    type = NavType.IntType
                    defaultValue = FinanceDestinations.NEW_RECORD_ID
                },
                navArgument(FinanceDestinations.RECORD_TYPE_ARG) {
                    type = NavType.StringType
                    defaultValue = RecordType.EXPENSE.name
                }
            )
        ) {
            EntryScreen(navigateBack = { navController.navigateUp() })
        }
        composable(route = FinanceDestinations.SETTINGS) {
            SettingsScreen(navigateBack = { navController.navigateUp() })
        }
    }
}
