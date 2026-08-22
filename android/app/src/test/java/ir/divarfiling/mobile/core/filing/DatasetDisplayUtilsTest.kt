package ir.divarfiling.mobile.core.filing

import ir.divarfiling.mobile.core.network.DatasetDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class DatasetDisplayUtilsTest {

    @Test
    fun displayTitle_translatesSlugAndSkipsEnglish() {
        val dataset = DatasetDto(
            id = "1",
            name = "raw-name",
            city = "ارومیه",
            transactionType = "اجاره مسکونی",
            category = "apartment-rent",
            subcategory = "آپارتمان",
            itemCount = 89,
        )

        val title = DatasetDisplayUtils.displayTitle(dataset)
        assertEquals("ارومیه · اجاره مسکونی · آپارتمان", title)
        assertFalse(title.contains("apartment"))
    }
}
