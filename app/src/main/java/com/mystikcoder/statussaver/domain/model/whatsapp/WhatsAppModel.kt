package com.mystikcoder.statussaver.domain.model.whatsapp

import android.net.Uri

data class WhatsAppModel(
    val name: String,
    val uri: Uri,
    val fileName: String,
    val path: String
)