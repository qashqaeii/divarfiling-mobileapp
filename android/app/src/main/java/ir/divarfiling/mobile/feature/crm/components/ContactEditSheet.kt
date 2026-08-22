package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfDropdown
import ir.divarfiling.mobile.core.design.components.DfFilterChipRow
import ir.divarfiling.mobile.core.design.components.DfFilterOption
import ir.divarfiling.mobile.core.design.components.DfMoneyField
import ir.divarfiling.mobile.core.design.components.DfSheetActions
import ir.divarfiling.mobile.core.design.components.DfSheetScaffold
import ir.divarfiling.mobile.core.design.components.DfSheetSection
import ir.divarfiling.mobile.core.util.PhoneNormalizer
import ir.divarfiling.mobile.feature.crm.CrmConstants
import ir.divarfiling.mobile.feature.crm.CrmTypeProfiles
import ir.divarfiling.mobile.feature.crm.TypeProfile

private enum class ContactEditTab(val label: String) {
    Identity("هویت"),
    Money("مالی"),
    Property("ملک"),
    Notes("یادداشت"),
}

data class ContactEditMoneyState(
    val budgetMin: String = "",
    val budgetMax: String = "",
    val depositMin: String = "",
    val depositMax: String = "",
    val rentMin: String = "",
    val rentMax: String = "",
)

data class ContactEditPrefsState(
    val propertyType: String = "",
    val rooms: String = "",
    val roomsMin: String = "",
    val roomsMax: String = "",
    val minArea: String = "",
    val maxArea: String = "",
    val areas: String = "",
    val city: String = "",
    val district: String = "",
    val yearMin: String = "",
    val yearMax: String = "",
    val floorMin: String = "",
    val floorMax: String = "",
    val wantParking: Boolean = false,
    val wantStorage: Boolean = false,
    val wantElevator: Boolean = false,
)

