package ir.divarfiling.mobile.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfBottomNavigation
import ir.divarfiling.mobile.core.design.components.DfNavItem
import ir.divarfiling.mobile.feature.auth.LoginScreen
import ir.divarfiling.mobile.feature.crm.ContactsScreen
import ir.divarfiling.mobile.feature.crm.CrmHubScreen
import ir.divarfiling.mobile.feature.crm.TodayScreen
import ir.divarfiling.mobile.feature.extract.ExtractScreen
import ir.divarfiling.mobile.feature.filing.DatasetsScreen
import ir.divarfiling.mobile.feature.filing.ListingsScreen
import ir.divarfiling.mobile.feature.home.HomeScreen
import ir.divarfiling.mobile.feature.settings.SettingsScreen
import kotlinx.coroutines.flow.first

object Routes {
    const val LOGIN = "login"
    const val MAIN = "main"
    const val HOME = "home"
    const val CRM = "crm"
    const val CRM_CONTACTS = "crm/contacts"
    const val CRM_TODAY = "crm/today"
    const val FILING = "filing"
    const val FILING_LISTINGS = "filing/{datasetId}"
    const val EXTRACT = "extract"
    const val SETTINGS = "settings"

    fun listings(datasetId: String) = "filing/$datasetId"

    val mainTabs = setOf(HOME, CRM, FILING, SETTINGS)
}

@Composable
fun DivarFilingNavHost(
    sessionViewModel: SessionViewModel = hiltViewModel(),
) {
    val navController = rememberNavController()
    var isLoggedIn by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(Unit) {
        isLoggedIn = sessionViewModel.isLoggedIn.first()
    }

    when (isLoggedIn) {
        null -> { /* splash */ }
        false -> {
            NavHost(navController, startDestination = Routes.LOGIN) {
                composable(Routes.LOGIN) {
                    LoginScreen(
                        onLoggedIn = { isLoggedIn = true },
                    )
                }
            }
        }
        true -> {
            val bottomItems = listOf(
                DfNavItem(Routes.FILING, "فایلینگ", DfIcons.Folder),
                DfNavItem(Routes.CRM, "CRM", DfIcons.Users),
                DfNavItem(Routes.HOME, "میزکار", DfIcons.Home, isCenter = true),
                DfNavItem(Routes.CRM_TODAY, "معاملات", DfIcons.Handshake),
                DfNavItem(Routes.SETTINGS, "تنظیمات", DfIcons.Settings),
            )
            val navBackStack by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStack?.destination?.route
            val showBottomBar = currentRoute in Routes.mainTabs ||
                currentRoute == Routes.CRM_TODAY

            Scaffold(
                bottomBar = {
                    if (showBottomBar) {
                        DfBottomNavigation(
                            items = bottomItems,
                            selectedRoute = when (currentRoute) {
                                Routes.CRM_CONTACTS -> Routes.CRM
                                Routes.FILING_LISTINGS -> Routes.FILING
                                else -> currentRoute ?: Routes.HOME
                            },
                            onItemClick = { route ->
                                navController.navigate(route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                        )
                    }
                },
            ) { padding ->
                NavHost(
                    navController = navController,
                    startDestination = Routes.HOME,
                    modifier = Modifier.padding(padding),
                ) {
                    composable(Routes.HOME) {
                        HomeScreen(
                            onNavigateToday = { navController.navigate(Routes.CRM_TODAY) },
                            onNavigateContacts = { navController.navigate(Routes.CRM_CONTACTS) },
                            onNavigateFiling = { navController.navigate(Routes.FILING) },
                            onNavigateExtract = { navController.navigate(Routes.EXTRACT) },
                            onNavigateCrm = { navController.navigate(Routes.CRM) },
                            onNavigateSettings = { navController.navigate(Routes.SETTINGS) },
                            onDatasetClick = { id -> navController.navigate(Routes.listings(id)) },
                        )
                    }
                    composable(Routes.CRM) {
                        CrmHubScreen(
                            onContacts = { navController.navigate(Routes.CRM_CONTACTS) },
                            onToday = { navController.navigate(Routes.CRM_TODAY) },
                        )
                    }
                    composable(Routes.CRM_CONTACTS) {
                        ContactsScreen(onBack = { navController.popBackStack() })
                    }
                    composable(Routes.CRM_TODAY) {
                        TodayScreen(onBack = { navController.popBackStack() })
                    }
                    composable(Routes.FILING) {
                        DatasetsScreen(
                            onDatasetClick = { id -> navController.navigate(Routes.listings(id)) },
                        )
                    }
                    composable(
                        route = Routes.FILING_LISTINGS,
                        arguments = listOf(navArgument("datasetId") { type = NavType.StringType }),
                    ) { entry ->
                        val id = entry.arguments?.getString("datasetId") ?: return@composable
                        ListingsScreen(
                            datasetId = id,
                            onBack = { navController.popBackStack() },
                        )
                    }
                    composable(Routes.EXTRACT) {
                        ExtractScreen(
                            onViewDataset = { id ->
                                navController.navigate(Routes.listings(id))
                            },
                        )
                    }
                    composable(Routes.SETTINGS) {
                        SettingsScreen(
                            onLoggedOut = { isLoggedIn = false },
                        )
                    }
                }
            }
        }
    }
}
