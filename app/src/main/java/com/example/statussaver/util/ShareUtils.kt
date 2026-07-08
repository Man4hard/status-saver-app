package com.example.statussaver.util

import android.content.ClipData
import android.content.Context
import android.content.Intent
import com.example.statussaver.data.StatusModel

object ShareUtils {
    fun shareMedia(context: Context, status: StatusModel) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = if (status.isVideo) "video/mp4" else "image/*"
            putExtra(Intent.EXTRA_STREAM, status.uri)
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            clipData = ClipData.newRawUri("", status.uri)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Status via"))
    }
}
