package ir.divarfiling.mobile.feature.license.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
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
import ir.divarfiling.mobile.core.design.components.DfGlassTextButton
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfSecondaryButton
import ir.divarfiling.mobile.core.design.components.DfTextField
import ir.divarfiling.mobile.core.license.LicenseState
import ir.divarfiling.mobile.core.network.ShopPlanDto
import ir.divarfiling.mobile.core.network.ShopDiscountPreviewData
import ir.divarfiling.mobile.feature.license.PlanMode
import ir.divarfiling.mobile.feature.license.baseUnitPrice
import ir.divarfiling.mobile.feature.license.durationLabelFa
import ir.divarfiling.mobile.feature.license.durationMetaFa
import ir.divarfiling.mobile.feature.license.effectiveUnitPrice
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
fun PlanModeTabs(
    mode: PlanMode,
    onModeChange: (PlanMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = DfThemeColors.surfaceVariant().copy(alpha = 0.65f),
        border = BorderStroke(1.dp, DfThemeColors.outlineSubtle()),
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            PlanModeTab(
                label = "پلن شخصی",
                icon = DfIcons.User,
                selected = mode == PlanMode.Personal,
                onClick = { onModeChange(PlanMode.Personal) },
                modifier = Modifier.weight(1f),
            )
            PlanModeTab(
                label = "پلن آژانس",
                icon = DfIcons.Users,
                selected = mode == PlanMode.Agency,
                onClick = { onModeChange(PlanMode.Agency) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun PlanModeTab(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = if (selected) DfThemeColors.surface() else Color.Transparent,
        shadowElevation = if (selected) AppElevations.subtle else AppElevations.none,
        border = if (selected) BorderStroke(1.dp, DfColors.Purple.copy(alpha = 0.18f)) else null,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) DfColors.Purple else DfThemeColors.textMuted(),
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = AppTypography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (selected) DfColors.Purple else DfThemeColors.textSecondary(),
            )
        }
    }
}

@Composable
fun PersonalPlanCard(
    plan: ShopPlanDto,
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val format = remember { NumberFormat.getInstance(Locale("fa", "IR")) }
    val blocked = plan.purchaseBlocked
    val unitPrice = plan.unitFinalPrice()
    val accent = when (plan.planType) {
        "monthly" -> DfColors.Blue
        "quarterly" -> DfColors.Purple
        "yearly" -> DfColors.Green
        else -> DfThemeColors.primary()
    }

    Surface(
        onClick = { if (!blocked) onSelect() },
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        color = when {
            blocked -> DfThemeColors.surfaceVariant().copy(alpha = 0.55f)
            selected -> accent.copy(alpha = 0.06f)
            else -> DfThemeColors.surface()
        },
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = when {
                blocked -> DfThemeColors.outlineSubtle()
                selected -> accent
                else -> DfThemeColors.outlineSubtle()
            },
        ),
        shadowElevation = if (selected) AppElevations.raised else AppElevations.subtle,
    ) {
        Column(modifier = Modifier.padding(AppSpacing.md), verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                verticalAlignment = Alignment.Top,
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(accent.copy(alpha = if (selected) 0.18f else 0.1f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = when (plan.planType) {
                            "monthly" -> DfIcons.Zap
                            "quarterly" -> DfIcons.Calendar
                            "yearly" -> DfIcons.Star
                            else -> DfIcons.Sparkles
                        },
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
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
                            !plan.offerBadge.isNullOrBlank() ->
                                PlanBadge(text = plan.offerBadge, color = DfColors.Amber, bg = DfColors.AmberLight)
                            plan.isFeatured ->
                                PlanBadge(text = "پیشنهادی", color = accent, bg = accent.copy(alpha = 0.12f))
                        }
                    }
                    plan.durationLabel?.let {
                        Text(it, style = AppTypography.meta, color = DfThemeColors.textSecondary())
                    }
                    plan.tagline?.takeIf { it.isNotBlank() }?.let {
                        Text(it, style = AppTypography.bodyDescription, color = DfThemeColors.textSecondary())
                    }
                }
                if (selected) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(accent),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(DfIcons.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                }
            }

            Surface(
                shape = AppShapes.GlassSmall,
                color = DfThemeColors.surfaceVariant().copy(alpha = 0.55f),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        if (plan.hasDiscount && plan.unitOriginalPrice() > unitPrice) {
                            Text(
                                format.format(plan.unitOriginalPrice()),
                                style = AppTypography.meta,
                                color = DfThemeColors.textMuted(),
                                textDecoration = TextDecoration.LineThrough,
                            )
                        }
                        Text(
                            "${format.format(unitPrice)} تومان",
                            style = AppTypography.sectionTitle,
                            fontWeight = FontWeight.Bold,
                            color = if (selected) accent else DfThemeColors.textPrimary(),
                        )
                        Text(
                            "قیمت پایه — تخفیف‌ها در خلاصه",
                            style = AppTypography.labelSmall,
                            color = DfThemeColors.textMuted(),
                        )
                    }
                }
            }

            if (plan.features.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    plan.features.take(4).forEach { feature ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                DfIcons.Check,
                                contentDescription = null,
                                tint = accent.copy(alpha = 0.85f),
                                modifier = Modifier.size(14.dp),
                            )
                            Text(
                                feature.text,
                                style = AppTypography.bodyDescription,
                                color = DfThemeColors.textSecondary(),
                            )
                        }
                    }
                }
            }

            if (blocked) {
                Text(plan.purchaseBlockMessage.orEmpty(), style = AppTypography.meta, color = DfThemeColors.error())
            }
        }
    }
}

