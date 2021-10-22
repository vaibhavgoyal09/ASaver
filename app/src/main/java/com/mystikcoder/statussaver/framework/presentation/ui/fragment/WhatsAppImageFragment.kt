package com.mystikcoder.statussaver.framework.presentation.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.mystikcoder.statussaver.R
import com.mystikcoder.statussaver.databinding.FragmentWhatsappImagesBinding
import com.mystikcoder.statussaver.framework.presentation.ui.adapters.WhatsAppItemsAdapter
import com.mystikcoder.statussaver.framework.presentation.ui.viewmodel.WhatsAppViewModel

class WhatsAppImageFragment : Fragment() {

    private lateinit var binding: FragmentWhatsappImagesBinding
    private lateinit var viewModel: WhatsAppViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_whatsapp_images, container, false)

        viewModel = ViewModelProvider(requireActivity()).get(WhatsAppViewModel::class.java)
        viewModel.getImages()

        viewModel.imagesData.observe(requireActivity()) { images ->
            val whatsAppItemsAdapter = WhatsAppItemsAdapter(requireContext(), images)
            binding.recyclerView.adapter = whatsAppItemsAdapter

            binding.textNoResult.visibility =
                if (whatsAppItemsAdapter.itemCount == 0) View.VISIBLE else View.GONE
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        viewModel.getImages()
    }
}
