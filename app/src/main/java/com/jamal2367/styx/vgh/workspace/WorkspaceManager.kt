/*
 * Vian AI Greenhouse - Workspace Manager
 * PRD v4.3 Section 16: Workspaces
 * Manages workspace creation, switching, persistence
 */

package com.jamal2367.styx.vgh.workspace

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class WorkspaceManager(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "vgh_workspaces",
        Context.MODE_PRIVATE
    )

    private val gson = Gson()

    private var currentWorkspaceId: String? = null
    private var workspaces: MutableList<WorkspaceState> = mutableListOf()

    init {
        loadWorkspaces()
    }

    fun getWorkspaces(): List<WorkspaceState> {
        return workspaces.toList()
    }

    fun getCurrentWorkspace(): WorkspaceState? {
        return currentWorkspaceId?.let { id ->
            workspaces.find { it.workspaceId == id }
        }
    }

    fun createWorkspace(name: String): WorkspaceState {
        val newWorkspace = WorkspaceState(
            workspaceId = generateWorkspaceId(),
            workspaceName = name,
            aiToolEntryId = "",
            aiToolEntryName = "Default AI",
            aiChatUrl = "https://claude.ai",
            repoUrl = "",
            branch = WorkspaceStateDefaults.DEFAULT_BRANCH,
            pushEncodingBase64 = WorkspaceStateDefaults.DEFAULT_ENCODING_RAW,
            researchPaneActivated = WorkspaceStateDefaults.DEFAULT_RESEARCH_ACTIVATED
        )

        workspaces.add(newWorkspace)
        saveWorkspaces()
        return newWorkspace
    }

    fun switchWorkspace(workspaceId: String): Boolean {
        val workspace = workspaces.find { it.workspaceId == workspaceId }
        return if (workspace != null) {
            currentWorkspaceId = workspaceId
            saveCurrentWorkspaceId()
            true
        } else {
            false
        }
    }

    fun updateWorkspace(workspace: WorkspaceState) {
        val index = workspaces.indexOfFirst { it.workspaceId == workspace.workspaceId }
        if (index != -1) {
            workspaces[index] = workspace
            saveWorkspaces()
        }
    }

    fun deleteWorkspace(workspaceId: String): Boolean {
        val removed = workspaces.removeIf { it.workspaceId == workspaceId }
        if (removed) {
            if (currentWorkspaceId == workspaceId) {
                currentWorkspaceId = workspaces.firstOrNull()?.workspaceId
                saveCurrentWorkspaceId()
            }
            saveWorkspaces()
        }
        return removed
    }

    fun starWorkspace(workspaceId: String) {
        val index = workspaces.indexOfFirst { it.workspaceId == workspaceId }
        if (index != -1) {
            workspaces[index] = workspaces[index].copy(isStarred = true)
            saveWorkspaces()
        }
    }

    fun unstarWorkspace(workspaceId: String) {
        val index = workspaces.indexOfFirst { it.workspaceId == workspaceId }
        if (index != -1) {
            workspaces[index] = workspaces[index].copy(isStarred = false)
            saveWorkspaces()
        }
    }

    fun getStarredWorkspaces(): List<WorkspaceState> {
        return workspaces.filter { it.isStarred }
    }

    fun getRecentWorkspaces(): List<WorkspaceState> {
        // Return workspaces sorted by last used (most recent first)
        return workspaces.toList()
    }

    private fun loadWorkspaces() {
        val json = prefs.getString("workspaces_json", null)
        if (json != null) {
            val type = object : TypeToken<List<WorkspaceState>>() {}.type
            workspaces = gson.fromJson(json, type) ?: mutableListOf()
        }

        currentWorkspaceId = prefs.getString("current_workspace_id", null)

        // If no workspaces exist, create default
        if (workspaces.isEmpty()) {
            createWorkspace("Default Workspace")
        }
    }

    private fun saveWorkspaces() {
        val json = gson.toJson(workspaces)
        prefs.edit().putString("workspaces_json", json).apply()
    }

    private fun saveCurrentWorkspaceId() {
        prefs.edit().putString("current_workspace_id", currentWorkspaceId).apply()
    }

    private fun generateWorkspaceId(): String {
        return "ws_${System.currentTimeMillis()}"
    }
}
