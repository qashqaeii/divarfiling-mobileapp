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
    val ownershipShare: String = "",
    val companyName: String = "",
    val companyNationalId: String = "",
    val representativeName: String = "",
    val representativeNationalId: String = "",
    val representativeMobile: String = "",
)

data class WizardUtilityDraft(
    val connectionType: String = "",
    val subscriptionNo: String = "",
    val billId: String = "",
)

data class WizardPropertyDraft(
    val crmPropertyId: String = "",
    val crmPropertyTitle: String = "",
    val propertyType: String = "",
    val usage: String = "",
    val province: String = "",
    val city: String = "",
    val county: String = "",
    val district: String = "",
    val neighborhood: String = "",
    val address: String = "",
    val postalCode: String = "",
    val area: String = "",
    val buildYear: String = "",
    val totalFloors: String = "",
    val unitsPerFloor: String = "",
    val floor: String = "",
    val unit: String = "",
    val bedrooms: String = "",
    val buildingDirection: String = "",
    val unitDirection: String = "",
    val facadeType: String = "",
    val floorType: String = "",
    val kitchenType: String = "",
    val bathroomType: String = "",
    val heatingSystem: String = "",
    val coolingSystem: String = "",
    val hasParking: Boolean = false,
    val parkingNumber: String = "",
    val hasStorage: Boolean = false,
    val storageNumber: String = "",
    val hasElevator: Boolean = false,
    val deedType: String = "",
    val deedNumber: String = "",
    val mainPlaque: String = "",
    val subPlaque: String = "",
    val registrationSection: String = "",
    val water: WizardUtilityDraft = WizardUtilityDraft(),
    val electricity: WizardUtilityDraft = WizardUtilityDraft(),
    val gas: WizardUtilityDraft = WizardUtilityDraft(),
    val phone: WizardUtilityDraft = WizardUtilityDraft(),
    val sewage: WizardUtilityDraft = WizardUtilityDraft(),
    val commonAreas: List<String> = emptyList(),
    val hasMortgage: Boolean = false,
    val hasTenant: Boolean = false,
    val hasLoan: Boolean = false,
    val hasDebt: Boolean = false,
    val hasCompletionCert: Boolean = false,
    val hasSeparationMinutes: Boolean = false,
    val declaredByParties: Boolean = true,
)

data class WizardPenaltyDraft(
    val enabled: Boolean = false,
    val amountPerDay: String = "",
    val unit: String = "daily",
)

data class WizardLeaseFinancialDraft(
    val leaseStart: String = "",
    val leaseEnd: String = "",
    val deposit: String = "",
    val monthlyRent: String = "",
    val rentPaymentDay: String = "1",
    val paymentMethod: String = "transfer",
    val paymentDestination: String = "",
    val paymentCard: String = "",
    val paymentAccount: String = "",
    val paymentIban: String = "",
    val deliveryDate: String = "",
    val deliveryDelayPenalty: String = "",
    val vacateDate: String = "",
    val depositMode: String = "new",
    val depositPaymentDate: String = "",
    val depositPaymentMethod: String = "",
    val previousDepositAmount: String = "",
    val previousContractRef: String = "",
    val previousContractDate: String = "",
    val usageType: String = "residential",
    val occupantsCount: String = "",
    val transferToThirdPartyAllowed: Boolean = false,
    val costs: Map<String, String> = emptyMap(),
    val penalties: Map<String, WizardPenaltyDraft> = emptyMap(),
)

data class WizardPaymentDraft(
    val label: String = "",
    val amount: String = "",
    val dueDate: String = "",
    val paymentMethod: String = "",
    val notes: String = "",
)

data class WizardSaleFinancialDraft(
    val totalPrice: String = "",
    val downPayment: String = "",
    val earnestMoney: String = "",
    val deliveryDate: String = "",
    val transferDate: String = "",
    val transferPlace: String = "",
    val notaryOfficeNumber: String = "",
    val remainingAmountAtTransfer: String = "",
    val officialTransferDate: String = "",
    val payments: List<WizardPaymentDraft> = emptyList(),
)

data class WizardJvMilestoneDraft(
    val title: String = "",
    val dueDate: String = "",
    val notes: String = "",
)

data class WizardJvUnitDraft(
    val floor: String = "",
    val unit: String = "",
    val ownerType: String = "",
    val notes: String = "",
)

data class WizardJvGuaranteeDraft(
    val type: String = "",
    val amount: String = "",
    val notes: String = "",
)

