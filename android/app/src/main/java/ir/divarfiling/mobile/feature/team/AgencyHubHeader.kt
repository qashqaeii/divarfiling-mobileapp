package ir.divarfiling.mobile.feature.team

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfAsyncImage
import ir.divarfiling.mobile.core.design.components.DfBadge
import ir.divarfiling.mobile.core.design.components.DfHubBackButton
import ir.divarfiling.mobile.core.image.ImageUrlFormatter

@Composable
fun AgencyHubHeader(
    agencyName: String,
    logoUrl: String?,
    roleLabel: String,
    unreadTotal: Int,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val accent = teamAccentFor(agencyName)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = teamHorizontalPadding())
            .padding(top = AppSpacing.xs, bottom = AppSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onBack != null) {
                DfHubBackButton(onClick = onBack, compact = true)
            } else {
                Box(modifier = Modifier.size(40.dp))
            }
            DfBadge(
                text = "میزکار آژانس",
                color = DfThemeColors.primaryContainer(),
                textColor = DfThemeColors.primary(),
            )
            Box(modifier = Modifier.size(40.dp))
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(AppShapes.Hero)
                .background(
                    Brush.linearGradient(
                        listOf(
                            accent.copy(alpha = 0.16f),
                            DfThemeColors.surface().copy(alpha = 0.98f),
                        ),
                    ),
                )
                .border(BorderStroke(1.dp, DfThemeColors.outlineSubtle()), AppShapes.Hero)
                .padding(AppSpacing.md),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TeamAgencyLogo(
                    agencyName = agencyName,
                    logoUrl = logoUrl,
                    size = 56.dp,
                    accent = accent,
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        agencyName,
                        style = AppTypography.pageTitle,
                        fontWeight = FontWeight.Bold,
                        color = DfThemeColors.textPrimary(),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        roleLabel.ifBlank { "عضو آژانس" },
                        style = AppTypography.bodyDescription,
                        color = DfThemeColors.textSecondary(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                        DfBadge(
                            text = roleLabel.ifBlank { "عضو" },
                            color = accent.copy(alpha = 0.14f),
                            textColor = accent,
                        )
                        if (unreadTotal > 0) {
                            DfBadge(
                                text = "$unreadTotal اعلان",
                                color = DfThemeColors.errorContainer(),
                                textColor = DfThemeColors.error(),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TeamAgencyLogo(
    agencyName: String,
    logoUrl: String?,
    size: androidx.compose.ui.unit.Dp,
    accent: androidx.compose.ui.graphics.Color = teamAccentFor(agencyName),
    modifier: Modifier = Modifier,
) {
    val resolvedUrl = remember(logoUrl) { ImageUrlFormatter.normalize(logoUrl) }
    val shape = RoundedCornerShape(14.dp)
    if (resolvedUrl != null) {
        DfAsyncImage(
            url = resolvedUrl,
            modifier = modifier
                .size(size)
                .border(BorderStroke(1.dp, DfThemeColors.outlineSubtle()), shape),
            shape = shape,
            contentDescription = agencyName,
        )
    } else {
        TeamAvatar(
            name = agencyName,
            size = size,
            accent = accent,
            modifier = modifier,
        )
    }
}
