package ir.divarfiling.mobile.feature.license.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppElevations
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.FormatUtils
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfDecorImage
import ir.divarfiling.mobile.core.license.LicenseState
import ir.divarfiling.mobile.core.network.ShopPlanDto
import ir.divarfiling.mobile.feature.license.isAgencyPlan
import ir.divarfiling.mobile.feature.license.totalFinalPrice
import ir.divarfiling.mobile.feature.license.unitFinalPrice
import ir.divarfiling.mobile.feature.license.unitOriginalPrice
import java.text.NumberFormat
import java.util.Locale

@Composable
fun LicenseStatusHero(
    license: LicenseState,
    modifier: Modifier = Modifier,
) {
    val isActive = license.valid
    val gradient = if (isActive) {
        Brush.linearGradient(listOf(DfColors.Purple.copy(alpha = 0.94f), DfColors.Blue.copy(alpha = 0.88f)))
    } else {
        Brush.linearGradient(listOf(DfColors.Rose.copy(alpha = 0.88f), DfColors.Amber.copy(alpha = 0.78f)))
    }
    val statusLabel = when {
        isActive && license.expiringSoon -> "فعال — نزدیک انقضا"
        isActive -> "لایسنس فعال"
        license.status == "expired" -> "منقضی شده"
        else -> "بدون لایسنس فعال"
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        color = Color.Transparent,
        shadowElevation = AppElevations.raised,
    ) {
        Column(
            modifier = Modifier
                .background(gradient, AppShapes.Card)
                .padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = statusLabel,
                        style = AppTypography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.88f),
                    )
                    Text(
                        text = license.plan?.takeIf { it.isNotBlank() } ?: "انتخاب پلن",
                        style = AppTypography.sectionTitle,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = license.expiryHeadline,
                        style = AppTypography.bodyDescription,
                        color = Color.White.copy(alpha = 0.9f),
                    )
                }
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center,
                ) {
                    DfDecorImage(
                        resId = if (isActive) DfDecorIcons.Sparkles else DfDecorIcons.Timer,
                        size = 26.dp,
                        contentDescription = null,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            ) {
                LicenseMetaChip(
                    label = "تاریخ خرید",
                    value = formatLicenseDate(license.purchasedAt ?: license.startedAt) ?: "—",
                    modifier = Modifier.weight(1f),
                )
                LicenseMetaChip(
                    label = "تاریخ انقضا",
                    value = formatLicenseDate(license.expiresAt) ?: "—",
                    modifier = Modifier.weight(1f),
                )
            }
            if (isActive && license.expiresAt != null) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            "زمان باقی‌مانده",
                            style = AppTypography.labelSmall,
                            color = Color.White.copy(alpha = 0.78f),
                        )
                        Text(
                            "${license.expiryProgressPercent}٪",
                            style = AppTypography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.92f),
                        )
                    }
                    LinearProgressIndicator(
                        progress = { license.expiryProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(AppShapes.Chip),
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.22f),
                    )
                }
            }
        }
    }
}

@Composable
fun PlansSectionHeader(
    title: String,
    subtitle: String,
    badge: String? = null,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = AppTypography.sectionTitle,
                fontWeight = FontWeight.Bold,
                color = DfThemeColors.textPrimary(),
                modifier = Modifier.weight(1f, fill = false),
            )
            badge?.let {
                PlanBadge(text = it, color = DfColors.Purple, bg = DfColors.PurpleLight.copy(alpha = 0.35f))
            }
        }
        Text(
            text = subtitle,
            style = AppTypography.bodyDescription,
            color = DfThemeColors.textSecondary(),
        )
    }
}

