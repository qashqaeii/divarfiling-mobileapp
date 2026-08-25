package ir.divarfiling.mobile.feature.crm

import ir.divarfiling.mobile.core.network.PropertyDto

data class PropertyFormState(
    val title: String = "",
    val propertyType: String = PropertyConstants.PROPERTY_TYPES.first(),
    val dealMode: String = PropertyConstants.DEAL_MODES.first(),
    val transactionStatus: String = PropertyConstants.TX_STATUSES.first(),
    val publishStatus: String = PropertyConstants.PUBLISH_STATUSES.first(),
    val businessType: String = "",
    val ownerPhone: String = "",
    val ownerName: String = "",
    val link: String = "",
    val token: String = "",
    val address: String = "",
    val city: String = "",
    val district: String = "",
    val neighborhood: String = "",
    val latitude: String = "",
    val longitude: String = "",
    val area: String = "",
    val rooms: String = "",
    val floor: String = "",
    val totalFloors: String = "",
    val buildYear: String = "",
    val amenityLabels: Set<String> = emptySet(),
    val amenitiesExtra: String = "",
    val salePrice: String = "",
    val deposit: String = "",
    val rent: String = "",
    val isVacant: Boolean = false,
    val tenantName: String = "",
    val tenantPhone: String = "",
    val vacancyDate: String = "",
    val notes: String = "",
    val images: List<String> = emptyList(),
    val features: Map<String, String> = emptyMap(),
) {
    val isRentDeal: Boolean
        get() = dealMode.contains("اجاره") || dealMode.contains("رهن")

    companion object {
        fun fromProperty(property: PropertyDto): PropertyFormState {
            val labels = PropertyAmenityCatalog.parseAmenityLabels(
                amenitiesText = property.amenities.orEmpty(),
                hasParking = property.hasParking,
                hasStorage = property.hasStorage,
                hasElevator = property.hasElevator,
            )
            val areaText = property.area?.let { v ->
                if (v % 1.0 == 0.0) v.toInt().toString() else v.toString()
            }.orEmpty()
            return PropertyFormState(
                title = property.title,
                propertyType = property.propertyType ?: PropertyConstants.PROPERTY_TYPES.first(),
                dealMode = property.dealMode ?: PropertyConstants.DEAL_MODES.first(),
                transactionStatus = property.transactionStatus ?: PropertyConstants.TX_STATUSES.first(),
                publishStatus = property.publishStatus ?: PropertyConstants.PUBLISH_STATUSES.first(),
                businessType = property.businessType.orEmpty(),
                ownerPhone = property.phone.orEmpty(),
                ownerName = property.ownerName.orEmpty(),
                link = property.link.orEmpty(),
                token = property.token.orEmpty(),
                address = property.address.orEmpty(),
                city = property.city.orEmpty(),
                district = property.district.orEmpty(),
                neighborhood = property.neighborhood.orEmpty(),
                latitude = property.latitude?.toString().orEmpty(),
                longitude = property.longitude?.toString().orEmpty(),
                area = areaText,
                rooms = property.rooms.orEmpty(),
                floor = property.floor?.toString().orEmpty(),
                totalFloors = property.totalFloors?.toString().orEmpty(),
                buildYear = property.buildYear?.toString().orEmpty(),
                amenityLabels = labels,
                amenitiesExtra = PropertyAmenityCatalog.extractExtraText(property.amenities.orEmpty(), labels),
                salePrice = property.salePrice?.toString().orEmpty(),
                deposit = property.deposit?.toString().orEmpty(),
                rent = property.rent?.toString().orEmpty(),
                isVacant = property.isVacant,
                tenantName = property.tenantName.orEmpty(),
                tenantPhone = property.tenantPhone.orEmpty(),
                vacancyDate = property.vacancyDate.orEmpty()
                    .ifBlank { property.featureData["تاریخ تخلیه"].orEmpty() },
                notes = property.notes.orEmpty(),
                images = property.images,
                features = property.featureData.filterKeys { it != "تاریخ تخلیه" },
            )
        }
    }
}