data class WizardJvFinancialDraft(
    val ownerPercent: String = "",
    val builderPercent: String = "",
    val startDate: String = "",
    val durationEstimate: String = "",
    val deliveryDate: String = "",
    val ownerContribution: String = "",
    val builderContribution: String = "",
    val milestones: List<WizardJvMilestoneDraft> = emptyList(),
    val unitAllocations: List<WizardJvUnitDraft> = emptyList(),
    val guarantees: List<WizardJvGuaranteeDraft> = emptyList(),
)

data class WizardWitnessDraft(
    val witnessNumber: Int = 1,
    val witnessRole: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val nationalId: String = "",
    val mobile: String = "",
)

data class WizardCustomAgreementDraft(
    val title: String = "",
    val body: String = "",
)

data class WizardClausesDraft(
    val selectedOptionalCodes: Set<String> = emptySet(),
    val customAgreements: List<WizardCustomAgreementDraft> = emptyList(),
    val customTitle: String = "",
    val customBody: String = "",
    val transferChoice: String = "denied",
    val usageType: String = "residential",
    val occupantsCount: String = "",
    val propertyHasTenant: Boolean = false,
    val leaseCosts: Map<String, String> = emptyMap(),
    val leasePenalties: Map<String, WizardPenaltyDraft> = emptyMap(),
    val saleCosts: Map<String, String> = emptyMap(),
    val salePenalties: Map<String, WizardPenaltyDraft> = emptyMap(),
)

val LEASE_COST_KEYS = listOf(
    "water", "electricity", "gas", "phone", "charge", "minor_repairs", "major_repairs", "property_tax",
)

val SALE_COST_KEYS = listOf(
    "transfer_costs", "notary_costs", "pre_delivery_costs", "post_delivery_costs", "other_costs",
)

val LEASE_PENALTY_KEYS = listOf(
    "landlord_delivery_delay", "tenant_vacate_delay", "deposit_return_delay",
)

val SALE_PENALTY_KEYS = listOf(
    "payment_delay", "delivery_delay", "transfer_delay",
)

private fun JsonObject.string(key: String): String {
    val el = get(key) ?: return ""
    return when {
        el.jsonPrimitive.isString -> el.jsonPrimitive.content
        else -> el.jsonPrimitive.contentOrNull.orEmpty()
    }
}

private fun JsonObject.bool(key: String): Boolean {
    val el = get(key) ?: return false
    return when {
        el.jsonPrimitive.isString -> el.jsonPrimitive.content in listOf("true", "1", "yes", "on")
        else -> el.jsonPrimitive.contentOrNull?.toBooleanStrictOrNull() == true
    }
}

private fun parseUtility(o: JsonObject?): WizardUtilityDraft {
    if (o == null) return WizardUtilityDraft()
    return WizardUtilityDraft(
        connectionType = o.string("connection_type"),
        subscriptionNo = o.string("subscription_no"),
        billId = o.string("bill_id"),
    )
}

private fun applyUtilitiesFromJson(draft: WizardPropertyDraft, o: JsonObject): WizardPropertyDraft {
    val util = o["utilities"]?.jsonObject ?: return draft
    return draft.copy(
        water = parseUtility(util["water"]?.jsonObject),
        electricity = parseUtility(util["electricity"]?.jsonObject),
        gas = parseUtility(util["gas"]?.jsonObject),
        phone = parseUtility(util["phone"]?.jsonObject),
        sewage = parseUtility(util["sewage"]?.jsonObject),
    )
}

private fun parseCostsMap(o: JsonObject?): Map<String, String> {
    if (o == null) return emptyMap()
    return o.mapNotNull { (k, v) ->
        v.jsonPrimitive.contentOrNull?.takeIf { it.isNotBlank() }?.let { k to it }
    }.toMap()
}

private fun parsePenaltiesMap(o: JsonObject?): Map<String, WizardPenaltyDraft> {
    if (o == null) return emptyMap()
    return o.mapNotNull { (k, v) ->
        val p = v.jsonObject
        k to WizardPenaltyDraft(
            enabled = p.bool("enabled"),
            amountPerDay = p.string("amount_per_day"),
            unit = p.string("unit").ifBlank { "daily" },
        )
    }.toMap()
}

private fun putUtility(build: kotlinx.serialization.json.JsonObjectBuilder, key: String, u: WizardUtilityDraft) {
    if (u.connectionType.isBlank() && u.subscriptionNo.isBlank() && u.billId.isBlank()) return
    build.put(
        key,
        buildJsonObject {
            put("connection_type", u.connectionType)
            put("subscription_no", u.subscriptionNo)
            put("bill_id", u.billId)
        },
    )
}

