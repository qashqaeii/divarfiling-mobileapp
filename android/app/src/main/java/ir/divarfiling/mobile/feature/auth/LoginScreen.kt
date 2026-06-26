package ir.divarfiling.mobile.feature.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.BuildConfig
import ir.divarfiling.mobile.R
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfShapes
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfScreenGradient

@Composable
fun LoginScreen(
    onLoggedIn: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DfScreenGradient())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(R.drawable.logo_divarfiling),
            contentDescription = "فایلینگ دیوار",
            modifier = Modifier.size(96.dp),
            contentScale = ContentScale.Fit,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "فایلینگ دیوار",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = DfColors.TextPrimary,
        )
        Text(
            text = "همراه هوشمند مشاور املاک",
            style = MaterialTheme.typography.bodyLarge,
            color = DfColors.TextSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = DfShapes.Card,
            colors = CardDefaults.cardColors(containerColor = DfColors.Surface.copy(alpha = 0.95f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        ) {
            Column(
                Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = state.username,
                    onValueChange = viewModel::onUsernameChange,
                    label = { Text("شماره موبایل / نام کاربری") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    shape = DfShapes.Field,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DfColors.Purple,
                        focusedLabelColor = DfColors.Purple,
                    ),
                )
                OutlinedTextField(
                    value = state.password,
                    onValueChange = viewModel::onPasswordChange,
                    label = { Text("رمز عبور") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DfColors.Purple,
                        focusedLabelColor = DfColors.Purple,
                    ),
                )

                state.error?.let { error ->
                    Text(text = error, color = MaterialTheme.colorScheme.error)
                }

                Spacer(Modifier.height(8.dp))
                DfPrimaryButton(
                    text = "ورود به میزکار",
                    onClick = { viewModel.login(onLoggedIn) },
                    loading = state.isLoading,
                )
            }
        }

        Spacer(Modifier.height(24.dp))
        Text(
            text = "نسخه ${BuildConfig.VERSION_NAME}",
            style = MaterialTheme.typography.labelSmall,
            color = DfColors.TextMuted,
        )
    }
}
