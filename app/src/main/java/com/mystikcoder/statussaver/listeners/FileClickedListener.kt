package com.mystikcoder.statussaver.listeners

import java.io.File

interface FileClickedListener {

    fun onFileClicked(position: Int, file: File)

}