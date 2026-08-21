package ir.divarfiling.mobile.navigation

import androidx.navigation.NavHostController

fun NavHostController.navigateDeepLink(target: DeepLinkTarget) {
    when (target) {
        DeepLinkTarget.Filing -> navigate(Routes.FILING)
        is DeepLinkTarget.FilingDataset -> navigate(Routes.listings(target.datasetId))
        is DeepLinkTarget.DatasetInsights -> navigate(Routes.datasetInsights(target.datasetId))
        is DeepLinkTarget.DatasetMap -> navigate(Routes.datasetMap(target.datasetId))
        is DeepLinkTarget.ListingDetail -> navigate(Routes.listingDetail(target.token))
        DeepLinkTarget.Crm -> navigate(Routes.CRM)
        is DeepLinkTarget.ContactDetail -> navigate(Routes.contactDetail(target.contactId))
        is DeepLinkTarget.ContactMatches -> navigate(Routes.contactDetail(target.contactId, openMatches = true))
        DeepLinkTarget.Today -> navigate(Routes.CRM_TODAY)
        DeepLinkTarget.Extract -> navigate(Routes.EXTRACT)
        DeepLinkTarget.ExtractSchedules -> navigate(Routes.EXTRACT_SCHEDULES)
        DeepLinkTarget.CloudExtract -> navigate(Routes.CLOUD_EXTRACT)
        DeepLinkTarget.More -> navigate(Routes.MORE)
        DeepLinkTarget.Tools -> navigate(Routes.TOOLS)
        DeepLinkTarget.Ai -> navigate(Routes.ai())
        DeepLinkTarget.Support -> navigate(Routes.SUPPORT)
        is DeepLinkTarget.SupportTicket -> navigate(Routes.supportDetail(target.ticketId))
        DeepLinkTarget.Calendar -> navigate(Routes.CALENDAR)
        DeepLinkTarget.Settings -> navigate(Routes.SETTINGS)
        DeepLinkTarget.Plans -> navigate(Routes.PLANS)
        is DeepLinkTarget.Payment -> navigate(Routes.PLANS)
    }
}
