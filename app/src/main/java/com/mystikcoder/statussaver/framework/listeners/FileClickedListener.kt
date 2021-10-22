package com.mystikcoder.statussaver.framework.listeners

import java.io.File

interface FileClickedListener {

    fun onFileClicked(position: Int, file: File)

}