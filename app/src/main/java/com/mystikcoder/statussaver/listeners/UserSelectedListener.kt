package com.mystikcoder.statussaver.listeners

import com.mystikcoder.statussaver.model.facebook.FacebookNode
import com.mystikcoder.statussaver.model.instagram.TrayModel

interface UserSelectedListener {

    fun onInstagramUserClicked(position: Int, trayModel: TrayModel)

    fun onFacebookUserClicked(position: Int, nodeModel: FacebookNode)
}