@Composable
fun AgencyPricingInfoBanner(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        color = DfColors.PurpleLight.copy(alpha = 0.22f),
        border = BorderStroke(1.dp, DfColors.Purple.copy(alpha = 0.18f)),
    ) {
        Row(
            modifier = Modifier.padding(AppSpacing.md),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(DfColors.Purple.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(DfIcons.Users, contentDescription = null, tint = DfColors.Purple, modifier = Modifier.size(18.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "قیمت پلن آژانس — به ازای هر مشاور",
                    style = AppTypography.cardTitle,
                    fontWeight = FontWeight.Bold,
                    color = DfThemeColors.textPrimary(),
                )
                Text(
                    "مبلغ نمایش‌داده‌شده برای یک لایسنس است. تعداد مشاوران را انتخاب کنید تا مبلغ نهایی محاسبه شود.",
                    style = AppTypography.bodyDescription,
                    color = DfThemeColors.textSecondary(),
                )
            }
        }
    }
}

@Composable
fun LicenseQuantityStepper(
    quantity: Int,
    minQuantity: Int,
    maxQuantity: Int,
    unitPrice: Long,
    onQuantityChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val persianQty = DateUtils.toPersianDigits(quantity.toString())
    val total = unitPrice * quantity
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        color = DfThemeColors.surface(),
        border = BorderStroke(1.5.dp, DfColors.Purple.copy(alpha = 0.22f)),
        shadowElevation = AppElevations.subtle,
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        "تعداد لایسنس آژانس",
                        style = AppTypography.cardTitle,
                        fontWeight = FontWeight.Bold,
                        color = DfThemeColors.textPrimary(),
                    )
                    Text(
                        "هر لایسنس برای یک مشاور · بازه ${DateUtils.toPersianDigits(minQuantity.toString())} تا ${DateUtils.toPersianDigits(maxQuantity.toString())}",
                        style = AppTypography.labelSmall,
                        color = DfThemeColors.textSecondary(),
                    )
                }
                Surface(shape = AppShapes.Chip, color = DfColors.PurpleLight.copy(alpha = 0.35f)) {
                    Text(
                        "$persianQty نفر",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = AppTypography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = DfColors.Purple,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                QuantityStepButton(
                    label = "کاهش",
                    enabled = enabled && quantity > minQuantity,
                    onClick = { onQuantityChange(quantity - 1) },
                    text = "−",
                )
                Column(
                    modifier = Modifier.padding(horizontal = AppSpacing.lg),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        persianQty,
                        style = AppTypography.statNumber,
                        fontWeight = FontWeight.Bold,
                        color = DfThemeColors.primary(),
                    )
                    Text(
                        "لایسنس",
                        style = AppTypography.labelSmall,
                        color = DfThemeColors.textMuted(),
                    )
                }
                QuantityStepButton(
                    label = "افزایش",
                    enabled = enabled && quantity < maxQuantity,
                    onClick = { onQuantityChange(quantity + 1) },
                    text = "+",
                )
            }

            Surface(
                shape = AppShapes.GlassSmall,
                color = DfThemeColors.primaryContainer().copy(alpha = 0.45f),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppSpacing.sm),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("قیمت هر لایسنس", style = AppTypography.labelSmall, color = DfThemeColors.textSecondary())
                        Text(
                            FormatUtils.formatPriceToman(unitPrice),
                            style = AppTypography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = DfThemeColors.textPrimary(),
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            "$persianQty × ${FormatUtils.formatPriceToman(unitPrice).removeSuffix(" تومان")}",
                            style = AppTypography.labelSmall,
                            color = DfThemeColors.textSecondary(),
                        )
                        Text(
                            FormatUtils.formatPriceToman(total),
                            style = AppTypography.cardTitle,
                            fontWeight = FontWeight.Bold,
                            color = DfThemeColors.primary(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuantityStepButton(
    label: String,
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        color = if (enabled) DfThemeColors.primary() else DfThemeColors.surfaceVariant(),
        shadowElevation = if (enabled) AppElevations.subtle else AppElevations.none,
        modifier = Modifier.size(44.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = AppTypography.sectionTitle,
                fontWeight = FontWeight.Bold,
                color = if (enabled) Color.White else DfThemeColors.textMuted(),
            )
        }
    }
}

