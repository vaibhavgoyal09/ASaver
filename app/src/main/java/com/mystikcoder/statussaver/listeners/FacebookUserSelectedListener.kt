package com.mystikcoder.statussaver.listeners

import com.mystikcoder.statussaver.domain.model.facebook.FacebookNode

interface FacebookUserSelectedListener {

    fun onFacebookUserClicked(position: Int, nodeModel: FacebookNode)
}