package com.snapvocab.app.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object FileUtils {

    fun fileFromUriOrPath(context: Context, uriOrPath: String): File {
        val directFile = File(uriOrPath)

        if (directFile.exists()) {
            return directFile
        }

        val uri = Uri.parse(uriOrPath)

        if (uri.scheme == "file" && uri.path != null) {
            val file = File(uri.path!!)

            if (file.exists()) {
                return file
            }
        }

        val mimeType = context.contentResolver.getType(uri)

        val extension = when (mimeType) {
            "image/png" -> "png"
            "image/webp" -> "webp"
            else -> "jpg"
        }

        val tempFile = File(
            context.cacheDir,
            "postcard_${System.currentTimeMillis()}.$extension"
        )

        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Không thể đọc ảnh từ Uri: $uriOrPath")

        inputStream.use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        }

        return tempFile
    }
}