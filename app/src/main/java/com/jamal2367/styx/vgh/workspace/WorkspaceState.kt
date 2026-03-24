/*
 * Vian AI Greenhouse - Workspace State Model
 * PRD v4.3 Section 16: Workspace holds pane configs, sessions, settings
 */

package com.jamal2367.styx.vgh.workspace

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class WorkspaceState(
    val workspaceId: String,
    val workspaceName: String,
    val isStarred: Boolean = false,
    val aiToolEntryId: String,
    val aiToolEntryName: String,
    val aiChatUrl: String,
    val repoUrl: String,
    val branch: String = "main",
    val ciUrl: String? = null,
    val researchPaneActivated: Boolean = false,
    val pwaUrl: String? = null,
    val pushEncodingBase64: Boolean = false,
    val availableAiTools: List<String> = emptyList(),
    val lastActivePane: String = "AI"
) : Parcelable

object WorkspaceStateDefaults {
    const val DEFAULT_BRANCH = "main"
    const val DEFAULT_ENCODING_RAW = false
    const val DEFAULT_RESEARCH_ACTIVATED = false
}
