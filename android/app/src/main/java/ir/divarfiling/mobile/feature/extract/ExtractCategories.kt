package ir.divarfiling.mobile.feature.extract

data class ExtractSubcategory(
    val label: String,
    val apiSlug: String,
)

data class ExtractTransactionType(
    val label: String,
    val subcategories: List<ExtractSubcategory>,
)

object ExtractCategories {
    val transactionTypes: List<ExtractTransactionType> = listOf(
        ExtractTransactionType(
            label = "اجاره مسکونی",
            subcategories = listOf(
                ExtractSubcategory("آپارتمان", "apartment-rent"),
                ExtractSubcategory("خانه و ویلا", "house-villa-rent"),
            ),
        ),
        ExtractTransactionType(
            label = "فروش مسکونی",
            subcategories = listOf(
                ExtractSubcategory("آپارتمان", "residential-sell"),
                ExtractSubcategory("خانه و ویلا", "house-villa-sell"),
                ExtractSubcategory("کلنگی", "plot-old"),
            ),
        ),
        ExtractTransactionType(
            label = "اجاره اداری و تجاری",
            subcategories = listOf(
                ExtractSubcategory("دفتر کار، اتاق اداری، مطب", "office-rent"),
                ExtractSubcategory("مغازه و غرفه", "shop-rent"),
                ExtractSubcategory("صنعتی، کشاورزی، تجاری", "industry-agriculture-business-rent"),
            ),
        ),
        ExtractTransactionType(
            label = "فروش اداری و تجاری",
            subcategories = listOf(
                ExtractSubcategory("دفتر کار، اتاق اداری، مطب", "office-sell"),
                ExtractSubcategory("مغازه و غرفه", "shop-sell"),
                ExtractSubcategory("صنعتی، کشاورزی، تجاری", "industry-agriculture-business-sell"),
            ),
        ),
        ExtractTransactionType(
            label = "پروژه‌های ساخت‌وساز",
            subcategories = listOf(
                ExtractSubcategory("پروژه‌های ساخت‌وساز", "real-estate-services"),
            ),
        ),
    )

    val sortOptions = listOf(
        "sort_date" to "جدیدترین",
        "sort_cheapest" to "ارزان‌ترین",
        "sort_expensive" to "گران‌ترین",
    )

    val advertiserOptions = listOf(
        "all" to "همه آگهی‌ها",
        "genuine_personal" to "مالک واقعی",
        "personal" to "شخصی",
        "consultant" to "مشاور",
    )

    fun slugFor(transactionLabel: String, subcategoryLabel: String): String? =
        transactionTypes
            .firstOrNull { it.label == transactionLabel }
            ?.subcategories
            ?.firstOrNull { it.label == subcategoryLabel }
            ?.apiSlug

    fun labelForSlug(slug: String?): String? {
        val normalized = slug?.trim()?.takeIf { it.isNotBlank() } ?: return null
        return allJobs.firstOrNull { it.apiSlug.equals(normalized, ignoreCase = true) }?.subcategoryLabel
    }

    fun needsCategoryViewFlags(slug: String): Boolean {
        val commercial = setOf(
            "office-sell", "shop-sell", "industry-agriculture-business-sell",
            "office-rent", "shop-rent", "industry-agriculture-business-rent",
        )
        return slug in commercial || slug in setOf("house-villa-sell", "house-villa-rent")
    }

    fun isRentCategory(slug: String): Boolean = "rent" in slug

    data class ExtractJob(
        val transactionType: String,
        val subcategoryLabel: String,
        val apiSlug: String,
    ) {
        val displayName: String get() = "$transactionType — $subcategoryLabel"
        val key: String get() = "$transactionType|$subcategoryLabel"
    }

    val allJobs: List<ExtractJob> = transactionTypes.flatMap { tx ->
        tx.subcategories.map { sub ->
            ExtractJob(
                transactionType = tx.label,
                subcategoryLabel = sub.label,
                apiSlug = sub.apiSlug,
            )
        }
    }

    private val residentialLabels = setOf("اجاره مسکونی", "فروش مسکونی")
    private val commercialLabels = setOf(
        "اجاره اداری و تجاری",
        "فروش اداری و تجاری",
        "پروژه‌های ساخت‌وساز",
    )

    val residentialJobs: List<ExtractJob>
        get() = allJobs.filter { it.transactionType in residentialLabels }

    val commercialJobs: List<ExtractJob>
        get() = allJobs.filter { it.transactionType in commercialLabels }

    fun jobsForPreset(preset: ExtractBatchPreset): List<ExtractJob> = when (preset) {
        ExtractBatchPreset.ALL -> allJobs
        ExtractBatchPreset.RESIDENTIAL -> residentialJobs
        ExtractBatchPreset.COMMERCIAL -> commercialJobs
        ExtractBatchPreset.CUSTOM -> emptyList()
        ExtractBatchPreset.SINGLE -> emptyList()
    }
}

enum class ExtractBatchPreset {
    SINGLE,
    ALL,
    RESIDENTIAL,
    COMMERCIAL,
    CUSTOM,
}