@Composable
fun AgencyPlanPanel(
    plans: List<ShopPlanDto>,
    selectedPlanId: Long?,
    quantity: Int,
    pricingPreview: ShopDiscountPreviewData?,
    isPricingLoading: Boolean,
    onSelectPlan: (Long) -> Unit,
    onQuantityChange: (Int) -> Unit,
    showQuantity: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val selectedPlan = plans.firstOrNull { it.id == selectedPlanId } ?: plans.firstOrNull() ?: return
    val total = pricingPreview?.finalPrice ?: selectedPlan.totalFinalPrice(quantity)
    val baseUnit = pricingPreview.baseUnitPrice(selectedPlan)
    val effectiveUnit = pricingPreview.effectiveUnitPrice(selectedPlan)
    val slots = pricingPreview?.totalActivationSlots ?: (quantity * 2)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent,
        shadowElevation = AppElevations.raised,
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(
                            DfColors.PurpleLight.copy(alpha = 0.95f),
                            DfThemeColors.surface(),
                            DfColors.BlueLight.copy(alpha = 0.35f),
                        ),
                    ),
                    RoundedCornerShape(20.dp),
                )
                .padding(AppSpacing.md),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                    verticalAlignment = Alignment.Top,
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.linearGradient(listOf(DfColors.PurpleLight, DfColors.PurpleContainer)),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(DfIcons.Building, contentDescription = null, tint = DfColors.Purple, modifier = Modifier.size(22.dp))
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            "پلن آژانس",
                            style = AppTypography.cardTitle,
                            fontWeight = FontWeight.Bold,
                            color = DfThemeColors.textPrimary(),
                        )
                        Text(
                            "مناسب تیم‌های ۲ تا ۲۵ مشاور — هر مشاور یک کلید مستقل",
                            style = AppTypography.bodyDescription,
                            color = DfThemeColors.textSecondary(),
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "مدت لایسنس",
                        style = AppTypography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = DfColors.Purple,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        plans.forEach { plan ->
                            AgencyDurationCard(
                                plan = plan,
                                selected = plan.id == selectedPlanId,
                                onSelect = { onSelectPlan(plan.id) },
                                enabled = enabled && !plan.purchaseBlocked,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }

                if (showQuantity) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            "تعداد مشاور",
                            style = AppTypography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = DfColors.Purple,
                        )
                        LicenseQuantityStepper(
                            quantity = quantity,
                            minQuantity = selectedPlan.minQuantity,
                            maxQuantity = selectedPlan.maxQuantity,
                            unitPrice = effectiveUnit,
                            onQuantityChange = onQuantityChange,
                            enabled = enabled,
                        )
                    }
                }

                AgencyLiveTotalBar(
                    total = total,
                    isLoading = isPricingLoading,
                )

                AgencyFeatureGrid()

                if (showQuantity) {
                    AgencyPricingBreakdown(
                        quantity = quantity,
                        slots = slots,
                        baseUnit = baseUnit,
                        effectiveUnit = effectiveUnit,
                        appliedReason = pricingPreview?.appliedDiscountReason,
                        appliedPercent = pricingPreview?.appliedDiscountPercent ?: 0,
                    )
                }
            }
        }
    }
}

