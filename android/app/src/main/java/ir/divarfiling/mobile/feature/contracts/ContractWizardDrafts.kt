package ir.divarfiling.mobile.feature.contracts

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

data class WizardPartyDraft(
    val role: String = "",
    val customerId: String = "",
    val personType: String = "natural",
    val firstName: String = "",
    val lastName: String = "",
    val fatherName: String = "",
    val nationalId: String = "",
    val mobile: String = "",
    val address: String = "",
    val postalCode: String = "",
    val companyName: String = "",
    val companyNationalId: String = "",
    val representativeName: String = "",
    val representativeNationalId: String = "",
    val representativeMobile: String = "",
)

data class WizardPropertyDraft(
    val crmPropertyId: String = "",
    val crmPropertyTitle: String = "",
    val propertyType: String = "",
    val usage: String = "",
    val city: String = "",
    val district: String = "",
    val neighborhood: String = "",
    val address: String = "",
    val postalCode: String = "",
    val area: String = "",
    val buildYear: String = "",
    val floor: String = "",
    val unit: String = "",
    val bedrooms: String = "",
    val hasParking: Boolean = false,
    val hasStorage: Boolean = false,
    val hasElevator: Boolean = false,
)

data class WizardLeaseFinancialDraft(
    val leaseStart: String = "",
    val leaseEnd: String = "",
    val deposit: String = "",
    val monthlyRent: String = "",
    val rentPaymentDay: String = "1",
    val paymentMethod: String = "transfer",
    val deliveryDate: String = "",
    val usageType: String = "residential",
    val occupantsCount: String = "",
)

data class WizardSaleFinancialDraft(
    val totalPrice: String = "",
    val downPayment: String = "",
    val earnestMoney: String = "",
    val deliveryDate: String = "",
    val transferDate: String = "",
    val transferPlace: String = "",
)

data class WizardJvFinancialDraft(
    val ownerPercent: String = "",
    val builderPercent: String = "",
    val startDate: String = "",
    val durationEstimate: String = "",
    val deliveryDate: String = "",
    val ownerContribution: String = "",
    val builderContribution: String = "",
)

data class WizardWitnessDraft(
    val witnessNumber: Int = 1,
    val witnessRole: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val nationalId: String = "",
    val mobile: String = "",
)

data class WizardClausesDraft(
    val selectedOptionalCodes: Set<String> = emptySet(),
    val customTitle: String = "",
    val customBody: String = "",
    val transferChoice: String = "denied",
    val usageType: String = "residential",
    val propertyHasTenant: Boolean = false,
)

private fun JsonObject.string(key: String): String =
    get(key)?.jsonPrimitive?.contentOrNull.orEmpty()

private fun JsonObject.bool(key: String): Boolean {
    val el = get(key) ?: return false
    return when {
        el.jsonPrimitive.isString -> el.jsonPrimitive.content in listOf("true", "1", "yes", "on")
        else -> el.jsonPrimitive.contentOrNull?.toBooleanStrictOrNull() == true
    }
}

fun partiesFromWizardData(wizard: JsonObject, defaultRoles: List<String>): List<WizardPartyDraft> {
    val arr = wizard["parties"]?.jsonArray ?: return defaultRoles.map { WizardPartyDraft(role = it) }
    val parsed = arr.mapNotNull { el ->
        val o = el.jsonObject
        WizardPartyDraft(
            role = o.string("role"),
            customerId = o.string("customer_id"),
            personType = o.string("person_type").ifBlank { "natural" },
            firstName = o.string("first_name"),
            lastName = o.string("last_name"),
            fatherName = o.string("father_name"),
            nationalId = o.string("national_id"),
            mobile = o.string("mobile"),
            address = o.string("address"),
            postalCode = o.string("postal_code"),
            companyName = o.string("company_name"),
            companyNationalId = o.string("company_national_id"),
            representativeName = o.string("representative_name"),
            representativeNationalId = o.string("representative_national_id"),
            representativeMobile = o.string("representative_mobile"),
        )
    }.filter { it.role.isNotBlank() }
    if (parsed.isNotEmpty()) return parsed
    return defaultRoles.map { WizardPartyDraft(role = it) }
}

fun propertyFromWizardData(wizard: JsonObject): WizardPropertyDraft {
    val o = wizard["property"]?.jsonObject ?: return WizardPropertyDraft()
    return WizardPropertyDraft(
        crmPropertyId = o.string("crm_property_id"),
        crmPropertyTitle = o.string("crm_property_title"),
        propertyType = o.string("property_type"),
        usage = o.string("usage"),
        city = o.string("city"),
        district = o.string("district"),
        neighborhood = o.string("neighborhood"),
        address = o.string("address"),
        postalCode = o.string("postal_code"),
        area = o.string("area"),
        buildYear = o.string("build_year"),
        floor = o.string("floor"),
        unit = o.string("unit"),
        bedrooms = o.string("bedrooms"),
        hasParking = o.bool("has_parking"),
        hasStorage = o.bool("has_storage"),
        hasElevator = o.bool("has_elevator"),
    )
}

