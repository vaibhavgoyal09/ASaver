package com.mystikcoder.statussaver.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.material.tabs.TabLayoutMediator
import com.mystikcoder.statussaver.R
import com.mystikcoder.statussaver.adapters.WhatsAppViewPagerAdapter
import com.mystikcoder.statussaver.databinding.ActivityWhatsappBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WhatsAppActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWhatsappBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_whatsapp)

        Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).also {

        }

        binding.viewPager.adapter = WhatsAppViewPagerAdapter(this)

        binding.imageBack.setOnClickListener {
            onBackPressed()
        }

        TabLayoutMediator(
            binding.tabLayout,
            binding.viewPager
        ) { _tab, _position ->
            when (_position) {
                0 -> _tab.text = "Images"
                1 -> _tab.text = "Videos"
            }
        }.attach()
    }
}