@Composable
private fun AgencyDurationCard(
    plan: ShopPlanDto,
    selected: Boolean,
    onSelect: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val format = remember { NumberFormat.getInstance(Locale("fa", "IR")) }
    Surface(
        onClick = onSelect,
        enabled = enabled,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = if (selected) DfThemeColors.surface() else DfThemeColors.surface().copy(alpha = 0.72f),
        border = BorderStroke(
            if (selected) 1.5.dp else 1.dp,
            if (selected) DfColors.Purple else DfThemeColors.outlineSubtle(),
        ),
        shadowElevation = if (selected) AppElevations.subtle else AppElevations.none,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                durationLabelFa(plan.planType),
                style = AppTypography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = DfThemeColors.textPrimary(),
            )
            Text(
                durationMetaFa(plan.planType),
                style = AppTypography.labelSmall,
                color = DfThemeColors.textMuted(),
            )
            Text(
                format.format(plan.unitFinalPrice()),
                style = AppTypography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = DfColors.Purple,
            )
            Text("هر مشاور", style = AppTypography.labelSmall, color = DfThemeColors.textMuted())
        }
    }
}

@Composable
private fun AgencyLiveTotalBar(
    total: Long,
    isLoading: Boolean,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = DfColors.Purple.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, DfColors.Purple.copy(alpha = 0.12f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.sm, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("مبلغ کل تیم", style = AppTypography.labelSmall, fontWeight = FontWeight.SemiBold, color = DfThemeColors.textSecondary())
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = DfColors.Purple,
                )
            } else {
                Text(
                    FormatUtils.formatPriceToman(total),
                    style = AppTypography.cardTitle,
                    fontWeight = FontWeight.Bold,
                    color = DfColors.Green,
                )
            }
        }
    }
}

@Composable
private fun AgencyFeatureGrid() {
    val items = listOf(
        DfIcons.KeyRound to "کلید مستقل هر مشاور",
        DfIcons.Smartphone to "ویندوز + اندروید",
        DfIcons.Users to "CRM و مدیریت تیم",
        DfIcons.BarChart to "استخراج و فایلینگ",
        DfIcons.Tag to "تخفیف حجمی تا ۲۵٪",
        DfIcons.MessageCircle to "پشتیبانی راه‌اندازی",
    )
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                rowItems.forEach { (icon, label) ->
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        color = DfThemeColors.surface().copy(alpha = 0.72f),
                        border = BorderStroke(1.dp, DfThemeColors.outlineSubtle()),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(icon, contentDescription = null, tint = DfColors.Purple, modifier = Modifier.size(14.dp))
                            Text(
                                label,
                                style = AppTypography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = DfThemeColors.textSecondary(),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
                if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun AgencyPricingBreakdown(
    quantity: Int,
    slots: Int,
    baseUnit: Long,
    effectiveUnit: Long,
    appliedReason: String?,
    appliedPercent: Int,
) {
    val persianQty = DateUtils.toPersianDigits(quantity.toString())
    val persianSlots = DateUtils.toPersianDigits(slots.toString())
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = DfThemeColors.surface().copy(alpha = 0.82f),
        border = BorderStroke(1.dp, DfColors.Purple.copy(alpha = 0.14f)),
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                AgencyBreakdownItem("تعداد مشاور", persianQty, Modifier.weight(1f))
                AgencyBreakdownItem("کلید مستقل", persianQty, Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                AgencyBreakdownItem("ظرفیت دستگاه", persianSlots, Modifier.weight(1f))
                AgencyBreakdownItem("قیمت پایه هر نفر", FormatUtils.formatPriceToman(baseUnit), Modifier.weight(1f))
            }
            if (!appliedReason.isNullOrBlank() && appliedPercent > 0) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = DfColors.GreenLight.copy(alpha = 0.65f),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(appliedReason, style = AppTypography.labelSmall, color = DfColors.Green, fontWeight = FontWeight.SemiBold)
                        Text("${DateUtils.toPersianDigits(appliedPercent.toString())}٪", style = AppTypography.labelSmall, fontWeight = FontWeight.Bold, color = DfColors.Green)
                    }
                }
            }
            AgencyBreakdownItem(
                label = "قیمت هر نفر",
                value = FormatUtils.formatPriceToman(effectiveUnit),
                modifier = Modifier.fillMaxWidth(),
                highlight = true,
            )
        }
    }
}

