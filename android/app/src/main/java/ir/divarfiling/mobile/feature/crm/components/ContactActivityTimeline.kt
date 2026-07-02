package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfPremiumCard
import ir.divarfiling.mobile.core.network.ActivityDto

@Composable
fun ContactActivityTimeline(
    activities: List<ActivityDto>,
    modifier: Modifier = Modifier,
) {
    DfPremiumCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 3.dp, height = 16.dp)
                        .clip(AppShapes.Chip)
                        .background(DfColors.Purple),
                )
                Text(
                    text = "تاریخچه فعالیت",
                    style = AppTypography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = DfColors.TextPrimary,
                    modifier = Modifier.weight(1f),
                )
                if (activities.isNotEmpty()) {
                    Surface(shape = AppShapes.Chip, color = DfColors.PurpleContainer) {
                        Text(
                            text = DateUtils.toPersianDigits(activities.size.toString()),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = AppTypography.labelSmall,
                            color = DfColors.Purple,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            if (activities.isEmpty()) {
                DfEmptyState(
                    title = "تایم‌لاین خالی است",
                    subtitle = "تماس، یادداشت یا ثبت فعالیت، تاریخچه را اینجا می‌سازد",
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                    activities.forEachIndexed { index, activity ->
                        ContactTimelineItem(
                            activity = activity,
                            isFirst = index == 0,
                            isLast = index == activities.lastIndex,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ContactTimelineItem(
    activity: ActivityDto,
    isFirst: Boolean,
    isLast: Boolean,
) {
    val typeKey = activity.activityTypeLabel ?: activity.activityType ?: activity.title.orEmpty()
    val (accent, icon) = activityVisual(typeKey)
    val (jalaliDate, jalaliTime) = ContactsFilters.splitUpdatedAt(activity.createdAt)
    val relative = ContactsFilters.relativeUpdatedLabel(activity.createdAt)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(28.dp),
        ) {
            if (!isFirst) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(10.dp)
                        .background(DfColors.Outline.copy(alpha = 0.35f)),
                )
            }
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(14.dp),
                )
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(DfColors.Outline.copy(alpha = 0.35f)),
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 0.dp else AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = activity.title
                            ?: activity.activityTypeLabel
                            ?: activity.activityType
                            ?: "فعالیت",
                        style = AppTypography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = DfColors.TextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    activity.activityTypeLabel?.takeIf {
                        it != activity.title && activity.title != null
                    }?.let {
                        Text(
                            text = it,
                            style = AppTypography.labelSmall,
                            color = accent,
                            maxLines = 1,
                        )
                    }
                }
                if (jalaliDate != "—") {
                    ContactTimelineDateChip(
                        primary = relative ?: jalaliDate,
                        secondary = when {
                            relative != null -> jalaliDate
                            jalaliTime.isNotBlank() -> jalaliTime
                            else -> null
                        },
                    )
                }
            }
            activity.content?.takeIf { it.isNotBlank() }?.let { content ->
                Surface(
                    shape = AppShapes.Chip,
                    color = DfColors.SurfaceVariant.copy(alpha = 0.55f),
                ) {
                    Text(
                        text = content,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        style = AppTypography.bodyDescription,
                        color = DfColors.TextSecondary,
                    )
                }
            }
            activity.customerName?.takeIf { it.isNotBlank() }?.let { name ->
                Text(
                    text = "مخاطب: $name",
                    style = AppTypography.labelSmall,
                    color = DfColors.Purple,
                )
            }
        }
    }
}

@Composable
private fun ContactTimelineDateChip(
    primary: String,
    secondary: String?,
) {
    Surface(shape = AppShapes.Chip, color = DfColors.SurfaceVariant) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            horizontalAlignment = Alignment.End,
        ) {
            Text(
                text = primary,
                style = AppTypography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = DfColors.TextSecondary,
                maxLines = 1,
            )
            secondary?.let {
                Text(
                    text = it,
                    style = AppTypography.labelSmall,
                    color = DfColors.TextMuted,
                    maxLines = 1,
                )
            }
        }
    }
}

private fun activityVisual(typeKey: String): Pair<Color, ImageVector> = when {
    typeKey.contains("تماس", ignoreCase = true) -> DfColors.Blue to Icons.Default.Call
    typeKey.contains("واتساپ", ignoreCase = true) -> DfColors.Green to DfIcons.MessageCircle
    typeKey.contains("پیامک", ignoreCase = true) -> DfColors.Amber to Icons.Default.Message
    typeKey.contains("یادآور", ignoreCase = true) -> DfColors.Rose to Icons.Default.Notifications
    typeKey.contains("یادداشت", ignoreCase = true) -> DfColors.Purple to Icons.Default.NoteAdd
    typeKey.contains("بازدید", ignoreCase = true) -> DfColors.Green to DfIcons.MapPin
    typeKey.contains("جلسه", ignoreCase = true) -> DfColors.Purple to DfIcons.Users
    typeKey.contains("فایل", ignoreCase = true) -> DfColors.Blue to DfIcons.Share2
    else -> DfColors.TextMuted to Icons.Default.History
}
