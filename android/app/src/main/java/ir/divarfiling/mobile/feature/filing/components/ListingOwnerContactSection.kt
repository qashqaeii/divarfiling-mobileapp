package ir.divarfiling.mobile.feature.filing.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.R
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.feature.extract.components.ExtractSectionCard

@Composable
fun ListingOwnerContactSection(
    name: String,
    phone: String,
    onCall: () -> Unit,
    onSms: () -> Unit,
    onWhatsApp: () -> Unit,
    onBale: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (phone.isBlank()) return
    ExtractSectionCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(
                        imageVector = DfIcons.Phone,
                        contentDescription = null,
                        tint = DfColors.Green,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = "ارتباط با مالک",
                        style = AppTypography.sectionTitle,
                        fontWeight = FontWeight.Bold,
                        color = DfThemeColors.textPrimary(),
                    )
                }
                TextButton(onClick = onEdit) { Text("ویرایش") }
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = name.ifBlank { "مالک آگهی" },
                    style = AppTypography.cardTitle,
                    fontWeight = FontWeight.Bold,
                    color = DfThemeColors.textPrimary(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = phone,
                    style = AppTypography.bodyDescription,
                    color = DfColors.Green,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            ListingOwnerContactActions(
                onCall = onCall,
                onSms = onSms,
                onWhatsApp = onWhatsApp,
                onBale = onBale,
            )
        }
    }
}

@Composable
fun ListingOwnerContactActions(
    onCall: () -> Unit,
    onSms: () -> Unit,
    onWhatsApp: () -> Unit,
    onBale: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        OwnerContactAction(
            label = "تماس",
            tint = DfColors.Blue,
            background = DfColors.BlueLight,
            icon = DfIcons.Phone,
            onClick = onCall,
            modifier = Modifier.weight(1f),
        )
        OwnerContactAction(
            label = "پیامک",
            tint = DfColors.Amber,
            background = DfColors.AmberLight,
            icon = DfIcons.MessageCircle,
            onClick = onSms,
            modifier = Modifier.weight(1f),
        )
        OwnerContactAction(
            label = "واتساپ",
            tint = DfColors.Green,
            background = DfColors.GreenLight,
            iconRes = R.drawable.ic_whatsapp,
            onClick = onWhatsApp,
            modifier = Modifier.weight(1f),
        )
        OwnerContactAction(
            label = "بله",
            tint = DfColors.Purple,
            background = DfColors.PurpleContainer,
            icon = DfIcons.Share2,
            onClick = onBale,
            modifier = Modifier.weight(1f),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OwnerContactAction(
    label: String,
    tint: Color,
    background: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconRes: Int? = null,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Surface(
            onClick = onClick,
            shape = CircleShape,
            color = background,
            modifier = Modifier.size(48.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                when {
                    iconRes != null -> Image(
                        painter = painterResource(iconRes),
                        contentDescription = label,
                        modifier = Modifier.size(22.dp),
                    )
                    icon != null -> Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = tint,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
        Text(
            text = label,
            style = AppTypography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = DfThemeColors.textSecondary(),
            maxLines = 1,
        )
    }
}
