package com.ashbreeze.shield_tv_controller

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

/**
 * Manages runtime configuration using SharedPreferences.
 * Provides fallbacks to BuildConfig values for backward compatibility.
 * Non-shield variants read from shield variant's ContentProvider.
 */
object ConfigurationManager {
    private const val PREFS_NAME = "shield_tv_controller_config"
    private const val KEY_HA_URL = "ha_url"
    private const val KEY_HA_TOKEN = "ha_token"
    private const val KEY_NETFLIX_BUTTON_TARGET_PACKAGE = "netflix_button_target_package"
    private const val TAG = "ConfigurationManager"

    /**
     * Gets SharedPreferences, using shield variant's prefs via sharedUserId if not shield
     */
    private fun getPrefs(context: Context): SharedPreferences {
        // If this is the shield variant, use own SharedPreferences
        if (BuildConfig.IS_SHIELD_PRODUCT_VARIANT) {
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }

        // Non-shield variant: try to read from shield variant's SharedPreferences via sharedUserId
        try {
            val shieldPackage = "${BuildConfig.BASE_PACKAGE_NAME}.shield"
            val shieldContext = context.createPackageContext(shieldPackage, Context.CONTEXT_IGNORE_SECURITY)
            return shieldContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        } catch (e: Exception) {
            Log.w(TAG, "Could not access shield variant's SharedPreferences, using own: ${e.message}")
            // Fallback to own SharedPreferences if shield variant not installed
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    /**
     * Get Home Assistant URL - returns runtime config or BuildConfig fallback
     */
    fun getHaUrl(context: Context): String {
        val saved = getPrefs(context).getString(KEY_HA_URL, null)
        return if (!saved.isNullOrEmpty()) {
            saved
        } else {
            BuildConfig.HA_BASE_URL
        }
    }

    /**
     * Get Home Assistant Token - returns runtime config or BuildConfig fallback
     */
    fun getHaToken(context: Context): String {
        val saved = getPrefs(context).getString(KEY_HA_TOKEN, null)
        return if (!saved.isNullOrEmpty()) {
            saved
        } else {
            BuildConfig.HA_TOKEN
        }
    }

    /**
     * Get Netflix Button Target Package - returns runtime config or BuildConfig fallback
     */
    fun getNetflixButtonTargetPackage(context: Context): String {
        val saved = getPrefs(context).getString(KEY_NETFLIX_BUTTON_TARGET_PACKAGE, null)
        return if (!saved.isNullOrEmpty()) {
            saved
        } else {
            BuildConfig.NETFLIX_BUTTON_APP_URI
        }
    }

    /**
     * Save all configuration values at once
     */
    fun saveConfiguration(context: Context, haUrl: String, haToken: String, netflixPackage: String) {
        getPrefs(context).edit().apply {
            putString(KEY_HA_URL, haUrl)
            putString(KEY_HA_TOKEN, haToken)
            putString(KEY_NETFLIX_BUTTON_TARGET_PACKAGE, netflixPackage)
        }.apply()
    }
}