fun leaseFinancialFromWizard(wizard: JsonObject): WizardLeaseFinancialDraft {
    val o = wizard["financial"]?.jsonObject ?: return WizardLeaseFinancialDraft()
    return WizardLeaseFinancialDraft(
        leaseStart = o.string("lease_start"),
        leaseEnd = o.string("lease_end"),
        deposit = o.string("deposit"),
        monthlyRent = o.string("monthly_rent"),
        rentPaymentDay = o.string("rent_payment_day").ifBlank { "1" },
        paymentMethod = o.string("payment_method").ifBlank { "transfer" },
        deliveryDate = o.string("delivery_date"),
        usageType = o.string("usage_type").ifBlank { "residential" },
        occupantsCount = o.string("occupants_count"),
    )
}

fun saleFinancialFromWizard(wizard: JsonObject): WizardSaleFinancialDraft {
    val o = wizard["financial"]?.jsonObject ?: return WizardSaleFinancialDraft()
    return WizardSaleFinancialDraft(
        totalPrice = o.string("total_price"),
        downPayment = o.string("down_payment"),
        earnestMoney = o.string("earnest_money"),
        deliveryDate = o.string("delivery_date"),
        transferDate = o.string("transfer_date"),
        transferPlace = o.string("transfer_place"),
    )
}

fun jvFinancialFromWizard(wizard: JsonObject): WizardJvFinancialDraft {
    val o = wizard["jv"]?.jsonObject ?: wizard["financial"]?.jsonObject ?: return WizardJvFinancialDraft()
    val contrib = o["contributions"]?.jsonObject
    return WizardJvFinancialDraft(
        ownerPercent = o.string("owner_percent"),
        builderPercent = o.string("builder_percent"),
        startDate = o.string("start_date"),
        durationEstimate = o.string("duration_estimate"),
        deliveryDate = o.string("delivery_date"),
        ownerContribution = contrib?.string("owner").orEmpty(),
        builderContribution = contrib?.string("builder").orEmpty(),
    )
}

fun witnessesFromWizard(wizard: JsonObject): List<WizardWitnessDraft> {
    val arr = wizard["witnesses"]?.jsonArray ?: return listOf(
        WizardWitnessDraft(witnessNumber = 1, witnessRole = "شاهد ۱"),
        WizardWitnessDraft(witnessNumber = 2, witnessRole = "شاهد ۲"),
    )
    val parsed = arr.mapIndexed { idx, el ->
        val o = el.jsonObject
        WizardWitnessDraft(
            witnessNumber = o["witness_number"]?.jsonPrimitive?.intOrNull ?: (idx + 1),
            witnessRole = o.string("witness_role").ifBlank { "شاهد ${idx + 1}" },
            firstName = o.string("first_name"),
            lastName = o.string("last_name"),
            nationalId = o.string("national_id"),
            mobile = o.string("mobile"),
        )
    }
    return parsed.ifEmpty {
        listOf(
            WizardWitnessDraft(witnessNumber = 1, witnessRole = "شاهد ۱"),
            WizardWitnessDraft(witnessNumber = 2, witnessRole = "شاهد ۲"),
        )
    }
}

fun clausesFromWizard(wizard: JsonObject): WizardClausesDraft {
    val clauseObj = wizard["clauses"]?.jsonObject
    val optional = buildSet {
        (clauseObj?.get("optional_clauses")?.jsonArray ?: wizard["optional_clauses"]?.jsonArray)?.forEach { el ->
            when {
                el.jsonPrimitive.isString -> add(el.jsonPrimitive.content)
                else -> el.jsonObject.string("code").takeIf { it.isNotBlank() }?.let { add(it) }
            }
        }
    }
    val custom = clauseObj?.get("custom_agreements")?.jsonArray?.firstOrNull()?.jsonObject
    return WizardClausesDraft(
        selectedOptionalCodes = optional,
        customTitle = custom?.string("title").orEmpty(),
        customBody = custom?.string("body").orEmpty(),
        transferChoice = clauseObj?.string("transfer_choice")?.ifBlank { "denied" } ?: "denied",
        usageType = clauseObj?.string("usage_type")?.ifBlank { "residential" } ?: "residential",
        propertyHasTenant = clauseObj?.bool("property_has_tenant") == true,
    )
}

