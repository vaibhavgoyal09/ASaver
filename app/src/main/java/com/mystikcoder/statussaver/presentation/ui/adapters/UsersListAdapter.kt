package com.mystikcoder.statussaver.presentation.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mystikcoder.statussaver.R
import com.mystikcoder.statussaver.databinding.ItemUsersListBinding
import com.mystikcoder.statussaver.domain.model.facebook.FacebookNode
import com.mystikcoder.statussaver.domain.model.instagram.TrayModel
import com.mystikcoder.statussaver.listeners.FacebookUserSelectedListener
import com.mystikcoder.statussaver.listeners.InstagramUserSelectedListener

class UsersListAdapter(
    private val context: Context,
    private val trayModelList: List<TrayModel>?,
    private val nodeModel: List<FacebookNode>?,
    private val fbUserSelected: FacebookUserSelectedListener?,
    private val instaUserSelected: InstagramUserSelectedListener?,
    private val usersToBindType: String
) : RecyclerView.Adapter<UsersListAdapter.UsersViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UsersListAdapter.UsersViewHolder {
        val layoutInflater = LayoutInflater.from(context)

        return UsersViewHolder(
            DataBindingUtil.inflate(
                layoutInflater,
                R.layout.item_users_list,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: UsersListAdapter.UsersViewHolder, position: Int) {
        if (usersToBindType == "Instagram") {
            holder.bindInstagramUsers(trayModelList?.get(position)!!)
        } else if (usersToBindType == "Facebook") {
            holder.bindFacebookUsers(nodeModel?.get(position)!!)
        }
    }

    override fun getItemCount():Int{
        return if (usersToBindType == "Instagram"){
            if (trayModelList?.size == 0){
                0
            }else {
                trayModelList?.size!!
            }
        }else {
            if (nodeModel?.size == 0){
                0
            }else{
                nodeModel?.size!!
            }
        }
    }

    inner class UsersViewHolder(private val binding: ItemUsersListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindInstagramUsers(trayModel: TrayModel) {
            binding.textRealName.text = trayModel.user.fullName

            Glide.with(context).load(trayModel.user.profilePicUrl).thumbnail(0.2f)
                .into(binding.storyIcon)

            binding.root.setOnClickListener {
                instaUserSelected!!.onInstagramUserClicked(adapterPosition, trayModel)
            }
        }

        fun bindFacebookUsers(nodeModel: FacebookNode) {
            binding.textRealName.text =
                nodeModel.nodeData.storyBucketOwner.get("name").asString

            Glide.with(context).load(
                nodeModel
                    .nodeData
                    .owner
                    ?.getAsJsonObject("profile_picture")
                    ?.get("uri")
                    ?.asString
            )
                .thumbnail(0.2f)
                .into(binding.storyIcon)

            binding.root.setOnClickListener {
                fbUserSelected!!.onFacebookUserClicked(adapterPosition, nodeModel)
            }
        }
    }
}
