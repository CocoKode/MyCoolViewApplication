package com.cearo.android.mycoolviewapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cearo.android.mycoolviewapplication.customViews.MyScrollLayout

internal class AmapActivity : AppCompatActivity(){

    lateinit var myScrollLayout: MyScrollLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_amap)

        myScrollLayout = findViewById(R.id.my_scroll_layout)
    }
}