@Composable
private fun AgencyBreakdownItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    highlight: Boolean = false,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = AppTypography.labelSmall, color = DfThemeColors.textMuted())
        Text(
            value,
            style = AppTypography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (highlight) DfColors.Green else DfThemeColors.textPrimary(),
        )
    }
}

@Composable
fun PlansCheckoutBottomBar(
    selectedPlan: ShopPlanDto?,
    quantity: Int,
    total: Long?,
    discountPreview: ShopDiscountPreviewData?,
    isCheckingOut: Boolean,
    isVerifying: Boolean,
    canCheckout: Boolean,
    showStartUsing: Boolean,
    checkoutLabel: String,
    onCheckout: () -> Unit,
    onVerify: () -> Unit,
    onStartUsing: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        color = DfThemeColors.surface(),
        shadowElevation = AppElevations.raised,
        border = BorderStroke(1.dp, DfThemeColors.outlineSubtle()),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.screenHorizontal)
                .padding(top = AppSpacing.sm, bottom = AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            if (selectedPlan != null && total != null) {
                LicenseCheckoutSummary(
                    planName = selectedPlan.name,
                    quantity = quantity,
                    unitPrice = discountPreview.effectiveUnitPrice(selectedPlan),
                    totalPrice = total,
                    isAgency = selectedPlan.isAgencyPlan(),
                )
            }
            if (showStartUsing) {
                DfPrimaryButton(text = "شروع استفاده", onClick = onStartUsing)
            }
            DfPrimaryButton(
                text = checkoutLabel,
                onClick = onCheckout,
                loading = isCheckingOut,
                enabled = canCheckout && !isCheckingOut,
            )
            DfSecondaryButton(
                text = "بررسی وضعیت",
                onClick = onVerify,
                loading = isVerifying,
                enabled = !isVerifying,
            )
        }
    }
}

