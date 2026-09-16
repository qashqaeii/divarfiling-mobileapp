package ir.divarfiling.mobile.core

import ir.divarfiling.mobile.navigation.Routes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WorkspaceBridgePathsTest {
    @Test
    fun resolveClientPath_acceptsKnownRelativePaths() {
        assertEquals(
            WorkspaceBridgePaths.CONTACT_IMPORT,
            WorkspaceBridgePaths.resolveClientPath(WorkspaceBridgePaths.CONTACT_IMPORT),
        )
        assertEquals(
            WorkspaceBridgePaths.CRM_TEAM,
            WorkspaceBridgePaths.resolveClientPath(AppLinks.WORKSPACE_TEAM),
        )
    }

    @Test
    fun resolveClientPath_rejectsUnknownWorkspacePaths() {
        assertNull(WorkspaceBridgePaths.resolveClientPath("/workspace/compare/"))
        assertNull(WorkspaceBridgePaths.resolveClientPath("/workspace/crm/deals/1/"))
    }

    @Test
    fun workspaceWebRoute_encodesBridgeUrl() {
        val url = "https://divarfiling.ir/workspace/app-bridge/token123/"
        assertEquals(
            "workspace/web?bridgeUrl=https%3A%2F%2Fdivarfiling.ir%2Fworkspace%2Fapp-bridge%2Ftoken123%2F&webTitle=%D9%85%DB%8C%D8%B2%DA%A9%D8%A7%D8%B1",
            Routes.workspaceWeb(url),
        )
    }
}