fun partiesToJson(parties: List<WizardPartyDraft>): JsonElement = buildJsonArray {
    parties.filter { it.role.isNotBlank() }.forEach { p ->
        add(
            buildJsonObject {
                put("role", p.role)
                if (p.customerId.isNotBlank()) put("customer_id", p.customerId)
                put("person_type", p.personType.ifBlank { "natural" })
                put("first_name", p.firstName)
                put("last_name", p.lastName)
                put("father_name", p.fatherName)
                put("national_id", p.nationalId)
                put("mobile", p.mobile)
                put("address", p.address)
                put("postal_code", p.postalCode)
                put("company_name", p.companyName)
                put("company_national_id", p.companyNationalId)
                put("representative_name", p.representativeName)
                put("representative_national_id", p.representativeNationalId)
                put("representative_mobile", p.representativeMobile)
            },
        )
    }
}

fun propertyToJson(p: WizardPropertyDraft): JsonElement = buildJsonObject {
    if (p.crmPropertyId.isNotBlank()) put("crm_property_id", p.crmPropertyId)
    put("crm_property_title", p.crmPropertyTitle)
    put("property_type", p.propertyType)
    put("usage", p.usage)
    put("city", p.city)
    put("district", p.district)
    put("neighborhood", p.neighborhood)
    put("address", p.address)
    put("postal_code", p.postalCode)
    put("area", p.area)
    put("build_year", p.buildYear)
    put("floor", p.floor)
    put("unit", p.unit)
    put("bedrooms", p.bedrooms)
    put("has_parking", p.hasParking)
    put("has_storage", p.hasStorage)
    put("has_elevator", p.hasElevator)
}

fun leaseFinancialToJson(f: WizardLeaseFinancialDraft): JsonElement = buildJsonObject {
    put("lease_start", f.leaseStart)
    put("lease_end", f.leaseEnd)
    put("deposit", f.deposit)
    put("monthly_rent", f.monthlyRent)
    put("rent_payment_day", f.rentPaymentDay)
    put("payment_method", f.paymentMethod)
    put("delivery_date", f.deliveryDate)
    put("usage_type", f.usageType)
    put("occupants_count", f.occupantsCount)
}

fun saleFinancialToJson(f: WizardSaleFinancialDraft): JsonElement = buildJsonObject {
    put("total_price", f.totalPrice)
    put("down_payment", f.downPayment)
    put("earnest_money", f.earnestMoney)
    put("delivery_date", f.deliveryDate)
    put("transfer_date", f.transferDate)
    put("transfer_place", f.transferPlace)
}

fun jvFinancialToJson(f: WizardJvFinancialDraft): JsonElement = buildJsonObject {
    put("owner_percent", f.ownerPercent)
    put("builder_percent", f.builderPercent)
    put("start_date", f.startDate)
    put("duration_estimate", f.durationEstimate)
    put("delivery_date", f.deliveryDate)
    put(
        "contributions",
        buildJsonObject {
            put("owner", f.ownerContribution)
            put("builder", f.builderContribution)
        },
    )
}

fun witnessesToJson(list: List<WizardWitnessDraft>): JsonElement = buildJsonArray {
    list.filter { it.firstName.isNotBlank() || it.mobile.isNotBlank() }.forEach { w ->
        add(
            buildJsonObject {
                put("witness_number", w.witnessNumber)
                put("witness_role", w.witnessRole)
                put("first_name", w.firstName)
                put("last_name", w.lastName)
                put("national_id", w.nationalId)
                put("mobile", w.mobile)
            },
        )
    }
}

fun clausesToJson(c: WizardClausesDraft, contractType: String): JsonElement = buildJsonObject {
    put(
        "optional_clauses",
        buildJsonArray {
            c.selectedOptionalCodes.forEach { code -> add(JsonPrimitive(code)) }
        },
    )
    if (c.customBody.isNotBlank()) {
        put(
            "custom_agreements",
            buildJsonArray {
                add(
                    buildJsonObject {
                        put("title", c.customTitle.ifBlank { "توافق اختصاصی" })
                        put("body", c.customBody)
                    },
                )
            },
        )
    }
    when (contractType) {
        "lease" -> {
            put("transfer_choice", c.transferChoice)
            put("usage_type", c.usageType)
        }
        "sale" -> put("property_has_tenant", c.propertyHasTenant)
    }
}

fun defaultRolesForType(contractType: String): List<String> = when (contractType) {
    "lease" -> listOf("landlord", "tenant")
    "jv" -> listOf("owner", "builder")
    else -> listOf("seller", "buyer")
}
