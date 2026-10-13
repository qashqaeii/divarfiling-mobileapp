package ir.divarfiling.mobile.feature.contracts

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppElevations
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfBadge
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfHeaderSections
import ir.divarfiling.mobile.core.design.components.DfHubPageHeader
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfSecondaryButton
import ir.divarfiling.mobile.core.design.components.DfTextButton
import ir.divarfiling.mobile.core.network.ContractAttachmentDto
import ir.divarfiling.mobile.core.network.ContractClauseSummaryDto
import ir.divarfiling.mobile.core.network.ContractDetailDto
import ir.divarfiling.mobile.core.network.ContractSignatureRowDto
import ir.divarfiling.mobile.core.network.ContractTimelineItemDto
import ir.divarfiling.mobile.core.network.ContractVersionSummaryDto

@Composable
fun ContractDetailPageHeader(
    detail: ContractDetailDto,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val subtitle = buildList {
        add(detail.contractTypeLabel)
        if (detail.internalNumber.isNotBlank()) add("پرونده ${detail.internalNumber}")
        if (detail.statusLabel.isNotBlank()) add(detail.statusLabel)
    }.joinToString(" · ")
    DfHubPageHeader(
        title = detail.title.ifBlank { detail.contractTypeLabel }.ifBlank { "قرارداد" },
        subtitle = subtitle.ifBlank { "جزئیات قرارداد دیجیتال" },
        sectionLabel = DfHeaderSections.CRM,
        titleIconRes = contractDetailTypeIconRes(detail.contractType),
        onBack = onBack,
        showBottomDivider = false,
        modifier = modifier,
    )
}

