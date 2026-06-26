package ir.divarfiling.mobile.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import ir.divarfiling.mobile.core.design.DfColors
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

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
                BottomItem(Routes.HOME, "خانه", Icons.Default.Home),
                BottomItem(Routes.CRM, "CRM", Icons.Default.People),
                BottomItem(Routes.FILING, "فایلینگ", Icons.Default.Folder),
                BottomItem(Routes.EXTRACT, "استخراج", Icons.Default.CloudDownload),
                BottomItem(Routes.SETTINGS, "بیشتر", Icons.Default.AccountCircle),
            )
            val navBackStack by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStack?.destination?.route

            val showBottomBar = currentRoute in bottomItems.map { it.route }

            Scaffold(
                bottomBar = {
                    if (showBottomBar) {
                        NavigationBar(containerColor = DfColors.Surface) {
                            bottomItems.forEach { item ->
                                NavigationBarItem(
                                    selected = currentRoute == item.route,
                                    onClick = {
                                        navController.navigate(item.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = { Icon(item.icon, contentDescription = item.label) },
                                    label = { Text(item.label) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = DfColors.Purple,
                                        selectedTextColor = DfColors.Purple,
                                        indicatorColor = DfColors.PurpleContainer,
                                    ),
                                )
                            }
                        }
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

private data class BottomItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
)
