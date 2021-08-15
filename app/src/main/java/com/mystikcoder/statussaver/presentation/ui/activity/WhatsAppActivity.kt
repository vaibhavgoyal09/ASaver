package com.mystikcoder.statussaver.presentation.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.material.tabs.TabLayoutMediator
import com.mystikcoder.statussaver.R
import com.mystikcoder.statussaver.databinding.ActivityWhatsappBinding
import com.mystikcoder.statussaver.extensions.startAnotherApp
import com.mystikcoder.statussaver.presentation.ui.adapters.WhatsAppViewPagerAdapter
import com.mystikcoder.statussaver.presentation.utils.DialogUtil
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WhatsAppActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWhatsappBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_whatsapp)

        Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).also {

        }

        binding.imageAppLogo.setOnClickListener {
            startAnotherApp("com.whatsapp", "com.whatsapp.w4b")
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

    override fun onBackPressed() {
        if (DialogUtil.isSheetShowing()){
            DialogUtil.hideSheet()
            return
        }else{
            super.onBackPressed()
        }
    }

}