@Composable
fun LicenseCheckoutSummary(
    planName: String,
    quantity: Int,
    unitPrice: Long,
    totalPrice: Long,
    isAgency: Boolean,
    modifier: Modifier = Modifier,
) {
    val persianQty = DateUtils.toPersianDigits(quantity.toString())
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.CardSmall,
        color = DfThemeColors.surface(),
        border = BorderStroke(1.dp, DfThemeColors.outlineSubtle()),
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                "خلاصه سفارش",
                style = AppTypography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = DfThemeColors.textMuted(),
            )
            Text(
                planName,
                style = AppTypography.cardTitle,
                fontWeight = FontWeight.Bold,
                color = DfThemeColors.textPrimary(),
            )
            if (isAgency && quantity > 1) {
                Text(
                    "$persianQty لایسنس × ${FormatUtils.formatPriceToman(unitPrice)}",
                    style = AppTypography.bodyDescription,
                    color = DfThemeColors.textSecondary(),
                )
            } else if (isAgency) {
                Text(
                    "۱ لایسنس · ${FormatUtils.formatPriceToman(unitPrice)}",
                    style = AppTypography.bodyDescription,
                    color = DfThemeColors.textSecondary(),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Text("مبلغ قابل پرداخت", style = AppTypography.labelSmall, color = DfThemeColors.textSecondary())
                Text(
                    FormatUtils.formatPriceToman(totalPrice),
                    style = AppTypography.sectionTitle,
                    fontWeight = FontWeight.Bold,
                    color = DfThemeColors.primary(),
                )
            }
        }
    }
}

