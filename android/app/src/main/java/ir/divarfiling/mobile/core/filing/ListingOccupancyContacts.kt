package ir.divarfiling.mobile.core.filing

import ir.divarfiling.mobile.core.network.ListingLinkedContactDto
import ir.divarfiling.mobile.core.util.PhoneNormalizer

data class ListingOccupancyPerson(
    val role: String,
    val name: String,
    val phone: String,
    val customerId: Long? = null,
    val linkId: Long? = null,
)

data class ListingContactsPresentation(
    val occupancy: List<ListingOccupancyPerson>,
    val others: List<ListingLinkedContactDto>,
)

object ListingOccupancyContacts {
    const val ROLE_OWNER = "مالک"
    const val ROLE_TENANT = "مستاجر"

    fun build(
        ownerName: String,
        ownerPhone: String,
        tenantName: String,
        tenantPhone: String,
        isVacant: Boolean,
        contacts: List<ListingLinkedContactDto>,
    ): ListingContactsPresentation {
        val ownerPhoneKey = PhoneNormalizer.normalize(ownerPhone)
        val tenantPhoneKey = PhoneNormalizer.normalize(tenantPhone)
        val occupancy = buildList {
            if (ownerName.isNotBlank() || ownerPhoneKey.isNotBlank()) {
                val match = matchByPhone(contacts, ownerPhoneKey)
                add(
                    ListingOccupancyPerson(
                        role = ROLE_OWNER,
                        name = ownerName.ifBlank { match?.fullName.orEmpty() }.ifBlank { "مالک ملک" },
                        phone = ownerPhone.ifBlank { match?.phone.orEmpty() },
                        customerId = match?.customerId,
                        linkId = match?.id,
                    ),
                )
            }
            if (!isVacant && (tenantName.isNotBlank() || tenantPhoneKey.isNotBlank())) {
                val match = matchByPhone(contacts, tenantPhoneKey)
                add(
                    ListingOccupancyPerson(
                        role = ROLE_TENANT,
                        name = tenantName.ifBlank { match?.fullName.orEmpty() }.ifBlank { "مستاجر ملک" },
                        phone = tenantPhone.ifBlank { match?.phone.orEmpty() },
                        customerId = match?.customerId,
                        linkId = match?.id,
                    ),
                )
            }
        }
        val occupancyPhones = occupancy
            .map { PhoneNormalizer.normalize(it.phone) }
            .filter { it.isNotBlank() }
            .toSet()
        val others = contacts.filter { contact ->
            val phone = PhoneNormalizer.normalize(contact.phone)
            phone.isBlank() || phone !in occupancyPhones
        }.map { contact ->
            contact.copy(role = displayRole(contact.role, contact.phone, ownerPhoneKey, tenantPhoneKey, isVacant))
        }
        return ListingContactsPresentation(occupancy = occupancy, others = others)
    }

    fun displayRole(
        currentRole: String,
        contactPhone: String,
        ownerPhone: String,
        tenantPhone: String,
        isVacant: Boolean,
    ): String {
        val phone = PhoneNormalizer.normalize(contactPhone)
        if (phone.isBlank()) return currentRole
        if (phone == PhoneNormalizer.normalize(ownerPhone)) return ROLE_OWNER
        if (!isVacant && phone == PhoneNormalizer.normalize(tenantPhone)) return ROLE_TENANT
        return currentRole
    }

    private fun matchByPhone(
        contacts: List<ListingLinkedContactDto>,
        phoneKey: String,
    ): ListingLinkedContactDto? {
        if (phoneKey.isBlank()) return null
        return contacts.firstOrNull { PhoneNormalizer.normalize(it.phone) == phoneKey }
    }
}
