package ir.divarfiling.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import ir.divarfiling.mobile.core.design.DivarFilingTheme
import ir.divarfiling.mobile.navigation.DivarFilingNavHost

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DivarFilingTheme {
                DivarFilingNavHost()
            }
        }
    }
}
