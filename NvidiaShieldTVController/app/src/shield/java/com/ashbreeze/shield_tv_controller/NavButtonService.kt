package com.ashbreeze.shield_tv_controller

import android.accessibilityservice.AccessibilityService
import android.content.IntentFilter
import android.os.SystemClock
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent

import android.content.Intent
import android.content.Context
import androidx.core.content.ContextCompat
import java.lang.IllegalArgumentException

class NavButtonService : AccessibilityService() {
    private val TAG = this.javaClass.canonicalName

    private val keyEventDownTimeMap = mutableMapOf<Int, Long?>()

    private val broadcastReceiver = CurrentStateBroadcastReceiver()

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
        ContextCompat.registerReceiver(
            applicationContext,
            broadcastReceiver,
            IntentFilter(CurrentStateBroadcastReceiver.HDMI_SELECTED_BROADCAST),
            ContextCompat.RECEIVER_EXPORTED
        )
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        applicationContext.unregisterReceiver(broadcastReceiver)
        super.onDestroy()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
    }

    override fun onInterrupt() {}

    override fun onKeyEvent(event: KeyEvent): Boolean {
        val keyCode = event.keyCode
        val action = event.action

        if (action != KeyEvent.ACTION_UP && action != KeyEvent.ACTION_DOWN || event.isCanceled) {
            Log.d(TAG, "Action not down or up, ignoring")
            return super.onKeyEvent(event)
        }

        if (NETFLIX_KEYCODE != keyCode) {
            if (BuildConfig.LOG_INPUT_KEYCODES) {
                Log.d(TAG, "Ignoring non intercepted event. $keyCode")
            }
            return super.onKeyEvent(event)
        }

        if (action == KeyEvent.ACTION_DOWN) {
            // We're interested in all events atm
            keyEventDownTimeMap[keyCode] = SystemClock.uptimeMillis()
            return true
        }
        val downEventTime = keyEventDownTimeMap[keyCode] ?: -1L
        if (downEventTime == -1L) {
            Log.e(TAG, "Received event that didn't have a recorded downtime.")
            return false
        }

        val duration = SystemClock.uptimeMillis() - downEventTime
        Log.d(TAG, "Received key event: $keyCode, duration: $duration ")

        return if (!broadcastReceiver.isShieldActiveInput || duration >= LONG_PRESS_DURATION) {
            Log.d(TAG, "intercepting broadcast.")
            HaCommand.SELECT_TV_INPUT.send(applicationContext, BuildConfig.HA_COMMAND) {
                broadcastReceiver.isShieldActiveInput = true
            }
            applicationContext.startHome()
            true
        } else {
            handleDefaultKeyIntent(keyCode)
            false
        }
    }

    fun handleDefaultKeyIntent(keyCode: Int) {
        when (keyCode) {
            NETFLIX_KEYCODE -> {
                Log.d(this::class.java.canonicalName, "Launching activity override defined for netflix button");
                applicationContext.packageManager.getLaunchIntentForPackage(ConfigurationManager.getNetflixButtonTargetPackage(applicationContext))?.let {
                    applicationContext.startActivity(it)
                }
            }
            HOME_EVENT -> {
                applicationContext.startHome()
            }
            else -> {
                throw IllegalArgumentException("Unhandled keycode event: $keyCode")
            }
        }

    }

    companion object {
        private const val NETFLIX_KEYCODE = 199
        private const val MENU_KEYCODE = 82
        private const val HOME_EVENT = 3

        private const val LONG_PRESS_DURATION = 1500L
    }
}

private fun Context.startHome() {
    Log.d(this::class.java.canonicalName, "Returning to home home");
    val homeIntent = Intent(Intent.ACTION_MAIN).apply {
        this.addCategory(Intent.CATEGORY_HOME)
        this.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    this.startActivity(homeIntent)
}
