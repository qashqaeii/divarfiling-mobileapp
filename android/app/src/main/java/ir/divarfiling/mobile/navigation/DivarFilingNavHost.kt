package ir.divarfiling.mobile.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import ir.divarfiling.mobile.feature.settings.InstallHelpScreen
import ir.divarfiling.mobile.feature.settings.SettingsScreen
import ir.divarfiling.mobile.feature.ai.AiAssistantScreen
import ir.divarfiling.mobile.feature.crm.calendar.CrmCalendarScreen
import ir.divarfiling.mobile.feature.crm.templates.MessageTemplatesScreen
import ir.divarfiling.mobile.feature.extract.cloud.CloudExtractScreen
import ir.divarfiling.mobile.feature.filing.insights.DatasetInsightsScreen
import ir.divarfiling.mobile.feature.filing.map.DatasetMapScreen
import ir.divarfiling.mobile.feature.license.PlansScreen
import ir.divarfiling.mobile.feature.more.MoreHubScreen
import ir.divarfiling.mobile.feature.support.SupportTicketDetailScreen
import ir.divarfiling.mobile.feature.support.SupportTicketsScreen
import ir.divarfiling.mobile.feature.tools.SmartToolCalculatorScreen
import ir.divarfiling.mobile.feature.tools.ToolsScreen
import ir.divarfiling.mobile.feature.tools.smartToolIdFromKey
import ir.divarfiling.mobile.feature.team.TeamAnnouncementsScreen
import ir.divarfiling.mobile.feature.team.TeamHubScreen
import ir.divarfiling.mobile.feature.team.TeamInboxScreen
import ir.divarfiling.mobile.feature.team.TeamMembersScreen
import ir.divarfiling.mobile.feature.team.TeamMessagesScreen
import ir.divarfiling.mobile.feature.team.TeamThreadDetailScreen
import kotlinx.coroutines.flow.first

object Routes {
    const val LOGIN = "login"
    const val MAIN = "main"
    const val HOME = "home"
    const val CRM = "crm"
    const val CRM_CONTACTS = "crm/contacts?customerType={customerType}"
    const val CRM_CONTACT_DETAIL = "crm/contacts/{contactId}?openMatches={openMatches}"
    const val CRM_TODAY = "crm/today"
    const val FILING = "filing"
    const val FILING_SEARCH = "filing/search?query={query}"
    const val FILING_LISTINGS = "filing/{datasetId}"
    const val FILING_LISTING_DETAIL = "filing/listing/{token}"
    const val EXTRACT = "extract"
    const val EXTRACT_SCHEDULES = "extract/schedules"
    const val SETTINGS = "settings"
    const val PLANS = "plans"
    const val SETTINGS_INSTALL_HELP = "settings/install-help"
    const val MORE = "more"
    const val FILING_INSIGHTS = "filing/{datasetId}/insights"
    const val FILING_MAP = "filing/{datasetId}/map"
    const val TEMPLATES = "templates"
    const val CALENDAR = "calendar"
    const val AI = "ai?contactId={contactId}&listingToken={listingToken}&mode={mode}"
    const val SUPPORT = "support"
    const val SUPPORT_DETAIL = "support/{ticketId}"
    const val CLOUD_EXTRACT = "cloud-extract"
    const val CRM_DEALS = "crm/deals"
    const val CRM_DEAL_DETAIL = "crm/deals/{dealId}"
    const val CRM_PROPERTIES = "crm/properties"
    const val CRM_PROPERTY_DETAIL = "crm/properties/{propertyId}"
    const val NOTIFICATIONS = "notifications"
    const val TOOLS = "tools"
    const val TOOL_CALCULATOR = "tools/{toolId}"
    const val TEAM = "team"
    const val TEAM_MESSAGES = "team/messages"
    const val TEAM_THREAD = "team/messages/{threadId}"
    const val TEAM_MEMBERS = "team/members"
    const val TEAM_ANNOUNCEMENTS = "team/announcements"
    const val TEAM_INBOX = "team/inbox"

