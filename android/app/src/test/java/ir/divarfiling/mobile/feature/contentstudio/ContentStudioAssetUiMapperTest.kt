package ir.divarfiling.mobile.feature.contentstudio

import ir.divarfiling.mobile.feature.contentstudio.canvas.PcsAssetFailure
import ir.divarfiling.mobile.feature.contentstudio.canvas.PcsAssetFailureReason
import ir.divarfiling.mobile.feature.contentstudio.canvas.PcsAssetKind
import ir.divarfiling.mobile.feature.contentstudio.canvas.PcsLoadedAssets
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ContentStudioAssetUiMapperTest {

    @Test
    fun missingPropertyImagesDoNotBlockRender() {
        val assets = PcsLoadedAssets(
            propertyImages = listOf(null),
            missingPropertyIndices = setOf(0),
            failures = listOf(
                PcsAssetFailure(
                    kind = PcsAssetKind.PROPERTY_IMAGE,
                    reason = PcsAssetFailureReason.HTTP,
                    host = "cdn.example.com",
                    httpStatus = 404,
                    imageIndex = 0,
                ),
            ),
        )
        assertFalse(assets.hasCriticalFailure)
        val ui = ContentStudioAssetUiMapper.mapLoadedAssets(assets)
        assertFalse(ui.overlayLoadFailed)
        assertTrue(ui.assetNotice.orEmpty().contains("تصاویر"))
    }

    @Test
    fun overlayFailureSetsRetryStateWithoutCriticalFlag() {
        val assets = PcsLoadedAssets(
            overlay = null,
            failures = listOf(
                PcsAssetFailure(
                    kind = PcsAssetKind.OVERLAY,
                    reason = PcsAssetFailureReason.NETWORK,
                    host = "divarfiling.ir",
                ),
            ),
        )
        assertFalse(assets.hasCriticalFailure)
        val ui = ContentStudioAssetUiMapper.mapLoadedAssets(assets)
        assertTrue(ui.overlayLoadFailed)
        assertTrue(ui.assetNotice.orEmpty().contains("Overlay"))
    }

    @Test
    fun mixedFailuresKeepPartialNotice() {
        val assets = PcsLoadedAssets(
            propertyImages = listOf(null, null),
            missingPropertyIndices = setOf(1),
            failures = listOf(
                PcsAssetFailure(PcsAssetKind.PROPERTY_IMAGE, PcsAssetFailureReason.DECODE, imageIndex = 1),
                PcsAssetFailure(PcsAssetKind.OVERLAY, PcsAssetFailureReason.HTTP, httpStatus = 500),
            ),
        )
        val ui = ContentStudioAssetUiMapper.mapLoadedAssets(assets)
        assertTrue(ui.overlayLoadFailed)
        assertTrue(ui.assetNotice.orEmpty().contains("تصاویر"))
    }

    @Test
    fun successfulReloadClearsOverlayFailureFlags() {
        val ok = ContentStudioAssetUiMapper.mapLoadedAssets(PcsLoadedAssets())
        assertFalse(ok.overlayLoadFailed)
        assertNull(ok.assetNotice)
    }

    @Test
    fun sessionNotFoundDistinctFromAssetFailures() {
        assertTrue(ContentStudioAssetUiMapper.isSessionLevelNotFound("NOT_FOUND"))
        assertTrue(ContentStudioAssetUiMapper.isSessionLevelNotFound("http_404"))
        assertFalse(ContentStudioAssetUiMapper.isSessionLevelNotFound("MANIFEST_VERSION_MISMATCH"))
        assertFalse(ContentStudioAssetUiMapper.isSessionLevelNotFound(null))
    }

    @Test
    fun exportBlockedOnlyWhenAllPropertyImagesMissing() {
        val blocked = PcsLoadedAssets(
            propertyImages = listOf(null, null),
            missingPropertyIndices = setOf(0, 1),
        )
        assertEquals(
            "هیچ تصویر ملک برای خروجی در دسترس نیست.",
            ContentStudioAssetUiMapper.exportBlockedMessage(blocked),
        )
        val partial = PcsLoadedAssets(
            propertyImages = listOf(null),
            missingPropertyIndices = setOf(0),
            overlay = null,
            failures = listOf(PcsAssetFailure(PcsAssetKind.OVERLAY, PcsAssetFailureReason.NETWORK)),
        )
        assertNull(ContentStudioAssetUiMapper.exportBlockedMessage(partial))
    }
}