@Composable
fun ContractDetailHero(
    detail: ContractDetailDto,
    modifier: Modifier = Modifier,
) {
    val (statusBg, statusFg) = contractStatusThemeColors(detail.statusLabel)
    val accent = statusFg
    val iconRes = contractDetailTypeIconRes(detail.contractType)
    val signedCount = detail.signatures.count { it.accepted }
    val totalParties = detail.signatures.size.coerceAtLeast(1)
    val progress = signedCount.toFloat() / totalParties.toFloat()

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Hero,
        color = DfThemeColors.surface(),
        border = BorderStroke(1.dp, DfThemeColors.outlineSubtle()),
        shadowElevation = AppElevations.subtle,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(accent.copy(alpha = 0.16f), Color.Transparent),
                    ),
                )
                .padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(AppShapes.IconContainer)
                        .background(accent.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = painterResource(iconRes),
                        contentDescription = null,
                        modifier = Modifier.size(34.dp),
                    )
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = detail.contractTypeLabel,
                        style = AppTypography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = DfThemeColors.textSecondary(),
                    )
                    Text(
                        text = detail.title.ifBlank { "بدون عنوان" },
                        style = AppTypography.cardTitle,
                        fontWeight = FontWeight.Bold,
                        color = DfThemeColors.textPrimary(),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DfBadge(text = detail.statusLabel, color = statusBg, textColor = statusFg)
                if (detail.currentVersionNumber > 0) {
                    DfBadge(
                        text = "نسخه ${DateUtils.toPersianDigits(detail.currentVersionNumber.toString())}",
                        color = DfThemeColors.surfaceVariant(),
                        textColor = DfThemeColors.textSecondary(),
                    )
                }
                if (detail.dealTitle.isNotBlank()) {
                    DfBadge(
                        text = detail.dealTitle,
                        color = DfThemeColors.infoContainer(),
                        textColor = DfThemeColors.info(),
                    )
                }
            }
            if (detail.signatures.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(DfIcons.CircleCheck, contentDescription = null, tint = accent, modifier = Modifier.size(16.dp))
                            Text("پیشرفت امضا", style = AppTypography.labelSmall, color = DfThemeColors.textMuted())
                        }
                        Text(
                            "${DateUtils.toPersianDigits(signedCount.toString())} از ${DateUtils.toPersianDigits(totalParties.toString())}",
                            style = AppTypography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = accent,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(AppShapes.CardSmall)
                            .background(DfThemeColors.surfaceVariant()),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress.coerceIn(0f, 1f))
                                .fillMaxHeight()
                                .clip(AppShapes.CardSmall)
                                .background(accent),
                        )
                    }
                }
            }
            detail.updatedAt?.let { iso ->
                DateUtils.parseInstantMillis(iso)?.let { ms ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(DfIcons.Clock, contentDescription = null, tint = DfThemeColors.textMuted(), modifier = Modifier.size(14.dp))
                        Text(
                            DateUtils.formatJalaliDateTimeLatinFromMillis(ms),
                            style = AppTypography.labelSmall,
                            color = DfThemeColors.textMuted(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ContractDetailNextActionCard(
    label: String,
    onPrimary: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    if (label.isBlank()) return
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        color = DfThemeColors.surface(),
        border = BorderStroke(1.dp, DfThemeColors.outlineSubtle()),
    ) {
        ContractDetailLinkRow(
            icon = DfIcons.Pencil,
            title = label,
            subtitle = "گام بعدی",
            onClick = onPrimary,
            enabled = enabled,
            iconTint = DfThemeColors.primary(),
            modifier = Modifier.padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
        )
    }
}

@Composable
fun ContractDetailSummaryCard(
    finHighlight: String?,
    propertyLine: String,
    partyLine: String?,
    onOpenDeal: (() -> Unit)?,
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
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Text(
                "نمای کلی",
                style = AppTypography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = DfThemeColors.textPrimary(),
            )
            finHighlight?.let {
                ContractDetailInsightRow(
                    icon = DfIcons.Coins,
                    iconTint = DfThemeColors.warning(),
                    label = "مالی",
                    value = it,
                )
            }
            if (propertyLine.isNotBlank()) {
                ContractDetailInsightRow(
                    icon = DfIcons.Building,
                    iconTint = DfThemeColors.info(),
                    label = "ملک",
                    value = propertyLine,
                )
            }
            partyLine?.let {
                ContractDetailInsightRow(
                    icon = DfIcons.User,
                    iconTint = DfThemeColors.primary(),
                    label = "طرف اصلی",
                    value = it,
                )
            }
            onOpenDeal?.let { open ->
                HorizontalDivider(color = DfThemeColors.outlineSubtle().copy(alpha = 0.6f))
                ContractDetailLinkRow(
                    icon = DfIcons.Briefcase,
                    title = "معامله مرتبط",
                    subtitle = "باز کردن پرونده معامله",
                    onClick = open,
                )
            }
        }
    }
}

@Composable
private fun ContractDetailInsightRow(
    icon: ImageVector,
    iconTint: Color,
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(AppShapes.IconContainer)
                .background(iconTint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, style = AppTypography.labelSmall, color = DfThemeColors.textMuted())
            Text(
                value,
                style = AppTypography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = DfThemeColors.textPrimary(),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun ContractDetailPartyRow(row: ContractSignatureRowDto) {
    val (chipBg, chipFg) = contractStatusThemeColors(row.statusLabel)
    val initial = row.name.trim().firstOrNull()?.toString() ?: "?"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(chipBg),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                DateUtils.toPersianDigits(initial),
                style = AppTypography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = chipFg,
            )
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(row.name, fontWeight = FontWeight.SemiBold, style = AppTypography.cardTitle, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(row.roleLabel, style = AppTypography.labelSmall, color = DfThemeColors.textMuted())
        }
        DfBadge(text = row.statusLabel, color = chipBg, textColor = chipFg)
    }
}

@Composable
fun ContractDetailSignatureRow(
    row: ContractSignatureRowDto,
    onResend: (() -> Unit)?,
) {
    val (chipBg, chipFg) = contractStatusThemeColors(row.statusLabel)
    val signed = row.accepted || row.statusLabel.contains("امض") || row.statusLabel.contains("تأیید")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (signed) DfIcons.CircleCheck else DfIcons.Clock,
            contentDescription = null,
            tint = if (signed) DfThemeColors.success() else DfThemeColors.textMuted(),
            modifier = Modifier.size(20.dp),
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("${row.roleLabel} · ${row.name}", style = AppTypography.labelLarge, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(row.statusLabel, style = AppTypography.labelSmall, color = chipFg)
        }
        if (onResend != null) {
            DfTextButton(text = "ارسال مجدد", onClick = onResend, compact = true)
        } else {
            DfBadge(text = row.statusLabel, color = chipBg, textColor = chipFg)
        }
    }
}

