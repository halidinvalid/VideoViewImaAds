package com.example.imavideoview

import android.content.Context
import android.media.MediaPlayer
import android.util.AttributeSet
import android.widget.MediaController
import android.widget.VideoView
import java.lang.UnsupportedOperationException
//jenkins1
class VideoPlayerView : VideoView {

    private var onVideoCompletedListener = ArrayList<OnVideoCompletedListener>()

    constructor(context: Context, attrs: AttributeSet, defStyle: Int)
            : super(context, attrs, defStyle)

    constructor(context: Context, attrs: AttributeSet)
            : super(context, attrs)

    constructor(context: Context)
            : super(context)

    init {
        val mediaController = MediaController(context)
        mediaController.setAnchorView(this)

        super.setOnCompletionListener { mp ->
            mp.reset()
            mp.setDisplay(holder)
            for (listener: OnVideoCompletedListener in onVideoCompletedListener)
                listener.onVideoCompleted()

        }

        super.setOnErrorListener { _, _, _ -> true }
    }

    override fun setOnCompletionListener(l: MediaPlayer.OnCompletionListener?) =
        throw UnsupportedOperationException()


    public fun play() {
        start()
    }

    public fun addVideoCompletedListener(listener: OnVideoCompletedListener) =
        onVideoCompletedListener.add(listener)


    interface OnVideoCompletedListener {
        fun onVideoCompleted()
    }

}