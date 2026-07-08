package com.example.statussaver.data

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class StatusRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    suspend fun getStatuses(treeUri: Uri): List<StatusModel> = withContext(Dispatchers.IO) {
        try {
            val documentFile = DocumentFile.fromTreeUri(context, treeUri)
            
            if (documentFile == null || !documentFile.exists() || !documentFile.isDirectory || !documentFile.canRead()) {
                return@withContext emptyList()
            }

            val statuses = mutableListOf<StatusModel>()
            
            documentFile.listFiles().forEach { file ->
                if (file.name?.startsWith(".") == true) return@forEach // Skip hidden files if any inside

                val isImage = file.name?.endsWith(".jpg", true) == true || 
                              file.name?.endsWith(".jpeg", true) == true || 
                              file.name?.endsWith(".png", true) == true || 
                              file.name?.endsWith(".webp", true) == true
                
                val isVideo = file.name?.endsWith(".mp4", true) == true

                if (isImage || isVideo) {
                    statuses.add(
                        StatusModel(
                            uri = file.uri,
                            name = file.name ?: "Unknown",
                            isVideo = isVideo,
                            dateModified = file.lastModified()
                        )
                    )
                }
            }
            
            statuses.sortedByDescending { it.dateModified }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
