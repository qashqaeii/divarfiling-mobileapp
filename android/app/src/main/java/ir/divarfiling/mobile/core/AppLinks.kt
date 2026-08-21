package ir.divarfiling.mobile.core

/** مقصدهای وب، پرداخت و پشتیبانی — منبع واحد لینک خارجی. */
object AppLinks {
    const val SITE_ORIGIN = "https://divarfiling.ir"

    const val SITE = "$SITE_ORIGIN/"
    const val SHOP_BOT = "$SITE_ORIGIN/products/divar-extraction-bot/"
    const val BUY_BOT = "$SITE_ORIGIN/products/divar-extraction-bot/buy/"
    const val DASHBOARD_LICENSES = "$SITE_ORIGIN/accounts/dashboard/?tab=licenses#licenses"
    const val PRIVACY = "$SITE_ORIGIN/privacy/"
    const val TERMS = "$SITE_ORIGIN/terms/"
    const val ACADEMY = "$SITE_ORIGIN/academy/"
    const val WORKSPACE = "$SITE_ORIGIN/workspace/"
    const val WORKSPACE_AI = "$SITE_ORIGIN/workspace/ai/"
    const val WORKSPACE_SUPPORT = "$SITE_ORIGIN/workspace/support/"
    const val WORKSPACE_TEAM = "$SITE_ORIGIN/workspace/crm/team/"
    const val WORKSPACE_TEAM_TV = "$SITE_ORIGIN/workspace/crm/team/tv/"
    const val WORKSPACE_TEAM_REPORT = "$SITE_ORIGIN/workspace/crm/team/report.csv"
    const val WORKSPACE_TEMPLATES = "$SITE_ORIGIN/workspace/crm/templates/"
    const val WORKSPACE_CALENDAR = "$SITE_ORIGIN/workspace/crm/calendar/"
    const val WORKSPACE_CONTACT_IMPORT = "$SITE_ORIGIN/workspace/crm/contacts/import/"
    const val WORKSPACE_CONTACT_IMPORT_TEMPLATE = "$SITE_ORIGIN/workspace/crm/contacts/import/template/"
    const val WORKSPACE_COMPARE = "$SITE_ORIGIN/workspace/compare/"
    const val WORKSPACE_DATASETS = "$SITE_ORIGIN/workspace/datasets/"
    const val PRODUCT_HUB = "$SITE_ORIGIN/workspace/product/"

    fun buyPlan(planId: Long? = null, renewLicenseId: Long? = null): String {
        val params = buildList {
            if (planId != null) add("plan=$planId")
            if (renewLicenseId != null) add("renew=$renewLicenseId")
        }
        return if (params.isEmpty()) BUY_BOT else "$BUY_BOT?${params.joinToString("&")}"
    }

    fun paymentAppReturn(orderId: String): String =
        "$SITE_ORIGIN/payment/app-return/$orderId/"

    fun appPaymentDeepLink(orderId: String): String =
        "divarfiling://payment?order_id=$orderId"

    fun workspaceDataset(datasetId: String): String =
        "$WORKSPACE_DATASETS$datasetId/"
}
