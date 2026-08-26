package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfGlassTextButton
import ir.divarfiling.mobile.core.design.components.DfModalBottomSheet
import ir.divarfiling.mobile.core.design.components.DfSecondaryButton
import ir.divarfiling.mobile.core.design.components.DfSheetScaffold
import ir.divarfiling.mobile.core.design.components.DfSheetSection
import ir.divarfiling.mobile.core.network.ContactSuggestionItemDto
import ir.divarfiling.mobile.core.network.ContactSuggestResponse
import ir.divarfiling.mobile.feature.crm.ContactSocialShare
import ir.divarfiling.mobile.feature.crm.CrmContactChannels

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PropertySuggestionResultSheet(
    result: ContactSuggestResponse,
    onDismiss: () -> Unit,
    onShare: (ContactSuggestionItemDto, String) -> Unit,
    onCopyText: (String) -> Unit,
    onOpenPublicLink: (String) -> Unit,
    onCopyPublicLink: (String) -> Unit,
) {
    val items = if (result.suggestions.isNotEmpty()) {
        result.suggestions
    } else {
        listOfNotNull(
            ContactSuggestionItemDto(
                customerId = 0,
                name = "مشتری",
                whatsappText = result.whatsappText,
            ).takeIf { !result.whatsappText.isNullOrBlank() },
        )
    }
    val publicUrl = result.publicUrl?.takeIf { it.isNotBlank() }

    DfModalBottomSheet(onDismissRequest = onDismiss) {
        DfSheetScaffold(
            title = "پیشنهاد ثبت شد",
            subtitle = "برای ${DateUtils.toPersianDigits(result.suggestedCount.toString())} مشتری ذخیره شد — پیام آماده با لینک عمومی",
            icon = DfIcons.Check,
            iconContainerColor = DfColors.GreenLight,
            iconTint = DfColors.Green,
            onClose = onDismiss,
            bodyHeightFraction = 0.82f,
            footer = {
                DfSecondaryButton(
                    text = "بستن",
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
        ) {
            SuggestionSuccessHero(count = result.suggestedCount)

            publicUrl?.let { url ->
                DfSheetSection(title = "لینک عمومی فایل") {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = AppShapes.Card,
                        color = DfColors.SurfaceVariant.copy(alpha = 0.45f),
                        border = BorderStroke(0.5.dp, DfColors.Outline.copy(alpha = 0.2f)),
                    ) {
                        Column(
                            modifier = Modifier.padding(AppSpacing.sm),
                            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                        ) {
                            Text(
                                url,
                                style = AppTypography.labelSmall,
                                color = DfColors.TextSecondary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                                DfGlassTextButton(text = "باز کردن", onClick = { onOpenPublicLink(url) }, compact = true)
                                DfGlassTextButton(text = "کپی لینک", onClick = { onCopyPublicLink(url) }, compact = true)
                            }
                        }
                    }
                }
            }

            DfSheetSection(title = "ارسال پیام به مشتریان") {
                items.forEach { item ->
                    SuggestionCustomerShareCard(
                        item = item,
                        publicUrl = publicUrl,
                        onShare = onShare,
                        onCopyText = onCopyText,
                    )
                }
            }
        }
    }
}

@Composable
private fun SuggestionSuccessHero(count: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AppShapes.Card)
            .background(
                Brush.horizontalGradient(
                    listOf(
                        DfColors.Green.copy(alpha = 0.08f),
                        DfColors.Blue.copy(alpha = 0.05f),
                        Color.Transparent,
                    ),
                ),
            )
            .padding(AppSpacing.md),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(DfColors.Green.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(DfIcons.Sparkles, contentDescription = null, tint = DfColors.Green, modifier = Modifier.size(20.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "پیشنهاد برای مشتری‌ها ثبت شد",
                    style = AppTypography.cardTitle,
                    fontWeight = FontWeight.Bold,
                    color = DfColors.TextPrimary,
                )
                Text(
                    "${DateUtils.toPersianDigits(count.toString())} مشتری به این فایل پیوند خورد. متن هر پیام شامل لینک عمومی است.",
                    style = AppTypography.bodyDescription,
                    color = DfColors.TextSecondary,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SuggestionCustomerShareCard(
    item: ContactSuggestionItemDto,
    publicUrl: String?,
    onShare: (ContactSuggestionItemDto, String) -> Unit,
    onCopyText: (String) -> Unit,
) {
    val message = suggestionMessage(item, publicUrl)
    val channels = ContactSocialShare.resolveMessagingChannels(
        item.phone,
        item.activeChannels,
        item.socialLinks,
    )
    val registeredSocial = item.socialLinks.filter { (key, value) ->
        value.isNotBlank() && key !in setOf("whatsapp", "bale", "telegram")
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        color = DfColors.Surface,
        border = BorderStroke(0.5.dp, DfColors.Outline.copy(alpha = 0.22f)),
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        item.name.orEmpty().ifBlank { "مشتری" },
                        style = AppTypography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    item.phone?.takeIf { it.isNotBlank() }?.let { phone ->
                        Text(phone, style = AppTypography.labelSmall, color = DfColors.TextMuted)
                    }
                }
                DfGlassTextButton(text = "کپی متن", onClick = { onCopyText(message) }, compact = true)
            }

            if (registeredSocial.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    registeredSocial.forEach { (key, handle) ->
                        val label = CrmContactChannels.channelDef(key)?.label ?: key
                        Surface(shape = AppShapes.Chip, color = DfColors.PurpleContainer.copy(alpha = 0.5f)) {
                            Text(
                                "$label: $handle",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = AppTypography.labelSmall,
                                color = DfColors.PurpleDark,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }

            if (channels.isNotEmpty()) {
                Text(
                    "ارسال در",
                    style = AppTypography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = DfColors.TextMuted,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    channels.forEach { channel ->
                        PropertySocialShareButton(
                            channel = channel,
                            enabled = message.isNotBlank(),
                            onClick = { onShare(item, channel.key) },
                        )
                    }
                }
            }
        }
    }
}

fun suggestionMessage(item: ContactSuggestionItemDto, publicUrl: String?): String {
    val base = item.whatsappText.orEmpty().trim()
    val url = publicUrl?.trim().orEmpty()
    if (url.isBlank() || base.contains(url)) return base
    return if (base.isBlank()) url else "$base\n\n🔗 $url"
}
