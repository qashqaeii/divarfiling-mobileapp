package ir.divarfiling.mobile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dagger.hilt.android.AndroidEntryPoint
import ir.divarfiling.mobile.core.datastore.SessionStore
import ir.divarfiling.mobile.core.design.DivarFilingTheme
import ir.divarfiling.mobile.feature.onboarding.NotificationPermissionGate
import ir.divarfiling.mobile.navigation.DeepLinkParser
import ir.divarfiling.mobile.navigation.DeepLinkTarget
import ir.divarfiling.mobile.navigation.DivarFilingNavHost
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var sessionStore: SessionStore

    private var pendingDeepLink by mutableStateOf<DeepLinkTarget?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        pendingDeepLink = parseDeepLink(intent)
        setContent {
            DivarFilingTheme {
                NotificationPermissionGate(sessionStore = sessionStore) {
                    DivarFilingNavHost(
                        deepLink = pendingDeepLink,
                        onDeepLinkHandled = { pendingDeepLink = null },
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingDeepLink = parseDeepLink(intent)
    }

    private fun parseDeepLink(intent: Intent?): DeepLinkTarget? {
        val uri: Uri? = intent?.data
        return DeepLinkParser.parse(uri)
    }
}
