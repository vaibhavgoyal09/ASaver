package com.mystikcoder.statussaver.presentation.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.mystikcoder.statussaver.R
import com.mystikcoder.statussaver.databinding.FragmentWhatsappVideosBinding
import com.mystikcoder.statussaver.presentation.ui.adapters.WhatsAppItemsAdapter
import com.mystikcoder.statussaver.presentation.viewmodel.WhatsAppViewModel

class WhatsAppVideoFragment : Fragment() {

    private lateinit var viewModel: WhatsAppViewModel
    private lateinit var binding: FragmentWhatsappVideosBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_whatsapp_videos, container, false)

        viewModel = ViewModelProvider(requireActivity()).get(WhatsAppViewModel::class.java)
        viewModel.getVideos()

        viewModel.videoData.observe(requireActivity()) { videos ->
            val whatsAppItemsAdapter = WhatsAppItemsAdapter(requireContext(), videos)
            binding.recyclerView.adapter = whatsAppItemsAdapter
            binding.textNoResult.visibility = if (whatsAppItemsAdapter.itemCount == 0) View.VISIBLE else View.GONE
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        viewModel.getVideos()
    }
}