fun partyDraftFromCustomerPayload(role: String, payload: JsonObject): WizardPartyDraft {
    val customerId = payload["customer_id"]?.jsonPrimitive?.contentOrNull.orEmpty()
    return WizardPartyDraft(
        role = role,
        customerId = customerId,
        personType = payload.string("person_type").ifBlank { "natural" },
        firstName = payload.string("first_name"),
        lastName = payload.string("last_name"),
        fatherName = payload.string("father_name"),
        nationalId = payload.string("national_id"),
        mobile = payload.string("mobile"),
        address = payload.string("address"),
        postalCode = payload.string("postal_code"),
        ownershipShare = payload.string("ownership_share"),
        companyName = payload.string("company_name"),
        companyNationalId = payload.string("company_national_id"),
        representativeName = payload.string("representative_name"),
        representativeNationalId = payload.string("representative_national_id"),
        representativeMobile = payload.string("representative_mobile"),
    )
}

fun propertyDraftFromWizardPayload(payload: JsonObject): WizardPropertyDraft {
    return propertyFromWizardData(buildJsonObject { put("property", payload) })
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
            ownershipShare = o.string("ownership_share"),
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
    val common = o["common_areas"]?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull } ?: emptyList()
    return WizardPropertyDraft(
        crmPropertyId = o.string("crm_property_id"),
        crmPropertyTitle = o.string("crm_property_title"),
        propertyType = o.string("property_type"),
        usage = o.string("usage"),
        province = o.string("province"),
        city = o.string("city"),
        county = o.string("county"),
        district = o.string("district"),
        neighborhood = o.string("neighborhood"),
        address = o.string("address"),
        postalCode = o.string("postal_code"),
        area = o.string("area"),
        buildYear = o.string("build_year"),
        totalFloors = o.string("total_floors"),
        unitsPerFloor = o.string("units_per_floor"),
        floor = o.string("floor"),
        unit = o.string("unit"),
        bedrooms = o.string("bedrooms"),
        buildingDirection = o.string("building_direction"),
        unitDirection = o.string("unit_direction"),
        facadeType = o.string("facade_type"),
        floorType = o.string("floor_type"),
        kitchenType = o.string("kitchen_type"),
        bathroomType = o.string("bathroom_type"),
        heatingSystem = o.string("heating_system"),
        coolingSystem = o.string("cooling_system"),
        hasParking = o.bool("has_parking"),
        parkingNumber = o.string("parking_number"),
        hasStorage = o.bool("has_storage"),
        storageNumber = o.string("storage_number"),
        hasElevator = o.bool("has_elevator"),
        deedType = o.string("deed_type"),
        deedNumber = o.string("deed_number"),
        mainPlaque = o.string("main_plaque"),
        subPlaque = o.string("sub_plaque"),
        registrationSection = o.string("registration_section"),
        commonAreas = common,
        hasMortgage = o.bool("has_mortgage"),
        hasTenant = o.bool("has_tenant"),
        hasLoan = o.bool("has_loan"),
        hasDebt = o.bool("has_debt"),
        hasCompletionCert = o.bool("has_completion_cert"),
        hasSeparationMinutes = o.bool("has_separation_minutes"),
        declaredByParties = o.bool("declared_by_parties"),
    ).let { applyUtilitiesFromJson(it, o) }
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
        paymentDestination = o.string("payment_destination"),
        paymentCard = o.string("payment_card"),
        paymentAccount = o.string("payment_account"),
        paymentIban = o.string("payment_iban"),
        deliveryDate = o.string("delivery_date"),
        deliveryDelayPenalty = o.string("delivery_delay_penalty"),
        vacateDate = o.string("vacate_date"),
        depositMode = o.string("deposit_mode").ifBlank { "new" },
        depositPaymentDate = o.string("deposit_payment_date"),
        depositPaymentMethod = o.string("deposit_payment_method"),
        previousDepositAmount = o.string("previous_deposit_amount"),
        previousContractRef = o.string("previous_contract_ref"),
        previousContractDate = o.string("previous_contract_date"),
        usageType = o.string("usage_type").ifBlank { "residential" },
        occupantsCount = o.string("occupants_count"),
        transferToThirdPartyAllowed = o.bool("transfer_to_third_party_allowed"),
        costs = parseCostsMap(o["costs"]?.jsonObject),
        penalties = parsePenaltiesMap(o["penalties"]?.jsonObject),
    )
}

