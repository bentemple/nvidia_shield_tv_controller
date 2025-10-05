/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.ashbreeze.shield_tv_controller

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import kotlin.system.exitProcess

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        // All variants now send HA command directly using shared configuration
        if (BuildConfig.IS_SHIELD_PRODUCT_VARIANT) {
            // Shield variant: run variant-specific code (Netflix button service, etc.)
            VariantSpecificCode.run(this)
        }

        // Send Home Assistant command for this variant
        sendHomeAssistantCommand(BuildConfig.HA_COMMAND)
    }

    /**
     * Sends the Home Assistant command and handles the response
     */
    private fun sendHomeAssistantCommand(command: String) {
        HaCommand.SELECT_TV_INPUT.send(this, command) { success ->
            if (success) {
                Log.d(TAG, "Sent $command successfully")
            } else {
                Log.e(TAG, "Failed to send $command")
            }
            if (success) {
                CurrentStateBroadcastReceiver.setShieldActiveInput(
                    this,
                    BuildConfig.IS_SHIELD_PRODUCT_VARIANT
                )
            }
            finish()
            exitProcess(0)
        }
    }

    companion object {
        private const val TAG = "MainActivity"
        const val SEND_TIMEOUT: Long = 2000
    }
}
