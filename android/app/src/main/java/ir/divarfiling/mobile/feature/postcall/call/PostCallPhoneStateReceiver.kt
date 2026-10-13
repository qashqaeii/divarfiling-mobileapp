package ir.divarfiling.mobile.feature.postcall.call

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PostCallPhoneStateReceiver : BroadcastReceiver() {
    @Inject lateinit var handler: PostCallPhoneStateHandler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != TelephonyManager.ACTION_PHONE_STATE_CHANGED) return
        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE) ?: return
        val number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
        val pending = if (state == TelephonyManager.EXTRA_STATE_IDLE) goAsync() else null
        handler.onPhoneStateChanged(state, number, pending)
    }
}
