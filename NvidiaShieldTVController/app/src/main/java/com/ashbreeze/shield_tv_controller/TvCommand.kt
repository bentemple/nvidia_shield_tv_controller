package com.ashbreeze.shield_tv_controller

import android.os.Handler
import android.util.Log
import com.ashbreeze.shield_tv_controller.MainActivity.Companion.SEND_TIMEOUT
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.concurrent.Executors

/**
 * @author ben.temple@gmail.com (Benjamin Temple) 2020.04.01
 *
 * LG Commands [Source from http://www.proaudioinc.com/Dealer_Area/RS232C_EN_160526.pdf]:
 * Owners Manual: External Control Device Setup
 *
 * 01.  Power	POWER off	ex) POWER off ex) ASPECT_RATIO 4by3
 *
 * 02.  Aspect Ratio	ASPECT_RATIO [4by3 /16by9 / setbyoriginal] ex) SCREEN_MUTE screenmuteon
 *
 * 03.  Screen Mute	SCREEN_MUTE [screenmuteon / videomuteon / allmuteoff] ex) VOLUME_MUTE on
 *
 * 04.  Volume Mute	VOLUME_MUTE [on / off]	0 to 100 (Decimal) ex) VOLUME_CONTROL 15
 *
 * 05.  Volume Control	VOLUME_CONTROL [0 to 100]	0 to 100 (Decimal) ex) PICTURE_CONTRAST 50
 *
 * 06.  Contrast	PICTURE_CONTRAST [0 to 100]	0 to 100 (Decimal) ex) PICTURE_BRIGHTNESS 50 0 to 100 (Decimal)
 *
 * 07.  Brightness	PICTURE_BRIGHTNESS [0 to 100]	ex) PICTURE_COLOUR 50
 *      0 to 100 (Decimal)
 *
 * 08.  Color/Colour	PICTURE_COLOUR [0 to 100]	ex) PICTURE_TINT 50
 *      0 to 50 (Decimal)
 *
 * 09.  Tint	PICTURE_TINT [0 to 100]	ex) PICTURE_SHARPNESS 25 ex) OSD_SELECT on
 *
 * 10.  Sharpness	PICTURE_SHARPNESS [0 to 50]	ex) REMOTECONTROLER_LOCK on 0 to 100 (Decimal)
 *
 * 11.  OSD Select	OSD_SELECT [on / off]	ex) AUDIO_BALANCE 50
 *      0 to 100 (Decimal)
 *
 * 12.  Remote Control Lock Mode	REMOTECONTROLER_LOCK [on / off]	ex) PICTURE_COLOUR_TEMPERATURE 50
 *
 * 13.  Balance	AUDIO_BALANCE [0 to 100]	ex) ENERGY_SAVING screenoff
 *
 * 14.  Color(Colour) Temperature	PICTURE_COLOUR_TEMPERATURE [0 to 100]
 *
 * 16.  Energy Saving	ENERGY_SAVING [screenoff / maximum / medium / minimum / off]
 *
 * 17.  Tune Command
 *      CHANNEL_SETTING_ATSC_ATV [Channel Number] antenna
 *      CHANNEL_SETTING_ATSC_ATV [Channel Number] cable
 *      CHANNEL_SETTING_ATSC_DTV [Channel Number] cablemaj	ex) CHANNEL_SETTING_ATSC_ATV 11 antenna
 *
 *      CHANNEL_SETTING_ATSC_DTV [Maj. Channel Number] [Min.Channel Number] antennanotphy
 *      CHANNEL_SETTING_ATSC_DTV [Maj. Channel Number] [Min.Channel Number] cablenotphy	ex) CHANNEL_SETTING_ATSC_DTV 10 2 antennanotphy
 *
 * 18.  Channel(Programme) Add/Del   CHANNEL_ADD_DELETE [add / delete]    ex) CHANNEL_ADD_DELETE add
 *
 * 19.  Key	"KEY_ACTION [exit / channelup / channeldown / volumeup / volumedown / arrowright / arrowleft / volumemute
 *      / deviceinput / sleepreserve / livetv / previouschannel / favoritechannel / teletext / teletextoption / returnback
 *      / avmode / captionsubtitle / arrowup / arrowdown / myapp / settingmenu / ok / quickmenu / videomode
 *      / audiomode / channellist / bluebutton / yellowbutton / greenbutton / redbutton / aspectratio / audiodescription
 *      / programmorder / userguide / smarthome / simplelink / fastforward / rewind / programminfo / programguide
 *      / play / slowplay / soccerscreen / record / 3d / autoconfig / app / screenbright
 *      / number0 / number1 / number2 / number3 / number4 / number5 / number6 / number7 / number8 / number9]"	ex) KEY_ACTION ok
 *
 * 20.  Control Backlight	PICTURE_BACKLIGHT [0 to 100]	"0 to 100 (Decimal)
 *      * Precondition : All settings > picture > Energy Saving off
 *      ex) PICTURE_BACKLIGHT 50"
 *
 * 21.  Input select	INPUT_SELECT [dtv / atv / cadtv / catv / avav1 / component1 / hdmi1 / hdmi2 / hdmi3]
 *      ex) INPUT_SELECT dtv
 *
 * 22.  3D (only 3D models)	PICTURE_3D [off / 3dto2d]	ex) PICTURE_3D off
 *      PICTURE_3D 2dto3d [righttoleft / lefttoright] [0 to 20]	ex) PICTURE_3D 2dto3d righttoleft 10
 *      "PICTURE_3D on [topandbottom / sidebyside / checkboard / framesequential / columninterleaving / rowinterleaving] [righttoleft / lefttoright] [0 to 20]"
 *      ex) PICTURE_3D on topandbottom righttoleft 0
 */

enum class TvCommand(private val commandValue: String) {
    POWER_OFF("POWER off"),
    SELECT_INPUT("INPUT_SELECT");

    fun send(params: String? = null, onComplete: ((Boolean) -> Unit)) {
        val TAG = this::class.java.simpleName

        val timeoutHandler = Handler()

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

