package ir.divarfiling.mobile



import android.app.Application

import coil.ImageLoader
import coil.ImageLoaderFactory
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.EntryPointAccessors
import ir.divarfiling.mobile.core.fcm.FcmEntryPoint
import ir.divarfiling.mobile.core.sync.BackgroundWorkManager
import ir.divarfiling.mobile.core.sync.WorkerSessionEntryPoint
import org.osmdroid.config.Configuration
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class DivarFilingApp : Application(), ImageLoaderFactory {
    @Inject lateinit var imageLoader: ImageLoader

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate() {
        super.onCreate()
        Configuration.getInstance().userAgentValue = packageName
        Configuration.getInstance().osmdroidBasePath = cacheDir
        Configuration.getInstance().osmdroidTileCache = File(cacheDir, "osmdroid/tiles")
        appScope.launch(Dispatchers.IO) {
            val sessionStore = EntryPointAccessors.fromApplication(
                this@DivarFilingApp,
                WorkerSessionEntryPoint::class.java,
            ).sessionStore()
            BackgroundWorkManager.syncWithSession(this@DivarFilingApp, sessionStore)
            if (sessionStore.isLoggedIn.first()) {
                EntryPointAccessors.fromApplication(
                    this@DivarFilingApp,
                    FcmEntryPoint::class.java,
                ).fcmTokenSync().syncWithRetry()
            }
        }
    }

    override fun newImageLoader(): ImageLoader = imageLoader
}
