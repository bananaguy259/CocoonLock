package com.luke.cocoonlock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // The accessibility service is restarted by the system automatically
        // once it's been enabled in Settings > Accessibility. This receiver
        // exists as a placeholder in case you later add extra boot-time setup.
    }
}
