package ir.divarfiling.mobile.feature.postcall

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.rememberCoroutineScope
import dagger.hilt.android.AndroidEntryPoint
import ir.divarfiling.mobile.core.design.DivarFilingTheme
import ir.divarfiling.mobile.feature.postcall.presentation.PostCallNotifier
import ir.divarfiling.mobile.feature.postcall.ui.PostCallSheetContent
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PostCallAssistantActivity : ComponentActivity() {
    @Inject lateinit var notifier: PostCallNotifier
    @Inject lateinit var eventHub: PostCallEventHub

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val phone = intent.getStringExtra(EXTRA_PHONE).orEmpty()
        val callKind = intent.getStringExtra(EXTRA_CALL_KIND)
            ?.let { runCatching { PostCallLogic.CallEndKind.valueOf(it) }.getOrNull() }
            ?: PostCallLogic.CallEndKind.Answered
        if (phone.isBlank()) {
            finish()
            return
        }
        notifier.cancel()
        setContent {
            DivarFilingTheme {
                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                val scope = rememberCoroutineScope()
                ModalBottomSheet(
                    onDismissRequest = { finish() },
                    sheetState = sheetState,
                ) {
                    PostCallSheetContent(
                        phone = phone,
                        callEndKind = callKind,
                        onDismiss = {
                            scope.launch {
                                sheetState.hide()
                                finish()
                            }
                        },
                        onOpenContact = { contactId ->
                            val main = Intent(this@PostCallAssistantActivity, ir.divarfiling.mobile.MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                putExtra(EXTRA_OPEN_CONTACT_ID, contactId)
                            }
                            startActivity(main)
                            finish()
                        },
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        eventHub.dismiss()
        super.onDestroy()
    }

    companion object {
        const val EXTRA_PHONE = "post_call_phone"
        const val EXTRA_CALL_KIND = "post_call_call_kind"
        const val EXTRA_OPEN_CONTACT_ID = "post_call_open_contact_id"
        private const val EXTRA_FROM_NOTIFICATION = "post_call_from_notification"

        fun intent(
            context: Context,
            phone: String,
            callEndKind: PostCallLogic.CallEndKind,
            fromNotification: Boolean,
        ): Intent =
            Intent(context, PostCallAssistantActivity::class.java).apply {
                putExtra(EXTRA_PHONE, phone)
                putExtra(EXTRA_CALL_KIND, callEndKind.name)
                putExtra(EXTRA_FROM_NOTIFICATION, fromNotification)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                if (!fromNotification) {
                    addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION)
                }
            }
    }
}
