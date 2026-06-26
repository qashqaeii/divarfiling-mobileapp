package ir.divarfiling.mobile.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.components.DfCard
import ir.divarfiling.mobile.core.design.components.DfTopBar
import ir.divarfiling.mobile.core.license.LicenseState

@Composable
fun HomeScreen(
    onNavigateToday: () -> Unit,
    onNavigateContacts: () -> Unit,
    onNavigateFiling: () -> Unit = {},
    onNavigateExtract: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val license by viewModel.licenseState.collectAsStateWithLifecycle(
        initialValue = LicenseState(),
    )

    Scaffold(
        topBar = { DfTopBar(title = "میزکار", showLogo = true) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DfCard(containerColor = DfColors.PurpleContainer) {
                Text("وضعیت حساب", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(license.licenseLabel, color = DfColors.TextSecondary)
                if (license.canUseLightExtract) {
                    Text("استخراج سبک فعال — حداکثر ۱۰۰ آگهی", color = DfColors.PurpleDark)
                }
            }

            Text(
                "دسترسی سریع",
                style = MaterialTheme.typography.titleSmall,
                color = DfColors.TextMuted,
                modifier = Modifier.padding(top = 8.dp),
            )

            HomeShortcutCard(
                title = "کارهای امروز",
                subtitle = "پیگیری‌ها و یادآورهای امروز",
                icon = Icons.Default.Today,
                onClick = onNavigateToday,
            )
            HomeShortcutCard(
                title = "مخاطبین CRM",
                subtitle = "مدیریت مشتریان و سرنخ‌ها",
                icon = Icons.Default.People,
                onClick = onNavigateContacts,
            )
            HomeShortcutCard(
                title = "فایلینگ دیوار",
                subtitle = "مشاهده فایل‌های استخراج‌شده",
                icon = Icons.Default.Folder,
                onClick = onNavigateFiling,
            )
            HomeShortcutCard(
                title = "استخراج سبک",
                subtitle = "جمع‌آوری آگهی و آپلود به میزکار",
                icon = Icons.Default.CloudDownload,
                onClick = onNavigateExtract,
            )
        }
    }
}

@Composable
private fun HomeShortcutCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    DfCard(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(icon, contentDescription = null, tint = DfColors.Purple)
                Column {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = DfColors.TextSecondary)
                }
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = DfColors.TextMuted,
            )
        }
    }
}
