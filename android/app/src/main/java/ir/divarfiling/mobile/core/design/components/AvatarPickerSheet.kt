package ir.divarfiling.mobile.core.design.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.avatar.AvatarCatalog
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvatarPickerSheet(
    visible: Boolean,
    selectedKey: String?,
    canSave: Boolean,
    isSaving: Boolean,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
) {
    if (!visible) return
    val columns = if (LocalConfiguration.current.screenWidthDp >= 400) 4 else 3
    val effectiveSelection = AvatarCatalog.normalizeKey(selectedKey ?: AvatarCatalog.DEFAULT_KEY)
    val gridHeight = (LocalConfiguration.current.screenHeightDp * 0.34f).coerceIn(220f, 320f).dp

    DfModalBottomSheet(onDismissRequest = onDismiss) {
        DfSheetScaffold(
            title = "انتخاب آواتار",
            subtitle = "یک تصویر برای پروفایل خود انتخاب کنید",
            icon = DfIcons.User,
            onClose = onDismiss,
            scrollable = false,
            bodyHeightFraction = 0.78f,
            footer = {
                DfSheetActions(
                    primaryText = if (isSaving) "در حال ذخیره…" else "ذخیره تغییرات",
                    onPrimary = onSave,
                    primaryEnabled = canSave && !isSaving,
                    isSubmitting = isSaving,
                    onSecondary = onDismiss,
                )
            },
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = AppShapes.Card,
                color = DfThemeColors.surfaceVariant().copy(alpha = 0.45f),
                border = BorderStroke(1.dp, DfThemeColors.outlineSubtle()),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = AppSpacing.lg, horizontal = AppSpacing.md),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                ) {
                    Text(
                        text = "پیش‌نمایش",
                        style = AppTypography.labelSmall,
                        color = DfThemeColors.textMuted(),
                    )
                    Crossfade(
                        targetState = effectiveSelection,
                        label = "avatarPickerHero",
                    ) { key ->
                        AppPresetAvatar(
                            drawableRes = AvatarCatalog.drawableForKey(key),
                            size = AppAvatarSize.Hero,
                            selected = false,
                            contentDescription = "پیش‌نمایش آواتار",
                        )
                    }
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(gridHeight),
                contentPadding = PaddingValues(
                    top = AppSpacing.xs,
                    bottom = AppSpacing.md,
                ),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                items(AvatarCatalog.allKeys, key = { it }) { key ->
                    val index = AvatarCatalog.allKeys.indexOf(key) + 1
                    AvatarPickerGridCell(
                        avatarKey = key,
                        index = index,
                        selected = key == effectiveSelection,
                        onClick = { onSelect(key) },
                    )
                }
            }

            Text(
                text = "آواتار انتخاب‌شده با حاشیه بنفش مشخص شده است",
                style = AppTypography.labelSmall,
                color = DfThemeColors.textMuted(),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun AvatarPickerGridCell(
    avatarKey: String,
    index: Int,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1f else 0.96f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "avatarCellScale",
    )
    val a11yLabel = "آواتار شماره $index، ${if (selected) "انتخاب‌شده" else "انتخاب نشده"}"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                role = Role.Button
                contentDescription = a11yLabel
                stateDescription = if (selected) "انتخاب‌شده" else "انتخاب نشده"
            },
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            onClick = onClick,
            shape = CircleShape,
            color = if (selected) DfColors.Purple.copy(alpha = 0.08f) else Color.Transparent,
            border = if (selected) {
                BorderStroke(2.dp, DfColors.Purple)
            } else {
                BorderStroke(1.dp, DfThemeColors.outlineSubtle().copy(alpha = 0.65f))
            },
            modifier = Modifier
                .sizeIn(minWidth = 72.dp, minHeight = 72.dp)
                .padding(4.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
            ) {
                AppPresetAvatar(
                    drawableRes = AvatarCatalog.drawableForKey(avatarKey),
                    size = AppAvatarSize.Picker,
                    selected = false,
                    contentDescription = a11yLabel,
                    modifier = Modifier.graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    },
                )
                AnimatedVisibility(
                    visible = selected,
                    enter = scaleIn(initialScale = 0.85f) + fadeIn(),
                    exit = scaleOut(targetScale = 0.85f) + fadeOut(),
                    modifier = Modifier.align(Alignment.BottomEnd),
                ) {
                    Surface(
                        shape = CircleShape,
                        color = DfColors.Purple,
                        border = BorderStroke(1.5.dp, DfThemeColors.surface()),
                        modifier = Modifier.size(20.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                DfIcons.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(11.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