    fun listings(datasetId: String) = "filing/$datasetId"
    fun contacts(customerType: String? = null) =
        "crm/contacts?customerType=${Uri.encode(customerType.orEmpty())}"
    fun toolCalculator(toolId: String) = "tools/$toolId"
    fun filingSearch(query: String = "") = "filing/search?query=${Uri.encode(query)}"
    fun contactDetail(contactId: Long, openMatches: Boolean = false) =
        "crm/contacts/$contactId?openMatches=$openMatches"
    fun dealDetail(dealId: Long) = "crm/deals/$dealId"
    fun propertyDetail(propertyId: Long) = "crm/properties/$propertyId"
    fun listingDetail(token: String) = "filing/listing/$token"
    fun datasetInsights(datasetId: String) = "filing/$datasetId/insights"
    fun datasetMap(datasetId: String) = "filing/$datasetId/map"
    fun supportDetail(ticketId: Long) = "support/$ticketId"
    fun ai(
        contactId: Long? = null,
        listingToken: String? = null,
        mode: String? = null,
    ): String {
        val contact = Uri.encode(contactId?.toString().orEmpty())
        val token = Uri.encode(listingToken.orEmpty())
        val action = Uri.encode(mode.orEmpty())
        return "ai?contactId=$contact&listingToken=$token&mode=$action"
    }
    fun teamThread(threadId: Long) = "team/messages/$threadId"

