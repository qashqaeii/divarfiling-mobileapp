package ir.divarfiling.mobile.feature.contracts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfSecondaryButton
import ir.divarfiling.mobile.core.design.components.DfTextField
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Composable
fun WizardSectionTitle(title: String) {
    Text(
        title,
        style = AppTypography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = DfThemeColors.primary(),
        modifier = Modifier.padding(top = AppSpacing.xs),
    )
}

@Composable
fun WizardField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    minLines: Int = 1,
) {
    DfTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        singleLine = singleLine,
        minLines = minLines,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
fun WizardToggle(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, enabled: Boolean = true) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = AppTypography.labelSmall)
        Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
    }
}

@Composable
fun WizardCostRow(
    label: String,
    value: String,
    options: List<WizardChoiceOption>,
    onChange: (String) -> Unit,
    enabled: Boolean,
) {
    WizardChoiceChips(label, options, value, onChange, enabled)
}

private val LEASE_COST_OPTIONS = listOf(
    WizardChoiceOption("landlord", "موجر"),
    WizardChoiceOption("tenant", "مستأجر"),
)
private val SALE_COST_OPTIONS = listOf(
    WizardChoiceOption("seller", "فروشنده"),
    WizardChoiceOption("buyer", "خریدار"),
    WizardChoiceOption("shared", "مشترک"),
)

