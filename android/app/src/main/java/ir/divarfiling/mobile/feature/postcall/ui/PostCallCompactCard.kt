package ir.divarfiling.mobile.feature.postcall.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.network.PostCallContactBriefDto
import ir.divarfiling.mobile.feature.postcall.PostCallCallMetadata
import ir.divarfiling.mobile.feature.postcall.PostCallUiCopy
import ir.divarfiling.mobile.feature.postcall.PostCallUiMode

@Composable
fun PostCallCompactCard(
    metadata: PostCallCallMetadata,
    mode: PostCallUiMode,
    contact: PostCallContactBriefDto?,
    displayName: String?,
    onDismiss: () -> Unit,
    onPrimaryAction: () -> Unit,
    onRetryResolve: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val visual = PostCallCallKindVisuals.forKind(metadata.callEndKind)
    val phone = metadata.phone
    val title = displayName?.takeIf { it.isNotBlank() } ?: phone
    val showPhoneSubtitle = displayName != null && displayName.isNotBlank()
    val statusLabel = PostCallUiCopy.crmStatusLabel(mode)
    val contextLine = PostCallUiCopy.formatCallContextLine(metadata)
    val primaryText = PostCallUiCopy.compactPrimaryCta(mode, metadata.callEndKind)
    val isLoading = mode == PostCallUiMode.Loading
    val primaryEnabled = mode != PostCallUiMode.Invalid && !isLoading
    val onPrimary = when (mode) {
        PostCallUiMode.Error -> onRetryResolve
        PostCallUiMode.Invalid -> onDismiss
        else -> onPrimaryAction
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(22.dp), clip = false),
        shape = RoundedCornerShape(22.dp),
        color = DfThemeColors.surface(),
        tonalElevation = 1.dp,
        shadowElevation = 0.dp,
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .align(Alignment.CenterStart)
                    .width(5.dp)
                    .background(
                        visual.accent,
                        RoundedCornerShape(topStart = 22.dp, bottomStart = 22.dp),
                    ),
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = AppSpacing.sm + 5.dp, end = AppSpacing.md, top = AppSpacing.md, bottom = AppSpacing.md),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = visual.container,
                ) {
                    Text(
                        visual.kindLabel,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = AppTypography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = visual.onContainer,
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        modifier = Modifier.size(48.dp).clip(CircleShape),
                        color = visual.accent.copy(alpha = 0.14f),
                    ) {
                        Icon(
                            visual.icon,
                            contentDescription = null,
                            tint = visual.accent,
                            modifier = Modifier.padding(12.dp),
                        )
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = AppSpacing.sm),
                    ) {
                        Text(
                            title,
                            style = AppTypography.cardTitle,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (showPhoneSubtitle) {
                            Text(
                                phone,
                                style = AppTypography.labelSmall,
                                color = DfThemeColors.textSecondary(),
                                maxLines = 1,
                            )
                        }
                        Text(
                            contextLine,
                            style = AppTypography.labelSmall,
                            color = DfThemeColors.textSecondary(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = AppSpacing.xxs),
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = statusBadgeColor(mode, visual),
                        ) {
                            Text(
                                statusLabel,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = AppTypography.labelSmall,
                                color = statusBadgeTextColor(mode),
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(DfIcons.X, contentDescription = "بستن", tint = DfThemeColors.textSecondary())
                        }
                    }
                }
                contact?.customerType?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        "نقش: $it",
                        style = AppTypography.labelSmall,
                        color = visual.accent,
                        fontWeight = FontWeight.Medium,
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = AppSpacing.sm),
                            color = visual.accent,
                        )
                    } else {
                        DfPrimaryButton(
                            text = primaryText,
                            onClick = onPrimary,
                            enabled = primaryEnabled,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (mode != PostCallUiMode.Invalid && mode != PostCallUiMode.Error) {
                        TextButton(onClick = onDismiss) {
                            Text("بعداً", style = AppTypography.labelLarge, color = DfThemeColors.textSecondary())
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun statusBadgeColor(mode: PostCallUiMode, visual: PostCallCallKindVisual) = when (mode) {
    PostCallUiMode.Existing -> visual.container
    PostCallUiMode.New -> DfThemeColors.surfaceVariant().copy(alpha = 0.7f)
    PostCallUiMode.Ambiguous -> DfThemeColors.surfaceVariant().copy(alpha = 0.7f)
    PostCallUiMode.Error -> DfThemeColors.errorContainer()
    PostCallUiMode.Invalid -> DfThemeColors.errorContainer()
    PostCallUiMode.Loading -> DfThemeColors.surfaceVariant().copy(alpha = 0.5f)
}

@Composable
private fun statusBadgeTextColor(mode: PostCallUiMode) = when (mode) {
    PostCallUiMode.Existing -> DfThemeColors.textSecondary()
    PostCallUiMode.Error, PostCallUiMode.Invalid -> DfThemeColors.onError()
    else -> DfThemeColors.textSecondary()
}
