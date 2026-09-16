package ir.divarfiling.mobile.feature.crm

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfEmptyVariant
import ir.divarfiling.mobile.core.design.components.DfHeaderSections
import ir.divarfiling.mobile.core.design.components.DfHubPageHeader
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkspaceWebScreen(
    bridgeUrl: String,
    title: String,
    onBack: () -> Unit,
    onFinished: () -> Unit = onBack,
) {
    var pageLoading by remember(bridgeUrl) { mutableStateOf(true) }
    var loadProgress by remember(bridgeUrl) { mutableIntStateOf(0) }
    var loadError by remember(bridgeUrl) { mutableStateOf<String?>(null) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    fun reloadWebView() {
        loadError = null
        pageLoading = true
        loadProgress = 0
        webViewRef?.loadUrl(bridgeUrl)
    }

    Scaffold(
        containerColor = DfScreenContainerColor,
        topBar = {
            Column(modifier = Modifier.statusBarsPadding()) {
                DfHubPageHeader(
                    title = title.ifBlank { "میزکار" },
                    subtitle = "ادامه در میزکار",
                    sectionLabel = DfHeaderSections.CRM,
                    titleIconRes = DfDecorIcons.Handshake,
                    onBack = {
                        val wv = webViewRef
                        if (wv != null && wv.canGoBack()) {
                            wv.goBack()
                        } else {
                            onFinished()
                        }
                    },
                    showBottomDivider = true,
                )
                if (pageLoading && loadError == null) {
                    LinearProgressIndicator(
                        progress = { loadProgress / 100f },
                        modifier = Modifier.fillMaxWidth(),
                        color = DfThemeColors.primary(),
                        trackColor = DfThemeColors.outlineSubtle(),
                    )
                }
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when {
                loadError != null -> {
                    DfEmptyState(
                        title = "بارگذاری میزکار ناموفق بود",
                        subtitle = loadError.orEmpty(),
                        variant = DfEmptyVariant.Error,
                        actionLabel = "تلاش دوباره",
                        onAction = ::reloadWebView,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = AppSpacing.screenHorizontal),
                    )
                }
                bridgeUrl.isBlank() -> {
                    DfEmptyState(
                        title = "مسیر میزکار نامعتبر است",
                        subtitle = "به صفحه قبل برگردید و دوباره تلاش کنید.",
                        variant = DfEmptyVariant.Error,
                        actionLabel = "بازگشت",
                        onAction = onFinished,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = AppSpacing.screenHorizontal),
                    )
                }
                else -> {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { context ->
                            WebView(context).apply {
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                CookieManager.getInstance().setAcceptCookie(true)
                                webChromeClient = object : WebChromeClient() {
                                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                        loadProgress = newProgress.coerceIn(0, 100)
                                        if (newProgress >= 100) pageLoading = false
                                    }
                                }
                                webViewClient = object : WebViewClient() {
                                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                        pageLoading = true
                                        loadError = null
                                    }

                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        pageLoading = false
                                    }

                                    override fun onReceivedError(
                                        view: WebView?,
                                        request: WebResourceRequest?,
                                        error: WebResourceError?,
                                    ) {
                                        if (request?.isForMainFrame == true) {
                                            pageLoading = false
                                            loadError = "اتصال برقرار نشد. اینترنت را بررسی کنید."
                                        }
                                    }

                                    override fun shouldOverrideUrlLoading(
                                        view: WebView?,
                                        request: WebResourceRequest?,
                                    ): Boolean = false
                                }
                                loadUrl(bridgeUrl)
                                webViewRef = this
                            }
                        },
                        update = { view ->
                            webViewRef = view
                        },
                    )
                }
            }
        }
    }
}