@Composable
fun WizardPartiesStep(
    parties: List<WizardPartyDraft>,
    contractType: String,
    canEdit: Boolean,
    pickerEnabled: Boolean,
    choices: JsonObject,
    onPartyChange: (Int, WizardPartyDraft) -> Unit,
    onPickContact: (Int) -> Unit,
    onAddParty: (String) -> Unit,
    onRemoveParty: (Int) -> Unit,
    onClearPartyCustomer: (Int) -> Unit,
) {
    val roleOptions = choices.labeledOptions("party_roles")
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        parties.forEachIndexed { index, party ->
            Surface(shape = AppShapes.Card, color = DfThemeColors.surface()) {
                Column(
                    Modifier.padding(AppSpacing.sm),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(roleLabelFa(party.role), fontWeight = FontWeight.Bold, color = DfThemeColors.primary())
                        if (canEdit && canRemoveWizardParty(parties, index, contractType)) {
                            TextButton(onClick = { onRemoveParty(index) }) {
                                Text("حذف", color = DfThemeColors.warning())
                            }
                        }
                    }
                    if (roleOptions.size > 1 && canEdit) {
                        WizardChoiceChips(
                            label = "نقش",
                            options = roleOptions,
                            selected = party.role,
                            onSelect = { onPartyChange(index, party.copy(role = it)) },
                            enabled = canEdit,
                        )
                    }
                    if (party.customerId.isNotBlank()) {
                        Text("متصل به CRM", style = AppTypography.labelSmall, color = DfThemeColors.textMuted())
                        if (canEdit) {
                            TextButton(onClick = { onClearPartyCustomer(index) }) { Text("حذف اتصال CRM") }
                        }
                    }
                    if (pickerEnabled && canEdit) {
                        DfSecondaryButton(
                            text = if (party.customerId.isBlank()) "انتخاب از CRM" else "تغییر مخاطب CRM",
                            onClick = { onPickContact(index) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    WizardChoiceChips(
                        label = "نوع شخص",
                        options = choices.labeledOptions("person_types").ifEmpty { PERSON_TYPE_OPTIONS },
                        selected = party.personType.ifBlank { "natural" },
                        onSelect = { onPartyChange(index, party.copy(personType = it)) },
                        enabled = canEdit,
                    )
                    WizardField("نام", party.firstName, { onPartyChange(index, party.copy(firstName = it)) }, enabled = canEdit)
                    WizardField("نام خانوادگی", party.lastName, { onPartyChange(index, party.copy(lastName = it)) }, enabled = canEdit)
                    WizardField("نام پدر", party.fatherName, { onPartyChange(index, party.copy(fatherName = it)) }, enabled = canEdit)
                    WizardField("کد ملی", party.nationalId, { onPartyChange(index, party.copy(nationalId = it)) }, enabled = canEdit)
                    WizardField("موبایل", party.mobile, { onPartyChange(index, party.copy(mobile = it)) }, enabled = canEdit)
                    WizardField("آدرس", party.address, { onPartyChange(index, party.copy(address = it)) }, enabled = canEdit)
                    WizardField("کد پستی", party.postalCode, { onPartyChange(index, party.copy(postalCode = it)) }, enabled = canEdit)
                    WizardField("سهم مالکیت", party.ownershipShare, { onPartyChange(index, party.copy(ownershipShare = it)) }, enabled = canEdit)
                    if (party.personType == "legal") {
                        WizardSectionTitle("اطلاعات حقوقی")
                        WizardField("نام شرکت", party.companyName, { onPartyChange(index, party.copy(companyName = it)) }, enabled = canEdit)
                        WizardField("شناسه ملی شرکت", party.companyNationalId, { onPartyChange(index, party.copy(companyNationalId = it)) }, enabled = canEdit)
                        WizardField("نام نماینده", party.representativeName, { onPartyChange(index, party.copy(representativeName = it)) }, enabled = canEdit)
                        WizardField("کد ملی نماینده", party.representativeNationalId, { onPartyChange(index, party.copy(representativeNationalId = it)) }, enabled = canEdit)
                        WizardField("موبایل نماینده", party.representativeMobile, { onPartyChange(index, party.copy(representativeMobile = it)) }, enabled = canEdit)
                    }
                }
            }
        }
        if (canEdit) {
            WizardSectionTitle("افزودن طرف")
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            ) {
                when (contractType) {
                    "lease" -> listOf("landlord" to "موجر", "tenant" to "مستأجر")
                    "jv" -> listOf("owner" to "مالک", "builder" to "سازنده")
                    else -> listOf("seller" to "فروشنده", "buyer" to "خریدار")
                }.forEach { (role, label) ->
                    DfSecondaryButton(
                        text = "+ $label",
                        onClick = { onAddParty(role) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
fun WizardPropertyStep(
    property: WizardPropertyDraft,
    canEdit: Boolean,
    pickerEnabled: Boolean,
    choices: JsonObject,
    onChange: (WizardPropertyDraft) -> Unit,
    onPickProperty: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        if (pickerEnabled && canEdit) {
            DfSecondaryButton(text = "انتخاب فایل CRM", onClick = onPickProperty, modifier = Modifier.fillMaxWidth())
        }
        if (property.crmPropertyTitle.isNotBlank()) {
            Text("فایل: ${property.crmPropertyTitle}", style = AppTypography.labelLarge)
        }
        WizardSectionTitle("موقعیت")
        WizardField("نوع ملک", property.propertyType, { onChange(property.copy(propertyType = it)) }, enabled = canEdit)
        WizardField("کاربری", property.usage, { onChange(property.copy(usage = it)) }, enabled = canEdit)
        WizardField("استان", property.province, { onChange(property.copy(province = it)) }, enabled = canEdit)
        WizardField("شهر", property.city, { onChange(property.copy(city = it)) }, enabled = canEdit)
        WizardField("شهرستان", property.county, { onChange(property.copy(county = it)) }, enabled = canEdit)
        WizardField("منطقه", property.district, { onChange(property.copy(district = it)) }, enabled = canEdit)
        WizardField("محله", property.neighborhood, { onChange(property.copy(neighborhood = it)) }, enabled = canEdit)
        WizardField("آدرس", property.address, { onChange(property.copy(address = it)) }, enabled = canEdit, singleLine = false, minLines = 2)
        WizardField("کد پستی", property.postalCode, { onChange(property.copy(postalCode = it)) }, enabled = canEdit)
        WizardSectionTitle("مشخصات")
        WizardField("متراژ", property.area, { onChange(property.copy(area = it)) }, enabled = canEdit)
        WizardField("سال ساخت", property.buildYear, { onChange(property.copy(buildYear = it)) }, enabled = canEdit)
        WizardField("طبقات کل", property.totalFloors, { onChange(property.copy(totalFloors = it)) }, enabled = canEdit)
        WizardField("واحد در طبقه", property.unitsPerFloor, { onChange(property.copy(unitsPerFloor = it)) }, enabled = canEdit)
        WizardField("طبقه", property.floor, { onChange(property.copy(floor = it)) }, enabled = canEdit)
        WizardField("واحد", property.unit, { onChange(property.copy(unit = it)) }, enabled = canEdit)
        WizardField("اتاق", property.bedrooms, { onChange(property.copy(bedrooms = it)) }, enabled = canEdit)
        WizardStringChoiceChips(
            "جهت ساختمان",
            choices.propertyChoices("building_direction"),
            property.buildingDirection,
            { onChange(property.copy(buildingDirection = it)) },
            canEdit,
        )
        WizardStringChoiceChips(
            "جهت واحد",
            choices.propertyChoices("unit_direction"),
            property.unitDirection,
            { onChange(property.copy(unitDirection = it)) },
            canEdit,
        )
        WizardStringChoiceChips(
            "نما",
            choices.propertyChoices("facade_type"),
            property.facadeType,
            { onChange(property.copy(facadeType = it)) },
            canEdit,
        )
        WizardStringChoiceChips(
            "کف‌پوش",
            choices.propertyChoices("floor_type"),
            property.floorType,
            { onChange(property.copy(floorType = it)) },
            canEdit,
        )
        WizardStringChoiceChips(
            "آشپزخانه",
            choices.propertyChoices("kitchen_type"),
            property.kitchenType,
            { onChange(property.copy(kitchenType = it)) },
            canEdit,
        )
        WizardStringChoiceChips(
            "سرویس",
            choices.propertyChoices("bathroom_type"),
            property.bathroomType,
            { onChange(property.copy(bathroomType = it)) },
            canEdit,
        )
        WizardStringChoiceChips(
            "گرمایش",
            choices.propertyChoices("heating_type"),
            property.heatingSystem,
            { onChange(property.copy(heatingSystem = it)) },
            canEdit,
        )
        WizardStringChoiceChips(
            "سرمایش",
            choices.propertyChoices("cooling_type"),
            property.coolingSystem,
            { onChange(property.copy(coolingSystem = it)) },
            canEdit,
        )
        WizardToggle("پارکینگ", property.hasParking, { onChange(property.copy(hasParking = it)) }, canEdit)
        if (property.hasParking) {
            WizardField(
                "شماره پارکینگ",
                property.parkingNumber,
                onValueChange = { onChange(property.copy(parkingNumber = it)) },
                enabled = canEdit,
            )
        }
        WizardToggle("انباری", property.hasStorage, { onChange(property.copy(hasStorage = it)) }, canEdit)
        if (property.hasStorage) {
            WizardField(
                "شماره انباری",
                property.storageNumber,
                onValueChange = { onChange(property.copy(storageNumber = it)) },
                enabled = canEdit,
            )
        }
        WizardToggle("آسانسور", property.hasElevator, { onChange(property.copy(hasElevator = it)) }, canEdit)
        WizardSectionTitle("سند و وضعیت")
        WizardField("نوع سند", property.deedType, { onChange(property.copy(deedType = it)) }, enabled = canEdit)
        WizardField("شماره سند", property.deedNumber, { onChange(property.copy(deedNumber = it)) }, enabled = canEdit)
        WizardField("پلاک اصلی", property.mainPlaque, { onChange(property.copy(mainPlaque = it)) }, enabled = canEdit)
        WizardField("پلاک فرعی", property.subPlaque, { onChange(property.copy(subPlaque = it)) }, enabled = canEdit)
        WizardField("بخش ثبتی", property.registrationSection, { onChange(property.copy(registrationSection = it)) }, enabled = canEdit)
        WizardToggle("رهن", property.hasMortgage, { onChange(property.copy(hasMortgage = it)) }, canEdit)
        WizardToggle("مستأجر", property.hasTenant, { onChange(property.copy(hasTenant = it)) }, canEdit)
        WizardToggle("وام", property.hasLoan, { onChange(property.copy(hasLoan = it)) }, canEdit)
        WizardToggle("بدهی", property.hasDebt, { onChange(property.copy(hasDebt = it)) }, canEdit)
        WizardToggle("پایان کار", property.hasCompletionCert, { onChange(property.copy(hasCompletionCert = it)) }, canEdit)
        WizardToggle("صورت‌مجلس تفکیکی", property.hasSeparationMinutes, { onChange(property.copy(hasSeparationMinutes = it)) }, canEdit)
        WizardSectionTitle("انشعابات")
        listOf(
            Triple("آب", property.water) { u: WizardUtilityDraft -> onChange(property.copy(water = u)) },
            Triple("برق", property.electricity) { u: WizardUtilityDraft -> onChange(property.copy(electricity = u)) },
            Triple("گاز", property.gas) { u: WizardUtilityDraft -> onChange(property.copy(gas = u)) },
            Triple("تلفن", property.phone) { u: WizardUtilityDraft -> onChange(property.copy(phone = u)) },
            Triple("فاضلاب", property.sewage) { u: WizardUtilityDraft -> onChange(property.copy(sewage = u)) },
        ).forEach { (title, util, setter) ->
            Text(title, fontWeight = FontWeight.SemiBold, style = AppTypography.labelSmall)
            WizardField("نوع انشعاب", util.connectionType, { setter(util.copy(connectionType = it)) }, enabled = canEdit)
            WizardField("شماره اشتراک", util.subscriptionNo, { setter(util.copy(subscriptionNo = it)) }, enabled = canEdit)
            WizardField("شناسه قبض", util.billId, { setter(util.copy(billId = it)) }, enabled = canEdit)
        }
        WizardSectionTitle("فضاهای مشترک")
        WIZARD_COMMON_AREAS.forEach { area ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(area, style = AppTypography.labelSmall)
                Checkbox(
                    checked = area in property.commonAreas,
                    onCheckedChange = { checked ->
                        val next = property.commonAreas.toMutableSet()
                        if (checked) next.add(area) else next.remove(area)
                        onChange(property.copy(commonAreas = next.toList()))
                    },
                    enabled = canEdit,
                )
            }
        }
    }
}

@Composable
fun WizardFinancialStep(
    contractType: String,
    lease: WizardLeaseFinancialDraft,
    sale: WizardSaleFinancialDraft,
    jv: WizardJvFinancialDraft,
    canEdit: Boolean,
    choices: JsonObject,
    onLeaseChange: (WizardLeaseFinancialDraft) -> Unit,
    onSaleChange: (WizardSaleFinancialDraft) -> Unit,
    onJvChange: (WizardJvFinancialDraft) -> Unit,
) {
    when (contractType) {
        "lease" -> WizardLeaseFinancialForm(lease, canEdit, onLeaseChange)
        "jv" -> WizardJvFinancialForm(jv, canEdit, onJvChange)
        else -> WizardSaleFinancialForm(sale, canEdit, onSaleChange)
    }
}

@Composable
private fun WizardLeaseFinancialForm(
    lease: WizardLeaseFinancialDraft,
    canEdit: Boolean,
    onChange: (WizardLeaseFinancialDraft) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
        WizardSectionTitle("مدت و مبالغ")
        WizardJalaliField("شروع اجاره", lease.leaseStart, { onChange(lease.copy(leaseStart = it)) }, canEdit)
        WizardJalaliField("پایان اجاره", lease.leaseEnd, { onChange(lease.copy(leaseEnd = it)) }, canEdit)
        WizardMoneyField("ودیعه", lease.deposit, { onChange(lease.copy(deposit = it)) }, canEdit)
        WizardMoneyField("اجاره ماهانه", lease.monthlyRent, { onChange(lease.copy(monthlyRent = it)) }, canEdit)
        WizardField("روز پرداخت (۱–۳۱)", lease.rentPaymentDay, { onChange(lease.copy(rentPaymentDay = it)) }, enabled = canEdit)
        WizardChoiceChips(
            "روش پرداخت",
            PAYMENT_METHOD_OPTIONS,
            lease.paymentMethod.ifBlank { "transfer" },
            { onChange(lease.copy(paymentMethod = it)) },
            canEdit,
        )
        WizardSectionTitle("حساب واریز")
        WizardField("مقصد پرداخت", lease.paymentDestination, { onChange(lease.copy(paymentDestination = it)) }, enabled = canEdit)
        WizardField("شماره کارت", lease.paymentCard, { onChange(lease.copy(paymentCard = it)) }, enabled = canEdit)
        WizardField("شماره حساب", lease.paymentAccount, { onChange(lease.copy(paymentAccount = it)) }, enabled = canEdit)
        WizardField("شبا", lease.paymentIban, { onChange(lease.copy(paymentIban = it)) }, enabled = canEdit)
        WizardSectionTitle("تحویل و تخلیه")
        WizardJalaliField("تاریخ تحویل", lease.deliveryDate, { onChange(lease.copy(deliveryDate = it)) }, canEdit)
        WizardMoneyField("جریمه تأخیر تحویل", lease.deliveryDelayPenalty, { onChange(lease.copy(deliveryDelayPenalty = it)) }, canEdit)
        WizardJalaliField("تاریخ تخلیه", lease.vacateDate, { onChange(lease.copy(vacateDate = it)) }, canEdit)
        WizardSectionTitle("ودیعه قبلی")
        WizardChoiceChips(
            "حالت ودیعه",
            DEPOSIT_MODE_OPTIONS,
            lease.depositMode.ifBlank { "new" },
            { onChange(lease.copy(depositMode = it)) },
            canEdit,
        )
        WizardJalaliField("تاریخ پرداخت ودیعه", lease.depositPaymentDate, { onChange(lease.copy(depositPaymentDate = it)) }, canEdit)
        WizardChoiceChips(
            "روش پرداخت ودیعه",
            PAYMENT_METHOD_OPTIONS,
            lease.depositPaymentMethod,
            { onChange(lease.copy(depositPaymentMethod = it)) },
            canEdit,
        )
        WizardMoneyField("مبلغ ودیعه قبلی", lease.previousDepositAmount, { onChange(lease.copy(previousDepositAmount = it)) }, canEdit)
        WizardField("مرجع قرارداد قبلی", lease.previousContractRef, { onChange(lease.copy(previousContractRef = it)) }, enabled = canEdit)
        WizardJalaliField("تاریخ قرارداد قبلی", lease.previousContractDate, { onChange(lease.copy(previousContractDate = it)) }, canEdit)
        WizardChoiceChips(
            "نوع استفاده",
            USAGE_TYPE_OPTIONS,
            lease.usageType.ifBlank { "residential" },
            { onChange(lease.copy(usageType = it)) },
            canEdit,
        )
        WizardField("تعداد ساکنین", lease.occupantsCount, { onChange(lease.copy(occupantsCount = it)) }, enabled = canEdit)
        WizardToggle("انتقال به غیر مجاز", lease.transferToThirdPartyAllowed, { onChange(lease.copy(transferToThirdPartyAllowed = it)) }, canEdit)
        WizardSectionTitle("مسئولیت هزینه‌ها")
        LEASE_COST_KEYS.forEach { key ->
            WizardCostRow(
                LEASE_COST_LABELS[key] ?: key,
                lease.costs[key].orEmpty(),
                LEASE_COST_OPTIONS,
                { onChange(lease.copy(costs = lease.costs + (key to it))) },
                canEdit,
            )
        }
        WizardSectionTitle("جریمه‌ها")
        LEASE_PENALTY_KEYS.forEach { key ->
            val pen = lease.penalties[key] ?: WizardPenaltyDraft()
            WizardPenaltyBlock(LEASE_PENALTY_LABELS[key] ?: key, pen, {
                onChange(lease.copy(penalties = lease.penalties + (key to it)))
            }, canEdit)
        }
    }
}

@Composable
private fun WizardSaleFinancialForm(
    sale: WizardSaleFinancialDraft,
    canEdit: Boolean,
    onChange: (WizardSaleFinancialDraft) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
        WizardSectionTitle("مبالغ")
        WizardMoneyField("مبلغ کل", sale.totalPrice, { onChange(sale.copy(totalPrice = it)) }, canEdit)
        WizardMoneyField("پیش‌پرداخت", sale.downPayment, { onChange(sale.copy(downPayment = it)) }, canEdit)
        WizardMoneyField("بیعانه", sale.earnestMoney, { onChange(sale.copy(earnestMoney = it)) }, canEdit)
        WizardMoneyField("مانده در انتقال", sale.remainingAmountAtTransfer, { onChange(sale.copy(remainingAmountAtTransfer = it)) }, canEdit)
        WizardSectionTitle("انتقال سند")
        WizardJalaliField("تاریخ تحویل", sale.deliveryDate, { onChange(sale.copy(deliveryDate = it)) }, canEdit)
        WizardJalaliField("تاریخ انتقال", sale.transferDate, { onChange(sale.copy(transferDate = it)) }, canEdit)
        WizardJalaliField("انتقال رسمی", sale.officialTransferDate, { onChange(sale.copy(officialTransferDate = it)) }, canEdit)
        WizardField("محل انتقال", sale.transferPlace, { onChange(sale.copy(transferPlace = it)) }, enabled = canEdit)
        WizardField("شماره دفترخانه", sale.notaryOfficeNumber, { onChange(sale.copy(notaryOfficeNumber = it)) }, enabled = canEdit)
        WizardSectionTitle("جدول پرداخت")
        sale.payments.forEachIndexed { index, payment ->
            Surface(shape = AppShapes.CardSmall, color = DfThemeColors.surfaceVariant().copy(alpha = 0.35f)) {
                Column(Modifier.padding(AppSpacing.sm), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("قسط ${index + 1}", fontWeight = FontWeight.SemiBold)
                    WizardField("عنوان", payment.label, {
                        val next = sale.payments.toMutableList()
                        next[index] = payment.copy(label = it)
                        onChange(sale.copy(payments = next))
                    }, enabled = canEdit)
                    WizardMoneyField("مبلغ", payment.amount, {
                        val next = sale.payments.toMutableList()
                        next[index] = payment.copy(amount = it)
                        onChange(sale.copy(payments = next))
                    }, canEdit)
                    WizardJalaliField("سررسید", payment.dueDate, {
                        val next = sale.payments.toMutableList()
                        next[index] = payment.copy(dueDate = it)
                        onChange(sale.copy(payments = next))
                    }, canEdit)
                    WizardChoiceChips(
                        "روش",
                        PAYMENT_METHOD_OPTIONS,
                        payment.paymentMethod,
                        {
                            val next = sale.payments.toMutableList()
                            next[index] = payment.copy(paymentMethod = it)
                            onChange(sale.copy(payments = next))
                        },
                        canEdit,
                    )
                    WizardField("یادداشت", payment.notes, {
                        val next = sale.payments.toMutableList()
                        next[index] = payment.copy(notes = it)
                        onChange(sale.copy(payments = next))
                    }, enabled = canEdit, singleLine = false, minLines = 2)
                    if (canEdit) {
                        TextButton(onClick = {
                            onChange(sale.copy(payments = sale.payments.filterIndexed { i, _ -> i != index }))
                        }) { Text("حذف قسط") }
                    }
                }
            }
        }
        if (canEdit) {
            DfSecondaryButton(
                text = "افزودن قسط",
                onClick = { onChange(sale.copy(payments = sale.payments + WizardPaymentDraft(label = "قسط ${sale.payments.size + 1}"))) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun WizardJvFinancialForm(
    jv: WizardJvFinancialDraft,
    canEdit: Boolean,
    onChange: (WizardJvFinancialDraft) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
        WizardField("سهم مالک (%)", jv.ownerPercent, { onChange(jv.copy(ownerPercent = it)) }, enabled = canEdit)
        WizardField("سهم سازنده (%)", jv.builderPercent, { onChange(jv.copy(builderPercent = it)) }, enabled = canEdit)
        WizardJalaliField("شروع پروژه", jv.startDate, { onChange(jv.copy(startDate = it)) }, canEdit)
        WizardField("مدت تخمینی", jv.durationEstimate, { onChange(jv.copy(durationEstimate = it)) }, enabled = canEdit)
        WizardJalaliField("تحویل", jv.deliveryDate, { onChange(jv.copy(deliveryDate = it)) }, canEdit)
        WizardField("آورده مالک", jv.ownerContribution, { onChange(jv.copy(ownerContribution = it)) }, enabled = canEdit, singleLine = false, minLines = 2)
        WizardField("آورده سازنده", jv.builderContribution, { onChange(jv.copy(builderContribution = it)) }, enabled = canEdit, singleLine = false, minLines = 2)
        WizardSectionTitle("مراحل ساخت")
        jv.milestones.forEachIndexed { index, m ->
            WizardField("عنوان مرحله", m.title, {
                val list = jv.milestones.toMutableList()
                list[index] = m.copy(title = it)
                onChange(jv.copy(milestones = list))
            }, enabled = canEdit)
            WizardJalaliField("موعد", m.dueDate, {
                val list = jv.milestones.toMutableList()
                list[index] = m.copy(dueDate = it)
                onChange(jv.copy(milestones = list))
            }, canEdit)
        }
        if (canEdit) {
            DfSecondaryButton(
                text = "افزودن مرحله",
                onClick = { onChange(jv.copy(milestones = jv.milestones + WizardJvMilestoneDraft())) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        WizardSectionTitle("تخصیص واحد")
        jv.unitAllocations.forEachIndexed { index, u ->
            WizardField("طبقه", u.floor, {
                val list = jv.unitAllocations.toMutableList()
                list[index] = u.copy(floor = it)
                onChange(jv.copy(unitAllocations = list))
            }, enabled = canEdit)
            WizardField("واحد", u.unit, {
                val list = jv.unitAllocations.toMutableList()
                list[index] = u.copy(unit = it)
                onChange(jv.copy(unitAllocations = list))
            }, enabled = canEdit)
        }
        if (canEdit) {
            DfSecondaryButton(
                text = "افزودن واحد",
                onClick = { onChange(jv.copy(unitAllocations = jv.unitAllocations + WizardJvUnitDraft())) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        WizardSectionTitle("تضمین‌ها")
        jv.guarantees.forEachIndexed { index, g ->
            WizardField("نوع", g.type, {
                val list = jv.guarantees.toMutableList()
                list[index] = g.copy(type = it)
                onChange(jv.copy(guarantees = list))
            }, enabled = canEdit)
            WizardMoneyField("مبلغ", g.amount, {
                val list = jv.guarantees.toMutableList()
                list[index] = g.copy(amount = it)
                onChange(jv.copy(guarantees = list))
            }, canEdit)
        }
        if (canEdit) {
            DfSecondaryButton(
                text = "افزودن تضمین",
                onClick = { onChange(jv.copy(guarantees = jv.guarantees + WizardJvGuaranteeDraft())) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
fun WizardWitnessesStep(
    witnesses: List<WizardWitnessDraft>,
    canEdit: Boolean,
    onChange: (Int, WizardWitnessDraft) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        witnesses.forEachIndexed { index, w ->
            Surface(shape = AppShapes.Card, color = DfThemeColors.surface()) {
                Column(Modifier.padding(AppSpacing.sm), verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                    Text(w.witnessRole, fontWeight = FontWeight.SemiBold)
                    WizardField("نام", w.firstName, { onChange(index, w.copy(firstName = it)) }, enabled = canEdit)
                    WizardField("نام خانوادگی", w.lastName, { onChange(index, w.copy(lastName = it)) }, enabled = canEdit)
                    WizardField("کد ملی", w.nationalId, { onChange(index, w.copy(nationalId = it)) }, enabled = canEdit)
                    WizardField("موبایل", w.mobile, { onChange(index, w.copy(mobile = it)) }, enabled = canEdit)
                }
            }
        }
    }
}

@Composable
fun WizardClausesStep(
    clauses: WizardClausesDraft,
    contractType: String,
    options: List<Pair<String, String>>,
    clauseSections: JsonArray?,
    clauseWarnings: List<String>,
    canEdit: Boolean,
    onChange: (WizardClausesDraft) -> Unit,
    onToggleOptional: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        Text(
            "تصمیم‌های قراردادی را انتخاب کنید؛ متن حقوقی هر بند را با «مشاهده متن قرارداد» ببینید.",
            style = AppTypography.bodyDescription,
            color = DfThemeColors.textSecondary(),
        )
        clauseWarnings.forEach { w ->
            Text("⚠ $w", style = AppTypography.labelSmall, color = DfThemeColors.warning())
        }
        WizardClauseSections(
            contractType = contractType,
            clauses = clauses,
            clauseSections = clauseSections,
            canEdit = canEdit,
            onChange = onChange,
            onToggleOptional = onToggleOptional,
        )
        if (clauseSections.isNullOrEmpty() && options.isNotEmpty()) {
            WizardSectionTitle("بندهای اختیاری")
            options.forEach { (code, title) ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Checkbox(
                        checked = code in clauses.selectedOptionalCodes,
                        onCheckedChange = { if (canEdit) onToggleOptional(code) },
                        enabled = canEdit,
                    )
                    Text(title, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun WizardReviewStep(
    parties: List<WizardPartyDraft>,
    property: WizardPropertyDraft,
    contractType: String,
    validationErrors: List<String>,
    statusLabel: String,
    onEditStep: (String) -> Unit,
    onPrepareSend: (() -> Unit)?,
    prepareSendEnabled: Boolean,
    prepareSendLoading: Boolean,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        Text("مرور نهایی", style = AppTypography.sectionTitle, fontWeight = FontWeight.Bold)
        Text("وضعیت: $statusLabel", color = DfThemeColors.textSecondary())
        ReviewSection("طرفین", parties.joinToString(" · ") { "${roleLabelFa(it.role)}: ${it.firstName} ${it.lastName}".trim() }) {
            onEditStep("parties")
        }
        ReviewSection(
            "ملک",
            property.address.ifBlank { property.city }.ifBlank { "—" },
        ) { onEditStep("property") }
        ReviewSection("مالی", contractType) { onEditStep("financial") }
        ReviewSection("بندها", "شرایط و توافقات") { onEditStep("clauses") }
        if (validationErrors.isEmpty()) {
            Text("اعتبارسنجی اولیه بدون خطا.", style = AppTypography.bodyDescription)
        } else {
            validationErrors.forEach { Text("• $it", color = DfThemeColors.warning()) }
        }
        if (onPrepareSend != null && prepareSendEnabled) {
            DfSecondaryButton(
                text = if (prepareSendLoading) "در حال ارسال…" else "آماده‌سازی و ارسال برای طرفین",
                onClick = onPrepareSend,
                enabled = !prepareSendLoading && validationErrors.isEmpty(),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun ReviewSection(title: String, summary: String, onEdit: () -> Unit) {
    Surface(shape = AppShapes.Card, color = DfThemeColors.surface()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(AppSpacing.sm),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, style = AppTypography.labelLarge)
                Text(summary, style = AppTypography.labelSmall, color = DfThemeColors.textSecondary())
            }
            TextButton(onClick = onEdit) { Text("ویرایش") }
        }
    }
}

fun clauseSectionsFromUi(clauseUi: JsonObject?): JsonArray? =
    clauseUi?.get("clause_sections")?.jsonArray
