package ir.divarfiling.mobile.feature.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import compose.icons.LucideIcons
import compose.icons.lucideicons.Eye
import compose.icons.lucideicons.EyeOff
import ir.divarfiling.mobile.BuildConfig
import ir.divarfiling.mobile.R
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfHaptics
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfCard
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfDecorImage
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfScreenBackground
import ir.divarfiling.mobile.core.design.components.DfStatusBanner
import ir.divarfiling.mobile.core.design.components.DfStatusTone
import ir.divarfiling.mobile.core.design.components.DfTextField

@Composable
fun LoginScreen(
    onLoggedIn: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var passwordVisible by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val haptics = DfHaptics.rememberPerformer()

    DfScreenBackground(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = AppSpacing.screenHorizontal)
                .padding(top = AppSpacing.xxl, bottom = AppSpacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 4 },
            ) {
                LoginHeroSection()
            }

            Spacer(Modifier.height(AppSpacing.lg))

            LoginFeatureChips()

            Spacer(Modifier.height(AppSpacing.sectionGap))

            DfCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                    Text(
                        text = "ورود به میزکار",
                        style = AppTypography.sectionTitle,
                        fontWeight = FontWeight.Bold,
                        color = DfThemeColors.textPrimary(),
                    )
                    Text(
                        text = "با حساب فایلینگ دیوار وارد شوید",
                        style = AppTypography.bodyDescription,
                        color = DfThemeColors.textSecondary(),
                    )

                    Spacer(Modifier.height(AppSpacing.xxs))

                    DfTextField(
                        value = state.username,
                        onValueChange = viewModel::onUsernameChange,
                        label = "شماره موبایل / نام کاربری",
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Next,
                        ),
                        leadingIcon = {
                            Icon(
                                DfIcons.Smartphone,
                                contentDescription = null,
                                tint = DfThemeColors.primary(),
                                modifier = Modifier.size(20.dp),
                            )
                        },
                    )

                    DfTextField(
                        value = state.password,
                        onValueChange = viewModel::onPasswordChange,
                        label = "رمز عبور",
                        visualTransformation = if (passwordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                haptics.confirm()
                                viewModel.login(onLoggedIn)
                            },
                        ),
                        leadingIcon = {
                            Icon(
                                DfIcons.Lock,
                                contentDescription = null,
                                tint = DfThemeColors.primary(),
                                modifier = Modifier.size(20.dp),
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) LucideIcons.EyeOff else LucideIcons.Eye,
                                    contentDescription = if (passwordVisible) "مخفی کردن رمز" else "نمایش رمز",
                                    tint = DfThemeColors.textMuted(),
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        },
                    )

                    AnimatedVisibility(visible = state.error != null) {
                        state.error?.let { error ->
                            DfStatusBanner(
                                message = error,
                                tone = DfStatusTone.Error,
                            )
                        }
                    }

                    Spacer(Modifier.height(AppSpacing.xxs))

                    DfPrimaryButton(
                        text = "ورود به میزکار",
                        onClick = {
                            haptics.confirm()
                            viewModel.login(onLoggedIn)
                        },
                        loading = state.isLoading,
                    )
                }
            }

            Spacer(Modifier.height(AppSpacing.xxl))

            Text(
                text = "نسخه ${BuildConfig.VERSION_NAME}",
                style = AppTypography.labelSmall,
                color = DfThemeColors.textMuted(),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun LoginHeroSection() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Box(
            modifier = Modifier.size(96.dp),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.logo_divarfiling),
                contentDescription = "فایلینگ دیوار",
                modifier = Modifier.size(72.dp),
                contentScale = ContentScale.Fit,
            )
        }

        Text(
            text = "فایلینگ دیوار",
            style = AppTypography.pageTitle,
            fontWeight = FontWeight.Bold,
            color = DfThemeColors.primary(),
            textAlign = TextAlign.Center,
        )

        Text(
            text = "همراه هوشمند مشاور املاک",
            style = AppTypography.bodyDescription,
            color = DfThemeColors.textSecondary(),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun LoginFeatureChips() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs, Alignment.CenterHorizontally),
    ) {
        LoginFeatureChip(iconRes = DfDecorIcons.Folder, label = "فایلینگ")
        LoginFeatureChip(iconRes = DfDecorIcons.Users, label = "CRM")
        LoginFeatureChip(iconRes = DfDecorIcons.Rocket, label = "استخراج")
    }
}

@Composable
private fun LoginFeatureChip(
    label: String,
    iconRes: Int,
) {
    Row(
        modifier = Modifier
            .padding(horizontal = AppSpacing.xs, vertical = AppSpacing.xxs),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DfDecorImage(resId = iconRes, size = 16.dp)
        Text(
            text = label,
            style = AppTypography.labelSmall,
            color = DfThemeColors.textSecondary(),
            fontWeight = FontWeight.Medium,
        )
    }
}
