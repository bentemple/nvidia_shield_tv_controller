package com.ashbreeze.shield_tv_controller

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * @author ben.temple@gmail.com (Benjamin Temple) 2020.04.01
 */

class CurrentStateBroadcastReceiver: BroadcastReceiver() {
    var isShieldActiveInput = true

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "Currently selected hdmi received broadcast.")
        if (intent?.hasExtra(IS_SHIELD_ACTIVE_INPUT_KEY) == true) {
            isShieldActiveInput = intent.getBooleanExtra(IS_SHIELD_ACTIVE_INPUT_KEY, true)
            Log.d(TAG, "Received isShieldActiveInput broadcast, value: $isShieldActiveInput")
        }
    }

    companion object {
        private val TAG = CurrentStateBroadcastReceiver::class.java.canonicalName
        const val HDMI_SELECTED_BROADCAST = "com.ashbreeze.shield_tv_controller.HDMI_SELECTED_BROADCAST"
        const val IS_SHIELD_ACTIVE_INPUT_KEY = "IS_SHIELD_ACTIVE_INPUT_KEY"

        fun setShieldActiveInput(context: Context, isActive: Boolean) {
            Log.d(TAG, "Notifying that isShieldActiveInput: $isActive")
            val intent = Intent(HDMI_SELECTED_BROADCAST).apply {
                this.putExtra(IS_SHIELD_ACTIVE_INPUT_KEY, isActive)
                this.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
            }
            context.sendBroadcast(intent)
        }
    }
}