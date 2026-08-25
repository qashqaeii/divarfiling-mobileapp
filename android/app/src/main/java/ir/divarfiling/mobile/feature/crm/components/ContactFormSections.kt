package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfFilterChipRow
import ir.divarfiling.mobile.core.design.components.DfFilterOption
import ir.divarfiling.mobile.core.design.components.DfMoneyField
import ir.divarfiling.mobile.core.design.components.DfSheetSection
import ir.divarfiling.mobile.feature.crm.ContactTypeVisuals
import ir.divarfiling.mobile.feature.crm.CrmConstants
import ir.divarfiling.mobile.feature.crm.CrmContactChannels
import ir.divarfiling.mobile.feature.crm.CrmTypeProfiles

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ContactTypeSelectorSection(
    selectedType: String,
    enabled: Boolean,
    onTypeChange: (String) -> Unit,
) {
    val type = selectedType.ifBlank { "سرنخ" }
    val selectedVisual = ContactTypeVisuals.visualFor(type)

    DfSheetSection(title = "نوع مخاطب") {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = AppShapes.Card,
            color = selectedVisual.container.copy(alpha = 0.65f),
            border = BorderStroke(1.dp, selectedVisual.accent.copy(alpha = 0.35f)),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = selectedVisual.icon,
                    contentDescription = null,
                    tint = selectedVisual.accent,
                    modifier = Modifier.size(22.dp),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "نقش انتخاب‌شده",
                        style = AppTypography.labelSmall,
                        color = DfThemeColors.textMuted(),
                    )
                    Text(
                        text = type,
                        style = AppTypography.cardTitle,
                        fontWeight = FontWeight.Bold,
                        color = selectedVisual.accent,
                    )
                }
            }
        }

        Text(
            text = "برای تغییر نقش، یکی از گزینه‌های زیر را انتخاب کنید",
            style = MaterialTheme.typography.bodySmall,
            color = DfColors.TextMuted,
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            CrmConstants.CUSTOMER_TYPES.forEach { customerType ->
                val visual = ContactTypeVisuals.visualFor(customerType)
                val selected = type == customerType
                Surface(
                    onClick = { onTypeChange(customerType) },
                    enabled = enabled,
                    shape = AppShapes.Chip,
                    color = if (selected) visual.container else DfThemeColors.surfaceVariant(),
                    border = BorderStroke(
                        1.dp,
                        if (selected) visual.accent.copy(alpha = 0.45f) else DfThemeColors.outlineSubtle(),
                    ),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = visual.icon,
                            contentDescription = null,
                            tint = if (selected) visual.accent else DfThemeColors.textMuted(),
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = customerType,
                            style = AppTypography.labelSmall,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            color = if (selected) visual.accent else DfThemeColors.textSecondary(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ContactMoneyFormSection(
    customerType: String,
    money: ContactEditMoneyState,
    builder: ContactEditBuilderState,
    enabled: Boolean,
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
    val profile = CrmTypeProfiles.profileFor(customerType.ifBlank { "سرنخ" })
    val showBudget = CrmTypeProfiles.showsBudget(profile.moneyMode)
    val showRent = CrmTypeProfiles.showsRent(profile.moneyMode)
    val showBuilderBuy = CrmTypeProfiles.showsBuilderBuy(profile.moneyMode)

    if (showBudget || showRent) {
        DfSheetSection(title = if (showBuilderBuy) "خط فروش — آپارتمان" else "بودجه و مالی") {
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
                    enabled = enabled,
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
                    enabled = enabled,
                    money = true,
                    onMinChange = onDepositMinChange,
                    onMaxChange = onDepositMaxChange,
                )
                ContactMoneyRangeRow(
                    minValue = money.rentMin,
                    maxValue = money.rentMax,
                    minLabel = profile.rentLabels.first,
                    maxLabel = profile.rentLabels.second,
                    enabled = enabled,
                    money = true,
                    onMinChange = onRentMinChange,
                    onMaxChange = onRentMaxChange,
                )
            }
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
                enabled = enabled,
                money = true,
                onMinChange = onBuilderBuyBudgetMinChange,
                onMaxChange = onBuilderBuyBudgetMaxChange,
            )
            ContactMoneyRangeRow(
                minValue = builder.buyMinArea,
                maxValue = builder.buyMaxArea,
                minLabel = "متراژ خرید از",
                maxLabel = "متراژ خرید تا",
                enabled = enabled,
                onMinChange = onBuilderBuyMinAreaChange,
                onMaxChange = onBuilderBuyMaxAreaChange,
            )
            OutlinedTextField(
                value = builder.buyAreas,
                onValueChange = onBuilderBuyAreasChange,
                label = { Text("محله‌های هدف خرید") },
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                placeholder = { Text("اختیاری — ونک، نیاوران، …") },
            )
            OutlinedTextField(
                value = builder.buyPropertyTypes,
                onValueChange = onBuilderBuyTypesChange,
                label = { Text("انواع ملک هدف خرید") },
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                placeholder = { Text("ویلا, کلنگی, زمین") },
            )
        }
    }
}

@Composable
fun ContactPropertyPrefsFormSection(
    customerType: String,
    prefs: ContactEditPrefsState,
    matchingTolerancePercent: Int,
    enabled: Boolean,
    onPropertyTypeChange: (String) -> Unit,
    onRoomsChange: (String) -> Unit,
    onRoomsMinChange: (String) -> Unit,
    onRoomsMaxChange: (String) -> Unit,
    onMinAreaChange: (String) -> Unit,
    onMaxAreaChange: (String) -> Unit,
    onAreasChange: (String) -> Unit,
    onYearMinChange: (String) -> Unit,
    onYearMaxChange: (String) -> Unit,
    onFloorMinChange: (String) -> Unit,
    onFloorMaxChange: (String) -> Unit,
    onWantParkingChange: (Boolean) -> Unit,
    onWantStorageChange: (Boolean) -> Unit,
    onWantElevatorChange: (Boolean) -> Unit,
    onMatchingToleranceChange: (Int) -> Unit,
) {
    val showBuilderBuy = CrmTypeProfiles.showsBuilderBuy(
        CrmTypeProfiles.profileFor(customerType).moneyMode,
    )
    val matchEligible = CrmConstants.isMatchEligible(customerType)
    val landLike = prefs.propertyType in setOf("زمین", "کلنگی")

    DfSheetSection(title = if (showBuilderBuy) "ترجیحات — واحد فروش" else "ترجیحات ملک") {
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

        if (!landLike) {
            ContactMoneyRangeRow(
                minValue = prefs.roomsMin,
                maxValue = prefs.roomsMax,
                minLabel = "اتاق از",
                maxLabel = "اتاق تا",
                enabled = enabled,
                onMinChange = onRoomsMinChange,
                onMaxChange = onRoomsMaxChange,
            )
            OutlinedTextField(
                value = prefs.rooms,
                onValueChange = onRoomsChange,
                label = { Text("توضیح اتاق") },
                placeholder = { Text("اختیاری — مثلاً ۲+۱") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = enabled,
            )
        }

        ContactMoneyRangeRow(
            minValue = prefs.minArea,
            maxValue = prefs.maxArea,
            minLabel = "متراژ از",
            maxLabel = "متراژ تا",
            enabled = enabled,
            onMinChange = onMinAreaChange,
            onMaxChange = onMaxAreaChange,
        )

        OutlinedTextField(
            value = prefs.areas,
            onValueChange = onAreasChange,
            label = { Text(if (showBuilderBuy) "محله‌های فروش / پروژه" else "محله‌های مورد نظر") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            enabled = enabled,
            placeholder = { Text("اختیاری — ونک، نیاوران، …") },
        )

        if (!landLike) {
            ContactMoneyRangeRow(
                minValue = prefs.yearMin,
                maxValue = prefs.yearMax,
                minLabel = "سال ساخت از",
                maxLabel = "سال ساخت تا",
                enabled = enabled,
                onMinChange = onYearMinChange,
                onMaxChange = onYearMaxChange,
            )
            ContactMoneyRangeRow(
                minValue = prefs.floorMin,
                maxValue = prefs.floorMax,
                minLabel = "طبقه از",
                maxLabel = "طبقه تا",
                enabled = enabled,
                onMinChange = onFloorMinChange,
                onMaxChange = onFloorMaxChange,
            )
            Text(
                text = "امکانات مورد نیاز",
                style = AppTypography.labelSmall,
                color = DfColors.TextMuted,
            )
            ContactAmenityToggleRow("پارکینگ", prefs.wantParking, enabled, onWantParkingChange)
            ContactAmenityToggleRow("انباری", prefs.wantStorage, enabled, onWantStorageChange)
            ContactAmenityToggleRow("آسانسور", prefs.wantElevator, enabled, onWantElevatorChange)
        }
    }

    if (matchEligible) {
        DfSheetSection(title = "تطبیق هوشمند") {
            ContactMatchingToleranceSlider(
                value = matchingTolerancePercent,
                enabled = enabled,
                onValueChange = onMatchingToleranceChange,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ContactChannelsFormSection(
    phone: String,
    activeChannels: Set<String>,
    socialLinks: Map<String, String>,
    enabled: Boolean,
    onToggleChannel: (String, Boolean) -> Unit,
    onSocialLinkChange: (String, String) -> Unit,
) {
    DfSheetSection(title = "کانال‌های پیام") {
        Text(
            text = "روش‌هایی که می‌توانید با این مخاطب در ارتباط باشید",
            style = MaterialTheme.typography.bodySmall,
            color = DfColors.TextMuted,
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CrmContactChannels.builtinChannels.forEach { channel ->
                val active = channel.key in activeChannels
                Surface(
                    onClick = { onToggleChannel(channel.key, !active) },
                    enabled = enabled && (channel.usesPhone.not() || phone.isNotBlank()),
                    shape = AppShapes.Chip,
                    color = if (active) channel.accent.copy(alpha = 0.14f) else DfThemeColors.surfaceVariant(),
                    border = BorderStroke(
                        1.dp,
                        if (active) channel.accent.copy(alpha = 0.5f) else DfThemeColors.outlineSubtle(),
                    ),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = channel.icon,
                            contentDescription = null,
                            tint = if (active) channel.accent else DfThemeColors.textMuted(),
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = channel.label,
                            style = AppTypography.labelSmall,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                            color = if (active) channel.accent else DfThemeColors.textSecondary(),
                        )
                        if (active) {
                            Icon(
                                imageVector = DfIcons.Check,
                                contentDescription = null,
                                tint = channel.accent,
                                modifier = Modifier.size(14.dp),
                            )
                        }
                    }
                }
            }
        }
        if (phone.isBlank()) {
            Text(
                text = "شماره موبایل را وارد کنید تا واتساپ، پیامک و تماس فعال شوند.",
                style = MaterialTheme.typography.bodySmall,
                color = DfColors.TextMuted,
            )
        }
    }

    DfSheetSection(title = "شبکه‌های اجتماعی") {
        Text(
            text = "شبکه‌هایی که مخاطب در آن‌ها فعال است",
            style = MaterialTheme.typography.bodySmall,
            color = DfColors.TextMuted,
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            CrmContactChannels.socialNetworks.forEach { network ->
                val active = network.key in activeChannels
                val linkValue = socialLinks[network.key].orEmpty()
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = AppShapes.Card,
                    color = if (active) network.accent.copy(alpha = 0.08f) else DfThemeColors.surfaceVariant(),
                    border = BorderStroke(
                        1.dp,
                        if (active) network.accent.copy(alpha = 0.35f) else DfThemeColors.outlineSubtle(),
                    ),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = network.icon,
                                    contentDescription = null,
                                    tint = network.accent,
                                    modifier = Modifier.size(18.dp),
                                )
                                Text(
                                    text = network.label,
                                    style = AppTypography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                            Switch(
                                checked = active,
                                onCheckedChange = { onToggleChannel(network.key, it) },
                                enabled = enabled,
                            )
                        }
                        AnimatedVisibility(
                            visible = active,
                            enter = expandVertically(),
                            exit = shrinkVertically(),
                        ) {
                            OutlinedTextField(
                                value = linkValue,
                                onValueChange = { onSocialLinkChange(network.key, it) },
                                label = { Text("شناسه ${network.label}") },
                                placeholder = { Text(network.placeholder) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                enabled = enabled,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ContactMoneyRangeRow(
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

@Composable
fun ContactAmenityToggleRow(
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
fun ContactMatchingToleranceSlider(
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
            Text("انعطاف تطبیق")
            Text(
                text = "$coerced٪",
                style = AppTypography.labelSmall,
                color = DfColors.Purple,
                fontWeight = FontWeight.SemiBold,
            )
        }
        androidx.compose.material3.Slider(
            value = coerced.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = 0f..50f,
            steps = 9,
            enabled = enabled,
        )
        Text(
            text = "هرچه بیشتر باشد، پیشنهادهای بیشتری (با فاصله از معیارها) نمایش داده می‌شود.",
            style = MaterialTheme.typography.bodySmall,
            color = DfColors.TextMuted,
        )
    }
}
