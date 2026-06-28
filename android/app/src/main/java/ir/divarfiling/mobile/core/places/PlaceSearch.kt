package ir.divarfiling.mobile.core.places

data class PlaceSearchResult(
    val provinceId: Int,
    val provinceName: String,
    val cityId: Int?,
    val cityName: String?,
    val districtId: Int?,
    val districtName: String?,
    val matchType: PlaceMatchType,
    val matchedText: String,
)

enum class PlaceMatchType { PROVINCE, CITY, DISTRICT }

object PlaceSearch {
    fun normName(raw: String): String {
        var s = raw.trim().lowercase()
        s = s.replace('ي', 'ی').replace('ك', 'ک').replace('ۀ', 'ه').replace('ـ', ' ')
        s = s.replace('\u200c', ' ')
        return s.split(Regex("\\s+")).filter { it.isNotBlank() }.joinToString(" ")
    }

    fun search(queryRaw: String, index: PlaceSearchIndex, limit: Int = 12): List<PlaceSearchResult> {
        val query = normName(queryRaw)
        if (query.length < 2) return emptyList()

        val results = linkedSetOf<PlaceSearchResult>()

        fun addExact(indexMap: Map<String, List<PlaceSearchNode>>, type: PlaceMatchType) {
            indexMap[query]?.forEach { node ->
                results.add(index.toResult(node, type, query))
            }
        }

        fun addContains(indexMap: Map<String, List<PlaceSearchNode>>, type: PlaceMatchType) {
            indexMap.forEach { (key, nodes) ->
                if (key.contains(query)) {
                    nodes.forEach { node ->
                        results.add(index.toResult(node, type, query))
                    }
                }
            }
        }

        addExact(index.cities, PlaceMatchType.CITY)
        addExact(index.provinces, PlaceMatchType.PROVINCE)
        addExact(index.districts, PlaceMatchType.DISTRICT)

        if (results.isEmpty()) {
            addContains(index.cities, PlaceMatchType.CITY)
            addContains(index.provinces, PlaceMatchType.PROVINCE)
            addContains(index.districts, PlaceMatchType.DISTRICT)
        }

        return results
            .sortedWith(
                compareBy<PlaceSearchResult> {
                    when (it.matchType) {
                        PlaceMatchType.CITY -> 0
                        PlaceMatchType.DISTRICT -> 1
                        PlaceMatchType.PROVINCE -> 2
                    }
                }.thenBy { it.districtName != null }
                    .thenBy { it.matchedText.length },
            )
            .take(limit)
    }

    fun resolve(queryRaw: String, index: PlaceSearchIndex): PlaceResolved? {
        val query = normName(queryRaw)
        if (query.isBlank()) return null

        fun pickExact(map: Map<String, List<PlaceSearchNode>>) = map[query]?.firstOrNull()
        fun pickContains(map: Map<String, List<PlaceSearchNode>>): PlaceSearchNode? {
            if (pickExact(map) != null) return null
            return map.entries.firstOrNull { it.key.contains(query) }?.value?.firstOrNull()
        }

        val node = pickExact(index.cities)
            ?: pickExact(index.provinces)
            ?: pickExact(index.districts)
            ?: pickContains(index.cities)
            ?: pickContains(index.provinces)
            ?: pickContains(index.districts)
            ?: return null

        return index.resolveNode(node)
    }
}

data class PlaceSearchNode(
    val id: Int,
    val name: String,
    val type: String,
    val parentId: Int?,
)

data class PlaceResolved(
    val provinceId: Int,
    val provinceName: String,
    val cityId: String,
    val cityName: String,
    val districtId: String = "",
    val districtName: String = "",
)

class PlaceSearchIndex private constructor(
    val provinces: Map<String, List<PlaceSearchNode>>,
    val cities: Map<String, List<PlaceSearchNode>>,
    val districts: Map<String, List<PlaceSearchNode>>,
    private val idToNode: Map<Int, PlaceSearchNode>,
) {
    fun toResult(node: PlaceSearchNode, type: PlaceMatchType, query: String): PlaceSearchResult {
        val resolved = resolveNode(node) ?: return PlaceSearchResult(
            provinceId = node.id,
            provinceName = node.name,
            cityId = null,
            cityName = null,
            districtId = null,
            districtName = null,
            matchType = type,
            matchedText = node.name,
        )
        return PlaceSearchResult(
            provinceId = resolved.provinceId,
            provinceName = resolved.provinceName,
            cityId = resolved.cityId.toIntOrNull(),
            cityName = resolved.cityName,
            districtId = resolved.districtId.toIntOrNull(),
            districtName = resolved.districtName.takeIf { it.isNotBlank() },
            matchType = type,
            matchedText = when (type) {
                PlaceMatchType.PROVINCE -> resolved.provinceName
                PlaceMatchType.CITY -> resolved.cityName
                PlaceMatchType.DISTRICT -> resolved.districtName.ifBlank { node.name }
            },
        )
    }

    fun resolveNode(node: PlaceSearchNode): PlaceResolved? = when (node.type) {
        "4" -> {
            val city = idToNode[node.parentId ?: return null] ?: return null
            val prov = idToNode[city.parentId ?: return null] ?: return null
            PlaceResolved(
                provinceId = prov.id,
                provinceName = prov.name,
                cityId = city.id.toString(),
                cityName = city.name,
                districtId = node.id.toString(),
                districtName = node.name,
            )
        }
        "2" -> {
            val prov = idToNode[node.parentId ?: return null] ?: return null
            PlaceResolved(
                provinceId = prov.id,
                provinceName = prov.name,
                cityId = node.id.toString(),
                cityName = node.name,
            )
        }
        else -> PlaceResolved(
            provinceId = node.id,
            provinceName = node.name,
            cityId = "1",
            cityName = "",
        )
    }

    companion object {
        fun fromNodes(nodes: List<PlaceSearchNode>): PlaceSearchIndex {
            val idToNode = nodes.associateBy { it.id }

            fun buildIndex(filter: (PlaceSearchNode) -> Boolean): Map<String, List<PlaceSearchNode>> {
                val map = mutableMapOf<String, MutableList<PlaceSearchNode>>()
                idToNode.values.filter(filter).forEach { node ->
                    val keys = buildSet {
                        add(PlaceSearch.normName(node.name))
                        node.name.split(Regex("\\s+")).forEach { part ->
                            if (part.length >= 2) add(PlaceSearch.normName(part))
                        }
                    }
                    keys.filter { it.isNotBlank() }.forEach { key ->
                        map.getOrPut(key) { mutableListOf() }.add(node)
                    }
                }
                return map
            }

            return PlaceSearchIndex(
                provinces = buildIndex { it.type == "1" || (it.type.isBlank() && it.parentId == null) },
                cities = buildIndex { it.type == "2" },
                districts = buildIndex { it.type == "4" },
                idToNode = idToNode,
            )
        }
    }
}

