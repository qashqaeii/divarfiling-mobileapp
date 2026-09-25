package ir.divarfiling.mobile.feature.team

object AgencySpaceTab {
    const val ALL = "all"
    const val PROPERTIES = "properties"
    const val CONTACTS = "contacts"
    const val PUBLISHED_BY_ME = "published_by_me"
    const val SAVED_BY_ME = "saved_by_me"

    val labels: List<Pair<String, String>> = listOf(
        ALL to "همه",
        PROPERTIES to "فایل‌ها",
        CONTACTS to "مخاطبین",
        PUBLISHED_BY_ME to "منتشرشده من",
        SAVED_BY_ME to "CRM من",
    )
}
