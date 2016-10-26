package com.simplemobiletools.fileproperties.extensions

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import java.io.File
import java.util.*

fun File.isVideo() = getMimeType().startsWith("video")
fun File.isAudio() = getMimeType().startsWith("audio")

fun File.isImage(): Boolean {
    val options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    BitmapFactory.decodeFile(path, options)
    return options.outWidth != -1 && options.outHeight != -1
}

fun File.getMimeType(): String {
    try {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(path)
        return retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)
    } catch (ignored: Exception) {

    }
    return ""
}

fun File.getDuration(): String {
    val retriever = MediaMetadataRetriever()
    retriever.setDataSource(path)
    val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
    val timeInMs = java.lang.Long.parseLong(time)
    return getFormattedDuration((timeInMs / 1000).toInt())
}

fun File.getArtist(): String? {
    val retriever = MediaMetadataRetriever()
    retriever.setDataSource(path)
    return retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
}

fun File.getAlbum(): String? {
    val retriever = MediaMetadataRetriever()
    retriever.setDataSource(path)
    return retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
}

fun File.getVideoResolution(): String {
    try {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(path)
        val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
        val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
        return "$width x $height"
    } catch (ignored: Exception) {

    }
    return ""
}

fun File.getImageResolution(): String {
    val bitmap: Bitmap? = BitmapFactory.decodeFile(path)
    return if (bitmap == null)
        ""
    else
        "${bitmap.width} x ${bitmap.height}"
}

private fun getFormattedDuration(duration: Int): String {
    val sb = StringBuilder(8)
    val hours = duration / (60 * 60)
    val minutes = duration % (60 * 60) / 60
    val seconds = duration % (60 * 60) % 60

    if (duration > 3600) {
        sb.append(String.format(Locale.getDefault(), "%02d", hours)).append(":")
    }

    sb.append(String.format(Locale.getDefault(), "%02d", minutes))
    sb.append(":").append(String.format(Locale.getDefault(), "%02d", seconds))
    return sb.toString()
}
