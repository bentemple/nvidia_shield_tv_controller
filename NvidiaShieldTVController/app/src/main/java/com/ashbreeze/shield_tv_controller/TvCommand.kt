package com.ashbreeze.shield_tv_controller

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.ashbreeze.shield_tv_controller.MainActivity.Companion.SEND_TIMEOUT
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.concurrent.Executors

/**
 * @author ben.temple@gmail.com (Benjamin Temple) 2020.04.01
 *
 * See top level text document `LG_TV_IP_CONTROL_CODES.txt` for more command options.
 *
 * LG Commands [Source from http://www.proaudioinc.com/Dealer_Area/RS232C_EN_160526.pdf]:
 * Owners Manual: External Control Device Setup
 */

enum class TvCommand(private val commandValue: String) {
    POWER_OFF("POWER off"),
    SELECT_INPUT("INPUT_SELECT");

    fun send(params: String? = null, onComplete: ((Boolean) -> Unit)) {
        val TAG = this::class.java.simpleName

        val timeoutHandler = Handler(Looper.getMainLooper())

        val executor = Executors.newSingleThreadExecutor()
        val onCompleteInternal: (Boolean) -> Unit = { success ->
            if (success) {
                Log.d(TAG, "Sent ${this.name} successfully")
            } else {
                Log.e(TAG, "Failed to send ${this.name}")
            }
            onComplete.invoke(success)
            executor.shutdownNow()
        }

        timeoutHandler.postDelayed({
            executor.shutdownNow()
            onComplete.invoke(false)
        }, SEND_TIMEOUT)

        executor.execute {
            Log.d(TAG, "Connecting to server ${BuildConfig.TV_IP_ADDRESS}")
            val client = TelnetConnection(BuildConfig.TV_IP_ADDRESS, BuildConfig.TV_PORT).apply {
                connect()
            }
            Log.d(TAG, "isConnected ${client.isConnected}")
            if (!client.isConnected) {
                onCompleteInternal.invoke(false)
                return@execute
            }

            val reader = BufferedReader(InputStreamReader(client.bufferedInputStream, StandardCharsets.UTF_8))

            client.sendCommand("$commandValue ${params ?: ""}")

            val response = reader.readLine()
            Log.d(TAG, "Server response: $response")

            client.disconnect()
            timeoutHandler.removeCallbacksAndMessages(null)
            timeoutHandler.post {
                // Post back on the main thread
                onCompleteInternal(response == "OK")
            }
        }
    }
}

