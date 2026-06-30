package ir.divarfiling.mobile.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
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
import android.net.Uri
import ir.divarfiling.mobile.core.design.components.DfBottomNavigation
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfNavItem
import ir.divarfiling.mobile.feature.auth.LoginScreen
import ir.divarfiling.mobile.feature.crm.ContactDetailScreen
import ir.divarfiling.mobile.feature.crm.ContactsScreen
import ir.divarfiling.mobile.feature.crm.CrmHubScreen
import ir.divarfiling.mobile.feature.crm.DealDetailScreen
import ir.divarfiling.mobile.feature.crm.DealsScreen
import ir.divarfiling.mobile.feature.crm.PropertiesScreen
import ir.divarfiling.mobile.feature.crm.PropertyDetailScreen
import ir.divarfiling.mobile.feature.crm.TodayScreen
import ir.divarfiling.mobile.feature.extract.ExtractScreen
import ir.divarfiling.mobile.feature.extract.schedule.ExtractSchedulesScreen
import ir.divarfiling.mobile.feature.filing.DatasetsScreen
import ir.divarfiling.mobile.feature.filing.FilingSearchScreen
import ir.divarfiling.mobile.feature.filing.ListingDetailScreen
import ir.divarfiling.mobile.feature.filing.ListingsScreen
import ir.divarfiling.mobile.feature.home.HomeScreen
import ir.divarfiling.mobile.feature.notifications.NotificationsScreen
import ir.divarfiling.mobile.feature.settings.SettingsScreen
import ir.divarfiling.mobile.feature.tools.SmartToolCalculatorScreen
import ir.divarfiling.mobile.feature.tools.ToolsScreen
import ir.divarfiling.mobile.feature.tools.smartToolIdFromKey
import kotlinx.coroutines.flow.first

object Routes {
    const val LOGIN = "login"
    const val MAIN = "main"
    const val HOME = "home"
    const val CRM = "crm"
    const val CRM_CONTACTS = "crm/contacts"
    const val CRM_CONTACT_DETAIL = "crm/contacts/{contactId}"
    const val CRM_TODAY = "crm/today"
    const val FILING = "filing"
    const val FILING_SEARCH = "filing/search?query={query}"
    const val FILING_LISTINGS = "filing/{datasetId}"
    const val FILING_LISTING_DETAIL = "filing/listing/{token}"
    const val EXTRACT = "extract"
    const val EXTRACT_SCHEDULES = "extract/schedules"
    const val SETTINGS = "settings"
    const val CRM_DEALS = "crm/deals"
    const val CRM_DEAL_DETAIL = "crm/deals/{dealId}"
    const val CRM_PROPERTIES = "crm/properties"
    const val CRM_PROPERTY_DETAIL = "crm/properties/{propertyId}"
    const val NOTIFICATIONS = "notifications"
    const val TOOLS = "tools"
    const val TOOL_CALCULATOR = "tools/{toolId}"

    fun listings(datasetId: String) = "filing/$datasetId"
    fun toolCalculator(toolId: String) = "tools/$toolId"
    fun filingSearch(query: String = "") = "filing/search?query=${Uri.encode(query)}"
    fun contactDetail(contactId: Long) = "crm/contacts/$contactId"
    fun dealDetail(dealId: Long) = "crm/deals/$dealId"
    fun propertyDetail(propertyId: Long) = "crm/properties/$propertyId"
    fun listingDetail(token: String) = "filing/listing/$token"

    val mainTabs = setOf(HOME, CRM, FILING, SETTINGS)
}

