package ir.divarfiling.mobile.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.core.license.LicenseState

@Composable
fun HomeScreen(
    onNavigateToday: () -> Unit,
    onNavigateContacts: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val license by viewModel.licenseState.collectAsStateWithLifecycle(
        initialValue = LicenseState(),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("خانه", style = MaterialTheme.typography.headlineSmall)
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("وضعیت لایسنس", style = MaterialTheme.typography.titleMedium)
                Text(license.licenseLabel)
                if (license.canUseLightExtract) {
                    Text("استخراج سبک: فعال (حداکثر ۱۰۰ آگهی)")
                }
            }
        }
        Card(onClick = onNavigateToday) {
            Column(Modifier.padding(16.dp)) {
                Text("کارهای امروز", style = MaterialTheme.typography.titleMedium)
                Text("پیگیری‌ها و یادآورهای امروز")
            }
        }
        Card(onClick = onNavigateContacts) {
            Column(Modifier.padding(16.dp)) {
                Text("مخاطبین CRM", style = MaterialTheme.typography.titleMedium)
                Text("مدیریت مشتریان و سرنخ‌ها")
            }
        }
    }
}
