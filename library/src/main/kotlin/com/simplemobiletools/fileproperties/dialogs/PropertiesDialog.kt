package com.simplemobiletools.fileproperties.dialogs

import android.app.AlertDialog
import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.ViewGroup
import com.simplemobiletools.filepicker.extensions.isAudioSlow
import com.simplemobiletools.filepicker.extensions.isImageSlow
import com.simplemobiletools.filepicker.extensions.isVideoSlow
import com.simplemobiletools.fileproperties.R
import com.simplemobiletools.fileproperties.extensions.*
import kotlinx.android.synthetic.main.smtpr_item_properties.view.*
import kotlinx.android.synthetic.main.smtpr_property_item.view.*
import java.io.File
import java.util.*

class PropertiesDialog() {
    lateinit var mInflater: LayoutInflater
    lateinit var mPropertyView: ViewGroup
    lateinit var mResources: Resources

    private var mCountHiddenItems = false
    private var mFilesCnt = 0

    /**
     * A File Properties dialog constructor with an optional parameter, usable at 1 file selected
     *
     * @param context has to be activity context to avoid some Theme.AppCompat issues
     * @param path the file path
     * @param countHiddenItems toggle determining if we will count hidden files themselves and their sizes (reasonable only at directory properties)
     */
    constructor(context: Context, path: String, countHiddenItems: Boolean = false) : this() {
        mCountHiddenItems = countHiddenItems
        mInflater = LayoutInflater.from(context)
        mResources = context.resources
        val view = mInflater.inflate(R.layout.smtpr_item_properties, null) as ViewGroup
        mPropertyView = view.smtpr_properties_holder

        val file = File(path)
        addProperty(R.string.smtpr_name, file.name)
        addProperty(R.string.smtpr_path, file.parent)
        addProperty(R.string.smtpr_size, getItemSize(file).formatSize())
        addProperty(R.string.smtpr_last_modified, file.lastModified().formatLastModified())

        if (file.isDirectory) {
            addProperty(R.string.smtpr_direct_children_count, getDirectChildrenCount(file, countHiddenItems))
            addProperty(R.string.smtpr_files_count, mFilesCnt.toString())
        } else if (file.isImageSlow()) {
            addProperty(R.string.smtpr_resolution, file.getImageResolution())
        } else if (file.isAudioSlow()) {
            addProperty(R.string.smtpr_duration, file.getDuration())
            addProperty(R.string.smtpr_artist, file.getArtist())
            addProperty(R.string.smtpr_album, file.getAlbum())
        } else if (file.isVideoSlow()) {
            addProperty(R.string.smtpr_duration, file.getDuration())
            addProperty(R.string.smtpr_resolution, file.getVideoResolution())
            addProperty(R.string.smtpr_artist, file.getArtist())
            addProperty(R.string.smtpr_album, file.getAlbum())
        }

        val dialog = AlertDialog.Builder(context)
                .setTitle(mResources.getString(R.string.smtpr_properties))
                .setView(view)
                .setPositiveButton(R.string.smtpr_ok, null)
                .create()

        dialog.setCanceledOnTouchOutside(true)
        dialog.show()
    }

    /**
     * A File Properties dialog constructor with an optional parameter, usable at multiple items selected
     *
     * @param context has to be activity context to avoid some Theme.AppCompat issues
     * @param path the file path
     * @param countHiddenItems toggle determining if we will count hidden files themselves and their sizes
     */
    constructor(context: Context, paths: List<String>, countHiddenItems: Boolean = false) : this() {
        mCountHiddenItems = countHiddenItems
        mInflater = LayoutInflater.from(context)
        mResources = context.resources
        val view = mInflater.inflate(R.layout.smtpr_item_properties, null) as ViewGroup
        mPropertyView = view.smtpr_properties_holder

        val files = ArrayList<File>(paths.size)
        paths.forEach { files.add(File(it)) }

        val isSameParent = isSameParent(files)

        addProperty(R.string.smtpr_items_selected, paths.size.toString())
        if (isSameParent)
            addProperty(R.string.smtpr_path, files[0].parent)
        addProperty(R.string.smtpr_size, getItemsSize(files).formatSize())
        addProperty(R.string.smtpr_files_count, mFilesCnt.toString())

        val dialog = AlertDialog.Builder(context)
                .setTitle(mResources.getString(R.string.smtpr_properties))
                .setView(view)
                .setPositiveButton(R.string.smtpr_ok, null)
                .create()

        dialog.setCanceledOnTouchOutside(true)
        dialog.show()
    }

    private fun isSameParent(files: List<File>): Boolean {
        var parent = files[0].parent
        for (file in files) {
            val curParent = file.parent
            if (curParent != parent)
                return false

            parent = curParent
        }
        return true
    }

    private fun getDirectChildrenCount(file: File, countHiddenItems: Boolean): String {
        return file.listFiles().filter { !it.isHidden || (it.isHidden && countHiddenItems) }.size.toString()
    }

    private fun addProperty(labelId: Int, value: String?) {
        if (value == null)
            return

        mInflater.inflate(R.layout.smtpr_property_item, mPropertyView, false).apply {
            property_label.text = mResources.getString(labelId)
            property_value.text = value
            mPropertyView.smtpr_properties_holder.addView(this)
        }
    }

    private fun getItemsSize(files: ArrayList<File>): Long {
        var size = 0L
        files.forEach { size += getItemSize(it) }
        return size
    }

    private fun getItemSize(file: File): Long {
        if (file.isDirectory) {
            return getDirectorySize(File(file.path))
        }

        mFilesCnt++
        return file.length()
    }

    private fun getDirectorySize(dir: File): Long {
        var size = 0L
        if (dir.exists()) {
            val files = dir.listFiles()
            for (i in files.indices) {
                if (files[i].isDirectory) {
                    size += getDirectorySize(files[i])
                } else if (!files[i].isHidden && !dir.isHidden || mCountHiddenItems) {
                    mFilesCnt++
                    size += files[i].length()
                }
            }
        }
        return size
    }
}
