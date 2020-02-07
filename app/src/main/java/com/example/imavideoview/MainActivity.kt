package com.example.imavideoview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.google.ads.interactivemedia.v3.api.*
import com.google.ads.interactivemedia.v3.api.player.ContentProgressProvider
import com.google.ads.interactivemedia.v3.api.player.VideoProgressUpdate
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), AdEvent.AdEventListener, AdErrorEvent.AdErrorListener {

    private lateinit var mSdkFactory: ImaSdkFactory
    private lateinit var mAdsLoader: AdsLoader
    private lateinit var mAdsManager: AdsManager
    private var mIsAdDisplay: Boolean = false
    private lateinit var adsDisplayContainer: AdDisplayContainer


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mSdkFactory = ImaSdkFactory.getInstance()
        adsDisplayContainer = mSdkFactory.createAdDisplayContainer()
        adsDisplayContainer.adContainer = videoPlayerWithAdPlayback
        val settings = mSdkFactory.createImaSdkSettings()
        mAdsLoader = mSdkFactory.createAdsLoader(this, settings, adsDisplayContainer)

        mAdsLoader.addAdErrorListener(this)
        mAdsLoader.addAdsLoadedListener {
            mAdsManager = it.adsManager

            mAdsManager.addAdErrorListener(this)
            mAdsManager.addAdEventListener(this)
            mAdsManager.init()
        }

        videoPlayerView.addVideoCompletedListener(object :
            VideoPlayerView.OnVideoCompletedListener {
            override fun onVideoCompleted() = mAdsLoader.contentComplete()
        })

        playButton.setOnClickListener {
            videoPlayerView.setVideoPath(getString(R.string.content_url))
            requestAds(getString(R.string.ad_tag_url))
            it.visibility = View.GONE

        }
    }

    private fun requestAds(adTagUrl: String) {
        val request = mSdkFactory.createAdsRequest()
        request.adTagUrl = adTagUrl
        request.contentProgressProvider = object : ContentProgressProvider {
            override fun getContentProgress(): VideoProgressUpdate {

                if (mIsAdDisplay || videoPlayerView.duration <= 0) {
                    return VideoProgressUpdate.VIDEO_TIME_NOT_READY
                }
                return VideoProgressUpdate(
                    videoPlayerView.currentPosition.toLong(),
                    videoPlayerView.duration.toLong()
                )
            }

        }
        mAdsLoader.requestAds(request)
    }

    override fun onAdEvent(adEvent: AdEvent) {

        when (adEvent.type) {
            AdEvent.AdEventType.LOADED -> mAdsManager.start()
            AdEvent.AdEventType.CONTENT_PAUSE_REQUESTED -> {
                mIsAdDisplay = true
                videoPlayerView.pause()
            }
            AdEvent.AdEventType.CONTENT_RESUME_REQUESTED -> {
                mIsAdDisplay = true
                videoPlayerView.play()
            }
            AdEvent.AdEventType.ALL_ADS_COMPLETED -> {
                mAdsManager.destroy()
            }
            else -> throw IllegalArgumentException("Invalid type for AdEventType")            
        }
    }

    override fun onAdError(adError: AdErrorEvent) = videoPlayerView.play()

    override fun onResume() {
        if (mIsAdDisplay)
            mAdsManager?.resume()
        else
            videoPlayerView.play()
        super.onResume()
    }

    override fun onPause() {
        if (mIsAdDisplay)
            mAdsManager?.pause()
        else
            videoPlayerView.pause()
        super.onPause()
    }
}
