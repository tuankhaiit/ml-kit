package com.example.myapplication.awesomeLayout.simpleList

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentItemListBinding

class ItemListFragment : Fragment() {
    private lateinit var binding: FragmentItemListBinding

    var onItemClickListener: OnItemClickListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_item_list, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvItem.let {
            val adapter = ItemAdapter(object : OnItemClickListener {
                override fun onItemClick(item: Item) {
                    onItemClickListener?.onItemClick(item)
                }

            })
            it.layoutManager = LinearLayoutManager(requireContext())
            it.adapter = adapter
            adapter.submitList(Item.generateList(0))
        }
    }

    interface OnItemClickListener {
        fun onItemClick(item: Item)
    }
}