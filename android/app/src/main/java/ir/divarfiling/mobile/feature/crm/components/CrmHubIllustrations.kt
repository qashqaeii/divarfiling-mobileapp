package ir.divarfiling.mobile.feature.crm.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.R

@Composable
fun CrmContactsIllustration(
    modifier: Modifier = Modifier,
    @Suppress("UNUSED_PARAMETER") tint: Color = Color.Unspecified,
    @Suppress("UNUSED_PARAMETER") background: Color = Color.Unspecified,
) {
    CrmCardIllustration(
        drawableRes = R.drawable.crm_illustration_contacts,
        modifier = modifier,
    )
}

@Composable
fun CrmTodayIllustration(
    modifier: Modifier = Modifier,
    @Suppress("UNUSED_PARAMETER") tint: Color = Color.Unspecified,
    @Suppress("UNUSED_PARAMETER") background: Color = Color.Unspecified,
) {
    CrmCardIllustration(
        drawableRes = R.drawable.crm_illustration_today,
        modifier = modifier,
    )
}

@Composable
fun CrmDealsIllustration(
    modifier: Modifier = Modifier,
    @Suppress("UNUSED_PARAMETER") tint: Color = Color.Unspecified,
    @Suppress("UNUSED_PARAMETER") background: Color = Color.Unspecified,
) {
    CrmCardIllustration(
        drawableRes = R.drawable.crm_illustration_deals,
        modifier = modifier,
    )
}

@Composable
fun CrmPropertiesIllustration(
    modifier: Modifier = Modifier,
    @Suppress("UNUSED_PARAMETER") tint: Color = Color.Unspecified,
    @Suppress("UNUSED_PARAMETER") background: Color = Color.Unspecified,
) {
    CrmCardIllustration(
        drawableRes = R.drawable.crm_illustration_properties,
        modifier = modifier,
    )
}

@Composable
private fun CrmCardIllustration(
    @DrawableRes drawableRes: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.size(width = 96.dp, height = 88.dp),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(drawableRes),
            contentDescription = null,
            modifier = Modifier.size(width = 88.dp, height = 80.dp),
            contentScale = ContentScale.Fit,
        )
    }
}
