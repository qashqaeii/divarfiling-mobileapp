package ir.divarfiling.mobile.core.design.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.TextButton
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextOverflow
import ir.divarfiling.mobile.R
import ir.divarfiling.mobile.core.design.AppElevations
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfShapes
import ir.divarfiling.mobile.core.design.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DfTopBar(
    title: String,
    showLogo: Boolean = false,
    onBack: (() -> Unit)? = null,
    actions: @Composable () -> Unit = {},
) {
    TopAppBar(
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                }
            }
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (showLogo) {
                    Image(
                        painter = painterResource(R.drawable.logo_divarfiling),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        contentScale = ContentScale.Fit,
                    )
                }
                Text(title, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        },
        actions = { actions() },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = DfColors.TextPrimary,
        ),
        modifier = Modifier
            .padding(horizontal = AppSpacing.xs, vertical = AppSpacing.xxs),
    )
}

@Composable
fun DfCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    containerColor: Color = DfColors.Surface,
    content: @Composable () -> Unit,
) {
    DfPremiumCard(
        modifier = modifier,
        onClick = onClick,
        containerColor = containerColor,
        content = content,
    )
}

@Composable
fun DfPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    val shape = DfShapes.Button
    Box(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 52.dp)
            .shadow(
                elevation = if (enabled && !loading) AppElevations.raised else AppElevations.none,
                shape = shape,
                ambientColor = DfColors.Purple.copy(alpha = 0.3f),
            )
            .clip(shape)
            .background(
                if (enabled && !loading) {
                    Brush.horizontalGradient(listOf(DfColors.PurpleGradientStart, DfColors.PurpleGradientEnd))
                } else {
                    Brush.horizontalGradient(
                        listOf(
                            DfColors.Purple.copy(alpha = 0.4f),
                            DfColors.PurpleDark.copy(alpha = 0.35f),
                        ),
                    )
                },
            )
            .then(
                if (enabled && !loading) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = Color.White,
                strokeWidth = 2.dp,
            )
        } else {
            Text(
                text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
            )
        }
    }
}

@Composable
fun DfScreenGradient(): Brush = Brush.verticalGradient(
    colors = listOf(
        DfColors.PurpleLight.copy(alpha = 0.65f),
        DfColors.Background,
        DfColors.Background,
    ),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DfDropdown(
    label: String,
    value: String,
    options: List<String>,
    enabled: Boolean,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = !expanded },
        modifier = modifier.fillMaxWidth(),
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            enabled = enabled,
            singleLine = true,
            shape = DfShapes.Field,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = DfColors.Purple,
                focusedLabelColor = DfColors.Purple,
            ),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
fun DfProgressBlock(
    current: Int,
    total: Int,
    label: String = "در حال استخراج…",
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = DfColors.TextSecondary)
        if (total > 0) {
            LinearProgressIndicator(
                progress = { current.toFloat() / total },
                modifier = Modifier.fillMaxWidth(),
                color = DfColors.Purple,
            )
            Text(
                "$current / $total آگهی",
                style = MaterialTheme.typography.labelLarge,
                color = DfColors.PurpleDark,
            )
        } else {
            CircularProgressIndicator(color = DfColors.Purple)
        }
    }
}

@Composable
fun DfStatChip(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = DfShapes.Chip,
        colors = CardDefaults.cardColors(containerColor = DfColors.PurpleContainer),
    ) {
        Column(Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            Text(
                value,
                fontWeight = FontWeight.Bold,
                color = DfColors.PurpleDark,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = DfColors.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun DfSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "جستجو…",
    onSearch: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = DfColors.TextMuted) },
        singleLine = true,
        shape = DfShapes.Field,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = DfColors.Purple,
            focusedLabelColor = DfColors.Purple,
        ),
        trailingIcon = onSearch?.let {
            {
                TextButton(onClick = it) { Text("اعمال", color = DfColors.Purple) }
            }
        },
    )
}

@Composable
fun DfEmptyState(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.Inbox,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(AppSpacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        DfGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(DfColors.PurpleContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = DfColors.Purple,
                    )
                }
                Text(
                    title,
                    style = AppTypography.cardTitle,
                    fontWeight = FontWeight.SemiBold,
                    color = DfColors.TextPrimary,
                )
                if (subtitle.isNotBlank()) {
                    Text(
                        subtitle,
                        style = AppTypography.bodyDescription,
                        color = DfColors.TextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                }
                if (actionLabel != null && onAction != null) {
                    DfPrimaryButton(
                        text = actionLabel,
                        onClick = onAction,
                        modifier = Modifier.padding(top = AppSpacing.xs),
                    )
                }
            }
        }
    }
}

