package com.mystikcoder.statussaver.presentation.ui.fragment

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class LogOutDialogFragment: DialogFragment() {

    private var listener: (() -> Unit)? = null

    fun initDialog(listener: () -> Unit) {
        this.listener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState)
    }
}