package ir.divarfiling.mobile.feature.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.BuildConfig
import ir.divarfiling.mobile.R
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.components.DfCard
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfTopBar
import ir.divarfiling.mobile.core.license.LicenseState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onLoggedOut: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val license by viewModel.licenseState.collectAsStateWithLifecycle(
        initialValue = LicenseState(),
    )
    val scroll = rememberScrollState()

    Scaffold(topBar = { DfTopBar(title = "تنظیمات و حساب", showLogo = true) }) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scroll)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(R.drawable.logo_divarfiling),
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    contentScale = ContentScale.Fit,
                )
            }
            Text(
                "فایلینگ دیوار",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            Text(
                "نسخه ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                style = MaterialTheme.typography.bodySmall,
                color = DfColors.TextMuted,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )

            DfCard(containerColor = DfColors.PurpleContainer) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Verified, contentDescription = null, tint = DfColors.Purple)
                    Column {
                        Text("لایسنس", fontWeight = FontWeight.SemiBold)
                        Text(license.licenseLabel, color = DfColors.TextSecondary)
                        license.expiresAt?.let { Text("انقضا: $it", style = MaterialTheme.typography.bodySmall) }
                        Text(
                            if (license.canUseLightExtract) "استخراج سبک: فعال" else "استخراج سبک: نیاز به ارتقا",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (license.canUseLightExtract) DfColors.Green else DfColors.Amber,
                        )
                    }
                }
            }

            DfCard {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = DfColors.Blue)
                    Column {
                        Text("نصب امن APK", fontWeight = FontWeight.SemiBold)
                        Text(
                            "اگر Play Protect هشدار داد، گزینه Install anyway را بزنید. " +
                                "جزئیات در docs/INSTALL_GUIDE_FA.md",
                            style = MaterialTheme.typography.bodySmall,
                            color = DfColors.TextSecondary,
                        )
                    }
                }
            }

            DfCard {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = DfColors.TextMuted)
                    Column {
                        Text("میزکار وب", fontWeight = FontWeight.SemiBold)
                        Text("divarfiling.ir", color = DfColors.Purple)
                        Text(
                            "استخراج حرفه‌ای و Excel از نرم‌افزار ویندوز",
                            style = MaterialTheme.typography.bodySmall,
                            color = DfColors.TextSecondary,
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            DfPrimaryButton(text = "خروج از حساب", onClick = { viewModel.logout(onLoggedOut) })
        }
    }
}