@Composable
fun DfBadge(
    text: String,
    color: Color = DfColors.PurpleContainer,
    textColor: Color = DfColors.PurpleDark,
) {
    Surface(
        shape = DfShapes.Chip,
        color = color,
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun DfSectionHeader(title: String, count: Int? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
        count?.let {
            DfBadge(text = "$it مورد", color = DfColors.SurfaceVariant, textColor = DfColors.TextSecondary)
        }
    }
}

@Composable
fun DfErrorBanner(
    message: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = DfShapes.Chip,
        color = DfColors.Rose.copy(alpha = 0.12f),
    ) {
        Text(
            message,
            modifier = Modifier.padding(12.dp),
            color = DfColors.Rose,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DfPullRefresh(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    DfScreenBackground(modifier = modifier) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize(),
        ) {
            content()
        }
    }
}

@Composable
fun DfFab(
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = DfColors.Purple,
        contentColor = Color.White,
        shape = CircleShape,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = AppElevations.floating,
            pressedElevation = AppElevations.raised,
        ),
    ) {
        icon()
    }
}

@Composable
fun DfExtendedFab(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String = text,
) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = DfColors.Purple,
        contentColor = Color.White,
        shape = AppShapes.Hero,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = AppElevations.floating,
            pressedElevation = AppElevations.raised,
        ),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(AppSpacing.xs))
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun DfActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconRes: Int? = null,
    containerColor: Color = DfColors.PurpleContainer,
    contentColor: Color = DfColors.Purple,
    filled: Boolean = false,
) {
    val bg = if (filled) contentColor else containerColor
    val fg = if (filled) Color.White else contentColor
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = DfShapes.Chip,
        color = bg,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            when {
                iconRes != null -> Icon(
                    painter = androidx.compose.ui.res.painterResource(iconRes),
                    contentDescription = null,
                    tint = fg,
                    modifier = Modifier.size(18.dp),
                )
                icon != null -> Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = fg,
                    modifier = Modifier.size(18.dp),
                )
            }
            Text(
                text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = fg,
            )
        }
    }
}

@Composable
fun DfListingRow(
    title: String,
    price: Long? = null,
    deposit: Long? = null,
    rent: Long? = null,
    area: Int? = null,
    rooms: Int? = null,
    district: String? = null,
    advertiserType: String? = null,
    onClick: (() -> Unit)? = null,
) {
    DfCard(onClick = onClick) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                price?.let {
                    DfBadge(
                        text = FormatUtils.formatPriceShort(it),
                        color = DfColors.Purple.copy(alpha = 0.1f),
                        textColor = DfColors.PurpleDark,
                    )
                }
                deposit?.let {
                    DfBadge(
                        text = "ودیعه ${FormatUtils.formatPriceShort(it)}",
                        color = DfColors.Blue.copy(alpha = 0.1f),
                        textColor = DfColors.Blue,
                    )
                }
                rent?.let {
                    DfBadge(
                        text = "اجاره ${FormatUtils.formatPriceShort(it)}",
                        color = DfColors.Green.copy(alpha = 0.12f),
                        textColor = DfColors.Green,
                    )
                }
                area?.let {
                    DfBadge(
                        text = FormatUtils.formatArea(it),
                        color = DfColors.SurfaceVariant,
                        textColor = DfColors.TextSecondary,
                    )
                }
                rooms?.let {
                    DfBadge(
                        text = FormatUtils.formatRooms(it),
                        color = DfColors.SurfaceVariant,
                        textColor = DfColors.TextSecondary,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                district?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = DfColors.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                } ?: Spacer(Modifier.weight(1f))
                advertiserType?.takeIf { it.isNotBlank() }?.let {
                    Spacer(Modifier.width(8.dp))
                    DfBadge(
                        text = it,
                        color = if (it.contains("مشاور")) DfColors.Blue.copy(alpha = 0.12f) else DfColors.Green.copy(alpha = 0.12f),
                        textColor = if (it.contains("مشاور")) DfColors.Blue else DfColors.Green,
                    )
                }
            }
        }
    }
}

@Composable
fun DfContactRow(
    name: String,
    phone: String?,
    status: String?,
    customerType: String?,
    onClick: (() -> Unit)? = null,
) {
    DfCard(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(DfColors.PurpleContainer),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    name.firstOrNull()?.toString() ?: "?",
                    fontWeight = FontWeight.Bold,
                    color = DfColors.PurpleDark,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                phone?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = DfColors.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    status?.let { DfBadge(text = it, color = DfColors.PurpleContainer, textColor = DfColors.PurpleDark) }
                    customerType?.let { DfBadge(text = it, color = DfColors.SurfaceVariant, textColor = DfColors.TextSecondary) }
                }
            }
        }
    }
}
