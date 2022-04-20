package com.example.myapplication.awesomeLayout

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.myapplication.R
import com.example.myapplication.awesomeLayout.slidingPane.SlidingPaneActivity
import com.example.myapplication.databinding.ActivityAwesomeLayoutBinding

class AwesomeLayoutActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAwesomeLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_awesome_layout)

        initViews()

        binding.btnSlidingPaneLayout.performClick()
    }

    private fun initViews() {
        binding.apply {
            btnSlidingPaneLayout.setOnClickListener {
                SlidingPaneActivity.start(this@AwesomeLayoutActivity)
            }
        }
    }

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, AwesomeLayoutActivity::class.java))
        }
    }
}