    val mainTabs = setOf(HOME, FILING, MORE, CRM_TODAY, CRM_CONTACTS)
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
        sessionViewModel.isLoggedIn.collect { loggedIn ->
            isLoggedIn = loggedIn
        }
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
                    if (target is DeepLinkTarget.Payment) {
                        sessionViewModel.rememberPendingOrder(target.orderId)
                    }
                    navController.navigateDeepLink(target)
                    onDeepLinkHandled()
                }
            }
            val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner) {
                val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                    if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                        sessionViewModel.consumePendingPayment()
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
            }
            val bottomItems = listOf(
                DfNavItem(Routes.HOME, "میزکار", iconRes = DfDecorIcons.House),
                DfNavItem(Routes.FILING, "فایلینگ", iconRes = DfDecorIcons.Folder),
                DfNavItem(Routes.CRM_CONTACTS, "مخاطبین", iconRes = DfDecorIcons.Users),
                DfNavItem(Routes.CRM_TODAY, "امروز", iconRes = DfDecorIcons.Handshake),
                DfNavItem(Routes.MORE, "بیشتر", iconRes = DfDecorIcons.Layers),
            )
            val navBackStack by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStack?.destination?.route
            val showBottomBar = currentRoute in Routes.mainTabs

            Scaffold(
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                bottomBar = {
                    if (showBottomBar) {
                        DfBottomNavigation(
                            items = bottomItems,
                            selectedRoute = when {
                                currentRoute == Routes.CRM_CONTACTS -> Routes.CRM_CONTACTS
                                currentRoute == Routes.FILING_LISTINGS -> Routes.FILING
                                else -> currentRoute ?: Routes.HOME
                            },
                            onItemClick = { route ->
                                val destination = if (route == Routes.CRM_CONTACTS) {
                                    Routes.contacts()
                                } else {
                                    route
                                }
                                navController.navigate(destination) {
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
                            onNavigateContacts = { navController.navigate(Routes.contacts()) },
                            onNavigateFiling = { navController.navigate(Routes.FILING) },
                            onNavigateFilingSearch = { navController.navigate(Routes.filingSearch()) },
                            onNavigateExtract = { navController.navigate(Routes.EXTRACT) },
                            onNavigateCrm = { navController.navigate(Routes.CRM) },
                            onNavigateProperties = { navController.navigate(Routes.CRM_PROPERTIES) },
                            onNavigateSettings = { navController.navigate(Routes.SETTINGS) },
                            onNavigateTools = { navController.navigate(Routes.TOOLS) },
                            onNavigateMore = { navController.navigate(Routes.MORE) },
                            onDatasetClick = { id -> navController.navigate(Routes.listings(id)) },
                            onNotificationDeepLink = { target -> navController.navigateDeepLink(target) },
                        )
                    }
                    composable(Routes.CRM) {
                        CrmHubScreen(
                            onBack = { navController.popBackStack() },
                            onContacts = { navController.navigate(Routes.contacts()) },
                            onToday = { navController.navigate(Routes.CRM_TODAY) },
                            onDeals = { navController.navigate(Routes.CRM_DEALS) },
                            onProperties = { navController.navigate(Routes.CRM_PROPERTIES) },
                            onOwners = { navController.navigate(Routes.contacts("مالک")) },
                            onTemplates = { navController.navigate(Routes.TEMPLATES) },
                            onCalendar = { navController.navigate(Routes.CALENDAR) },
                        )
                    }
                    composable(
                        route = Routes.CRM_CONTACTS,
                        arguments = listOf(
                            navArgument("customerType") {
                                type = NavType.StringType
                                defaultValue = ""
                            },
                        ),
                    ) { entry ->
                        val ownerFilter = entry.arguments?.getString("customerType").orEmpty()
                        ContactsScreen(
                            onBack = if (ownerFilter.isNotBlank()) {
                                { navController.popBackStack() }
                            } else {
                                null
                            },
                            onContactClick = { id -> navController.navigate(Routes.contactDetail(id)) },
                            onContactSuggest = { id ->
                                navController.navigate(Routes.contactDetail(id, openMatches = true))
                            },
                            onNavigateNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
                            onNavigateSettings = { navController.navigate(Routes.SETTINGS) },
                        )
                    }
                    composable(
                        route = Routes.CRM_CONTACT_DETAIL,
                        arguments = listOf(
                            navArgument("contactId") { type = NavType.LongType },
                            navArgument("openMatches") {
                                type = NavType.BoolType
                                defaultValue = false
                            },
                        ),
                    ) {
                        ContactDetailScreen(
                            onBack = { navController.popBackStack() },
                            onDealClick = { id -> navController.navigate(Routes.dealDetail(id)) },
                            onPropertyClick = { id -> navController.navigate(Routes.propertyDetail(id)) },
                            onOpenAi = { contactId ->
                                navController.navigate(Routes.ai(contactId = contactId, mode = "draft"))
                            },
                        )
                    }
                    composable(Routes.CRM_DEALS) {
                        DealsScreen(
                            onBack = { navController.popBackStack() },
                            onDealClick = { id -> navController.navigate(Routes.dealDetail(id)) },
                            onNavigateContacts = { navController.navigate(Routes.contacts()) },
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
                            onPropertyClick = { id -> navController.navigate(Routes.propertyDetail(id)) },
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
                            onBack = null,
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
                            onInsights = { navController.navigate(Routes.datasetInsights(id)) },
                            onMap = { navController.navigate(Routes.datasetMap(id)) },
                        )
                    }
                    composable(
                        route = Routes.FILING_LISTING_DETAIL,
                        arguments = listOf(navArgument("token") { type = NavType.StringType }),
                    ) {
                        ListingDetailScreen(
                            onBack = { navController.popBackStack() },
                            onOpenCreatedProperty = { id -> navController.navigate(Routes.propertyDetail(id)) },
                            onOpenAi = { token ->
                                navController.navigate(
                                    Routes.ai(listingToken = token, mode = "summarize"),
                                )
                            },
                        )
                    }
                    composable(
                        route = Routes.FILING_INSIGHTS,
                        arguments = listOf(navArgument("datasetId") { type = NavType.StringType }),
                    ) {
                        DatasetInsightsScreen(onBack = { navController.popBackStack() })
                    }
                    composable(
                        route = Routes.FILING_MAP,
                        arguments = listOf(navArgument("datasetId") { type = NavType.StringType }),
                    ) {
                        DatasetMapScreen(
                            onBack = { navController.popBackStack() },
                            onListingClick = { token -> navController.navigate(Routes.listingDetail(token)) },
                        )
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
                            onNavigatePlans = { navController.navigate(Routes.PLANS) },
                        )
                    }
                    composable(Routes.EXTRACT_SCHEDULES) {
                        ExtractSchedulesScreen(onBack = { navController.popBackStack() })
                    }
                    composable(Routes.SETTINGS) {
                        SettingsScreen(
                            onLoggedOut = { isLoggedIn = false },
                            onNavigateNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
                            onNavigateTools = { navController.navigate(Routes.TOOLS) },
                            onNavigateSupport = { navController.navigate(Routes.SUPPORT) },
                            onNavigateInstallHelp = { navController.navigate(Routes.SETTINGS_INSTALL_HELP) },
                            onNavigatePlans = { navController.navigate(Routes.PLANS) },
                        )
                    }
                    composable(Routes.PLANS) {
                        PlansScreen(
                            onBack = { navController.popBackStack() },
                            onStartUsing = {
                                navController.navigate(Routes.HOME) {
                                    popUpTo(Routes.HOME) { inclusive = false }
                                    launchSingleTop = true
                                }
                            },
                        )
                    }
                    composable(Routes.SETTINGS_INSTALL_HELP) {
                        InstallHelpScreen(onBack = { navController.popBackStack() })
                    }
                    composable(Routes.MORE) {
                        MoreHubScreen(
                            onNavigateTools = { navController.navigate(Routes.TOOLS) },
                            onNavigateExtract = { navController.navigate(Routes.EXTRACT) },
                            onNavigateFilingSearch = { navController.navigate(Routes.filingSearch()) },
                            onNavigateTemplates = { navController.navigate(Routes.TEMPLATES) },
                            onNavigateCalendar = { navController.navigate(Routes.CALENDAR) },
                            onNavigateAi = { navController.navigate(Routes.ai()) },
                            onNavigateCloudExtract = { navController.navigate(Routes.CLOUD_EXTRACT) },
                            onNavigateTeam = { navController.navigate(Routes.TEAM) },
                            onNavigateSupport = { navController.navigate(Routes.SUPPORT) },
                            onNavigateInstallHelp = { navController.navigate(Routes.SETTINGS_INSTALL_HELP) },
                            onNavigateSettings = { navController.navigate(Routes.SETTINGS) },
                            onNavigateNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
                            onNavigateDeals = { navController.navigate(Routes.CRM_DEALS) },
                            onNavigateProperties = { navController.navigate(Routes.CRM_PROPERTIES) },
                            onNavigateCrm = { navController.navigate(Routes.CRM) },
                            onNavigatePlans = { navController.navigate(Routes.PLANS) },
                        )
                    }
                    composable(Routes.TEAM) {
                        TeamHubScreen(
                            onBack = { navController.popBackStack() },
                            onOpenMessages = { navController.navigate(Routes.TEAM_MESSAGES) },
                            onOpenMembers = { navController.navigate(Routes.TEAM_MEMBERS) },
                            onOpenAnnouncements = { navController.navigate(Routes.TEAM_ANNOUNCEMENTS) },
                            onOpenInbox = { navController.navigate(Routes.TEAM_INBOX) },
                        )
                    }
                    composable(Routes.TEAM_MESSAGES) {
                        TeamMessagesScreen(
                            onBack = { navController.popBackStack() },
                            onOpenThread = { id -> navController.navigate(Routes.teamThread(id)) },
                        )
                    }
                    composable(
                        route = Routes.TEAM_THREAD,
                        arguments = listOf(navArgument("threadId") { type = NavType.LongType }),
                    ) {
                        TeamThreadDetailScreen(onBack = { navController.popBackStack() })
                    }
                    composable(Routes.TEAM_MEMBERS) {
                        TeamMembersScreen(onBack = { navController.popBackStack() })
                    }
                    composable(Routes.TEAM_ANNOUNCEMENTS) {
                        TeamAnnouncementsScreen(onBack = { navController.popBackStack() })
                    }
                    composable(Routes.TEAM_INBOX) {
                        TeamInboxScreen(onBack = { navController.popBackStack() })
                    }
                    composable(Routes.TEMPLATES) {
                        MessageTemplatesScreen(onBack = { navController.popBackStack() })
                    }
                    composable(Routes.CALENDAR) {
                        CrmCalendarScreen(
                            onBack = { navController.popBackStack() },
                            onOpenContact = { id -> navController.navigate(Routes.contactDetail(id)) },
                        )
                    }
                    composable(
                        route = Routes.AI,
                        arguments = listOf(
                            navArgument("contactId") {
                                type = NavType.StringType
                                defaultValue = ""
                            },
                            navArgument("listingToken") {
                                type = NavType.StringType
                                defaultValue = ""
                            },
                            navArgument("mode") {
                                type = NavType.StringType
                                defaultValue = ""
                            },
                        ),
                    ) {
                        AiAssistantScreen(onBack = { navController.popBackStack() })
                    }
                    composable(Routes.SUPPORT) {
                        SupportTicketsScreen(
                            onBack = { navController.popBackStack() },
                            onOpenTicket = { id -> navController.navigate(Routes.supportDetail(id)) },
                        )
                    }
                    composable(
                        route = Routes.SUPPORT_DETAIL,
                        arguments = listOf(navArgument("ticketId") { type = NavType.LongType }),
                    ) {
                        SupportTicketDetailScreen(onBack = { navController.popBackStack() })
                    }
                    composable(Routes.CLOUD_EXTRACT) {
                        CloudExtractScreen(
                            onBack = { navController.popBackStack() },
                            onOpenDataset = { id -> navController.navigate(Routes.listings(id)) },
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
