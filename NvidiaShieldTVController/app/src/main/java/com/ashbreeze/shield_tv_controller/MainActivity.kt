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
import android.os.Bundle
import kotlin.system.exitProcess

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        VariantSpecificCode.run(this)

        if (BuildConfig.USE_HOME_ASSISTANT) {
            HaCommand.SELECT_TV_INPUT.send(BuildConfig.HA_COMMAND) { success ->
                if (success) {
                    CurrentStateBroadcastReceiver.setShieldActiveInput(
                        this,
                        BuildConfig.IS_SHIELD_PRODUCT_VARIANT
                    )
                }
                exitProcess(0)
            }
        } else {
            TvCommand.SELECT_INPUT.send(BuildConfig.SELECT_INPUT_PARAMS) { success ->
                if (success) {
                    CurrentStateBroadcastReceiver.setShieldActiveInput(
                        this,
                        BuildConfig.IS_SHIELD_PRODUCT_VARIANT
                    )
                }
                exitProcess(0)
            }
        }
    }

    companion object {
        const val SEND_TIMEOUT: Long = 2000
    }
}
