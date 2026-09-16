package ir.divarfiling.mobile.feature.filing.map

data class MapViewportState(
    val north: Double,
    val south: Double,
    val east: Double,
    val west: Double,
    val zoom: Double,
    val centerLat: Double,
    val centerLon: Double,
) {
    fun toQueryParams(): Map<String, String> = mapOf(
        "north" to north.toString(),
        "south" to south.toString(),
        "east" to east.toString(),
        "west" to west.toString(),
    )
}

object ViewportLoadGate {
    private const val MIN_LAT_LNG_DELTA = 0.015
    private const val MIN_ZOOM_DELTA = 0.35

    fun shouldFetch(previous: MapViewportState?, next: MapViewportState): Boolean {
        if (previous == null) return true
        if (kotlin.math.abs(previous.zoom - next.zoom) >= MIN_ZOOM_DELTA) return true
        val latSpan = kotlin.math.abs(previous.north - previous.south)
        val lngSpan = kotlin.math.abs(previous.east - previous.west)
        val minMove = kotlin.math.max(MIN_LAT_LNG_DELTA, kotlin.math.min(latSpan, lngSpan) * 0.12)
        val latMoved = kotlin.math.abs(previous.centerLat - next.centerLat)
        val lngMoved = kotlin.math.abs(previous.centerLon - next.centerLon)
        return latMoved >= minMove || lngMoved >= minMove
    }
}

fun isStaleMapResponse(requestId: Int, currentGeneration: Int): Boolean =
    requestId != currentGeneration

object FilingDatasetSelectionLabels {
    fun toolbarLabel(selectedCount: Int, toPersianDigits: (String) -> String): String {
        return if (selectedCount <= 0) {
            "همه فایل‌ها"
        } else {
            "${toPersianDigits(selectedCount.toString())} فایل انتخاب شده"
        }
    }

    fun sheetSelectedCount(selectedCount: Int, toPersianDigits: (String) -> String): String {
        return if (selectedCount <= 0) {
            "همه فایل‌ها"
        } else {
            "${toPersianDigits(selectedCount.toString())} فایل انتخاب شده"
        }
    }
}
