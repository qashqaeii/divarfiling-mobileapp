package ir.divarfiling.mobile.feature.postcall.presentation

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.divarfiling.mobile.core.design.DivarFilingTheme
import ir.divarfiling.mobile.feature.postcall.PostCallAssistantActivity
import ir.divarfiling.mobile.feature.postcall.PostCallLogic
import ir.divarfiling.mobile.feature.postcall.debug.PostCallTrace
import ir.divarfiling.mobile.feature.postcall.permission.PostCallPermissionManager
import ir.divarfiling.mobile.feature.postcall.ui.PostCallCompactOverlayContent
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostCallOverlayController @Inject constructor(
    @ApplicationContext private val context: Context,
    private val permissions: PostCallPermissionManager,
) {
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var composeView: ComposeView? = null
    private var lifecycleOwner: PostCallOverlayLifecycleOwner? = null
    private var showingKey: String? = null

    fun isShowing(): Boolean = composeView != null

    fun show(phone: String, callEndKind: PostCallLogic.CallEndKind): Boolean {
        if (!permissions.canDrawOverlays()) return false
        val key = "${phone}|${callEndKind.name}"
        if (showingKey == key && composeView != null) return true
        dismiss()
        val owner = PostCallOverlayLifecycleOwner().also {
            it.onCreate()
            lifecycleOwner = it
        }
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.BOTTOM
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
        val view = ComposeView(context).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
            setViewTreeLifecycleOwner(owner)
            setViewTreeViewModelStoreOwner(owner)
            setViewTreeSavedStateRegistryOwner(owner)
            setContent {
                DivarFilingTheme {
                    PostCallCompactOverlayContent(
                        phone = phone,
                        callEndKind = callEndKind,
                        onDismiss = { dismiss() },
                        onExpand = {
                            openExpandedActivity(phone, callEndKind)
                            dismiss()
                        },
                    )
                }
            }
        }
        return try {
            windowManager.addView(view, params)
            composeView = view
            showingKey = key
            owner.onStart()
            owner.onResume()
            PostCallTrace.log(PostCallTrace.Stage.Displayed, "overlay ${PostCallTrace.maskPhone(phone)}")
            true
        } catch (e: Exception) {
            PostCallTrace.log(PostCallTrace.Stage.Skipped, "overlay add failed: ${e.javaClass.simpleName}")
            owner.onDestroy()
            lifecycleOwner = null
            false
        }
    }

    fun dismiss() {
        val view = composeView
        if (view != null) {
            runCatching { windowManager.removeView(view) }
        }
        composeView = null
        showingKey = null
        lifecycleOwner?.onDestroy()
        lifecycleOwner = null
    }

    private fun openExpandedActivity(phone: String, callEndKind: PostCallLogic.CallEndKind) {
        val intent = PostCallAssistantActivity.intent(
            context,
            phone,
            callEndKind,
            fromNotification = false,
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(intent) }
    }
}
