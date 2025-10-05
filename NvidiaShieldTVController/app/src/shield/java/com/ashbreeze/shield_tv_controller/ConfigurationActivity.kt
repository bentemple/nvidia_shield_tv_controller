package com.ashbreeze.shield_tv_controller

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

/**
 * Configuration activity for the accessibility service.
 * This activity is launched when the user clicks the configuration button 
 * in the Android TV accessibility settings menu.
 */
class ConfigurationActivity : Activity() {
    
    private lateinit var editHaUrl: EditText
    private lateinit var editHaToken: EditText
    private lateinit var editNetflixPackage: EditText
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configuration)
        
        initViews()
        loadCurrentConfiguration()
        setupClickListeners()
    }

    private fun initViews() {
        editHaUrl = findViewById(R.id.editHaUrl)
        editHaToken = findViewById(R.id.editHaToken)
        editNetflixPackage = findViewById(R.id.editNetflixPackage)
        btnSave = findViewById(R.id.btnSave)
        btnCancel = findViewById(R.id.btnCancel)
        
        // Set initial focus to first input for better controller navigation
        editHaUrl.requestFocus()
    }

    private fun loadCurrentConfiguration() {
        // Load current values (either from SharedPreferences or BuildConfig fallbacks)
        editHaUrl.setText(ConfigurationManager.getHaUrl(this))
        editHaToken.setText(ConfigurationManager.getHaToken(this))
        editNetflixPackage.setText(ConfigurationManager.getNetflixButtonTargetPackage(this))
    }

    private fun setupClickListeners() {
        btnSave.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) hideKeyboard()
        }

        btnSave.setOnClickListener {
            saveConfiguration()
        }

        btnCancel.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) hideKeyboard()
        }

        btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        currentFocus?.let {
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

    private fun saveConfiguration() {
        try {
            val haUrl = editHaUrl.text.toString().trim()
            val haToken = editHaToken.text.toString().trim()
            val netflixPackage = editNetflixPackage.text.toString().trim()

            // Basic validation
            if (haUrl.isEmpty()) {
                editHaUrl.error = "URL cannot be empty"
                editHaUrl.requestFocus()
                return
            }

            if (haToken.isEmpty()) {
                editHaToken.error = "Token cannot be empty"
                editHaToken.requestFocus()
                return
            }

            if (netflixPackage.isEmpty()) {
                editNetflixPackage.error = "Package name cannot be empty"
                editNetflixPackage.requestFocus()
                return
            }

            // URL validation
            if (!haUrl.startsWith("http://") && !haUrl.startsWith("https://")) {
                editHaUrl.error = "URL must start with http:// or https://"
                editHaUrl.requestFocus()
                return
            }

            // Package name validation - warn if app is not installed but allow saving
            if (!isPackageInstalled(netflixPackage)) {
                showPackageWarningDialog(haUrl, haToken, netflixPackage)
                return
            }

            // Save configuration
            saveConfigurationInternal(haUrl, haToken, netflixPackage)

        } catch (e: Exception) {
            Toast.makeText(this, R.string.save_error, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Shows a warning dialog when the package is not installed
     */
    private fun showPackageWarningDialog(haUrl: String, haToken: String, netflixPackage: String) {
        AlertDialog.Builder(this)
            .setTitle("Package Not Found")
            .setMessage("The app with package '$netflixPackage' is not installed on this device. Do you want to save this configuration anyway?")
            .setPositiveButton("Save Anyway") { _, _ ->
                saveConfigurationInternal(haUrl, haToken, netflixPackage)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Internal method to save configuration and finish activity
     */
    private fun saveConfigurationInternal(haUrl: String, haToken: String, netflixPackage: String) {
        ConfigurationManager.saveConfiguration(this, haUrl, haToken, netflixPackage)
        Toast.makeText(this, R.string.save_success, Toast.LENGTH_SHORT).show()
        finish()
    }

    /**
     * Checks if a package with the given name is installed on the device
     */
    private fun isPackageInstalled(packageName: String): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
}