package ir.divarfiling.mobile.feature.filing.components

import ir.divarfiling.mobile.core.network.DatasetDto
import org.junit.Assert.assertEquals
import org.junit.Test

class FilingDatasetFiltersTest {

    @Test
    fun groupByLocation_splitsSubcategoriesUnderSameDistrict() {
        val rentApt = dataset("1", "اجاره", "آپارتمان", "تهران", "استاد معین")
        val sellApt = dataset("2", "فروش مسکونی", "آپارتمان", "تهران", "استاد معین")
        val villa = dataset("3", "فروش مسکونی", "خانه و ویلا", "تهران", "استاد معین")
        val other = dataset("4", "اجاره مسکونی", "آپارتمان", "تهران", "صادقیه")

        val grouped = FilingDatasetFilters.groupByLocation(listOf(rentApt, sellApt, villa, other))

        assertEquals(listOf("استاد معین", "صادقیه"), grouped.map { it.first })
        assertEquals(3, grouped[0].second.size)
        assertEquals("آپارتمان", grouped[0].second[0].subcategory)
        assertEquals("خانه و ویلا", grouped[0].second.last().subcategory)
        assertEquals(1, grouped[1].second.size)
    }

    @Test
    fun datasetDisplayTitle_usesTransactionAndSubcategory() {
        val dataset = dataset("1", "فروش مسکونی", "آپارتمان", "تهران", "استاد معین")
        assertEquals("فروش مسکونی — آپارتمان", datasetDisplayTitle(dataset))
    }

    private fun dataset(
        id: String,
        transaction: String,
        subcategory: String,
        city: String,
        district: String,
    ) = DatasetDto(
        id = id,
        name = "$district — $transaction — $subcategory",
        transactionType = transaction,
        subcategory = subcategory,
        city = city,
        district = district,
        itemCount = 10,
    )
}
