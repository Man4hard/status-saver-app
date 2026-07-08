package com.example.statussaver

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.VideoFrameDecoder

@HiltAndroidApp
class StatusSaverApp : Application(), ImageLoaderFactory {
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                add(VideoFrameDecoder.Factory())
            }
            .build()
    }
}
