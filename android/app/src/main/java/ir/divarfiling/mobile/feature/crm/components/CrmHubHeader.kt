package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.R
import ir.divarfiling.mobile.core.design.AppElevations
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DivarFilingTheme

@Composable
fun CrmHubHeader(
    userName: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.screenHorizontal, vertical = AppSpacing.md),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.iconTextGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = DfColors.PurpleContainer,
                    shadowElevation = AppElevations.subtle,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = userName.firstOrNull()?.toString() ?: "؟",
                            style = AppTypography.cardTitle,
                            fontWeight = FontWeight.Bold,
                            color = DfColors.PurpleDark,
                            maxLines = 1,
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(2.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(DfColors.Green),
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.titleSubtitleGap),
            ) {
                Text(
                    text = "مدیریت مشتری",
                    style = AppTypography.pageTitle,
                    color = DfColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "مشتریان، یادآورها و سرنخ‌های شما در یک نگاه",
                    style = AppTypography.bodyDescription,
                    color = DfColors.TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Surface(
            modifier = Modifier.size(44.dp),
            shape = CircleShape,
            color = DfColors.SurfaceVariant,
            shadowElevation = AppElevations.subtle,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(R.drawable.logo_divarfiling),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    contentScale = ContentScale.Fit,
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun CrmHubHeaderPreview() {
    DivarFilingTheme {
        CrmHubHeader(userName = "حسین")
    }
}
