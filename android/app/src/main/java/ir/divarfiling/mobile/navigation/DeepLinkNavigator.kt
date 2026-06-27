package ir.divarfiling.mobile.navigation

import androidx.navigation.NavHostController

fun NavHostController.navigateDeepLink(target: DeepLinkTarget) {
    when (target) {
        DeepLinkTarget.Filing -> navigate(Routes.FILING)
        is DeepLinkTarget.FilingDataset -> navigate(Routes.listings(target.datasetId))
        is DeepLinkTarget.ListingDetail -> navigate(Routes.listingDetail(target.token))
        DeepLinkTarget.Crm -> navigate(Routes.CRM)
        is DeepLinkTarget.ContactDetail -> navigate(Routes.contactDetail(target.contactId))
        DeepLinkTarget.Today -> navigate(Routes.CRM_TODAY)
        DeepLinkTarget.Extract -> navigate(Routes.EXTRACT)
        DeepLinkTarget.ExtractSchedules -> navigate(Routes.EXTRACT_SCHEDULES)
        DeepLinkTarget.Settings -> navigate(Routes.SETTINGS)
    }
}
