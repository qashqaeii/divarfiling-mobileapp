package ir.divarfiling.mobile.feature.export

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import compose.icons.LucideIcons
import compose.icons.lucideicons.Eye
import compose.icons.lucideicons.EyeOff
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfSheetScaffold
import ir.divarfiling.mobile.core.design.components.DfStatusBanner
import ir.divarfiling.mobile.core.design.components.DfStatusTone
import ir.divarfiling.mobile.core.design.components.DfTextButton
import ir.divarfiling.mobile.core.design.components.DfTextField

@Composable
fun ExportSecuritySheet(
    onVerified: () -> Unit,
    onDismiss: () -> Unit,
    viewModel: ExportSecurityViewModel,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.verified) {
        if (state.verified) {
            onVerified()
            viewModel.consumeVerified()
        }
    }

    if (!state.isVisible) return

    var passwordVisible by remember(state.isVisible) { mutableStateOf(false) }

    DfSheetScaffold(
        title = "تأیید هویت برای خروجی",
        subtitle = if (state.exportLabel.isNotBlank()) {
            "محافظت از ${state.exportLabel}"
        } else {
            "رمز عبور + کد پیامک ورود"
        },
        icon = DfIcons.Lock,
        onClose = onDismiss,
    ) {
        StepIndicator(current = state.step)

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = AppShapes.Card,
            color = DfThemeColors.warningContainer().copy(alpha = 0.55f),
        ) {
            Text(
                text = "برای امنیت مخاطبین و فایل‌های شخصی، خروجی فقط پس از تأیید هویت امکان‌پذیر است.",
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                style = AppTypography.bodyDescription,
                color = DfThemeColors.textPrimary(),
            )
        }

        state.error?.let { message ->
            DfStatusBanner(message = message, tone = DfStatusTone.Error)
        }

        if (state.isBusy) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = DfThemeColors.primary(),
            )
        }

        when (state.step) {
            ExportSecurityStep.Password -> {
                DfTextField(
                    value = state.password,
                    onValueChange = viewModel::onPasswordChange,
                    label = "رمز عبور حساب",
                    placeholder = "همان رمز ورود به اپ",
                    visualTransformation = if (passwordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) LucideIcons.EyeOff else LucideIcons.Eye,
                                contentDescription = null,
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(onDone = { viewModel.submitPassword() }),
                    enabled = !state.isBusy,
                )
                DfPrimaryButton(
                    text = "ارسال کد پیامک",
                    onClick = viewModel::submitPassword,
                    enabled = !state.isBusy,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            ExportSecurityStep.Otp -> {
                Text(
                    text = "کد ورود به ${state.phoneDisplay.ifBlank { "شماره شما" }} ارسال شد.",
                    style = AppTypography.bodyDescription,
                    color = DfThemeColors.textMuted(),
                )
                DfTextField(
                    value = state.otpCode,
                    onValueChange = viewModel::onOtpChange,
                    label = "کد تأیید",
                    placeholder = "کد ۶ رقمی",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(onDone = { viewModel.submitOtp() }),
                    enabled = !state.isBusy,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    DfTextButton(
                        text = "بازگشت",
                        onClick = {
                            if (!state.isBusy) viewModel.backToPassword()
                        },
                    )
                    DfTextButton(
                        text = if (state.resendIn > 0) {
                            "ارسال مجدد (${state.resendIn})"
                        } else {
                            "ارسال مجدد"
                        },
                        onClick = {
                            if (!state.isBusy && state.resendIn <= 0) viewModel.resendOtp()
                        },
                    )
                }
                DfPrimaryButton(
                    text = "تأیید و ادامه خروجی",
                    onClick = viewModel::submitOtp,
                    enabled = !state.isBusy,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        Text(
            text = "مجوز خروجی تا ۵ دقیقه معتبر است.",
            style = AppTypography.labelSmall,
            color = DfThemeColors.textMuted(),
            modifier = Modifier.padding(top = AppSpacing.xs),
        )
    }
}

@Composable
private fun StepIndicator(current: ExportSecurityStep) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StepChip(
            index = 1,
            label = "رمز عبور",
            active = current == ExportSecurityStep.Password,
            done = current == ExportSecurityStep.Otp,
        )
        StepChip(
            index = 2,
            label = "کد پیامک",
            active = current == ExportSecurityStep.Otp,
            done = false,
        )
    }
}

@Composable
private fun StepChip(
    index: Int,
    label: String,
    active: Boolean,
    done: Boolean,
) {
    val bg = when {
        done -> DfThemeColors.successContainer()
        active -> DfThemeColors.primaryContainer()
        else -> DfThemeColors.surfaceVariant()
    }
    val fg = when {
        done -> DfThemeColors.success()
        active -> DfThemeColors.primary()
        else -> DfThemeColors.textMuted()
    }
    Surface(shape = AppShapes.Chip, color = bg) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = index.toString(),
                style = AppTypography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = fg,
            )
            Text(
                text = label,
                style = AppTypography.labelSmall,
                color = fg,
            )
        }
    }
}
