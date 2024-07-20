package com.ashbreeze.shield_tv_controller

import android.util.Log
import com.ashbreeze.shield_tv_controller.MainActivity.Companion.SEND_TIMEOUT

import io.ktor.client.*
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.headers
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.Serializable

/**
 * @author ben.temple@epicgames.com (Benjamin Temple) 2024.07.19
 */

val client = HttpClient(Android) {
    install(Logging)
    install(ContentNegotiation) {
        json()
    }
    install(HttpRequestRetry) {
        retryOnServerErrors(maxRetries = 5)
        constantDelay(200)
    }
}

@Serializable
data class CommandRequest(
    val command: String,
    val value: String,
)

enum class HaCommand {
    SELECT_TV_INPUT;

    fun send(eventValue: String, onComplete: ((Boolean) -> Unit)) {
        val TAG = this::class.java.simpleName

        val eventId = "nvidia_shield_tv_request"
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            withTimeout(SEND_TIMEOUT) {
                val httpResponse: HttpResponse = client.post {
                    url(urlString = "${BuildConfig.HA_BASE_URL}/api/events/${eventId}")
                    contentType(ContentType.Application.Json)
                    headers {
                        append("Authorization", "Bearer ${BuildConfig.HA_TOKEN}")
                    }
                    setBody(CommandRequest(
                        command = this@HaCommand.name,
                        value = eventValue,
                    ))

                }
                if (httpResponse.status.isSuccess()) {
                    Log.d(TAG, "Sent ${this@HaCommand.name}:$eventValue successfully")
                } else {
                    Log.e(TAG, "Failed to send ${this@HaCommand.name}:$eventValue")
                }
                onComplete.invoke(httpResponse.status.isSuccess())
            }
        }
    }
}