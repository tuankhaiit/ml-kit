package com.example.myapplication.awesomeLayout.slidingPane

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.slidingpanelayout.widget.SlidingPaneLayout
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.WindowMetricsCalculator
import com.example.myapplication.R
import com.example.myapplication.awesomeLayout.simpleList.Item
import com.example.myapplication.awesomeLayout.simpleList.ItemDetailFragment
import com.example.myapplication.awesomeLayout.simpleList.ItemListFragment
import com.example.myapplication.databinding.ActivitySlidingPaneBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SlidingPaneActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySlidingPaneBinding
    private val viewModel: SlidingPaneViewModel by viewModels()

    private lateinit var windowInfoTracker: WindowInfoTracker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sliding_pane)
        windowInfoTracker = WindowInfoTracker.getOrCreate(this)

        obtainWindowMetrics()
        onWindowLayoutInfoChange()
        initViews()
        initEvents()

//        lifecycle.addObserver(object : DefaultLifecycleObserver {
//            override fun onCreate(owner: LifecycleOwner) {
//                super.onCreate(owner)
//                Log.e("khaitdt", "onCreate")
//            }
//
//            override fun onStart(owner: LifecycleOwner) {
//                super.onStart(owner)
//                Log.e("khaitdt", "onStart")
//
//            }
//
//            override fun onResume(owner: LifecycleOwner) {
//                super.onResume(owner)
//                Log.e("khaitdt", "onResume: ${viewModel.openItemEvent.value?.toString()}")
//            }
//
//            override fun onPause(owner: LifecycleOwner) {
//                super.onPause(owner)
//                Log.e("khaitdt", "onPause")
//            }
//
//            override fun onStop(owner: LifecycleOwner) {
//                super.onStop(owner)
//                Log.e("khaitdt", "onStop")
//            }
//
//            override fun onDestroy(owner: LifecycleOwner) {
//                super.onDestroy(owner)
//                Log.e("khaitdt", "onDestroy")
//            }
//        })
    }

    private fun obtainWindowMetrics() {
        val wmc = WindowMetricsCalculator.getOrCreate()
        val currentWM = wmc.computeCurrentWindowMetrics(this).bounds.flattenToString()
        val maximumWM = wmc.computeMaximumWindowMetrics(this).bounds.flattenToString()
        Log.e("khaitdt", "CurrentWindowMetrics: ${currentWM}\nMaximumWindowMetrics: ${maximumWM}")
    }

    private fun onWindowLayoutInfoChange() {
        lifecycleScope.launch(Dispatchers.Main) {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                windowInfoTracker.windowLayoutInfo(this@SlidingPaneActivity)
                    .collect { value ->
                        Log.e("khaitdt", "LayoutChanged: ${value.toString()}")
                    }
            }
        }
    }

    private fun initViews() {
        binding.apply {
            slidingPaneLayout.lockMode = SlidingPaneLayout.LOCK_MODE_LOCKED_CLOSED
            onBackPressedDispatcher.addCallback(
                this@SlidingPaneActivity,
                SlidingPaneOnBackPressedCallback(slidingPaneLayout)
            )
            paneFragment.getFragment<ItemListFragment>().onItemClickListener =
                object : ItemListFragment.OnItemClickListener {
                    override fun onItemClick(item: Item) {
                        viewModel.openItemEvent.value = item
                    }
                }
        }
    }

    private fun initEvents() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.openItemEvent.collect {
                    it?.let { openDetails(it) }
                }
            }
        }
    }

    private fun openDetails(item: Item) {
        Log.e("khaitdt", "openDetail")
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace<ItemDetailFragment>(
                R.id.detailFragment,
                "ItemDetailFragment${item.id}",
                bundleOf(Pair("item", item))
            )
            if (binding.slidingPaneLayout.isOpen) {
                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            }
        }
        binding.slidingPaneLayout.open()
    }

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, SlidingPaneActivity::class.java))
        }
    }
}