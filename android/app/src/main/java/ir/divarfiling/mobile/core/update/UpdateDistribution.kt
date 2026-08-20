package ir.divarfiling.mobile.core.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import ir.divarfiling.mobile.BuildConfig

object UpdateDistribution {
    const val PACKAGE_ID = "ir.divarfiling.mobile"

    val usesInAppApkUpdate: Boolean = !BuildConfig.USE_STORE_UPDATE

    val storeUrl: String = BuildConfig.STORE_UPDATE_URL

    fun openStorePage(context: Context) {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(storeUrl)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }
}
