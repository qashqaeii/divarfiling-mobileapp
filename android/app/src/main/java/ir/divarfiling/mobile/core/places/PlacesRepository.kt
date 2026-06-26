package ir.divarfiling.mobile.core.places

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
private data class PlaceNode(
    val id: Int,
    val name: String,
    val slug: String? = null,
    val parent: Int? = null,
    val type: String? = null,
)

data class PlaceOption(
    val id: String,
    val name: String,
    val slug: String = "",
)

@Singleton
class PlacesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val json: Json,
) {
    private var loaded = false
    private val idToNode = mutableMapOf<Int, PlaceNode>()
    private val provinces = mutableListOf<PlaceOption>()
    private val citiesByProvince = mutableMapOf<Int, MutableList<PlaceOption>>()
    private val districtsByCity = mutableMapOf<Int, MutableList<PlaceOption>>()

    @Synchronized
    fun ensureLoaded() {
        if (loaded) return
        val raw = context.assets.open("places-web.json").bufferedReader().use { it.readText() }
        val nodes = json.decodeFromString<List<PlaceNode>>(raw)
        idToNode.clear()
        nodes.forEach { node -> idToNode[node.id] = node }

        val cities = nodes.filter { it.type == "2" }
        val provinceIds = cities.mapNotNull { it.parent }.toSet()
        provinces.clear()
        provinceIds.sorted().forEach { pid ->
            idToNode[pid]?.let { provinces.add(PlaceOption(pid.toString(), it.name)) }
        }
        provinces.sortBy { it.name }

        citiesByProvince.clear()
        cities.forEach { city ->
            val pid = city.parent ?: return@forEach
            citiesByProvince.getOrPut(pid) { mutableListOf() }
                .add(PlaceOption(city.id.toString(), city.name, city.slug.orEmpty()))
        }
        citiesByProvince.values.forEach { list -> list.sortBy { it.name } }

        districtsByCity.clear()
        nodes.filter { it.type == "4" }.forEach { district ->
            val cid = district.parent ?: return@forEach
            districtsByCity.getOrPut(cid) { mutableListOf() }
                .add(PlaceOption(district.id.toString(), district.name, district.slug.orEmpty()))
        }
        districtsByCity.values.forEach { list -> list.sortBy { it.name } }

        loaded = true
    }

    fun provinceNames(): List<String> {
        ensureLoaded()
        return provinces.map { it.name }
    }

    fun citiesForProvince(provinceName: String): List<PlaceOption> {
        ensureLoaded()
        val prov = provinces.firstOrNull { it.name == provinceName } ?: return emptyList()
        return citiesByProvince[prov.id.toInt()].orEmpty()
    }

    fun districtsForCity(cityId: String): List<PlaceOption> {
        ensureLoaded()
        return districtsByCity[cityId.toIntOrNull() ?: return emptyList()].orEmpty()
    }

    fun provinceForCity(cityId: String): PlaceOption? {
        ensureLoaded()
        val city = idToNode[cityId.toIntOrNull() ?: return null] ?: return null
        val pid = city.parent ?: return null
        val prov = idToNode[pid] ?: return null
        return PlaceOption(pid.toString(), prov.name)
    }

    fun cityName(cityId: String): String {
        ensureLoaded()
        return idToNode[cityId.toIntOrNull() ?: return ""]?.name ?: ""
    }
}
