package ir.divarfiling.mobile.feature.contentstudio.canvas

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class PcsSceneMergerTest {

    @Test
    fun mergeKeepsContentContractFromManifest() {
        val base = PcsSceneDocument(
            layers = listOf(
                PcsLayer(
                    id = "headline-1",
                    type = "text",
                    x = 0f,
                    y = 0f,
                    width = 100f,
                    height = 40f,
                    contentContract = PcsContentContract(resolver = "poster_headline", templateRole = "headline"),
                ),
            ),
            layerOrder = listOf("headline-1"),
        )
        val draft = base.copy(
            layers = listOf(
                base.layers.first().copy(x = 50f, text = "سلام", contentContract = null),
            ),
        )
        val merged = PcsSceneMerger.mergeManifestWithDraft(base, draft)
        val layer = merged.layers.first()
        assertEquals(50f, layer.x)
        assertEquals("poster_headline", layer.contentContract?.resolver)
    }

    @Test
    fun mergeUsesDraftLayerOrderWhenValid() {
        val base = PcsSceneDocument(
            layers = listOf(
                PcsLayer(id = "a", type = "text", width = 1f, height = 1f),
                PcsLayer(id = "b", type = "text", width = 1f, height = 1f),
            ),
            layerOrder = listOf("a", "b"),
        )
        val draft = base.copy(layerOrder = listOf("b", "a"))
        val merged = PcsSceneMerger.mergeManifestWithDraft(base, draft)
        assertEquals(listOf("b", "a"), merged.layerOrder)
    }
}
