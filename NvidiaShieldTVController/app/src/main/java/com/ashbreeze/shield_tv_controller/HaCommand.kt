package com.ashbreeze.shield_tv_controller

import android.content.Context
import android.util.Log
import com.ashbreeze.shield_tv_controller.MainActivity.Companion.SEND_TIMEOUT
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.net.HttpURLConnection
import java.net.URL

/**
 * @author ben.temple@epicgames.com (Benjamin Temple) 2024.07.19
 */

enum class HaCommand {
    SELECT_TV_INPUT;

    fun send(context: Context, eventValue: String, onComplete: ((Boolean) -> Unit)) {
        val TAG = this::class.java.canonicalName

        val eventId = "nvidia_shield_tv_request"
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            try {
                withTimeout(SEND_TIMEOUT) {
                    val haUrl = ConfigurationManager.getHaUrl(context)
                    val haToken = ConfigurationManager.getHaToken(context)

                    Log.d(TAG, "Sending HA Command: $eventValue, URL: $haUrl, Token: $haToken")

                    val jsonBody = "{\"command\":\"${this@HaCommand.name}\",\"value\":\"${eventValue}\"}"
                    var success = false

                    // Retry logic: up to 5 attempts with 200ms delay
                    for (attempt in 1..5) {
                        try {
                            val url = URL("${haUrl}/api/events/${eventId}")
                            val connection = url.openConnection() as HttpURLConnection
                            connection.requestMethod = "POST"
                            connection.setRequestProperty("Content-Type", "application/json")
                            connection.setRequestProperty("Authorization", "Bearer ${haToken}")
                            connection.doOutput = true

                            connection.outputStream.use { os ->
                                os.write(jsonBody.toByteArray())
                            }

                            val responseCode = connection.responseCode
                            success = responseCode in 200..299

                            connection.disconnect()

                            if (success) {
                                break
                            } else if (responseCode >= 500 && attempt < 5) {
                                delay(200)
                            } else {
                                break
                            }
                        } catch (e: Exception) {
                            if (attempt == 5) throw e
                            delay(200)
                        }
                    }

                    onComplete.invoke(success)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send HA command", e)
                onComplete.invoke(false)
            }
        }
    }
}