package com.cearo.android.mycoolviewapplication.customViews

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout

class MyScrollLayout @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null)
    : FrameLayout(context, attrs) {

    private var lastY = 0f;
    private var positon = 0

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val action = event?.action
        val currY = event!!.y

        when (action) {
            MotionEvent.ACTION_DOWN -> lastY = currY
            MotionEvent.ACTION_MOVE -> {
                val diff = currY - lastY
                scrollTo(0, positon -(diff).toInt())
            }
            MotionEvent.ACTION_UP -> positon = scrollY
        }
        return true
    }

}