package ir.divarfiling.mobile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import ir.divarfiling.mobile.core.design.DivarFilingTheme
import ir.divarfiling.mobile.navigation.DeepLinkParser
import ir.divarfiling.mobile.navigation.DeepLinkTarget
import ir.divarfiling.mobile.navigation.DivarFilingNavHost

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val deepLink = parseDeepLink(intent)
        setContent {
            DivarFilingTheme {
                DivarFilingNavHost(startDeepLink = deepLink)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    private fun parseDeepLink(intent: Intent?): DeepLinkTarget? {
        val uri: Uri? = intent?.data
        return DeepLinkParser.parse(uri)
    }
}
