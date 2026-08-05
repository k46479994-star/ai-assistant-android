package com.example.aiassistant.ui

import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class PreviewView(context: Context) : LinearLayout(context) {
    private lateinit var saveButton: Button
    private lateinit var errorView: TextView
    private var formValid: Boolean = false
    private var saving: Boolean = false

    internal fun attachControls(saveButton: Button, errorView: TextView) {
        this.saveButton = saveButton
        this.errorView = errorView
        updateSaveEnabled()
    }

    internal fun setFormValidity(valid: Boolean, message: String?) {
        formValid = valid
        if (message.isNullOrBlank()) {
            errorView.visibility = View.GONE
        } else {
            errorView.text = message
            errorView.visibility = View.VISIBLE
        }
        updateSaveEnabled()
    }

    fun setSaving(saving: Boolean) {
        this.saving = saving
        updateSaveEnabled()
    }

    fun showError(message: String) {
        errorView.text = message
        errorView.visibility = View.VISIBLE
    }

    override fun getSolidColor(): Int = PremiumColors.Background

    private fun updateSaveEnabled() {
        if (::saveButton.isInitialized) {
            saveButton.isEnabled = formValid && !saving
        }
    }
}
