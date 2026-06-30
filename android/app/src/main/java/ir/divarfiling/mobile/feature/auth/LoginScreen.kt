package ir.divarfiling.mobile.feature.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfDecorImage
import ir.divarfiling.mobile.core.design.components.DfGlassButtonVariant
import ir.divarfiling.mobile.core.design.components.DfGlassCard
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfScreenBackground
import ir.divarfiling.mobile.core.design.components.liquidGlassSurface

@Composable
fun LoginScreen(
    onLoggedIn: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var passwordVisible by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

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
                enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 3 },
            ) {
                LoginHeroSection()
            }

            Spacer(Modifier.height(AppSpacing.sectionGap))

            AnimatedVisibility(
                visible = true,
                enter = fadeIn(tween(600, delayMillis = 120)) +
                    slideInVertically(tween(600, delayMillis = 120)) { it / 4 },
            ) {
                LoginFeatureChips()
            }

            Spacer(Modifier.height(AppSpacing.sectionGap))

            AnimatedVisibility(
                visible = true,
                enter = fadeIn(tween(700, delayMillis = 200)) +
                    slideInVertically(tween(700, delayMillis = 200)) { it / 5 },
            ) {
                DfGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 24.dp,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                        Text(
                            text = "ورود به میزکار",
                            style = AppTypography.sectionTitle,
                            fontWeight = FontWeight.Bold,
                            color = DfColors.TextPrimary,
                        )
                        Text(
                            text = "با حساب فایلینگ دیوار خود وارد شوید",
                            style = AppTypography.bodyDescription,
                            color = DfColors.TextSecondary,
                        )

                        Spacer(Modifier.height(AppSpacing.xs))

                        OutlinedTextField(
                            value = state.username,
                            onValueChange = viewModel::onUsernameChange,
                            label = { Text("شماره موبایل / نام کاربری", style = AppTypography.labelSmall) },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Next,
                            ),
                            singleLine = true,
                            shape = AppShapes.Field,
                            leadingIcon = {
                                Icon(
                                    DfIcons.Smartphone,
                                    contentDescription = null,
                                    tint = DfColors.Purple,
                                    modifier = Modifier.size(20.dp),
                                )
                            },
                            colors = loginFieldColors(),
                        )

                        OutlinedTextField(
                            value = state.password,
                            onValueChange = viewModel::onPasswordChange,
                            label = { Text("رمز عبور", style = AppTypography.labelSmall) },
                            modifier = Modifier.fillMaxWidth(),
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
                                onDone = { viewModel.login(onLoggedIn) },
                            ),
                            singleLine = true,
                            shape = AppShapes.Field,
                            leadingIcon = {
                                Icon(
                                    DfIcons.User,
                                    contentDescription = null,
                                    tint = DfColors.Purple,
                                    modifier = Modifier.size(20.dp),
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) {
                                            LucideIcons.EyeOff
                                        } else {
                                            LucideIcons.Eye
                                        },
                                        contentDescription = if (passwordVisible) {
                                            "مخفی کردن رمز"
                                        } else {
                                            "نمایش رمز"
                                        },
                                        tint = DfColors.TextMuted,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            },
                            colors = loginFieldColors(),
                        )

                        AnimatedVisibility(visible = state.error != null) {
                            state.error?.let { error ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .liquidGlassSurface(
                                            shape = AppShapes.CardSmall,
                                            variant = DfGlassButtonVariant.Accent,
                                            accent = DfColors.Rose,
                                            elevation = 2.dp,
                                        )
                                        .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
                                ) {
                                    Text(
                                        text = error,
                                        style = AppTypography.labelSmall,
                                        color = DfColors.Rose,
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(AppSpacing.xs))

                        DfPrimaryButton(
                            text = "ورود به میزکار",
                            onClick = { viewModel.login(onLoggedIn) },
                            loading = state.isLoading,
                        )
                    }
                }
            }

            Spacer(Modifier.height(AppSpacing.xxl))

            Text(
                text = "نسخه ${BuildConfig.VERSION_NAME}",
                style = AppTypography.labelSmall,
                color = DfColors.TextMuted,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun LoginHeroSection() {
    val pulse by rememberInfiniteTransition(label = "loginLogoPulse").animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "loginLogoScale",
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Box(
            modifier = Modifier
                .size(108.dp)
                .scale(pulse)
                .liquidGlassSurface(
                    shape = AppShapes.Hero,
                    variant = DfGlassButtonVariant.Primary,
                    elevation = 12.dp,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.logo_divarfiling),
                contentDescription = "فایلینگ دیوار",
                modifier = Modifier.size(64.dp),
                contentScale = ContentScale.Fit,
            )
        }

        Text(
            text = "فایلینگ دیوار",
            style = AppTypography.pageTitle.copy(
                brush = Brush.linearGradient(
                    colors = listOf(
                        DfColors.PurpleGradientStart,
                        DfColors.Purple,
                        DfColors.PurpleGradientEnd,
                    ),
                ),
            ),
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )

        Text(
            text = "همراه هوشمند مشاور املاک",
            style = AppTypography.bodyDescription,
            color = DfColors.TextSecondary,
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
        LoginFeatureChip(iconRes = DfDecorIcons.Folder, label = "فایلینگ", tint = DfColors.Purple)
        LoginFeatureChip(iconRes = DfDecorIcons.Users, label = "CRM", tint = DfColors.Blue)
        LoginFeatureChip(iconRes = DfDecorIcons.Rocket, label = "استخراج", tint = DfColors.Green)
    }
}

@Composable
private fun LoginFeatureChip(
    label: String,
    tint: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    iconRes: Int? = null,
) {
    Row(
        modifier = Modifier
            .liquidGlassSurface(
                shape = AppShapes.Chip,
                variant = DfGlassButtonVariant.Accent,
                accent = tint,
                elevation = 4.dp,
            )
            .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when {
            iconRes != null -> DfDecorImage(resId = iconRes, size = 16.dp)
            icon != null -> Icon(
                icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(16.dp),
            )
        }
        Text(
            text = label,
            style = AppTypography.labelSmall,
            color = DfColors.TextPrimary,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun loginFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = DfColors.Purple,
    focusedLabelColor = DfColors.Purple,
    unfocusedBorderColor = DfColors.Outline,
    focusedContainerColor = Color.White.copy(alpha = 0.55f),
    unfocusedContainerColor = Color.White.copy(alpha = 0.35f),
)
