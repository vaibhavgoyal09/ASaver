package com.mystikcoder.statussaver.presentation.ui.fragment

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mystikcoder.statussaver.R

class LogOutDialogFragment : DialogFragment() {

    private var logOutListener: (() -> Unit)? = null
    private var cancelListener: (() -> Unit)? = null
    private var message: String? = null

    fun initDialog(message: String, logOutListener: () -> Unit, cancelListener: () -> Unit) {
        this.logOutListener = logOutListener
        this.cancelListener = cancelListener
        this.message = message
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return MaterialAlertDialogBuilder(requireActivity(), R.style.AlertDialogTheme)
            .setTitle("Want to Log Out?")
            .setMessage(message)
            .setPositiveButton("Log Out") { dialog, _ ->
                logOutListener?.let { logOut ->
                    logOut()
                }
            }
            .setNegativeButton("No") { dialog, _ ->
                cancelListener?.let {
                    it()
                }
                dialog.cancel()
            }
            .create()
    }
}