package ir.divarfiling.mobile.feature.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfScreenBackground

@Composable
fun LoginScreen(
    onLoggedIn: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    DfScreenBackground(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(AppSpacing.screenHorizontal),
    ) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(R.drawable.logo_divarfiling),
            contentDescription = "فایلینگ دیوار",
            modifier = Modifier.size(88.dp),
            contentScale = ContentScale.Fit,
        )
        Spacer(Modifier.height(AppSpacing.md))
        Text(
            text = "فایلینگ دیوار",
            style = AppTypography.pageTitle,
            fontWeight = FontWeight.Bold,
            color = DfColors.TextPrimary,
        )
        Text(
            text = "همراه هوشمند مشاور املاک",
            style = AppTypography.bodyDescription,
            color = DfColors.TextSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(AppSpacing.sectionGap))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = AppShapes.Hero,
            colors = CardDefaults.cardColors(containerColor = DfColors.Surface.copy(alpha = 0.92f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(
                Modifier.padding(AppSpacing.cardPadding),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                OutlinedTextField(
                    value = state.username,
                    onValueChange = viewModel::onUsernameChange,
                    label = { Text("شماره موبایل / نام کاربری", style = AppTypography.labelSmall) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    shape = AppShapes.Field,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DfColors.Purple,
                        focusedLabelColor = DfColors.Purple,
                        unfocusedBorderColor = DfColors.Outline,
                    ),
                )
                OutlinedTextField(
                    value = state.password,
                    onValueChange = viewModel::onPasswordChange,
                    label = { Text("رمز عبور", style = AppTypography.labelSmall) },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    shape = AppShapes.Field,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DfColors.Purple,
                        focusedLabelColor = DfColors.Purple,
                        unfocusedBorderColor = DfColors.Outline,
                    ),
                )

                state.error?.let { error ->
                    Text(
                        text = error,
                        style = AppTypography.labelSmall,
                        color = DfColors.Rose,
                    )
                }

                Spacer(Modifier.height(AppSpacing.xs))
                DfPrimaryButton(
                    text = "ورود به میزکار",
                    onClick = { viewModel.login(onLoggedIn) },
                    loading = state.isLoading,
                )
            }
        }

        Spacer(Modifier.height(AppSpacing.lg))
        Text(
            text = "نسخه ${BuildConfig.VERSION_NAME}",
            style = AppTypography.labelSmall,
            color = DfColors.TextMuted,
        )
    }
    }
}
