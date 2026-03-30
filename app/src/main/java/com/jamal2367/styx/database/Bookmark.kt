package com.jamal2367.styx.database

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class Bookmark {
    @Parcelize
    data class Entry(
        val url: String,
        val title: String,
        val position: Int,
        val folder: Folder
    ) : Bookmark(), Parcelable

    @Parcelize
    data class Folder(
        val title: String
    ) : Bookmark(), Parcelable
}

fun String?.asFolder(): Bookmark.Folder = Bookmark.Folder(this ?: "")
