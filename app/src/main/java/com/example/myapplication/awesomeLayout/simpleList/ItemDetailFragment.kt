package com.example.myapplication.awesomeLayout.simpleList

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentItemDetailBinding

class ItemDetailFragment : Fragment() {
    private lateinit var binding: FragmentItemDetailBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_item_detail, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            it.getParcelable<Item>("item")?.let { item ->
                binding.txtBody.text = item.toString()
            } ?: emptyState()
        } ?: emptyState()
    }

    private fun emptyState() {
        binding.txtBody.text = "Create new note"
    }
}