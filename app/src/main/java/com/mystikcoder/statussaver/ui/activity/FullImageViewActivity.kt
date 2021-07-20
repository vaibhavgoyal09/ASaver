package com.mystikcoder.statussaver.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.mystikcoder.statussaver.R
import com.mystikcoder.statussaver.databinding.ActivityFullViewBinding

class FullImageViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFullViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_full_view)

        Glide.with(applicationContext)
            .load(intent.getStringExtra("imageUri"))
            .into(binding.imageView)

        binding.imageBack.setOnClickListener {
            onBackPressed()
        }
    }
}