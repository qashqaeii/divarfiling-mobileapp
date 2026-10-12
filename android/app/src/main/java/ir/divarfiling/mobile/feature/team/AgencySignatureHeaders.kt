package ir.divarfiling.mobile.feature.team

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfBadge
import ir.divarfiling.mobile.core.design.components.DfHubBackButton

enum class AgencyHeaderAccent {
    Dashboard,
    Management,
    Detail,
    Conversation,
}

@Composable
private fun AgencyHeaderAccent.tint(): Color = when (this) {
    AgencyHeaderAccent.Dashboard -> DfThemeColors.primary()
    AgencyHeaderAccent.Management -> DfThemeColors.info()
    AgencyHeaderAccent.Detail -> DfThemeColors.success()
    AgencyHeaderAccent.Conversation -> DfThemeColors.primary()
}

@Composable
fun AgencySectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector = DfIcons.Layers,
    accent: AgencyHeaderAccent = AgencyHeaderAccent.Management,
    action: @Composable (() -> Unit)? = null,
) {
    val tint = accent.tint()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = AppSpacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f),
        ) {
            AgencyHeaderIconContainer(icon = icon, tint = tint)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    title,
                    style = AppTypography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = DfThemeColors.textPrimary(),
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        subtitle,
                        style = AppTypography.labelSmall,
                        color = DfThemeColors.textMuted(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        action?.invoke()
    }
}

@Composable
private fun AgencyHeaderIconContainer(
    icon: ImageVector,
    tint: Color,
    size: Dp = 40.dp,
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(AppShapes.IconContainer)
            .background(tint.copy(alpha = 0.12f))
            .border(BorderStroke(1.dp, tint.copy(alpha = 0.2f)), AppShapes.IconContainer),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
    }
}

@Composable
fun AgencyDashboardHeader(
    title: String,
    roleLabel: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    logoUrl: String? = null,
    statusChips: List<String> = emptyList(),
    trailingBadge: String? = null,
) {
    val accent = teamAccentFor(title)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = teamHorizontalPadding())
            .padding(bottom = AppSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            DfHubBackButton(onClick = onBack, compact = true)
            Text(
                "فضای آژانس",
                style = AppTypography.labelSmall,
                color = DfThemeColors.textMuted(),
                modifier = Modifier.weight(1f),
            )
            trailingBadge?.let {
                DfBadge(
                    text = it,
                    color = DfThemeColors.errorContainer(),
                    textColor = DfThemeColors.error(),
                )
            }
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = AppShapes.Hero,
            color = DfThemeColors.surface(),
            border = BorderStroke(1.dp, DfThemeColors.outlineSubtle()),
            shadowElevation = 1.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(accent.copy(alpha = 0.1f), Color.Transparent),
                        ),
                    )
                    .padding(AppSpacing.md),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TeamAgencyLogo(agencyName = title, logoUrl = logoUrl, size = 52.dp, accent = accent)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        title,
                        style = AppTypography.pageTitle,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        roleLabel.ifBlank { "عضو آژانس" },
                        style = AppTypography.bodyDescription,
                        color = DfThemeColors.textSecondary(),
                        maxLines = 1,
                    )
                    if (statusChips.isNotEmpty()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                            statusChips.take(3).forEach { chip ->
                                DfBadge(
                                    text = chip,
                                    color = DfThemeColors.primaryContainer(),
                                    textColor = DfThemeColors.primary(),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AgencyManagementHeader(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector = DfIcons.Users,
    accent: AgencyHeaderAccent = AgencyHeaderAccent.Management,
    actions: @Composable () -> Unit = {},
) {
    val tint = accent.tint()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = teamHorizontalPadding())
            .padding(bottom = AppSpacing.xs),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            DfHubBackButton(onClick = onBack, compact = true)
            AgencyHeaderIconContainer(icon = icon, tint = tint, size = 36.dp)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    title,
                    style = AppTypography.cardTitle,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        subtitle,
                        style = AppTypography.labelSmall,
                        color = DfThemeColors.textMuted(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            actions()
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = AppSpacing.xs)
                .height(1.dp)
                .background(DfThemeColors.outlineSubtle().copy(alpha = 0.65f)),
        )
    }
}

@Composable
fun AgencyConversationHeader(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onClose: (() -> Unit)? = null,
    actions: @Composable () -> Unit = {},
) {
    AgencyManagementHeader(
        title = title,
        subtitle = subtitle,
        onBack = onClose ?: onBack,
        modifier = modifier,
        icon = DfIcons.MessageCircle,
        accent = AgencyHeaderAccent.Conversation,
        actions = actions,
    )
}

@Composable
fun AgencyDetailProfileHeader(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    avatarName: String = title,
    avatarUrl: String? = null,
    metadataChips: List<String> = emptyList(),
    subtitle: String? = null,
) {
    val accent = teamAccentFor(avatarName)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = teamHorizontalPadding())
            .padding(bottom = AppSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DfHubBackButton(onClick = onBack, compact = true)
            Spacer(modifier = Modifier.weight(1f))
        }
        Surface(
            shape = AppShapes.Hero,
            color = DfThemeColors.surface(),
            border = BorderStroke(1.dp, DfThemeColors.outlineSubtle()),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.padding(AppSpacing.md),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TeamAvatar(name = avatarName, size = 56.dp, accent = accent)
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        title,
                        style = AppTypography.pageTitle,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (!subtitle.isNullOrBlank()) {
                        Text(
                            subtitle,
                            style = AppTypography.bodyDescription,
                            color = DfThemeColors.textSecondary(),
                        )
                    }
                    if (metadataChips.isNotEmpty()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                            metadataChips.take(4).forEach { chip ->
                                DfBadge(
                                    text = chip,
                                    color = DfThemeColors.surfaceVariant(),
                                    textColor = DfThemeColors.textSecondary(),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
