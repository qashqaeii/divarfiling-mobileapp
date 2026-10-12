package ir.divarfiling.mobile.feature.team

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfBadge
import ir.divarfiling.mobile.core.design.components.DfSecondaryButton
import ir.divarfiling.mobile.core.network.AgencyAdvisorDto
import ir.divarfiling.mobile.core.network.AgencyAvailableLicenseDto

@Composable
fun AgencySeatOccupiedRow(
    member: AgencyAdvisorDto,
    onRelease: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AgencySeatRowShell(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AgencySeatNumberBadge(sequence = member.sequence)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    member.name,
                    style = AppTypography.cardTitle,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    listOfNotNull(
                        member.plan.takeIf { it.isNotBlank() }?.let { "پلن $it" },
                        member.roleLabel.ifBlank { member.role }.takeIf { it.isNotBlank() },
                    ).joinToString(" · "),
                    style = AppTypography.labelSmall,
                    color = DfThemeColors.textMuted(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            val seatLabel = member.seatStatusBadgeLabel()
            val (seatBg, seatFg) = when (member.seatAssignmentState()) {
                AgencySeatAssignmentState.Active,
                AgencySeatAssignmentState.Unknown,
                -> DfThemeColors.successContainer() to DfThemeColors.success()
                AgencySeatAssignmentState.Expired -> DfThemeColors.errorContainer() to DfThemeColors.error()
                AgencySeatAssignmentState.Unassigned -> DfThemeColors.warningContainer() to DfThemeColors.warning()
            }
            DfBadge(text = seatLabel, color = seatBg, textColor = seatFg)
            DfSecondaryButton(text = "آزادسازی", onClick = onRelease)
        }
    }
}

@Composable
fun AgencySeatFreeRow(
    license: AgencyAvailableLicenseDto,
    onAssign: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.CardSmall,
        color = DfThemeColors.surface(),
        border = BorderStroke(1.dp, DfThemeColors.outlineSubtle()),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.sm, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(DfThemeColors.primaryContainer(), AppShapes.CardSmall),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = DfIcons.Building,
                    contentDescription = null,
                    tint = DfThemeColors.primary(),
                    modifier = Modifier.size(20.dp),
                )
            }
            AgencySeatNumberBadge(sequence = license.sequence)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    license.plan.ifBlank { "پلن آژانس" },
                    style = AppTypography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                )
                Text(
                    "صندلی آزاد",
                    style = AppTypography.labelSmall,
                    color = DfThemeColors.textMuted(),
                )
            }
            DfBadge(
                text = "آزاد",
                color = DfThemeColors.primaryContainer(),
                textColor = DfThemeColors.primary(),
            )
            TextButton(onClick = onAssign) {
                Text("تخصیص", color = DfThemeColors.primary(), fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun AgencyMemberWithoutSeatRow(
    member: AgencyAdvisorDto,
    onAssign: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AgencySeatRowShell(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TeamAvatar(name = member.name, size = 40.dp, accent = roleAccent(member.role))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(member.name, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
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
private fun AgencySeatNumberBadge(sequence: Int?) {
    val label = sequence?.let { DateUtils.toPersianDigits(it.toString()) } ?: "—"
    Column(
        modifier = Modifier
            .size(44.dp)
            .background(DfThemeColors.primaryContainer(), AppShapes.CardSmall),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("صندلی", style = AppTypography.labelSmall, color = DfThemeColors.primary())
        Text(label, style = AppTypography.cardTitle, fontWeight = FontWeight.Bold, color = DfThemeColors.primary())
    }
}

@Composable
private fun AgencySeatRowShell(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(DfThemeColors.surface(), AppShapes.CardSmall)
            .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.sm),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = DfIcons.Layers,
            contentDescription = null,
            tint = DfThemeColors.textMuted(),
            modifier = Modifier.size(16.dp),
        )
        content()
    }
}
