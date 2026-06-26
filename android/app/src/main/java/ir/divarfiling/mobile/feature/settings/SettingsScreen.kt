package ir.divarfiling.mobile.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onLoggedOut: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val license by viewModel.licenseState.collectAsStateWithLifecycle(
        initialValue = ir.divarfiling.mobile.core.license.LicenseState(),
    )

    Scaffold(topBar = { TopAppBar(title = { Text("تنظیمات") }) }) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("لایسنس: ${license.licenseLabel}", style = MaterialTheme.typography.titleMedium)
            license.expiresAt?.let { Text("انقضا: $it") }
            Text(
                if (license.canUseLightExtract) "استخراج سبک: مجاز" else "استخراج سبک: نیاز به لایسنس",
            )
            Button(
                onClick = { viewModel.logout(onLoggedOut) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("خروج")
            }
        }
    }
}
