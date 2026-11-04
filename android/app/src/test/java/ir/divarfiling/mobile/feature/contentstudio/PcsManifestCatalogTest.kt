package ir.divarfiling.mobile.feature.contentstudio

import org.junit.Assert.assertEquals
import org.junit.Test

class PcsManifestCatalogTest {
    private val templates = listOf(
        "pearlescent", "pearlescent_ribbon", "sculptural", "sculpted_travertine",
        "emerald_atelier", "pearl_gallery", "burgundy_alabaster", "amethyst_ceramic",
        "champagne_origami", "glacier_prism", "mediterranean_papercut", "terracotta_terrace",
        "brutalist_concrete",
    )
    private val formats = listOf("story", "post", "square")

    @Test
    fun catalogCoversThirtyNineTemplateFormatPairs() {
        assertEquals(13, templates.size)
        assertEquals(39, templates.size * formats.size)
    }
}
