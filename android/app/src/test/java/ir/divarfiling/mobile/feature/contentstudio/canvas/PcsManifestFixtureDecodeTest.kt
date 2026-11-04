package ir.divarfiling.mobile.feature.contentstudio.canvas

import kotlinx.serialization.json.Json
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PcsManifestFixtureDecodeTest {

    private val json = Json { ignoreUnknownKeys = true }

    private val templates = listOf(
        "pearlescent", "pearlescent_ribbon", "sculptural", "sculpted_travertine",
        "emerald_atelier", "pearl_gallery", "burgundy_alabaster", "amethyst_ceramic",
        "champagne_origami", "glacier_prism", "mediterranean_papercut", "terracotta_terrace",
        "brutalist_concrete",
    )
    private val formats = listOf("story", "post", "square")

    @Test
    fun allThirtyNineManifestFixturesDecodeScene() {
        val cl = javaClass.classLoader ?: error("no classloader")
        var count = 0
        for (tpl in templates) {
            for (fmt in formats) {
                val path = "pcs_manifests/$tpl/$fmt.json"
                val stream = cl.getResourceAsStream(path) ?: error("missing fixture $path")
                val text = stream.bufferedReader().readText()
                val root = json.parseToJsonElement(text) as kotlinx.serialization.json.JsonObject
                val sceneEl = root["scene"] ?: error("no scene in $path")
                val scene = json.decodeFromJsonElement(PcsSceneDocument.serializer(), sceneEl)
                assertTrue(scene.layers.isNotEmpty())
                count++
            }
        }
        assertTrue(count == 39)
    }

    @Test
    fun pearlescentStoryMergePreservesHeadlineResolver() {
        val cl = javaClass.classLoader!!
        val text = cl.getResourceAsStream("pcs_manifests/pearlescent/story.json")!!.bufferedReader().readText()
        val root = json.parseToJsonElement(text) as kotlinx.serialization.json.JsonObject
        val base = json.decodeFromJsonElement(PcsSceneDocument.serializer(), root["scene"]!!)
        val headline = base.layers.find { it.id == "headline-1" }
        assertNotNull(headline?.contentContract?.resolver)
        val draft = base.copy(
            layers = base.layers.map {
                if (it.id == "headline-1") it.copy(x = it.x + 10f, contentContract = null) else it
            },
        )
        val merged = PcsSceneMerger.mergeManifestWithDraft(base, draft)
        val mergedHeadline = merged.layers.find { it.id == "headline-1" }
        assertNotNull(mergedHeadline?.contentContract?.resolver)
    }
}