@Composable
fun ContractDetailExpandableSection(
    title: String,
    icon: ImageVector,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    badge: String? = null,
    accent: Color = DfThemeColors.primary(),
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        color = DfThemeColors.surface(),
        border = BorderStroke(1.dp, DfThemeColors.outlineSubtle()),
        shadowElevation = if (expanded) AppElevations.subtle else AppElevations.none,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(AppShapes.IconContainer)
                        .background(accent.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(title, style = AppTypography.labelLarge, fontWeight = FontWeight.Bold, color = DfThemeColors.textPrimary())
                    subtitle?.let {
                        Text(it, style = AppTypography.labelSmall, color = DfThemeColors.textMuted(), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                badge?.let {
                    DfBadge(
                        text = it,
                        color = DfThemeColors.surfaceVariant(),
                        textColor = DfThemeColors.textSecondary(),
                    )
                }
                Icon(
                    imageVector = if (expanded) DfIcons.ChevronUp else DfIcons.ChevronDown,
                    contentDescription = null,
                    tint = DfThemeColors.textMuted(),
                    modifier = Modifier.size(18.dp),
                )
            }
            if (expanded) {
                HorizontalDivider(color = DfThemeColors.outlineSubtle())
                Column(
                    modifier = Modifier.padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
fun ContractDetailClauseRow(clause: ContractClauseSummaryDto, index: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(DfThemeColors.primaryContainer()),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                DateUtils.toPersianDigits(index.toString()),
                style = AppTypography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = DfThemeColors.primary(),
            )
        }
        Text(
            clause.title,
            style = AppTypography.bodyDescription,
            color = DfThemeColors.textSecondary(),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
fun ContractDetailHandoverBlock(
    exists: Boolean,
    statusLabel: String,
    summary: String,
    actionLabel: String?,
    onAction: () -> Unit,
) {
    if (!exists) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(DfIcons.ClipboardList, contentDescription = null, tint = DfThemeColors.textMuted())
                Text("صورت‌جلسه تحویل هنوز ثبت نشده", style = AppTypography.bodyDescription, color = DfThemeColors.textMuted())
            }
            if (!actionLabel.isNullOrBlank()) {
                DfPrimaryButton(text = actionLabel, onClick = onAction, modifier = Modifier.fillMaxWidth())
            }
        }
        return
    }
    Surface(
        shape = AppShapes.CardSmall,
        color = DfThemeColors.successContainer().copy(alpha = 0.35f),
        border = BorderStroke(1.dp, DfThemeColors.success().copy(alpha = 0.25f)),
    ) {
        Column(Modifier.padding(AppSpacing.sm), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(statusLabel, style = AppTypography.labelLarge, fontWeight = FontWeight.SemiBold, color = DfThemeColors.success())
            if (summary.isNotBlank()) {
                Text(summary, style = AppTypography.bodyDescription, color = DfThemeColors.textSecondary())
            }
            if (!actionLabel.isNullOrBlank()) {
                DfSecondaryButton(text = actionLabel, onClick = onAction, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
fun ContractDetailVersionRow(ver: ContractVersionSummaryDto) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(DfIcons.Layers, contentDescription = null, tint = DfThemeColors.textMuted(), modifier = Modifier.size(16.dp))
            Text(
                "نسخه ${DateUtils.toPersianDigits(ver.versionNumber.toString())}",
                style = AppTypography.labelLarge,
                fontWeight = FontWeight.Medium,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(ver.statusLabel, style = AppTypography.labelSmall, color = DfThemeColors.textMuted())
            if (ver.isCurrent) {
                DfBadge(text = "فعلی", color = DfThemeColors.primaryContainer(), textColor = DfThemeColors.primary())
            }
        }
    }
}

@Composable
fun ContractDetailTimelineItem(
    event: ContractTimelineItemDto,
    isLast: Boolean,
) {
    val whenText = event.createdAt?.let { iso ->
        DateUtils.parseInstantMillis(iso)?.let(DateUtils::formatJalaliDateTimeLatinFromMillis)
    } ?: ""
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(DfThemeColors.primary()),
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .weight(1f)
                        .background(DfThemeColors.outlineSubtle()),
                )
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 0.dp else AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(event.label, style = AppTypography.labelLarge, fontWeight = FontWeight.SemiBold, color = DfThemeColors.textPrimary())
            if (whenText.isNotBlank()) {
                Text(whenText, style = AppTypography.labelSmall, color = DfThemeColors.textMuted())
            }
            if (event.detail.isNotBlank()) {
                Text(event.detail, style = AppTypography.bodyDescription, color = DfThemeColors.textSecondary())
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContractDetailAttachmentRow(
    att: ContractAttachmentDto,
    busy: Boolean,
    onOpen: () -> Unit,
    onDownload: () -> Unit,
    onShare: () -> Unit,
) {
    var menu by remember { mutableStateOf(false) }
    Surface(
        onClick = onOpen,
        enabled = !busy,
        shape = AppShapes.CardSmall,
        color = DfThemeColors.surfaceVariant().copy(alpha = 0.35f),
        border = BorderStroke(1.dp, DfThemeColors.outlineSubtle()),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(AppSpacing.sm),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(AppShapes.IconContainer)
                    .background(DfThemeColors.primaryContainer().copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(DfDecorIcons.FileText),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                )
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    att.title.ifBlank { "پیوست" },
                    style = AppTypography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    "${att.mimeType} · ${formatAttachmentSize(att.fileSize)}",
                    style = AppTypography.labelSmall,
                    color = DfThemeColors.textMuted(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Box {
                IconButton(onClick = { menu = true }) {
                    Icon(DfIcons.MoreVertical, contentDescription = null, tint = DfThemeColors.textMuted())
                }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    DropdownMenuItem(text = { Text("باز کردن") }, onClick = { menu = false; onOpen() })
                    DropdownMenuItem(text = { Text("دانلود") }, onClick = { menu = false; onDownload() })
                    DropdownMenuItem(text = { Text("اشتراک") }, onClick = { menu = false; onShare() })
                }
            }
        }
    }
    if (busy) {
        LinearProgressIndicator(
            Modifier.fillMaxWidth(),
            color = DfThemeColors.primary(),
            trackColor = DfThemeColors.primaryContainer(),
        )
    }
}

@Composable
fun ContractDetailManageAction(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    destructive: Boolean = false,
) {
    val tint = if (destructive) DfThemeColors.error() else DfThemeColors.primary()
    ContractDetailLinkRow(
        icon = icon,
        title = title,
        subtitle = subtitle,
        onClick = onClick,
        enabled = enabled,
        iconTint = tint,
    )
}

@Composable
private fun ContractDetailLinkRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    iconTint: Color = DfThemeColors.primary(),
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(AppShapes.CardSmall)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = AppSpacing.xs),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(AppShapes.IconContainer)
                .background(iconTint.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = AppTypography.labelLarge, fontWeight = FontWeight.SemiBold, color = DfThemeColors.textPrimary())
            Text(subtitle, style = AppTypography.labelSmall, color = DfThemeColors.textMuted())
        }
        Icon(DfIcons.ChevronLeft, contentDescription = null, tint = DfThemeColors.textMuted(), modifier = Modifier.size(18.dp))
    }
}

@Composable
fun ContractDetailLabeledRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Text(label, style = AppTypography.labelSmall, color = DfThemeColors.textMuted(), modifier = Modifier.weight(0.4f))
        Text(
            value,
            style = AppTypography.labelLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(0.6f),
            color = DfThemeColors.textPrimary(),
        )
    }
}

