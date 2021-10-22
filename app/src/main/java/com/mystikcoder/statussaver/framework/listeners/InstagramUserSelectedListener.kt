package com.mystikcoder.statussaver.framework.listeners

import com.mystikcoder.statussaver.core.domain.model.instagram.TrayModel

interface InstagramUserSelectedListener {

    fun onInstagramUserClicked(position: Int, trayModel: TrayModel)
}