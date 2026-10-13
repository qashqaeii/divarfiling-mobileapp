package ir.divarfiling.mobile.feature.team

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfSecondaryButton
import ir.divarfiling.mobile.core.network.AgencyAdvisorDto
import ir.divarfiling.mobile.core.network.AgencyAvailableLicenseDto
import ir.divarfiling.mobile.core.network.TeamMemberDto

fun AgencyAdvisorDto.toTeamMemberDto(): TeamMemberDto = TeamMemberDto(
    id = id,
    userId = userId,
    name = name,
    phone = phone,
    role = role,
    roleLabel = roleLabel,
    title = title,
    isActive = isActive,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgencySeatOccupiedRow(
    member: AgencyAdvisorDto,
    onRelease: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = roleAccent(member.role)
    val seatState = member.seatAssignmentState()
    val (statusBg, statusFg) = when (seatState) {
        AgencySeatAssignmentState.Active,
        AgencySeatAssignmentState.Unknown,
        -> DfThemeColors.successContainer() to DfThemeColors.success()
        AgencySeatAssignmentState.Expired -> DfThemeColors.errorContainer() to DfThemeColors.error()
        AgencySeatAssignmentState.Unassigned -> DfThemeColors.warningContainer() to DfThemeColors.warning()
    }
    val sequenceLabel = member.sequence?.let { DateUtils.toPersianDigits(it.toString()) } ?: "—"
    val expiry = member.seatExpiryLabel()

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
                    Brush.horizontalGradient(
                        listOf(accent.copy(alpha = 0.12f), Color.Transparent),
                    ),
                )
                .padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AgencySeatSequenceChip(sequence = sequenceLabel, tint = accent)
                DfBadge(
                    text = member.seatStatusBadgeLabel(),
                    color = statusBg,
                    textColor = statusFg,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TeamAvatar(name = member.name, size = 52.dp, accent = accent)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        member.name,
                        style = AppTypography.cardTitle,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        listOfNotNull(
                            member.roleLabel.ifBlank { member.role }.takeIf { it.isNotBlank() },
                            member.title.takeIf { it.isNotBlank() },
                        ).joinToString(" · "),
                        style = AppTypography.labelSmall,
                        color = DfThemeColors.textSecondary(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (member.phone.isNotBlank()) {
                        Text(
                            DateUtils.toPersianDigits(member.phone),
                            style = AppTypography.labelSmall,
                            color = DfThemeColors.textMuted(),
                            maxLines = 1,
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (member.plan.isNotBlank()) {
                    DfBadge(
                        text = "پلن ${member.plan}",
                        color = DfThemeColors.primaryContainer(),
                        textColor = DfThemeColors.primary(),
                    )
                }
                expiry?.let {
                    Text(
                        it,
                        style = AppTypography.labelSmall,
                        color = DfThemeColors.textMuted(),
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                } ?: Spacer(Modifier.weight(1f))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onRelease) {
                    Icon(
                        imageVector = DfIcons.LogOut,
                        contentDescription = null,
                        tint = DfThemeColors.error(),
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "آزادسازی صندلی",
                        color = DfThemeColors.error(),
                        fontWeight = FontWeight.SemiBold,
                        style = AppTypography.labelLarge,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgencySeatFreeRow(
    license: AgencyAvailableLicenseDto,
    onAssign: () -> Unit,
    modifier: Modifier = Modifier,
    unassignedCount: Int = 0,
) {
    val primary = DfThemeColors.primary()
    val sequenceLabel = DateUtils.toPersianDigits(license.sequence.toString())
    val hint = when {
        unassignedCount <= 0 -> "ابتدا مشاور بدون صندلی دعوت کنید"
        unassignedCount == 1 -> "یک مشاور آماده تخصیص است"
        else -> "${DateUtils.toPersianDigits(unassignedCount.toString())} مشاور بدون صندلی"
    }

    Surface(
        onClick = onAssign,
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Hero,
        color = DfThemeColors.surface(),
        border = BorderStroke(1.5.dp, primary.copy(alpha = 0.35f)),
        shadowElevation = AppElevations.subtle,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(primary.copy(alpha = 0.08f), Color.Transparent),
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
                        .size(52.dp)
                        .clip(AppShapes.IconContainer)
                        .background(primary.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("صندلی", style = AppTypography.labelSmall, color = primary)
                        Text(
                            sequenceLabel,
                            style = AppTypography.cardTitle,
                            fontWeight = FontWeight.Bold,
                            color = primary,
                        )
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        license.plan.ifBlank { "پلن آژانس" },
                        style = AppTypography.cardTitle,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        hint,
                        style = AppTypography.labelSmall,
                        color = DfThemeColors.textSecondary(),
                        maxLines = 2,
                    )
                }
                DfBadge(
                    text = "آزاد",
                    color = primary.copy(alpha = 0.14f),
                    textColor = primary,
                )
            }
            DfPrimaryButton(
                text = "تخصیص به مشاور",
                onClick = onAssign,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgencySeatLicensePickCard(
    license: AgencyAvailableLicenseDto,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val primary = DfThemeColors.primary()
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.CardSmall,
        color = DfThemeColors.surface(),
        border = BorderStroke(1.dp, DfThemeColors.outlineSubtle()),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.sm),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AgencySeatSequenceChip(
                sequence = DateUtils.toPersianDigits(license.sequence.toString()),
                tint = primary,
                compact = true,
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    license.plan.ifBlank { "پلن آژانس" },
                    style = AppTypography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                )
                Text(
                    "صندلی آزاد · آماده تخصیص",
                    style = AppTypography.labelSmall,
                    color = DfThemeColors.textMuted(),
                )
            }
            Icon(
                imageVector = DfIcons.ChevronLeft,
                contentDescription = null,
                tint = DfThemeColors.textMuted(),
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
fun AgencyMemberWithoutSeatRow(
    member: AgencyAdvisorDto,
    onAssign: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = roleAccent(member.role)
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.CardSmall,
        color = DfThemeColors.surface(),
        border = BorderStroke(1.dp, DfThemeColors.outlineSubtle()),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.sm),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TeamAvatar(name = member.name, size = 44.dp, accent = accent)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    member.name,
                    style = AppTypography.cardTitle,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    member.roleLabel.ifBlank { "مشاور" },
                    style = AppTypography.labelSmall,
                    color = DfThemeColors.textSecondary(),
                )
            }
            DfBadge(
                text = "بدون صندلی",
                color = DfThemeColors.warningContainer(),
                textColor = DfThemeColors.warning(),
            )
            DfSecondaryButton(text = "تخصیص", onClick = onAssign)
        }
    }
}

@Composable
private fun AgencySeatSequenceChip(
    sequence: String,
    tint: Color,
    compact: Boolean = false,
) {
    val padV = if (compact) 6.dp else 8.dp
    Column(
        modifier = Modifier
            .clip(AppShapes.CardSmall)
            .background(tint.copy(alpha = 0.14f))
            .padding(horizontal = AppSpacing.sm, vertical = padV),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (!compact) {
            Text("صندلی", style = AppTypography.labelSmall, color = tint)
        }
        Text(
            sequence,
            style = if (compact) AppTypography.labelLarge else AppTypography.cardTitle,
            fontWeight = FontWeight.Bold,
            color = tint,
        )
    }
}
