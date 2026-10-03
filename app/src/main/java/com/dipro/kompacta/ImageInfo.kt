package com.dipro.kompacta

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import java.io.IOException
import java.util.Locale

data class ImageInfo(
    val width: Int,
    val height: Int,
    val mimeType: String?,
    val fileSizeBytes: Long?,
    val estimatedBitmapBytes: Long
)

fun readImageInfo(context: Context, uri: Uri): ImageInfo {
    val resolver = context.contentResolver

    // Read only the image header: no pixels are loaded into memory.
    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    val stream = resolver.openInputStream(uri)
        ?: throw IOException("Could not open the selected file")
    stream.use { BitmapFactory.decodeStream(it, null, options) }

    if (options.outWidth <= 0 || options.outHeight <= 0) {
        throw IllegalArgumentException("This file is not a readable image")
    }

    // Ask the content provider for the file's size in bytes (may be unavailable).
    val fileSize: Long? = resolver
        .query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)
        ?.use { cursor ->
            val col = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (col >= 0 && cursor.moveToFirst() && !cursor.isNull(col)) cursor.getLong(col) else null
        }

    return ImageInfo(
        width = options.outWidth,
        height = options.outHeight,
        mimeType = options.outMimeType,
        fileSizeBytes = fileSize,
        estimatedBitmapBytes = options.outWidth.toLong() * options.outHeight.toLong() * 4L
    )
}

// Shows exact bytes plus both decimal (1 KB = 1000) and binary (1 KiB = 1024) units.
fun formatBytes(bytes: Long): String {
    return if (bytes >= 1_000_000) {
        val mb = "%.2f".format(Locale.US, bytes / 1_000_000.0)
        val mib = "%.2f".format(Locale.US, bytes / (1024.0 * 1024.0))
        "$bytes bytes ($mb MB decimal, $mib MiB binary)"
    } else {
        val kb = "%.1f".format(Locale.US, bytes / 1000.0)
        val kib = "%.1f".format(Locale.US, bytes / 1024.0)
        "$bytes bytes ($kb KB decimal, $kib KiB binary)"
    }
}