@Composable
fun DivarFilingNavHost(
    deepLink: DeepLinkTarget? = null,
    onDeepLinkHandled: () -> Unit = {},
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
            LaunchedEffect(deepLink) {
                deepLink?.let { target ->
                    navController.navigateDeepLink(target)
                    onDeepLinkHandled()
                }
            }
            val bottomItems = listOf(
                DfNavItem(Routes.FILING, "فایلینگ", iconRes = DfDecorIcons.Folder),
                DfNavItem(Routes.CRM, "CRM", iconRes = DfDecorIcons.Users),
                DfNavItem(Routes.HOME, "میزکار", iconRes = DfDecorIcons.House, isCenter = true),
                DfNavItem(Routes.CRM_TODAY, "امروز", iconRes = DfDecorIcons.Handshake),
                DfNavItem(Routes.SETTINGS, "تنظیمات", iconRes = DfDecorIcons.Settings),
            )
            val navBackStack by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStack?.destination?.route
            val showBottomBar = currentRoute in Routes.mainTabs ||
                currentRoute == Routes.CRM_TODAY

            Scaffold(
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
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
                            onNavigateNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
                            onNavigateContacts = { navController.navigate(Routes.CRM_CONTACTS) },
                            onNavigateFiling = { navController.navigate(Routes.FILING) },
                            onNavigateExtract = { navController.navigate(Routes.EXTRACT) },
                            onNavigateCrm = { navController.navigate(Routes.CRM) },
                            onNavigateSettings = { navController.navigate(Routes.SETTINGS) },
                            onDatasetClick = { id -> navController.navigate(Routes.listings(id)) },
                            onNotificationDeepLink = { target -> navController.navigateDeepLink(target) },
                        )
                    }
                    composable(Routes.CRM) {
                        CrmHubScreen(
                            onContacts = { navController.navigate(Routes.CRM_CONTACTS) },
                            onToday = { navController.navigate(Routes.CRM_TODAY) },
                            onDeals = { navController.navigate(Routes.CRM_DEALS) },
                            onProperties = { navController.navigate(Routes.CRM_PROPERTIES) },
                        )
                    }
                    composable(Routes.CRM_CONTACTS) {
                        ContactsScreen(
                            onBack = { navController.popBackStack() },
                            onContactClick = { id -> navController.navigate(Routes.contactDetail(id)) },
                            onNavigateNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
                            onNavigateSettings = { navController.navigate(Routes.SETTINGS) },
                        )
                    }
                    composable(
                        route = Routes.CRM_CONTACT_DETAIL,
                        arguments = listOf(navArgument("contactId") { type = NavType.LongType }),
                    ) {
                        ContactDetailScreen(
                            onBack = { navController.popBackStack() },
                            onDealClick = { id -> navController.navigate(Routes.dealDetail(id)) },
                            onPropertyClick = { id -> navController.navigate(Routes.propertyDetail(id)) },
                        )
                    }
                    composable(Routes.CRM_DEALS) {
                        DealsScreen(
                            onBack = { navController.popBackStack() },
                            onDealClick = { id -> navController.navigate(Routes.dealDetail(id)) },
                            onNavigateContacts = { navController.navigate(Routes.CRM_CONTACTS) },
                            onNavigateNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
                            onNavigateSettings = { navController.navigate(Routes.SETTINGS) },
                        )
                    }
                    composable(
                        route = Routes.CRM_DEAL_DETAIL,
                        arguments = listOf(navArgument("dealId") { type = NavType.LongType }),
                    ) {
                        DealDetailScreen(
                            onBack = { navController.popBackStack() },
                            onContactClick = { id -> navController.navigate(Routes.contactDetail(id)) },
                        )
                    }
                    composable(Routes.CRM_PROPERTIES) {
                        PropertiesScreen(
                            onBack = { navController.popBackStack() },
                            onPropertyClick = { id -> navController.navigate(Routes.propertyDetail(id)) },
                            onNavigateNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
                            onNavigateSettings = { navController.navigate(Routes.SETTINGS) },
                        )
                    }
                    composable(
                        route = Routes.CRM_PROPERTY_DETAIL,
                        arguments = listOf(navArgument("propertyId") { type = NavType.LongType }),
                    ) {
                        PropertyDetailScreen(
                            onBack = { navController.popBackStack() },
                            onContactClick = { id -> navController.navigate(Routes.contactDetail(id)) },
                        )
                    }
                    composable(Routes.CRM_TODAY) {
                        TodayScreen(
                            onBack = { navController.popBackStack() },
                            onContactClick = { id -> navController.navigate(Routes.contactDetail(id)) },
                        )
                    }
                    composable(Routes.FILING) {
                        DatasetsScreen(
                            onDatasetClick = { id -> navController.navigate(Routes.listings(id)) },
                            onGlobalSearch = { query -> navController.navigate(Routes.filingSearch(query)) },
                            onNavigateExtract = { navController.navigate(Routes.EXTRACT) },
                            onNavigateNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
                            onNavigateSettings = { navController.navigate(Routes.SETTINGS) },
                        )
                    }
                    composable(
                        route = Routes.FILING_SEARCH,
                        arguments = listOf(
                            navArgument("query") {
                                type = NavType.StringType
                                defaultValue = ""
                            },
                        ),
                    ) { entry ->
                        val query = entry.arguments?.getString("query").orEmpty()
                        FilingSearchScreen(
                            initialQuery = query,
                            onBack = { navController.popBackStack() },
                            onListingClick = { token -> navController.navigate(Routes.listingDetail(token)) },
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
                            onListingClick = { token -> navController.navigate(Routes.listingDetail(token)) },
                        )
                    }
                    composable(
                        route = Routes.FILING_LISTING_DETAIL,
                        arguments = listOf(navArgument("token") { type = NavType.StringType }),
                    ) {
                        ListingDetailScreen(onBack = { navController.popBackStack() })
                    }
                    composable(Routes.EXTRACT) {
                        ExtractScreen(
                            onViewDataset = { id ->
                                navController.navigate(Routes.listings(id))
                            },
                            onOpenSchedules = { navController.navigate(Routes.EXTRACT_SCHEDULES) },
                            onBack = { navController.popBackStack() },
                            onNotificationsClick = { navController.navigate(Routes.NOTIFICATIONS) },
                            onMenuClick = { navController.navigate(Routes.SETTINGS) },
                        )
                    }
                    composable(Routes.EXTRACT_SCHEDULES) {
                        ExtractSchedulesScreen(onBack = { navController.popBackStack() })
                    }
                    composable(Routes.SETTINGS) {
                        SettingsScreen(
                            onLoggedOut = { isLoggedIn = false },
                            onNavigateNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
                        )
                    }
                    composable(Routes.NOTIFICATIONS) {
                        NotificationsScreen(
                            onBack = { navController.popBackStack() },
                            onDeepLink = { target -> navController.navigateDeepLink(target) },
                        )
                    }
                    composable(Routes.TOOLS) {
                        ToolsScreen(
                            onBack = { navController.popBackStack() },
                            onNavigateNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
                            onNavigateSettings = { navController.navigate(Routes.SETTINGS) },
                            onToolClick = { toolId ->
                                navController.navigate(Routes.toolCalculator(toolId.key))
                            },
                        )
                    }
                    composable(
                        route = Routes.TOOL_CALCULATOR,
                        arguments = listOf(navArgument("toolId") { type = NavType.StringType }),
                    ) { entry ->
                        val key = entry.arguments?.getString("toolId").orEmpty()
                        val toolId = smartToolIdFromKey(key)
                        if (toolId != null) {
                            SmartToolCalculatorScreen(
                                toolId = toolId,
                                onBack = { navController.popBackStack() },
                            )
                        }
                    }
                }
            }
        }
    }
}
