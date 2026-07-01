package ir.divarfiling.mobile.core.image

import org.junit.Assert.assertEquals
import org.junit.Test

class DivarImageUrlUtilsTest {
    private val post =
        "https://postimage01.divarcdn.com/static/photo/neda/webp_post/eGxoc-iY-1xZAvjSar1Ciw/b3609a4e-b2f3-4c9f-bfe2-2c036d4349a9.webp"
    private val thumb =
        "https://postimage01.divarcdn.com/static/photo/neda/webp_thumbnail/94aKt6hlz-p4XRCnUkjHnw/b3609a4e-b2f3-4c9f-bfe2-2c036d4349a9.webp"

    @Test
    fun `prefers webp_post over thumbnail`() {
        val result = DivarImageUrlUtils.deduplicate(listOf(thumb, post))
        assertEquals(listOf(post), result)
    }

    @Test
    fun `keeps distinct images`() {
        val other =
            "https://postimage01.divarcdn.com/static/photo/neda/webp_post/abc/11111111-1111-1111-1111-111111111111.webp"
        val result = DivarImageUrlUtils.deduplicate(listOf(post, thumb, other))
        assertEquals(listOf(post, other), result)
    }
}
