package com.mystikcoder.statussaver.listeners

import com.mystikcoder.statussaver.domain.model.instagram.TrayModel

interface InstagramUserSelectedListener {

    fun onInstagramUserClicked(position: Int, trayModel: TrayModel)
}