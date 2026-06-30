package ir.divarfiling.mobile.feature.onboarding

import ir.divarfiling.mobile.core.design.DfColors

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfScreenBackground
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import ir.divarfiling.mobile.core.datastore.SessionStore
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.components.DfPremiumCard
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import kotlinx.coroutines.launch

@Composable
fun NotificationPermissionGate(
    sessionStore: SessionStore,
    content: @Composable () -> Unit,
) {
    var showOnboarding by remember { mutableStateOf<Boolean?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        showOnboarding = !sessionStore.hasSeenNotificationOnboarding()
    }

    when (showOnboarding) {
        null -> Unit
        true -> {
            NotificationOnboardingScreen(
                onEnable = {
                    scope.launch {
                        sessionStore.setNotificationOnboardingSeen()
                        showOnboarding = false
                    }
                },
            )
        }
        false -> content()
    }
}

@Composable
private fun NotificationOnboardingScreen(
    onEnable: () -> Unit,
) {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { onEnable() }

    fun requestPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            onEnable()
            return
        }
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            onEnable()
        } else {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    DfScreenBackground(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.xl),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                DfIcons.Bell,
                contentDescription = null,
                tint = DfColors.Purple,
                modifier = Modifier.size(72.dp),
            )
            Spacer(Modifier.height(24.dp))
            Text(
                "اعلان‌ها را فعال کنید",
                style = AppTypography.sectionTitle,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "برای یادآور تماس، پیگیری معوق، پایان استخراج و کارهای امروز، " +
                    "به اعلان‌ها نیاز دارید — حتی وقتی اپ باز نیست.",
                style = AppTypography.bodyDescription,
                color = DfColors.TextSecondary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(32.dp))
            DfPremiumCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BenefitRow("یادآور تماس و بازدید")
                    BenefitRow("پایان یا خطای استخراج")
                    BenefitRow("کارهای امروز و پیگیری معوق")
                    BenefitRow("فایل مناسب مشتری و کاهش قیمت")
                }
            }
            Spacer(Modifier.height(32.dp))
            DfPrimaryButton(
                text = "تایید",
                onClick = ::requestPermission,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun BenefitRow(text: String) {
    Text("• $text", style = AppTypography.bodyDescription, color = DfColors.TextSecondary)
}
