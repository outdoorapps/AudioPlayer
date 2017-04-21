package com.common.bting.audioplayer

import android.content.Context
import android.support.annotation.AttrRes
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.View
import android.widget.FrameLayout

/**
 * Created by bting on 2/1/17.
 */

class AudioPlayerLayout : FrameLayout {

    var audioBarFrameListener: AudioBarFrameListener? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        isFocusable = true
        isFocusableInTouchMode = true
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        audioBarFrameListener?.visibilityChanged(visibility)
        super.onVisibilityChanged(changedView, visibility)
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        // onBackPressed
        if (event.keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
            audioBarFrameListener?.onBackPressed()
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    interface AudioBarFrameListener {
        fun visibilityChanged(visibility: Int)
        fun onBackPressed()
    }
}