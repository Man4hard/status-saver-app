package com.example.statussaver.data

import android.net.Uri

data class StatusModel(
    val uri: Uri,
    val name: String,
    val isVideo: Boolean,
    val dateModified: Long
)
