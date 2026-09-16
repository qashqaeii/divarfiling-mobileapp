package ir.divarfiling.mobile.feature.filing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Draft dataset selection: changes apply only after explicit Apply (mirrors ViewModel contract). */
class FilingSearchUiStateTest {

    @Test
    fun defaultBrowseModeIsList() {
        assertEquals(FilingBrowseMode.LIST, FilingSearchUiState().browseMode)
    }

    @Test
    fun draftDoesNotChangeSelectedUntilApply() {
        var state = FilingSearchUiState(selectedDatasetIds = setOf("a"))
        state = state.copy(draftDatasetIds = setOf("a", "b"))
        assertEquals(setOf("a"), state.selectedDatasetIds)
        assertTrue(state.draftDatasetIds.contains("b"))
    }

    @Test
    fun applyCopiesDraftToSelectedAndClosesSheet() {
        val state = FilingSearchUiState(
            selectedDatasetIds = emptySet(),
            draftDatasetIds = setOf("x", "y"),
            showDatasetSelector = true,
        )
        val applied = state.copy(
            selectedDatasetIds = state.draftDatasetIds,
            showDatasetSelector = false,
        )
        assertEquals(setOf("x", "y"), applied.selectedDatasetIds)
        assertFalse(applied.showDatasetSelector)
    }

    @Test
    fun clearDraftResetsToAllFiles() {
        val state = FilingSearchUiState(draftDatasetIds = setOf("z"))
        val cleared = state.copy(draftDatasetIds = emptySet())
        assertTrue(cleared.draftDatasetIds.isEmpty())
    }
}
