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
    
    // Pane A: AI Tool
    val aiToolEntryId: String,
    val aiToolEntryName: String,
    val aiChatUrl: String,
    
    // Pane B: Repo
    val repoUrl: String,
    val branch: String = "main",
    val ciUrl: String? = null,
    val lastScrollPosition: Int = 0,
    
    // Pane C: Research (activated flag only)
    val researchPaneActivated: Boolean = false,
    
    // Mini PWA/CI
    val pwaUrl: String? = null,
    
    // Settings
    val pushEncodingBase64: Boolean = false, // false = Raw Text (default per PRD 16.1)
    
    // Available AI tools for dropdown
    val availableAiTools: List<String> = emptyList(),
    
    // Last active pane
    val lastActivePane: String = "AI"
) : Parcelable

object WorkspaceStateDefaults {
    const val DEFAULT_BRANCH = "main"
    const val DEFAULT_ENCODING_RAW = false
    const val DEFAULT_RESEARCH_ACTIVATED = false
}
