/*
 * Vian AI Greenhouse - Note Model
 * PRD v4.3 Section 7: Notes System
 */

package com.jamal2367.styx.vgh.notes

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class Note(
    val noteId: String = UUID.randomUUID().toString(),
    val workspaceId: String,
    val title: String,
    val body: String,
    val isStarred: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val tags: List<String> = emptyList()
) : Parcelable
