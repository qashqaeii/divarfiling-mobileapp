package ir.divarfiling.mobile.feature.contentstudio.canvas

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PcsMediaUrlResolverTest {

    @Test
    fun buildsStaticUrlFromRelativeOverlayPath() {
        val url = PcsMediaUrlResolver.resolveOverlayUrl(
            overlayUrl = "",
            overlayPath = "images/Pearlescent Blue Real Estate Story Template-3.webp",
        )
        assertEquals(
            "https://divarfiling.ir/static/images/Pearlescent Blue Real Estate Story Template-3.webp",
            url,
        )
    }

    @Test
    fun normalizesStaticOverlayPathPrefix() {
        val url = PcsMediaUrlResolver.resolveOverlayUrl(
            overlayUrl = "",
            overlayPath = "/static/workspace/pcs/templates/pearlescent/overlay.webp",
        )
        assertEquals(
            "https://divarfiling.ir/static/workspace/pcs/templates/pearlescent/overlay.webp",
            url,
        )
    }

    @Test
    fun prefersManifestOverlayUrl() {
        val url = PcsMediaUrlResolver.resolveOverlayUrl(
            overlayUrl = "https://divarfiling.ir/static/x.png",
            overlayPath = "/static/other.png",
        )
        assertEquals("https://divarfiling.ir/static/x.png", url)
    }

    @Test
    fun upgradesDivarfilingHttpToHttps() {
        val normalized = PcsMediaUrlResolver.normalizeAssetUrl("http://divarfiling.ir/static/a.webp")
        assertEquals("https://divarfiling.ir/static/a.webp", normalized)
    }

    @Test
    fun supportsProtocolRelativeUrls() {
        val normalized = PcsMediaUrlResolver.normalizeAssetUrl("//divarfiling.ir/media/x.jpg")
        assertTrue(normalized!!.startsWith("https://"))
    }
}