@Composable
fun PlansDiscountSection(
    discountCode: String,
    onDiscountCodeChange: (String) -> Unit,
    onApply: () -> Unit,
    onClear: () -> Unit,
    isApplying: Boolean,
    canApply: Boolean,
    preview: ShopDiscountPreviewData?,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        color = DfThemeColors.surface(),
        border = BorderStroke(1.dp, DfThemeColors.outlineSubtle()),
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            Text(
                "کد تخفیف",
                style = AppTypography.cardTitle,
                fontWeight = FontWeight.Bold,
                color = DfThemeColors.textPrimary(),
            )
            Text(
                "اختیاری — در صورت داشتن کد، قبل از پرداخت اعمال کنید",
                style = AppTypography.bodyDescription,
                color = DfThemeColors.textSecondary(),
            )
            DfTextField(
                value = discountCode,
                onValueChange = onDiscountCodeChange,
                label = "کد تخفیف",
                enabled = !isApplying,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DiscountApplyButton(
                    text = if (isApplying) "…" else "اعمال",
                    onClick = onApply,
                    enabled = canApply,
                    loading = isApplying,
                )
                if (preview != null) {
                    DfGlassTextButton(text = "حذف", onClick = onClear)
                }
            }
            preview?.let { data ->
                val before = data.baseFinalPrice ?: data.originalPrice
                val after = data.finalPrice
                if (before != null && after != null && before != after) {
                    Surface(
                        shape = AppShapes.GlassSmall,
                        color = DfColors.GreenLight.copy(alpha = 0.55f),
                    ) {
                        Text(
                            modifier = Modifier.padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
                            text = "قبل: ${FormatUtils.formatPriceToman(before)}  ←  بعد: ${FormatUtils.formatPriceToman(after)}",
                            style = AppTypography.bodyDescription,
                            fontWeight = FontWeight.SemiBold,
                            color = DfColors.Green,
                        )
                    }
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
    compact: Boolean = false,
) {
    if (compact) {
        LicenseQuantityStepperCompact(
            quantity = quantity,
            minQuantity = minQuantity,
            maxQuantity = maxQuantity,
            unitPrice = unitPrice,
            onQuantityChange = onQuantityChange,
            modifier = modifier,
            enabled = enabled,
        )
        return
    }
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
                        "تعداد مشاور",
                        style = AppTypography.cardTitle,
                        fontWeight = FontWeight.Bold,
                        color = DfThemeColors.textPrimary(),
                    )
                    Text(
                        "هر مشاور یک کلید مستقل · بازه ${DateUtils.toPersianDigits(minQuantity.toString())} تا ${DateUtils.toPersianDigits(maxQuantity.toString())}",
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
                        "مشاور",
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
                        Text("قیمت هر مشاور", style = AppTypography.labelSmall, color = DfThemeColors.textSecondary())
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
fun LicenseQuantityStepperCompact(
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
        shape = AppShapes.CardSmall,
        color = DfThemeColors.surface(),
        border = BorderStroke(1.dp, DfColors.Purple.copy(alpha = 0.2f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "تعداد مشاور",
                    style = AppTypography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = DfThemeColors.textPrimary(),
                )
                Text(
                    FormatUtils.formatPriceToman(total),
                    style = AppTypography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = DfThemeColors.primary(),
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                QuantityStepButton(
                    label = "کاهش",
                    enabled = enabled && quantity > minQuantity,
                    onClick = { onQuantityChange(quantity - 1) },
                    text = "−",
                    size = 36.dp,
                )
                Text(
                    persianQty,
                    style = AppTypography.cardTitle,
                    fontWeight = FontWeight.Bold,
                    color = DfThemeColors.primary(),
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
                QuantityStepButton(
                    label = "افزایش",
                    enabled = enabled && quantity < maxQuantity,
                    onClick = { onQuantityChange(quantity + 1) },
                    text = "+",
                    size = 36.dp,
                )
            }
        }
    }
}

@Composable
fun LicenseRenewalModeToggle(
    renewCurrentLicense: Boolean,
    onRenewCurrentLicenseChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        color = DfThemeColors.surface(),
        border = BorderStroke(1.dp, DfThemeColors.outlineSubtle()),
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            Text(
                "نوع خرید",
                style = AppTypography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = DfThemeColors.textPrimary(),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            ) {
                RenewalModeChip(
                    label = "خرید لایسنس جدید",
                    subtitle = "چند لایسنس آژانس",
                    selected = !renewCurrentLicense,
                    onClick = { onRenewCurrentLicenseChange(false) },
                    modifier = Modifier.weight(1f),
                )
                RenewalModeChip(
                    label = "تمدید لایسنس فعلی",
                    subtitle = "فقط یک لایسنس",
                    selected = renewCurrentLicense,
                    onClick = { onRenewCurrentLicenseChange(true) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun RenewalModeChip(
    label: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = AppShapes.CardSmall,
        color = if (selected) DfColors.PurpleContainer.copy(alpha = 0.55f) else DfThemeColors.surfaceVariant(),
        border = BorderStroke(
            if (selected) 1.5.dp else 1.dp,
            if (selected) DfColors.Purple else DfThemeColors.outlineSubtle(),
        ),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                label,
                style = AppTypography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (selected) DfColors.Purple else DfThemeColors.textPrimary(),
                maxLines = 2,
            )
            Text(
                subtitle,
                style = AppTypography.labelSmall,
                color = DfThemeColors.textMuted(),
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun QuantityStepButton(
    label: String,
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    size: androidx.compose.ui.unit.Dp = 44.dp,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        color = if (enabled) DfThemeColors.primary() else DfThemeColors.surfaceVariant(),
        shadowElevation = if (enabled) AppElevations.subtle else AppElevations.none,
        modifier = Modifier.size(size),
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
            if (isAgency && quantity >= 1) {
                val slots = quantity * 2
                Text(
                    "$persianQty مشاور · $persianQty کلید · ${DateUtils.toPersianDigits(slots.toString())} دستگاه",
                    style = AppTypography.bodyDescription,
                    color = DfThemeColors.textSecondary(),
                )
                Text(
                    "${FormatUtils.formatPriceToman(unitPrice)} برای هر مشاور",
                    style = AppTypography.labelSmall,
                    color = DfThemeColors.textMuted(),
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
    showQuantityControls: Boolean = false,
    onQuantityChange: ((Int) -> Unit)? = null,
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

            if (showQuantityControls && selected && isAgency && onQuantityChange != null) {
                LicenseQuantityStepperCompact(
                    quantity = quantity,
                    minQuantity = plan.minQuantity,
                    maxQuantity = plan.maxQuantity,
                    unitPrice = unitPrice,
                    onQuantityChange = onQuantityChange,
                    enabled = true,
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
