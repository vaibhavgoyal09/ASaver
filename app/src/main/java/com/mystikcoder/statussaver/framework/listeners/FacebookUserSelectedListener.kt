package com.mystikcoder.statussaver.framework.listeners

import com.mystikcoder.statussaver.core.domain.model.facebook.FacebookNode

interface FacebookUserSelectedListener {

    fun onFacebookUserClicked(position: Int, nodeModel: FacebookNode)
}