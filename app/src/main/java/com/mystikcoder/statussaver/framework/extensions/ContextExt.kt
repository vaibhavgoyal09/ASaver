package com.mystikcoder.statussaver.framework.extensions

import android.content.Context
import android.widget.Toast

fun Context.showShortToast(message: String) {
    Toast.makeText(this , message , Toast.LENGTH_SHORT).show()
}