@Composable
private fun LicenseMetaChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(AppShapes.GlassSmall)
            .background(Color.White.copy(alpha = 0.14f))
            .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(label, style = AppTypography.labelSmall, color = Color.White.copy(alpha = 0.75f))
        Text(
            value,
            style = AppTypography.bodyDescription,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun formatLicenseDate(iso: String?): String? {
    iso ?: return null
    return DateUtils.formatJalaliDateTime(iso)
        ?: DateUtils.formatJalaliDate(iso)
        ?: DateUtils.formatForDisplay(iso).takeIf { it != "—" }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LicensePlanCard(
    plan: ShopPlanDto,
    selected: Boolean,
    quantity: Int,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val format = remember { NumberFormat.getInstance(Locale("fa", "IR")) }
    val blocked = plan.purchaseBlocked
    val isAgency = plan.isAgencyPlan()
    val unitPrice = plan.unitFinalPrice()
    val displayTotal = if (selected && isAgency) plan.totalFinalPrice(quantity) else unitPrice
    val borderColor = when {
        blocked -> DfThemeColors.outlineSubtle()
        selected && isAgency -> DfColors.Purple
        selected -> DfThemeColors.primary()
        isAgency -> DfColors.Purple.copy(alpha = 0.35f)
        else -> DfThemeColors.outlineSubtle()
    }
    val container = when {
        blocked -> DfThemeColors.surfaceVariant().copy(alpha = 0.55f)
        selected && isAgency -> DfColors.PurpleLight.copy(alpha = 0.18f)
        selected -> DfThemeColors.primaryContainer().copy(alpha = 0.45f)
        isAgency -> DfThemeColors.surface()
        else -> DfThemeColors.surface()
    }

    Surface(
        onClick = { if (!blocked) onSelect() },
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        color = container,
        border = BorderStroke(if (selected) 2.dp else 1.dp, borderColor),
        shadowElevation = if (selected) AppElevations.raised else AppElevations.subtle,
        tonalElevation = AppElevations.none,
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                verticalAlignment = Alignment.Top,
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                selected && isAgency -> DfColors.Purple
                                selected -> DfThemeColors.primary()
                                else -> DfThemeColors.surfaceVariant()
                            },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (selected) {
                        Icon(
                            imageVector = DfIcons.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            plan.name,
                            style = AppTypography.cardTitle,
                            fontWeight = FontWeight.Bold,
                            color = DfThemeColors.textPrimary(),
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        when {
                            isAgency -> PlanBadge(text = "آژانس", color = DfColors.Purple, bg = DfColors.PurpleLight.copy(alpha = 0.45f))
                            !plan.offerBadge.isNullOrBlank() -> PlanBadge(text = plan.offerBadge, color = DfColors.Amber, bg = DfColors.AmberLight)
                            plan.isFeatured -> PlanBadge(text = "پیشنهادی", color = DfThemeColors.primary(), bg = DfThemeColors.primaryContainer())
                        }
                    }
                    plan.durationLabel?.let {
                        Text(it, style = AppTypography.meta, color = DfThemeColors.textSecondary())
                    }
                    plan.tagline?.takeIf { it.isNotBlank() }?.let {
                        Text(it, style = AppTypography.bodyDescription, color = DfThemeColors.textSecondary())
                    }
                    if (isAgency) {
                        Text(
                            "قیمت هر لایسنس · ${DateUtils.toPersianDigits(plan.minQuantity.toString())} تا ${DateUtils.toPersianDigits(plan.maxQuantity.toString())} نفر",
                            style = AppTypography.labelSmall,
                            color = DfColors.Purple,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                if (plan.hasDiscount && plan.unitOriginalPrice() > unitPrice) {
                    Text(
                        format.format(plan.unitOriginalPrice()),
                        style = AppTypography.meta,
                        color = DfThemeColors.textMuted(),
                        textDecoration = TextDecoration.LineThrough,
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        if (isAgency && selected && quantity > 1) {
                            FormatUtils.formatPriceToman(displayTotal)
                        } else {
                            "${format.format(unitPrice)} تومان"
                        },
                        style = AppTypography.sectionTitle,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            selected && isAgency -> DfColors.Purple
                            selected -> DfThemeColors.primary()
                            else -> DfThemeColors.textPrimary()
                        },
                    )
                    Text(
                        when {
                            isAgency && selected && quantity > 1 -> "${DateUtils.toPersianDigits(quantity.toString())} لایسنس × ${FormatUtils.formatPriceToman(unitPrice)}"
                            isAgency -> "به ازای هر مشاور"
                            else -> "مبلغ پلن"
                        },
                        style = AppTypography.labelSmall,
                        color = DfThemeColors.textMuted(),
                    )
                }
            }

            if (plan.features.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    plan.features.take(5).forEach { feature ->
                        PlanBadge(
                            text = feature.text,
                            color = DfThemeColors.textSecondary(),
                            bg = DfThemeColors.surfaceVariant(),
                        )
                    }
                }
            }

            if (blocked) {
                Text(
                    plan.purchaseBlockMessage.orEmpty(),
                    style = AppTypography.meta,
                    color = DfThemeColors.error(),
                )
            } else if (selected) {
                Text(
                    if (isAgency) "پلن آژانس انتخاب شد — تعداد را تنظیم کنید" else "پلن انتخاب‌شده برای خرید",
                    style = AppTypography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isAgency) DfColors.Purple else DfThemeColors.primary(),
                )
            }
        }
    }
}

@Composable
private fun PlanBadge(text: String, color: Color, bg: Color) {
    Surface(shape = AppShapes.Chip, color = bg) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = AppTypography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = color,
        )
    }
}

@Composable
fun DiscountApplyButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean,
    loading: Boolean,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier,
        shape = AppShapes.Button,
        color = if (enabled && !loading) DfThemeColors.primary() else DfThemeColors.surfaceVariant(),
        shadowElevation = if (enabled && !loading) AppElevations.subtle else AppElevations.none,
    ) {
        Box(
            modifier = Modifier.padding(horizontal = AppSpacing.md, vertical = 14.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                style = AppTypography.button,
                fontWeight = FontWeight.Bold,
                color = if (enabled && !loading) Color.White else DfThemeColors.textMuted(),
                maxLines = 1,
            )
        }
    }
}