data class ContactEditBuilderState(
    val buyBudgetMin: String = "",
    val buyBudgetMax: String = "",
    val buyMinArea: String = "",
    val buyMaxArea: String = "",
    val buyAreas: String = "",
    val buyPropertyTypes: String = "ویلا, کلنگی, زمین",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactEditSheet(
    name: String,
    phone: String,
    phoneAlt: String = "",
    email: String = "",
    source: String = "",
    status: String,
    customerType: String,
    priority: String,
    matchingTolerancePercent: Int = 20,
    money: ContactEditMoneyState,
    prefs: ContactEditPrefsState,
    builder: ContactEditBuilderState = ContactEditBuilderState(),
    notes: String,
    isSubmitting: Boolean,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onPhoneAltChange: (String) -> Unit = {},
    onEmailChange: (String) -> Unit = {},
    onSourceChange: (String) -> Unit = {},
    onStatusChange: (String) -> Unit,
    onCustomerTypeChange: (String) -> Unit,
    onPriorityChange: (String) -> Unit,
    onMatchingToleranceChange: (Int) -> Unit = {},
    onBudgetMinChange: (String) -> Unit,
    onBudgetMaxChange: (String) -> Unit,
    onDepositMinChange: (String) -> Unit,
    onDepositMaxChange: (String) -> Unit,
    onRentMinChange: (String) -> Unit,
    onRentMaxChange: (String) -> Unit,
    onPropertyTypeChange: (String) -> Unit,
    onRoomsChange: (String) -> Unit,
    onRoomsMinChange: (String) -> Unit = {},
    onRoomsMaxChange: (String) -> Unit = {},
    onMinAreaChange: (String) -> Unit,
    onMaxAreaChange: (String) -> Unit,
    onAreasChange: (String) -> Unit,
    onCityChange: (String) -> Unit = {},
    onDistrictChange: (String) -> Unit = {},
    onYearMinChange: (String) -> Unit = {},
    onYearMaxChange: (String) -> Unit = {},
    onFloorMinChange: (String) -> Unit = {},
    onFloorMaxChange: (String) -> Unit = {},
    onWantParkingChange: (Boolean) -> Unit = {},
    onWantStorageChange: (Boolean) -> Unit = {},
    onWantElevatorChange: (Boolean) -> Unit = {},
    onBuilderBuyBudgetMinChange: (String) -> Unit = {},
    onBuilderBuyBudgetMaxChange: (String) -> Unit = {},
    onBuilderBuyMinAreaChange: (String) -> Unit = {},
    onBuilderBuyMaxAreaChange: (String) -> Unit = {},
    onBuilderBuyAreasChange: (String) -> Unit = {},
    onBuilderBuyTypesChange: (String) -> Unit = {},
    onNotesChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    val profile = remember(customerType) {
        CrmTypeProfiles.profileFor(customerType.ifBlank { CrmConstants.CUSTOMER_TYPES.first() })
    }
    val showBudget = CrmTypeProfiles.showsBudget(profile.moneyMode)
    val showRent = CrmTypeProfiles.showsRent(profile.moneyMode)
    val showBuilderBuy = CrmTypeProfiles.showsBuilderBuy(profile.moneyMode)
    val matchEligible = CrmConstants.isMatchEligible(customerType)
    var selectedTab by remember { mutableStateOf(ContactEditTab.Identity) }

    val completionScore = remember(name, phone, customerType, money, prefs, notes, showBudget, showRent) {
        var filled = 0
        var total = 4
        if (name.isNotBlank()) filled++
        if (phone.isNotBlank()) filled++
        if (customerType.isNotBlank()) filled++
        if (notes.isNotBlank()) filled++
        if (showBudget && (money.budgetMin.isNotBlank() || money.budgetMax.isNotBlank())) filled++
        if (showBudget) total++
        if (showRent && (money.depositMin.isNotBlank() || money.rentMin.isNotBlank())) filled++
        if (showRent) total++
        if (prefs.propertyType.isNotBlank() || prefs.areas.isNotBlank()) filled++
        total++
        (filled * 100 / total.coerceAtLeast(1))
    }

    DfSheetScaffold(
        title = "ویرایش مخاطب",
        subtitle = "پروفایل کامل — هویت، نیاز مالی، ترجیحات ملک و یادداشت",
        icon = DfIcons.User,
        onClose = onDismiss,
        footer = {
            DfSheetActions(
                primaryText = if (isSubmitting) "در حال ذخیره…" else "ذخیره تغییرات",
                onPrimary = onSave,
                primaryEnabled = !isSubmitting && name.isNotBlank() && phone.isNotBlank(),
                isSubmitting = isSubmitting,
                onSecondary = onDismiss,
            )
        },
    ) {
        ContactEditSummaryCard(
            name = name.ifBlank { "بدون نام" },
            customerType = customerType,
            status = status,
            completionPercent = completionScore,
        )

        DfFilterChipRow(
            options = ContactEditTab.entries.map { DfFilterOption(it, it.label) },
            selected = selectedTab,
            onSelect = { selectedTab = it },
            modifier = Modifier.padding(bottom = 4.dp),
        )

        when (selectedTab) {
            ContactEditTab.Identity -> ContactEditIdentitySection(
                name = name,
                phone = phone,
                phoneAlt = phoneAlt,
                email = email,
                source = source,
                customerType = customerType,
                status = status,
                priority = priority,
                isSubmitting = isSubmitting,
                onNameChange = onNameChange,
                onPhoneChange = onPhoneChange,
                onPhoneAltChange = onPhoneAltChange,
                onEmailChange = onEmailChange,
                onSourceChange = onSourceChange,
                onCustomerTypeChange = onCustomerTypeChange,
                onStatusChange = onStatusChange,
                onPriorityChange = onPriorityChange,
            )

            ContactEditTab.Money -> ContactEditMoneySection(
                profile = profile,
                showBudget = showBudget,
                showRent = showRent,
                showBuilderBuy = showBuilderBuy,
                money = money,
                builder = builder,
                isSubmitting = isSubmitting,
                onBudgetMinChange = onBudgetMinChange,
                onBudgetMaxChange = onBudgetMaxChange,
                onDepositMinChange = onDepositMinChange,
                onDepositMaxChange = onDepositMaxChange,
                onRentMinChange = onRentMinChange,
                onRentMaxChange = onRentMaxChange,
                onBuilderBuyBudgetMinChange = onBuilderBuyBudgetMinChange,
                onBuilderBuyBudgetMaxChange = onBuilderBuyBudgetMaxChange,
                onBuilderBuyMinAreaChange = onBuilderBuyMinAreaChange,
                onBuilderBuyMaxAreaChange = onBuilderBuyMaxAreaChange,
                onBuilderBuyAreasChange = onBuilderBuyAreasChange,
                onBuilderBuyTypesChange = onBuilderBuyTypesChange,
            )

            ContactEditTab.Property -> ContactEditPropertySection(
                showBuilderBuy = showBuilderBuy,
                matchEligible = matchEligible,
                matchingTolerancePercent = matchingTolerancePercent,
                prefs = prefs,
                isSubmitting = isSubmitting,
                onPropertyTypeChange = onPropertyTypeChange,
                onRoomsChange = onRoomsChange,
                onRoomsMinChange = onRoomsMinChange,
                onRoomsMaxChange = onRoomsMaxChange,
                onMinAreaChange = onMinAreaChange,
                onMaxAreaChange = onMaxAreaChange,
                onAreasChange = onAreasChange,
                onCityChange = onCityChange,
                onDistrictChange = onDistrictChange,
                onYearMinChange = onYearMinChange,
                onYearMaxChange = onYearMaxChange,
                onFloorMinChange = onFloorMinChange,
                onFloorMaxChange = onFloorMaxChange,
                onWantParkingChange = onWantParkingChange,
                onWantStorageChange = onWantStorageChange,
                onWantElevatorChange = onWantElevatorChange,
                onMatchingToleranceChange = onMatchingToleranceChange,
            )

            ContactEditTab.Notes -> DfSheetSection(title = "یادداشت داخلی") {
                Text(
                    text = "یادداشت فقط برای تیم مشاوران است و در اشتراک با مشتری نمایش داده نمی‌شود.",
                    style = MaterialTheme.typography.bodySmall,
                    color = DfColors.TextMuted,
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = onNotesChange,
                    label = { Text("یادداشت") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 6,
                    enabled = !isSubmitting,
                    placeholder = { Text("نیازها، محدودیت‌ها، زمان‌بندی بازدید، نکات مذاکره…") },
                )
            }
        }
    }
}

@Composable
private fun ContactEditSummaryCard(
    name: String,
    customerType: String,
    status: String,
    completionPercent: Int,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = DfColors.SurfaceVariant.copy(alpha = 0.55f),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = name,
                    style = AppTypography.cardTitle,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
                Text(
                    text = listOfNotNull(
                        customerType.takeIf { it.isNotBlank() },
                        status.takeIf { it.isNotBlank() },
                    ).joinToString(" · "),
                    style = AppTypography.bodyDescription,
                    color = DfColors.TextMuted,
                    maxLines = 1,
                )
            }
            Text(
                text = "${DateUtils.toPersianDigits(completionPercent.toString())}٪ تکمیل",
                style = AppTypography.labelSmall,
                color = DfColors.Purple,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun ContactEditIdentitySection(
    name: String,
    phone: String,
    phoneAlt: String,
    email: String,
    source: String,
    customerType: String,
    status: String,
    priority: String,
    isSubmitting: Boolean,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onPhoneAltChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onSourceChange: (String) -> Unit,
    onCustomerTypeChange: (String) -> Unit,
    onStatusChange: (String) -> Unit,
    onPriorityChange: (String) -> Unit,
) {
    DfSheetSection(title = "اطلاعات تماس") {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("نام و نام خانوادگی") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isSubmitting,
        )
        OutlinedTextField(
            value = phone,
            onValueChange = { onPhoneChange(PhoneNormalizer.normalize(it)) },
            label = { Text("شماره موبایل اصلی") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isSubmitting,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        )
        OutlinedTextField(
            value = phoneAlt,
            onValueChange = { onPhoneAltChange(PhoneNormalizer.normalize(it)) },
            label = { Text("شماره تماس دوم (اختیاری)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isSubmitting,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        )
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("ایمیل (اختیاری)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isSubmitting,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        )
    }

    DfSheetSection(title = "طبقه‌بندی CRM") {
        DfDropdown(
            label = "نوع مخاطب",
            value = customerType.ifBlank { CrmConstants.CUSTOMER_TYPES.first() },
            options = CrmConstants.CUSTOMER_TYPES,
            enabled = !isSubmitting,
            onSelect = onCustomerTypeChange,
        )
        DfDropdown(
            label = "وضعیت پیگیری",
            value = status.ifBlank { CrmConstants.STATUSES.first() },
            options = CrmConstants.STATUSES,
            enabled = !isSubmitting,
            onSelect = onStatusChange,
        )
        DfDropdown(
            label = "اولویت",
            value = priority.ifBlank { CrmConstants.PRIORITIES[1] },
            options = CrmConstants.PRIORITIES,
            enabled = !isSubmitting,
            onSelect = onPriorityChange,
        )
        DfDropdown(
            label = "منبع جذب",
            value = source.ifBlank { "—" },
            options = listOf("—") + CrmConstants.SOURCES,
            enabled = !isSubmitting,
            onSelect = { onSourceChange(if (it == "—") "" else it) },
        )
    }
}

@Composable
private fun ContactEditMoneySection(
    profile: TypeProfile,
    showBudget: Boolean,
    showRent: Boolean,
    showBuilderBuy: Boolean,
    money: ContactEditMoneyState,
    builder: ContactEditBuilderState,
    isSubmitting: Boolean,
    onBudgetMinChange: (String) -> Unit,
    onBudgetMaxChange: (String) -> Unit,
    onDepositMinChange: (String) -> Unit,
    onDepositMaxChange: (String) -> Unit,
    onRentMinChange: (String) -> Unit,
    onRentMaxChange: (String) -> Unit,
    onBuilderBuyBudgetMinChange: (String) -> Unit,
    onBuilderBuyBudgetMaxChange: (String) -> Unit,
    onBuilderBuyMinAreaChange: (String) -> Unit,
    onBuilderBuyMaxAreaChange: (String) -> Unit,
    onBuilderBuyAreasChange: (String) -> Unit,
    onBuilderBuyTypesChange: (String) -> Unit,
) {
    if (showBudget || showRent) {
        DfSheetSection(title = if (showBuilderBuy) "خط فروش — آپارتمان" else "اطلاعات مالی") {
            Text(
                text = profile.sectionHint,
                style = MaterialTheme.typography.bodySmall,
                color = DfColors.TextMuted,
            )
            if (showBudget) {
                ContactMoneyRangeRow(
                    minValue = money.budgetMin,
                    maxValue = money.budgetMax,
                    minLabel = profile.budgetLabels.first,
                    maxLabel = profile.budgetLabels.second,
                    enabled = !isSubmitting,
                    money = true,
                    onMinChange = onBudgetMinChange,
                    onMaxChange = onBudgetMaxChange,
                )
            }
            if (showRent) {
                ContactMoneyRangeRow(
                    minValue = money.depositMin,
                    maxValue = money.depositMax,
                    minLabel = profile.depositLabels.first,
                    maxLabel = profile.depositLabels.second,
                    enabled = !isSubmitting,
                    money = true,
                    onMinChange = onDepositMinChange,
                    onMaxChange = onDepositMaxChange,
                )
                ContactMoneyRangeRow(
                    minValue = money.rentMin,
                    maxValue = money.rentMax,
                    minLabel = profile.rentLabels.first,
                    maxLabel = profile.rentLabels.second,
                    enabled = !isSubmitting,
                    money = true,
                    onMinChange = onRentMinChange,
                    onMaxChange = onRentMaxChange,
                )
            }
        }
    } else {
        DfSheetSection(title = "اطلاعات مالی") {
            Text(
                text = "برای این نوع مخاطب فیلد مالی اختصاصی تعریف نشده است.",
                style = MaterialTheme.typography.bodySmall,
                color = DfColors.TextMuted,
            )
        }
    }

    if (showBuilderBuy) {
        DfSheetSection(title = "تأمین پروژه — خرید زمین و کلنگی") {
            Text(
                text = "بودجه و منطقه خرید ویلا، کلنگی و زمین برای توسعه پروژه",
                style = MaterialTheme.typography.bodySmall,
                color = DfColors.TextMuted,
            )
            ContactMoneyRangeRow(
                minValue = builder.buyBudgetMin,
                maxValue = builder.buyBudgetMax,
                minLabel = "بودجه خرید از",
                maxLabel = "بودجه خرید تا",
                enabled = !isSubmitting,
                money = true,
                onMinChange = onBuilderBuyBudgetMinChange,
                onMaxChange = onBuilderBuyBudgetMaxChange,
            )
            ContactMoneyRangeRow(
                minValue = builder.buyMinArea,
                maxValue = builder.buyMaxArea,
                minLabel = "متراژ خرید از",
                maxLabel = "متراژ خرید تا",
                enabled = !isSubmitting,
                onMinChange = onBuilderBuyMinAreaChange,
                onMaxChange = onBuilderBuyMaxAreaChange,
            )
            OutlinedTextField(
                value = builder.buyAreas,
                onValueChange = onBuilderBuyAreasChange,
                label = { Text("محله‌های هدف خرید") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSubmitting,
                placeholder = { Text("ونک، نیاوران، …") },
            )
            OutlinedTextField(
                value = builder.buyPropertyTypes,
                onValueChange = onBuilderBuyTypesChange,
                label = { Text("انواع ملک هدف خرید") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSubmitting,
                placeholder = { Text("ویلا, کلنگی, زمین") },
            )
        }
    }
}

@Composable
private fun ContactEditPropertySection(
    showBuilderBuy: Boolean,
    matchEligible: Boolean,
    matchingTolerancePercent: Int,
    prefs: ContactEditPrefsState,
    isSubmitting: Boolean,
    onPropertyTypeChange: (String) -> Unit,
    onRoomsChange: (String) -> Unit,
    onRoomsMinChange: (String) -> Unit,
    onRoomsMaxChange: (String) -> Unit,
    onMinAreaChange: (String) -> Unit,
    onMaxAreaChange: (String) -> Unit,
    onAreasChange: (String) -> Unit,
    onCityChange: (String) -> Unit,
    onDistrictChange: (String) -> Unit,
    onYearMinChange: (String) -> Unit,
    onYearMaxChange: (String) -> Unit,
    onFloorMinChange: (String) -> Unit,
    onFloorMaxChange: (String) -> Unit,
    onWantParkingChange: (Boolean) -> Unit,
    onWantStorageChange: (Boolean) -> Unit,
    onWantElevatorChange: (Boolean) -> Unit,
    onMatchingToleranceChange: (Int) -> Unit,
) {
    DfSheetSection(title = if (showBuilderBuy) "مشخصات واحد فروش" else "نوع و اندازه ملک") {
        Text(
            text = "نوع ملک",
            style = AppTypography.labelSmall,
            color = DfColors.TextMuted,
        )
        DfFilterChipRow(
            options = listOf(DfFilterOption("", "نامشخص")) +
                CrmConstants.PROPERTY_TYPES.map { DfFilterOption(it, it) },
            selected = prefs.propertyType,
            onSelect = onPropertyTypeChange,
        )
        ContactMoneyRangeRow(
            minValue = prefs.roomsMin,
            maxValue = prefs.roomsMax,
            minLabel = "اتاق از",
            maxLabel = "اتاق تا",
            enabled = !isSubmitting,
            onMinChange = onRoomsMinChange,
            onMaxChange = onRoomsMaxChange,
        )
        OutlinedTextField(
            value = prefs.rooms,
            onValueChange = onRoomsChange,
            label = { Text("توضیح اتاق (اختیاری)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isSubmitting,
            placeholder = { Text("مثلاً ۲+۱ یا مستر") },
        )
        ContactMoneyRangeRow(
            minValue = prefs.minArea,
            maxValue = prefs.maxArea,
            minLabel = "متراژ از",
            maxLabel = "متراژ تا",
            enabled = !isSubmitting,
            onMinChange = onMinAreaChange,
            onMaxChange = onMaxAreaChange,
        )
    }

    DfSheetSection(title = "موقعیت و محله") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = prefs.city,
                onValueChange = onCityChange,
                label = { Text("شهر") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                enabled = !isSubmitting,
            )
            OutlinedTextField(
                value = prefs.district,
                onValueChange = onDistrictChange,
                label = { Text("منطقه / محله") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                enabled = !isSubmitting,
            )
        }
        OutlinedTextField(
            value = prefs.areas,
            onValueChange = onAreasChange,
            label = { Text(if (showBuilderBuy) "محله‌های فروش / پروژه" else "محله‌های مورد نظر") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            enabled = !isSubmitting,
            placeholder = { Text("ونک، نیاوران، … — با ویرگول جدا کنید") },
        )
    }

    DfSheetSection(title = "جزئیات ساختمان") {
        ContactMoneyRangeRow(
            minValue = prefs.yearMin,
            maxValue = prefs.yearMax,
            minLabel = "سال ساخت از",
            maxLabel = "سال ساخت تا",
            enabled = !isSubmitting,
            onMinChange = onYearMinChange,
            onMaxChange = onYearMaxChange,
        )
        ContactMoneyRangeRow(
            minValue = prefs.floorMin,
            maxValue = prefs.floorMax,
            minLabel = "طبقه از",
            maxLabel = "طبقه تا",
            enabled = !isSubmitting,
            onMinChange = onFloorMinChange,
            onMaxChange = onFloorMaxChange,
        )
        Text(
            text = "امکانات مورد نیاز",
            style = AppTypography.labelSmall,
            color = DfColors.TextMuted,
        )
        AmenityToggleRow("پارکینگ", prefs.wantParking, !isSubmitting, onWantParkingChange)
        AmenityToggleRow("انباری", prefs.wantStorage, !isSubmitting, onWantStorageChange)
        AmenityToggleRow("آسانسور", prefs.wantElevator, !isSubmitting, onWantElevatorChange)
    }

    if (matchEligible) {
        DfSheetSection(title = "تطبیق هوشمند") {
            MatchingToleranceSlider(
                value = matchingTolerancePercent,
                enabled = !isSubmitting,
                onValueChange = onMatchingToleranceChange,
            )
        }
    }
}

@Composable
private fun MatchingToleranceSlider(
    value: Int,
    enabled: Boolean,
    onValueChange: (Int) -> Unit,
) {
    val coerced = value.coerceIn(0, 50)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "انعطاف‌پذیری پیشنهادها",
                    style = AppTypography.bodyDescription,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = "مقدار کمتر = پیشنهادهای دقیق‌تر؛ بیشتر = محدوده وسیع‌تر",
                    style = MaterialTheme.typography.bodySmall,
                    color = DfColors.TextMuted,
                )
            }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = DfColors.Purple.copy(alpha = 0.12f),
            ) {
                Text(
                    text = "${DateUtils.toPersianDigits(coerced.toString())}٪",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = AppTypography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = DfColors.Purple,
                )
            }
        }
        Slider(
            value = coerced.toFloat(),
            onValueChange = { raw ->
                val snapped = ((raw / 5f).toInt() * 5).coerceIn(0, 50)
                onValueChange(snapped)
            },
            valueRange = 0f..50f,
            steps = 9,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            listOf(0, 25, 50).forEach { tick ->
                Text(
                    text = "${DateUtils.toPersianDigits(tick.toString())}٪",
                    style = AppTypography.labelSmall,
                    color = if (coerced == tick) DfColors.Purple else DfColors.TextMuted,
                )
            }
        }
    }
}

@Composable
private fun AmenityToggleRow(
    label: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label)
        Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
    }
}

@Composable
private fun ContactMoneyRangeRow(
    minValue: String,
    maxValue: String,
    minLabel: String,
    maxLabel: String,
    enabled: Boolean,
    onMinChange: (String) -> Unit,
    onMaxChange: (String) -> Unit,
    money: Boolean = false,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (money) {
            DfMoneyField(
                value = minValue,
                onValueChange = onMinChange,
                label = minLabel,
                modifier = Modifier.weight(1f),
                enabled = enabled,
            )
            DfMoneyField(
                value = maxValue,
                onValueChange = onMaxChange,
                label = maxLabel,
                modifier = Modifier.weight(1f),
                enabled = enabled,
            )
        } else {
            OutlinedTextField(
                value = minValue,
                onValueChange = onMinChange,
                label = { Text(minLabel) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                enabled = enabled,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                placeholder = { Text("از", color = DfColors.TextMuted) },
            )
            OutlinedTextField(
                value = maxValue,
                onValueChange = onMaxChange,
                label = { Text(maxLabel) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                enabled = enabled,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                placeholder = { Text("تا", color = DfColors.TextMuted) },
            )
        }
    }
}