@Composable
internal fun contractStatusThemeColors(statusLabel: String): Pair<Color, Color> {
    val label = statusLabel.trim()
    return when {
        label.contains("پیش") -> DfThemeColors.warningContainer() to DfThemeColors.warning()
        label.contains("نهای") || label.contains("امض") -> DfThemeColors.successContainer() to DfThemeColors.success()
        label.contains("انتظار") -> DfThemeColors.infoContainer() to DfThemeColors.info()
        label.contains("لغو") -> DfThemeColors.errorContainer() to DfThemeColors.error()
        else -> DfThemeColors.primaryContainer() to DfThemeColors.primary()
    }
}

@DrawableRes
fun contractDetailTypeIconRes(contractType: String): Int = when (contractType) {
    "lease" -> DfDecorIcons.House
    "jv" -> DfDecorIcons.Layers
    else -> DfDecorIcons.Handshake
}

private fun formatAttachmentSize(bytes: Long): String {
    if (bytes < 1024) return "${DateUtils.toPersianDigits(bytes.toString())} بایت"
    val kb = bytes / 1024.0
    if (kb < 1024) return "${DateUtils.toPersianDigits(kb.toInt().toString())} کیلوبایت"
    val mb = kb / 1024.0
    return "${DateUtils.toPersianDigits(String.format("%.1f", mb))} مگابایت"
}
