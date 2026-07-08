package com.example.statussaver.data

import android.content.ContentValues
import android.content.Context
import android.content.ContentUris
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class MediaRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun saveMediaToGallery(status: StatusModel): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                saveUsingMediaStoreAPI29(status)
            } else {
                saveUsingLegacyFileAPI(status)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun saveUsingMediaStoreAPI29(status: StatusModel): Result<String> {
        val contentResolver = context.contentResolver
        
        val collection = if (status.isVideo) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        }

        val relativePath = if (status.isVideo) "Movies/StatusSaver" else "Pictures/StatusSaver"
        
        val selection = "${MediaStore.MediaColumns.DISPLAY_NAME} = ? AND ${MediaStore.MediaColumns.RELATIVE_PATH} LIKE ?"
        val selectionArgs = arrayOf(status.name, "%$relativePath%")
        
        contentResolver.query(collection, arrayOf(MediaStore.MediaColumns._ID), selection, selectionArgs, null)?.use { cursor ->
            if (cursor.count > 0) {
                return Result.failure(Exception("File already saved"))
            }
        }

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, status.name)
            val mimeType = if (status.isVideo) "video/mp4" else {
                if (status.name.endsWith(".png", true)) "image/png" else "image/jpeg"
            }
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }

        val uri = contentResolver.insert(collection, contentValues)
            ?: return Result.failure(Exception("Failed to create MediaStore entry"))

        try {
            contentResolver.openInputStream(status.uri)?.use { input ->
                contentResolver.openOutputStream(uri)?.use { output ->
                    input.copyTo(output)
                }
            }
            contentValues.clear()
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
            contentResolver.update(uri, contentValues, null, null)
            return Result.success("Saved successfully")
        } catch (e: Exception) {
            contentResolver.delete(uri, null, null)
            return Result.failure(e)
        }
    }

    @Suppress("DEPRECATION")
    private fun saveLegacy(status: StatusModel): Result<String> {
        return saveUsingLegacyFileAPI(status)
    }

    @Suppress("DEPRECATION")
    private fun saveUsingLegacyFileAPI(status: StatusModel): Result<String> {
        val dirType = if (status.isVideo) Environment.DIRECTORY_MOVIES else Environment.DIRECTORY_PICTURES
        val baseDir = Environment.getExternalStoragePublicDirectory(dirType)
        val statusSaverDir = File(baseDir, "StatusSaver")
        
        if (!statusSaverDir.exists()) {
            statusSaverDir.mkdirs()
        }

        val destFile = File(statusSaverDir, status.name)
        if (destFile.exists()) {
            return Result.failure(Exception("File already saved"))
        }

        try {
            context.contentResolver.openInputStream(status.uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            
            // Trigger MediaScanner to make it show up in gallery
            android.media.MediaScannerConnection.scanFile(
                context, 
                arrayOf(destFile.absolutePath), 
                null, 
                null
            )
            
            return Result.success("Saved successfully")
        } catch (e: Exception) {
            if (destFile.exists()) {
                destFile.delete()
            }
            return Result.failure(e)
        }
    }

    suspend fun getSavedMedia(): List<StatusModel> = withContext(Dispatchers.IO) {
        val savedList = mutableListOf<StatusModel>()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val projection = arrayOf(
                MediaStore.MediaColumns._ID,
                MediaStore.MediaColumns.DISPLAY_NAME
            )
            
            // Query Images
            val imageSelection = "${MediaStore.MediaColumns.RELATIVE_PATH} LIKE ?"
            val imageArgs = arrayOf("%Pictures/StatusSaver%")
            context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection, imageSelection, imageArgs, null
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val name = cursor.getString(nameCol) ?: "Image"
                    val uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                    savedList.add(StatusModel(name, uri, false))
                }
            }
            
            // Query Videos
            val videoSelection = "${MediaStore.MediaColumns.RELATIVE_PATH} LIKE ?"
            val videoArgs = arrayOf("%Movies/StatusSaver%")
            context.contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection, videoSelection, videoArgs, null
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val name = cursor.getString(nameCol) ?: "Video"
                    val uri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
                    savedList.add(StatusModel(name, uri, true))
                }
            }
        } else {
            val picDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "StatusSaver")
            if (picDir.exists()) {
                picDir.listFiles()?.forEach { file ->
                    savedList.add(StatusModel(file.name, Uri.fromFile(file), false))
                }
            }
            val movDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "StatusSaver")
            if (movDir.exists()) {
                movDir.listFiles()?.forEach { file ->
                    savedList.add(StatusModel(file.name, Uri.fromFile(file), true))
                }
            }
        }
        
        savedList
    }
}
