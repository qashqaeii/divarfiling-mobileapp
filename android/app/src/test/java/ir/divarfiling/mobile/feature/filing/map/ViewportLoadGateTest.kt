package ir.divarfiling.mobile.feature.filing.map

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ViewportLoadGateTest {
    private fun viewport(
        centerLat: Double = 35.7,
        centerLon: Double = 51.4,
        zoom: Double = 13.0,
    ) = MapViewportState(
        north = centerLat + 0.05,
        south = centerLat - 0.05,
        east = centerLon + 0.05,
        west = centerLon - 0.05,
        zoom = zoom,
        centerLat = centerLat,
        centerLon = centerLon,
    )

    @Test
    fun firstViewportAlwaysFetches() {
        assertTrue(ViewportLoadGate.shouldFetch(null, viewport()))
    }

    @Test
    fun tinyPanDoesNotFetch() {
        val prev = viewport()
        val next = viewport(centerLat = prev.centerLat + 0.001, centerLon = prev.centerLon + 0.001)
        assertFalse(ViewportLoadGate.shouldFetch(prev, next))
    }

    @Test
    fun meaningfulPanFetches() {
        val prev = viewport()
        val next = viewport(centerLat = prev.centerLat + 0.03, centerLon = prev.centerLon)
        assertTrue(ViewportLoadGate.shouldFetch(prev, next))
    }

    @Test
    fun zoomChangeFetches() {
        val prev = viewport(zoom = 13.0)
        val next = viewport(zoom = 14.0)
        assertTrue(ViewportLoadGate.shouldFetch(prev, next))
    }
}
