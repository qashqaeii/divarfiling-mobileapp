package ir.divarfiling.mobile.core.design.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DfModalBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    content: @Composable ColumnScope.() -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = modifier,
        containerColor = DfColors.Surface,
        shape = AppShapes.Sheet,
        dragHandle = { DfSheetDragHandle() },
        tonalElevation = 0.dp,
        scrimColor = Color.Black.copy(alpha = 0.45f),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding(),
            content = content,
        )
    }
}

@Composable
fun DfSheetDragHandle(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = AppSpacing.sm, bottom = AppSpacing.xs),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .clip(CircleShape)
                .background(DfColors.Outline.copy(alpha = 0.45f)),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            DfColors.Purple.copy(alpha = 0.35f),
                            Color.Transparent,
                        ),
                    ),
                ),
        )
    }
}

@Composable
fun DfSheetScaffold(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    @DrawableRes iconRes: Int? = null,
    iconContainerColor: Color = DfColors.PurpleContainer,
    iconTint: Color = DfColors.Purple,
    onClose: (() -> Unit)? = null,
    scrollable: Boolean = true,
    footer: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val configuration = LocalConfiguration.current
    val maxBodyHeight = (configuration.screenHeightDp * 0.52f).dp
    val scrollState = rememberScrollState()
    Column(modifier = modifier.fillMaxWidth()) {
        DfSheetHeader(
            title = title,
            subtitle = subtitle,
            icon = icon,
            iconRes = iconRes,
            iconContainerColor = iconContainerColor,
            iconTint = iconTint,
            onClose = onClose,
        )
        val bodyModifier = Modifier
            .fillMaxWidth()
            .then(
                when {
                    scrollable -> Modifier
                        .heightIn(max = maxBodyHeight)
                        .verticalScroll(scrollState)
                    else -> Modifier
                },
            )
            .padding(horizontal = AppSpacing.lg)
            .padding(bottom = if (footer == null) AppSpacing.xl else 0.dp)
        Column(
            modifier = bodyModifier,
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
            content = content,
        )
        footer?.let {
            DfSheetFooter(content = it)
        }
    }
}

@Composable
fun DfSheetHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    @DrawableRes iconRes: Int? = null,
    iconContainerColor: Color = DfColors.PurpleContainer,
    iconTint: Color = DfColors.Purple,
    onClose: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.lg)
            .padding(bottom = AppSpacing.md),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        verticalAlignment = Alignment.Top,
    ) {
        when {
            iconRes != null -> DfDecorIconBox(
                resId = iconRes,
                containerSize = 44.dp,
                imageSize = 28.dp,
                background = iconContainerColor,
            )
            icon != null -> Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(AppShapes.IconContainer)
                    .background(iconContainerColor),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = AppTypography.sectionTitle,
                fontWeight = FontWeight.Bold,
                color = DfColors.TextPrimary,
            )
            subtitle?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    style = AppTypography.bodyDescription,
                    color = DfColors.TextMuted,
                )
            }
        }
        if (onClose != null) {
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(DfColors.SurfaceVariant),
            ) {
                Icon(
                    imageVector = DfIcons.X,
                    contentDescription = "بستن",
                    tint = DfColors.TextMuted,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
fun DfSheetSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Text(
            text = title,
            style = AppTypography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = DfColors.Purple,
        )
        content()
    }
}

@Composable
fun DfSheetFooter(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalDivider(color = DfColors.Outline.copy(alpha = 0.25f))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(DfColors.SurfaceVariant.copy(alpha = 0.35f))
                .padding(horizontal = AppSpacing.lg)
                .padding(top = AppSpacing.md, bottom = AppSpacing.xl),
        ) {
            content()
        }
    }
}

@Composable
fun DfSheetActions(
    primaryText: String,
    onPrimary: () -> Unit,
    modifier: Modifier = Modifier,
    primaryEnabled: Boolean = true,
    isSubmitting: Boolean = false,
    secondaryText: String = "انصراف",
    onSecondary: () -> Unit,
    destructive: Boolean = false,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (destructive) {
            Surface(
                onClick = onPrimary,
                enabled = primaryEnabled && !isSubmitting,
                modifier = Modifier.weight(1f),
                shape = AppShapes.Button,
                color = DfColors.OverdueAccent,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (isSubmitting) "لطفاً صبر کنید…" else primaryText,
                        style = AppTypography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
            }
        } else {
            DfPrimaryButton(
                text = if (isSubmitting) "لطفاً صبر کنید…" else primaryText,
                onClick = onPrimary,
                enabled = primaryEnabled && !isSubmitting,
                loading = isSubmitting,
                modifier = Modifier.weight(1f),
            )
        }
        DfGlassTextButton(text = secondaryText, onClick = onSecondary)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DfConfirmBottomSheet(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    confirmText: String = "تأیید",
    cancelText: String = "انصراف",
    destructive: Boolean = false,
    isSubmitting: Boolean = false,
    icon: ImageVector? = if (destructive) DfIcons.X else DfIcons.CircleCheck,
) {
    DfModalBottomSheet(onDismissRequest = onDismiss, modifier = modifier) {
        DfSheetScaffold(
            title = title,
            subtitle = message,
            icon = icon,
            iconContainerColor = if (destructive) DfColors.RoseLight else DfColors.PurpleContainer,
            iconTint = if (destructive) DfColors.OverdueAccent else DfColors.Purple,
            onClose = onDismiss,
            scrollable = false,
            footer = {
                DfSheetActions(
                    primaryText = confirmText,
                    onPrimary = onConfirm,
                    primaryEnabled = !isSubmitting,
                    isSubmitting = isSubmitting,
                    secondaryText = cancelText,
                    onSecondary = onDismiss,
                    destructive = destructive,
                )
            },
        ) {
            Spacer(modifier = Modifier.height(AppSpacing.xs))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DfSheetOptionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    trailing: String? = null,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Chip,
        color = if (selected) DfColors.PurpleContainer.copy(alpha = 0.65f) else DfColors.SurfaceVariant.copy(alpha = 0.5f),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.sm),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (selected) DfColors.Purple else DfColors.TextMuted,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Text(
                    text = label,
                    style = AppTypography.bodyDescription,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    color = if (selected) DfColors.Purple else DfColors.TextPrimary,
                )
            }
            trailing?.let {
                Text(
                    text = it,
                    style = AppTypography.labelSmall,
                    color = if (selected) DfColors.Purple else DfColors.TextMuted,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.End,
                )
            }
        }
    }
}

data class DfMoreAction(
    val label: String,
    val onClick: () -> Unit,
    val icon: ImageVector? = null,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DfMoreActionsSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    actions: List<DfMoreAction>,
    title: String = "اقدامات بیشتر",
) {
    if (!visible || actions.isEmpty()) return
    DfModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.md)
                .padding(bottom = AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            Text(
                text = title,
                style = AppTypography.cardTitle,
                fontWeight = FontWeight.SemiBold,
            )
            actions.forEach { action ->
                DfSheetOptionRow(
                    label = action.label,
                    selected = false,
                    onClick = {
                        onDismiss()
                        action.onClick()
                    },
                    icon = action.icon,
                )
            }
        }
    }
}

