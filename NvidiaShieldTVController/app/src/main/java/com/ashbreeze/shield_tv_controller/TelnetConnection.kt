package com.ashbreeze.shield_tv_controller

import android.util.Log
import org.apache.commons.net.telnet.TelnetClient
import java.io.BufferedInputStream
import java.io.IOException
import java.io.OutputStream
import java.net.SocketException

class TelnetConnection(private val SERVER_IP: String, private val SERVERPORT: Int) {
    private val TAG = this::class.java.simpleName

    private val connection: TelnetClient = TelnetClient().apply {
        this.connectTimeout = 100
    }

    val isConnected: Boolean
        get() = connection.isConnected

    val bufferedInputStream: BufferedInputStream
        get() = BufferedInputStream(connection.inputStream)

    private val outputStream: OutputStream
        get() = connection.outputStream

    @Throws(IOException::class)
    fun connect(): Boolean {
        Log.d(TAG, "Connecting to client: $SERVER_IP:$SERVERPORT")
        try {
            connection.connect(SERVER_IP, SERVERPORT)
        } catch (e: SocketException) {
            Log.e(TAG, "Connection error. ${e.message}")
            return false
        }
        return connection.isConnected
    }

    //exits telnet session and cleans up the telnet console
    fun disconnect(): Boolean {
        try {
            connection.disconnect()
        } catch (e: IOException) {
            Log.e(TAG, "Couldn't disconnect, error: ${e.message}")
            return false
        }
        return true
    }

    fun sendCommand(command: String): Boolean {
        if (!connection.isConnected) {
            return false
        }
        val commandBytes = (command + "\n\r").toByteArray()
        val outstream = outputStream
        Log.d(TAG, "Sending command: " + String(commandBytes, 0, commandBytes.size))

        return try {
            outstream.write(commandBytes, 0, commandBytes.size)
            outstream.flush()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error writing to output ${e.message}")
            false
        }
    }
}