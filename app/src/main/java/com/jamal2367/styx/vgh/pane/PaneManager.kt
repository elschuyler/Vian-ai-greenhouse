/*
 * Vian AI Greenhouse - Pane Manager
 * PRD v4.3 Section 1.5: Pane Switching Mechanics
 * Manages WebView lifecycle, state persistence, memory pressure
 */

package com.jamal2367.styx.vgh.pane

import android.webkit.WebView
import androidx.lifecycle.LifecycleOwner
import com.jamal2367.styx.vgh.workspace.WorkspaceState

class PaneManager(private val lifecycleOwner: LifecycleOwner) {

    private val webViewPool = mutableMapOf<VghPane, WebView?>()
    private var currentPane: VghPane = VghPane.AI
    private var workspaceState: WorkspaceState? = null

    // Pane state tracking
    private val paneStates = mutableMapOf<VghPane, PaneState>()

    // Memory pressure handling
    private var isUnderMemoryPressure = false

    init {
        // Initialize pane states
        VghPane.values().forEach { pane ->
            paneStates[pane] = PaneState(
                pane = pane,
                isActive = pane == VghPane.AI,
                url = getDefaultUrl(pane),
                scrollPosition = 0
            )
        }
    }

    fun getCurrentPane(): VghPane = currentPane

    fun getWorkspaceState(): WorkspaceState? = workspaceState

    fun setWorkspaceState(state: WorkspaceState) {
        workspaceState = state
        // Update pane URLs based on workspace
        paneStates[VghPane.AI] = paneStates[VghPane.AI]?.copy(url = state.aiChatUrl)
        paneStates[VghPane.SOURCE] = paneStates[VghPane.SOURCE]?.copy(url = state.repoUrl)
    }

    fun switchPane(targetPane: VghPane) {
        if (targetPane == currentPane) return

        // Pause current pane
        pausePane(currentPane)

        // Resume target pane
        resumePane(targetPane)

        // Update state
        paneStates[currentPane] = paneStates[currentPane]?.copy(isActive = false)
        paneStates[targetPane] = paneStates[targetPane]?.copy(isActive = true)
        currentPane = targetPane

        notifyPaneSwitched(targetPane)
    }

    fun getWebViewForPane(pane: VghPane): WebView? {
        return webViewPool[pane]
    }

    fun createWebViewForPane(pane: VghPane): WebView {
        // Destroy existing if any
        webViewPool[pane]?.destroy()

        val webView = WebView(lifecycleOwner)
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.cacheMode = android.webkit.WebSettings.LOAD_DEFAULT

        webViewPool[pane] = webView
        return webView
    }

    fun pausePane(pane: VghPane) {
        webViewPool[pane]?.onPause()
        paneStates[pane] = paneStates[pane]?.copy(
            isActive = false,
            lastPausedTime = System.currentTimeMillis()
        )
    }

    fun resumePane(pane: VghPane) {
        webViewPool[pane]?.onResume()
        paneStates[pane] = paneStates[pane]?.copy(isActive = true)
    }

    fun destroyPane(pane: VghPane) {
        webViewPool[pane]?.destroy()
        webViewPool[pane] = null
        paneStates[pane] = paneStates[pane]?.copy(
            isActive = false,
            isDestroyed = true
        )
    }

    fun handleMemoryPressure() {
        isUnderMemoryPressure = true

        // PRD v4.3: Pane C (Research) is first to be destroyed under memory pressure
        if (paneStates[VghPane.RESEARCH]?.isActive == false) {
            destroyPane(VghPane.RESEARCH)
        }

        // If still under pressure, pause inactive panes
        paneStates.forEach { (pane, state) ->
            if (state.isActive == false && pane != currentPane) {
                pausePane(pane)
            }
        }
    }

    fun saveScrollPosition(pane: VghPane, position: Int) {
        paneStates[pane] = paneStates[pane]?.copy(scrollPosition = position)
    }

    fun getScrollPosition(pane: VghPane): Int {
        return paneStates[pane]?.scrollPosition ?: 0
    }

    fun activateResearchPane() {
        paneStates[VghPane.RESEARCH] = paneStates[VghPane.RESEARCH]?.copy(
            isActivated = true
        )
        workspaceState?.let { state ->
            // Update workspace state to reflect research pane activation
        }
    }

    fun isResearchPaneActivated(): Boolean {
        return paneStates[VghPane.RESEARCH]?.isActivated == true
    }

    fun getPaneState(pane: VghPane): PaneState? {
        return paneStates[pane]
    }

    fun getAllPaneStates(): Map<VghPane, PaneState> {
        return paneStates.toMap()
    }

    fun cleanup() {
        webViewPool.forEach { (_, webView) ->
            webView?.destroy()
        }
        webViewPool.clear()
        paneStates.clear()
    }

    private fun getDefaultUrl(pane: VghPane): String {
        return when (pane) {
            VghPane.AI -> "https://claude.ai"
            VghPane.SOURCE -> "https://github.com"
            VghPane.RESEARCH -> "https://duckduckgo.com"
        }
    }

    private fun notifyPaneSwitched(pane: VghPane) {
        // Callback for UI updates
    }

    data class PaneState(
        val pane: VghPane,
        val isActive: Boolean = false,
        val isActivated: Boolean = false,
        val isDestroyed: Boolean = false,
        val url: String = "",
        val scrollPosition: Int = 0,
        val lastPausedTime: Long = 0L
    )
}
