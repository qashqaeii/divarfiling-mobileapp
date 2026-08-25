package ir.divarfiling.mobile.feature.crm

import ir.divarfiling.mobile.core.design.FormatUtils
import ir.divarfiling.mobile.core.network.PropertyCreateRequest
import ir.divarfiling.mobile.core.network.PropertyUpdateRequest

object PropertyFormMappers {
    fun toCreateRequest(form: PropertyFormState): PropertyCreateRequest {
        val (hasParking, hasStorage, hasElevator) = PropertyAmenityCatalog.syncCoreFlags(form.amenityLabels)
        val featureData = buildFeaturePayload(form)
        return PropertyCreateRequest(
            title = form.title.trim(),
            dealMode = form.dealMode,
            transactionStatus = form.transactionStatus,
            propertyType = form.propertyType,
            publishStatus = form.publishStatus,
            businessType = form.businessType.trim(),
            city = form.city.trim(),
            district = form.district.trim(),
            neighborhood = form.neighborhood.trim(),
            address = form.address.trim(),
            latitude = form.latitude.trim().replace(',', '.').toDoubleOrNull(),
            longitude = form.longitude.trim().replace(',', '.').toDoubleOrNull(),
            salePrice = if (!form.isRentDeal) parseMoney(form.salePrice) else null,
            deposit = if (form.isRentDeal) parseMoney(form.deposit) else null,
            rent = if (form.isRentDeal) parseMoney(form.rent) else null,
            area = form.area.trim().replace(',', '.').toDoubleOrNull(),
            rooms = form.rooms.trim(),
            floor = form.floor.trim().toIntOrNull(),
            totalFloors = form.totalFloors.trim().toIntOrNull()
                ?: featureData["تعداد کل طبقات ساختمان"]?.toIntOrNull(),
            buildYear = form.buildYear.trim().toIntOrNull(),
            hasParking = hasParking,
            hasStorage = hasStorage,
            hasElevator = hasElevator,
            isVacant = form.isVacant,
            tenantName = if (form.isVacant) "" else form.tenantName.trim(),
            tenantPhone = if (form.isVacant) "" else form.tenantPhone.trim(),
            vacancyDate = form.vacancyDate.trim(),
            amenities = PropertyAmenityCatalog.serializeAmenities(form.amenityLabels, form.amenitiesExtra),
            ownerPhone = form.ownerPhone.trim(),
            ownerName = form.ownerName.trim(),
            phone = form.ownerPhone.trim(),
            link = form.link.trim(),
            token = form.token.trim(),
            notes = form.notes.trim(),
            images = form.images.filter { it.isNotBlank() },
            featureData = featureData,
        )
    }

    fun toUpdateRequest(form: PropertyFormState): PropertyUpdateRequest {
        val (hasParking, hasStorage, hasElevator) = PropertyAmenityCatalog.syncCoreFlags(form.amenityLabels)
        val featureData = buildFeaturePayload(form)
        return PropertyUpdateRequest(
            title = form.title.trim(),
            dealMode = form.dealMode,
            transactionStatus = form.transactionStatus,
            propertyType = form.propertyType,
            publishStatus = form.publishStatus,
            businessType = form.businessType.trim(),
            city = form.city.trim(),
            district = form.district.trim(),
            neighborhood = form.neighborhood.trim(),
            address = form.address.trim(),
            latitude = form.latitude.trim().replace(',', '.').toDoubleOrNull(),
            longitude = form.longitude.trim().replace(',', '.').toDoubleOrNull(),
            salePrice = if (!form.isRentDeal) parseMoney(form.salePrice) else null,
            deposit = if (form.isRentDeal) parseMoney(form.deposit) else null,
            rent = if (form.isRentDeal) parseMoney(form.rent) else null,
            area = form.area.trim().replace(',', '.').toDoubleOrNull(),
            rooms = form.rooms.trim(),
            floor = form.floor.trim().toIntOrNull(),
            totalFloors = form.totalFloors.trim().toIntOrNull()
                ?: featureData["تعداد کل طبقات ساختمان"]?.toIntOrNull(),
            buildYear = form.buildYear.trim().toIntOrNull(),
            hasParking = hasParking,
            hasStorage = hasStorage,
            hasElevator = hasElevator,
            isVacant = form.isVacant,
            tenantName = if (form.isVacant) "" else form.tenantName.trim(),
            tenantPhone = if (form.isVacant) "" else form.tenantPhone.trim(),
            vacancyDate = form.vacancyDate.trim(),
            amenities = PropertyAmenityCatalog.serializeAmenities(form.amenityLabels, form.amenitiesExtra),
            ownerName = form.ownerName.trim(),
            ownerPhone = form.ownerPhone.trim(),
            phone = form.ownerPhone.trim(),
            link = form.link.trim(),
            token = form.token.trim(),
            notes = form.notes,
            images = form.images.filter { it.isNotBlank() },
            featureData = featureData,
        )
    }

    private fun buildFeaturePayload(form: PropertyFormState): Map<String, String> {
        val activeKeys = PropertyFeatureSchema.profileFor(form.dealMode, form.propertyType)
            .groups.flatMap { it.keys }.toSet()
            .groups.flatMap { it.keys }.toSet()
        val payload = form.features.filterKeys { it in activeKeys }
            .filterValues { it.isNotBlank() }
            .toMutableMap()
        if (form.vacancyDate.isNotBlank()) {
            payload["تاریخ تخلیه"] = form.vacancyDate.trim()
        }
        return payload
    }

    private fun parseMoney(raw: String): Long? = FormatUtils.parseLocalizedLong(raw.trim())
}