fun saleFinancialFromWizard(wizard: JsonObject): WizardSaleFinancialDraft {
    val o = wizard["financial"]?.jsonObject ?: return WizardSaleFinancialDraft()
    val payments = o["payments"]?.jsonArray?.map { el ->
        val p = el.jsonObject
        WizardPaymentDraft(
            label = p.string("label"),
            amount = p.string("amount"),
            dueDate = p.string("due_date"),
            paymentMethod = p.string("payment_method"),
            notes = p.string("notes"),
        )
    } ?: emptyList()
    return WizardSaleFinancialDraft(
        totalPrice = o.string("total_price"),
        downPayment = o.string("down_payment"),
        earnestMoney = o.string("earnest_money"),
        deliveryDate = o.string("delivery_date"),
        transferDate = o.string("transfer_date"),
        transferPlace = o.string("transfer_place"),
        notaryOfficeNumber = o.string("notary_office_number"),
        remainingAmountAtTransfer = o.string("remaining_amount_at_transfer"),
        officialTransferDate = o.string("official_transfer_date"),
        payments = payments,
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
        milestones = o["milestones"]?.jsonArray?.map { el ->
            val m = el.jsonObject
            WizardJvMilestoneDraft(m.string("title"), m.string("due_date"), m.string("notes"))
        } ?: emptyList(),
        unitAllocations = o["unit_allocations"]?.jsonArray?.map { el ->
            val u = el.jsonObject
            WizardJvUnitDraft(u.string("floor"), u.string("unit"), u.string("owner_type"), u.string("notes"))
        } ?: emptyList(),
        guarantees = o["guarantees"]?.jsonArray?.map { el ->
            val g = el.jsonObject
            WizardJvGuaranteeDraft(g.string("type"), g.string("amount"), g.string("notes"))
        } ?: emptyList(),
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
    val fin = wizard["financial"]?.jsonObject
    val prop = wizard["property"]?.jsonObject
    val clauseObj = wizard["clauses"]?.jsonObject

    val optional = buildSet {
        (wizard["clause_optional"]?.jsonArray
            ?: clauseObj?.get("optional_clauses")?.jsonArray
            ?: wizard["optional_clauses"]?.jsonArray)?.forEach { el ->
            when {
                el.jsonPrimitive.isString -> add(el.jsonPrimitive.content)
                else -> el.jsonObject.string("code").takeIf { it.isNotBlank() }?.let { add(it) }
            }
        }
    }

    val customs = (wizard["custom_agreements"]?.jsonArray
        ?: clauseObj?.get("custom_agreements")?.jsonArray)?.mapNotNull { el ->
        val o = el.jsonObject
        val body = o.string("body").ifBlank { o.string("body_text") }
        if (body.isBlank()) null else WizardCustomAgreementDraft(
            title = o.string("title").ifBlank { "توافق اختصاصی" },
            body = body,
        )
    } ?: emptyList()
    val legacy = customs.firstOrNull()

    val transferChoice = when {
        fin != null -> if (fin.bool("transfer_to_third_party_allowed")) "with_consent" else "denied"
        else -> clauseObj?.string("transfer_choice")?.ifBlank { "denied" } ?: "denied"
    }

    return WizardClausesDraft(
        selectedOptionalCodes = optional,
        customAgreements = customs,
        customTitle = legacy?.title.orEmpty(),
        customBody = legacy?.body.orEmpty(),
        transferChoice = transferChoice,
        usageType = fin?.string("usage_type")?.ifBlank { "residential" }
            ?: clauseObj?.string("usage_type")?.ifBlank { "residential" }
            ?: "residential",
        occupantsCount = fin?.string("occupants_count").orEmpty()
            .ifBlank { clauseObj?.string("occupants_count").orEmpty() },
        propertyHasTenant = prop?.bool("has_tenant") == true
            || fin?.bool("property_has_tenant") == true
            || clauseObj?.bool("property_has_tenant") == true,
        leaseCosts = parseCostsMap(fin?.get("costs")?.jsonObject ?: clauseObj?.get("costs")?.jsonObject),
        leasePenalties = parsePenaltiesMap(fin?.get("penalties")?.jsonObject ?: clauseObj?.get("penalties")?.jsonObject),
        saleCosts = parseCostsMap(fin?.get("sale_costs")?.jsonObject ?: clauseObj?.get("sale_costs")?.jsonObject),
        salePenalties = parsePenaltiesMap(fin?.get("penalties")?.jsonObject ?: clauseObj?.get("penalties")?.jsonObject),
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
                put("ownership_share", p.ownershipShare)
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
    put("province", p.province)
    put("city", p.city)
    put("county", p.county)
    put("district", p.district)
    put("neighborhood", p.neighborhood)
    put("address", p.address)
    put("postal_code", p.postalCode)
    put("area", p.area)
    put("build_year", p.buildYear)
    put("total_floors", p.totalFloors)
    put("units_per_floor", p.unitsPerFloor)
    put("floor", p.floor)
    put("unit", p.unit)
    put("bedrooms", p.bedrooms)
    put("building_direction", p.buildingDirection)
    put("unit_direction", p.unitDirection)
    put("facade_type", p.facadeType)
    put("floor_type", p.floorType)
    put("kitchen_type", p.kitchenType)
    put("bathroom_type", p.bathroomType)
    put("heating_system", p.heatingSystem)
    put("cooling_system", p.coolingSystem)
    put("has_parking", p.hasParking)
    put("parking_number", p.parkingNumber)
    put("has_storage", p.hasStorage)
    put("storage_number", p.storageNumber)
    put("has_elevator", p.hasElevator)
    put("deed_type", p.deedType)
    put("deed_number", p.deedNumber)
    put("main_plaque", p.mainPlaque)
    put("sub_plaque", p.subPlaque)
    put("registration_section", p.registrationSection)
    put(
        "utilities",
        buildJsonObject {
            putUtility(this, "water", p.water)
            putUtility(this, "electricity", p.electricity)
            putUtility(this, "gas", p.gas)
            putUtility(this, "phone", p.phone)
            putUtility(this, "sewage", p.sewage)
        },
    )
    if (p.commonAreas.isNotEmpty()) {
        put("common_areas", buildJsonArray { p.commonAreas.forEach { add(JsonPrimitive(it)) } })
    }
    put("has_mortgage", p.hasMortgage)
    put("has_tenant", p.hasTenant)
    put("has_loan", p.hasLoan)
    put("has_debt", p.hasDebt)
    put("has_completion_cert", p.hasCompletionCert)
    put("has_separation_minutes", p.hasSeparationMinutes)
    put("declared_by_parties", p.declaredByParties)
}

private fun putCosts(build: kotlinx.serialization.json.JsonObjectBuilder, costs: Map<String, String>) {
    if (costs.isEmpty()) return
    build.put(
        "costs",
        buildJsonObject {
            costs.forEach { (k, v) -> if (v.isNotBlank()) put(k, v) }
        },
    )
}

private fun putPenalties(build: kotlinx.serialization.json.JsonObjectBuilder, penalties: Map<String, WizardPenaltyDraft>) {
    if (penalties.isEmpty()) return
    build.put(
        "penalties",
        buildJsonObject {
            penalties.forEach { (k, p) ->
                if (p.enabled || p.amountPerDay.isNotBlank()) {
                    put(
                        k,
                        buildJsonObject {
                            put("enabled", p.enabled)
                            put("amount_per_day", p.amountPerDay)
                            put("unit", p.unit.ifBlank { "daily" })
                        },
                    )
                }
            }
        },
    )
}

fun leaseFinancialToJson(f: WizardLeaseFinancialDraft): JsonElement = buildJsonObject {
    put("lease_start", f.leaseStart)
    put("lease_end", f.leaseEnd)
    put("deposit", f.deposit)
    put("monthly_rent", f.monthlyRent)
    put("rent_payment_day", f.rentPaymentDay)
    put("payment_method", f.paymentMethod)
    put("payment_destination", f.paymentDestination)
    put("payment_card", f.paymentCard)
    put("payment_account", f.paymentAccount)
    put("payment_iban", f.paymentIban)
    put("delivery_date", f.deliveryDate)
    put("delivery_delay_penalty", f.deliveryDelayPenalty)
    put("vacate_date", f.vacateDate)
    put("deposit_mode", f.depositMode)
    put("deposit_payment_date", f.depositPaymentDate)
    put("deposit_payment_method", f.depositPaymentMethod)
    put("previous_deposit_amount", f.previousDepositAmount)
    put("previous_contract_ref", f.previousContractRef)
    put("previous_contract_date", f.previousContractDate)
    put("usage_type", f.usageType)
    put("occupants_count", f.occupantsCount)
    put("transfer_to_third_party_allowed", f.transferToThirdPartyAllowed)
    putCosts(this, f.costs)
    putPenalties(this, f.penalties)
}

fun saleFinancialToJson(f: WizardSaleFinancialDraft): JsonElement = buildJsonObject {
    put("total_price", f.totalPrice)
    put("down_payment", f.downPayment)
    put("earnest_money", f.earnestMoney)
    put("delivery_date", f.deliveryDate)
    put("transfer_date", f.transferDate)
    put("transfer_place", f.transferPlace)
    put("notary_office_number", f.notaryOfficeNumber)
    put("remaining_amount_at_transfer", f.remainingAmountAtTransfer)
    put("official_transfer_date", f.officialTransferDate)
    if (f.payments.isNotEmpty()) {
        put(
            "payments",
            buildJsonArray {
                f.payments.forEach { p ->
                    add(
                        buildJsonObject {
                            put("label", p.label.ifBlank { "قسط" })
                            put("amount", p.amount)
                            put("due_date", p.dueDate)
                            put("payment_method", p.paymentMethod)
                            put("notes", p.notes)
                        },
                    )
                }
            },
        )
    }
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
    if (f.milestones.isNotEmpty()) {
        put(
            "milestones",
            buildJsonArray {
                f.milestones.forEach { m ->
                    add(
                        buildJsonObject {
                            put("title", m.title)
                            put("due_date", m.dueDate)
                            put("notes", m.notes)
                        },
                    )
                }
            },
        )
    }
    if (f.unitAllocations.isNotEmpty()) {
        put(
            "unit_allocations",
            buildJsonArray {
                f.unitAllocations.forEach { u ->
                    add(
                        buildJsonObject {
                            put("floor", u.floor)
                            put("unit", u.unit)
                            put("owner_type", u.ownerType)
                            put("notes", u.notes)
                        },
                    )
                }
            },
        )
    }
    if (f.guarantees.isNotEmpty()) {
        put(
            "guarantees",
            buildJsonArray {
                f.guarantees.forEach { g ->
                    add(
                        buildJsonObject {
                            put("type", g.type)
                            put("amount", g.amount)
                            put("notes", g.notes)
                        },
                    )
                }
            },
        )
    }
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
    val agreements = c.customAgreements.filter { it.body.isNotBlank() }.ifEmpty {
        if (c.customBody.isNotBlank()) {
            listOf(WizardCustomAgreementDraft(c.customTitle.ifBlank { "توافق اختصاصی" }, c.customBody))
        } else {
            emptyList()
        }
    }
    if (agreements.isNotEmpty()) {
        put(
            "custom_agreements",
            buildJsonArray {
                agreements.forEach { a ->
                    add(
                        buildJsonObject {
                            put("title", a.title.ifBlank { "توافق اختصاصی" })
                            put("body", a.body)
                        },
                    )
                }
            },
        )
    }
    when (contractType) {
        "lease" -> {
            put("transfer_choice", c.transferChoice)
            put("usage_type", c.usageType)
            if (c.occupantsCount.isNotBlank()) put("occupants_count", c.occupantsCount)
            putCosts(this, c.leaseCosts)
            putPenalties(this, c.leasePenalties)
        }
        "sale" -> {
            put("property_has_tenant", c.propertyHasTenant)
            putPenalties(this, c.salePenalties)
            if (c.saleCosts.isNotEmpty()) {
                put(
                    "sale_costs",
                    buildJsonObject {
                        c.saleCosts.forEach { (k, v) -> if (v.isNotBlank()) put(k, v) }
                    },
                )
            }
        }
    }
}

fun canRemoveWizardParty(
    parties: List<WizardPartyDraft>,
    index: Int,
    contractType: String,
): Boolean {
    if (index !in parties.indices) return false
    val remaining = parties.filterIndexed { i, _ -> i != index }
    if (remaining.isEmpty()) return false
    return defaultRolesForType(contractType).all { role ->
        remaining.any { it.role == role }
    }
}

fun defaultRolesForType(contractType: String): List<String> = when (contractType) {
    "lease" -> listOf("landlord", "tenant")
    "jv" -> listOf("owner", "builder")
    else -> listOf("seller", "buyer")
}

fun roleLabelFa(role: String): String = when (role) {
    "landlord" -> "موجر"
    "tenant" -> "مستأجر"
    "seller" -> "فروشنده"
    "buyer" -> "خریدار"
    "owner" -> "مالک"
    "builder" -> "سازنده"
    else -> role
}
