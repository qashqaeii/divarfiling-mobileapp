package ir.divarfiling.mobile.feature.filing

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatasetsScreen(
    onDatasetClick: (String) -> Unit,
    viewModel: DatasetsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(topBar = { TopAppBar(title = { Text("فایلینگ") }) }) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
        ) {
            Text(
                "مجموعه‌های آپلودشده از ویندوز یا استخراج سبک",
                style = MaterialTheme.typography.bodySmall,
            )
            if (state.isLoading) CircularProgressIndicator()
            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.datasets, key = { it.id }) { ds ->
                    Card(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onDatasetClick(ds.id) },
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(ds.name, style = MaterialTheme.typography.titleMedium)
                            Text("${ds.itemCount} آگهی — ${ds.city ?: ""} ${ds.district ?: ""}")
                            ds.source?.let { Text("منبع: $it", style = MaterialTheme.typography.bodySmall) }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingsScreen(
    datasetId: String,
    viewModel: ListingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(datasetId) { viewModel.load(datasetId) }

    Scaffold(topBar = { TopAppBar(title = { Text("آگهی‌ها") }) }) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
        ) {
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("جستجو") },
            )
            if (state.isLoading) CircularProgressIndicator()
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.listings, key = { it.token }) { listing ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text(listing.title ?: "—", style = MaterialTheme.typography.titleMedium)
                            listing.price?.let { Text("قیمت: ${formatPrice(it)}") }
                            listing.district?.let { Text(it) }
                        }
                    }
                }
            }
        }
    }
}

private fun formatPrice(value: Long): String =
    "%,d تومان".format(value).replace(',', '٬')
