/*
 * Vian AI Greenhouse - Grid Menu Manager
 * PRD v4.3 Section 15: Paginated 2×5 grid menu
 * Handles button customization, Push List toggle, Auto Pilot
 */

package com.jamal2367.styx.vgh.menu

import android.content.Context
import android.content.SharedPreferences
import com.jamal2367.styx.vgh.push.CommitPushListManager

data class GridMenuItem(
    val id: String,
    val label: String,
    val iconResId: Int,
    val isDeletable: Boolean = true,
    val action: () -> Unit
)

class GridMenuManager(
    private val context: Context,
    private val pushListManager: CommitPushListManager
) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "vgh_grid_menu",
        Context.MODE_PRIVATE
    )

    private val menuItems = mutableListOf<GridMenuItem>()
    private var currentPage = 0
    private val itemsPerPage = 10 // 2×5 grid

    // Non-deletable buttons (PRD Section 15.1)
    private val protectedButtonIds = setOf("settings", "exit", "desktop_mobile")

    fun initialize() {
        loadMenuLayout()
        if (menuItems.isEmpty()) {
            setupDefaultMenu()
        }
    }

    private fun setupDefaultMenu() {
        menuItems.clear()

        // Page 1 defaults (PRD Section 15.2)
        menuItems.add(GridMenuItem("workspace_list", "Workspace List", 0))
        menuItems.add(GridMenuItem("add_note", "Add Note", 0))
        menuItems.add(GridMenuItem("current_notes", "Current Workspace Notes", 0))
        menuItems.add(GridMenuItem("global_notes", "Global Notes", 0))
        menuItems.add(GridMenuItem("open_research", "Open Research Pane", 0))
        menuItems.add(GridMenuItem("view_ci", "View CI", 0))
        menuItems.add(GridMenuItem("downloads", "Downloads", 0))
        menuItems.add(GridMenuItem("image_block", "Image Block Toggle", 0))
        menuItems.add(GridMenuItem("desktop_mobile", "Desktop / Mobile Toggle", 0, isDeletable = false))
        menuItems.add(GridMenuItem("settings", "Settings", 0, isDeletable = false))

        // Page 2 defaults (PRD Section 15.3)
        menuItems.add(GridMenuItem("adblocker", "Adblocker", 0))
        menuItems.add(GridMenuItem("view_pwa_ci", "View PWA / CI Pane", 0))
        menuItems.add(GridMenuItem("auto_pilot", "Auto Pilot", 0))
        menuItems.add(GridMenuItem("push_list_toggle", "Push List Toggle", 0))
        menuItems.add(GridMenuItem("attach_repo_auto", "Attach Repo Auto-Capture", 0))
        menuItems.add(GridMenuItem("exit", "Exit", 0, isDeletable = false))

        saveMenuLayout()
    }

    fun getMenuItems(): List<GridMenuItem> = menuItems.toList()

    fun getCurrentPageItems(): List<GridMenuItem> {
        val startIndex = currentPage * itemsPerPage
        val endIndex = minOf(startIndex + itemsPerPage, menuItems.size)
        return menuItems.subList(startIndex, endIndex)
    }

    fun getTotalPages(): Int {
        return (menuItems.size + itemsPerPage - 1) / itemsPerPage
    }

    fun nextPage() {
        currentPage = (currentPage + 1) % getTotalPages()
    }

    fun previousPage() {
        currentPage = if (currentPage > 0) currentPage - 1 else getTotalPages() - 1
    }

    fun addItem(item: GridMenuItem) {
        menuItems.add(item)
        saveMenuLayout()
    }

    fun removeItem(itemId: String): Boolean {
        val item = menuItems.find { it.id == itemId }
        if (item?.isDeletable == true) {
            menuItems.removeAll { it.id == itemId }
            saveMenuLayout()
            return true
        }
        return false
    }

    fun reorderItem(fromIndex: Int, toIndex: Int) {
        if (fromIndex in menuItems.indices && toIndex in menuItems.indices) {
            val item = menuItems.removeAt(fromIndex)
            menuItems.add(toIndex, item)
            saveMenuLayout()
        }
    }

    fun togglePushList(): Boolean {
        val newState = !pushListManager.isPushListEnabled()
        pushListManager.setPushListEnabled(newState)
        return newState
    }

    fun isPushListEnabled(): Boolean = pushListManager.isPushListEnabled()

    private fun loadMenuLayout() {
        // Load saved menu layout from SharedPreferences
        // TODO: Implement serialization/deserialization
    }

    private fun saveMenuLayout() {
        // Save menu layout to SharedPreferences
        // TODO: Implement serialization/deserialization
    }
}
