/*
 * Vian AI Greenhouse - Main Activity
 * PRD v4.3: Three-pane architecture with WebView lifecycle management
 * UPDATED: Phase 2 - Pane Manager integration
 */

package com.jamal2367.styx.vgh

import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView
import com.jamal2367.styx.R
import com.jamal2367.styx.vgh.pane.PaneManager
import com.jamal2367.styx.vgh.pane.PaneTabHeader
import com.jamal2367.styx.vgh.pane.VghPane
import com.jamal2367.styx.vgh.pane.VghWebView
import com.jamal2367.styx.vgh.workspace.WorkspaceManager
import com.jamal2367.styx.vgh.workspace.WorkspaceSettingsBottomSheet
import com.jamal2367.styx.vgh.workspace.WorkspaceState

class VghMainActivity : AppCompatActivity() {

    private lateinit var headerContainer: ComposeView
    private lateinit var overlayContainer: FrameLayout
    private lateinit var webViewContainer: FrameLayout

    private lateinit var paneManager: PaneManager
    private lateinit var workspaceManager: WorkspaceManager

    private var showSettingsSheet: Boolean = false
    private var currentWebView: VghWebView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vgh_main)

        headerContainer = findViewById(R.id.vgh_pane_header)
        overlayContainer = findViewById(R.id.vgh_overlay_container)
        webViewContainer = findViewById(R.id.vgh_webview_container)

        // Initialize managers
        paneManager = PaneManager(this)
        workspaceManager = WorkspaceManager(this)

        // Load current workspace
        workspaceManager.getCurrentWorkspace()?.let { workspace ->
            paneManager.setWorkspaceState(workspace)
        }

        setupHeader()
        setupWebView()
    }

    private fun setupHeader() {
        headerContainer.setContent {
            PaneTabHeader(
                currentPane = paneManager.getCurrentPane(),
                onPaneSelected = { pane ->
                    switchToPane(pane)
                },
                onWorkspaceSettingsClick = {
                    showSettingsSheet = true
                    renderSettingsSheet()
                }
            )
        }
    }

    private fun setupWebView() {
        // Create WebView for current pane
        val currentPane = paneManager.getCurrentPane()
        currentWebView = paneManager.createWebViewForPane(currentPane) as? VghWebView
        currentWebView?.setPaneType(currentPane)

        webViewContainer.removeAllViews()
        webViewContainer.addView(currentWebView)

        // Load URL for current pane
        paneManager.getPaneState(currentPane)?.url?.let { url ->
            currentWebView?.loadUrl(url)
        }
    }

    private fun switchToPane(targetPane: VghPane) {
        // Handle Research pane activation
        if (targetPane == VghPane.RESEARCH && !paneManager.isResearchPaneActivated()) {
            paneManager.activateResearchPane()
        }

        // Switch pane in manager
        paneManager.switchPane(targetPane)

        // Get or create WebView for target pane
        var targetWebView = paneManager.getWebViewForPane(targetPane) as? VghWebView
        if (targetWebView == null) {
            targetWebView = paneManager.createWebViewForPane(targetPane) as? VghWebView
            targetWebView?.setPaneType(targetPane)
        }

        // Remove old WebView
        webViewContainer.removeAllViews()

        // Add new WebView
        currentWebView = targetWebView
        webViewContainer.addView(targetWebView)

        // Resume target pane, pause previous
        currentWebView?.onPaneActivated()

        // Load URL if needed
        paneManager.getPaneState(targetPane)?.url?.let { url ->
            if (targetWebView?.url != url) {
                targetWebView.loadUrl(url)
            }
        }
    }

    private fun renderSettingsSheet() {
        if (showSettingsSheet) {
            overlayContainer.visibility = View.VISIBLE
            overlayContainer.removeAllViews()

            val workspace = workspaceManager.getCurrentWorkspace()
            if (workspace == null) return

            val sheetView = ComposeView(this).apply {
                setContent {
                    WorkspaceSettingsBottomSheet(
                        workspaceState = workspace,
                        onDismiss = {
                            showSettingsSheet = false
                            overlayContainer.visibility = View.GONE
                        },
                        onAiToolSelected = { toolName ->
                            // Update workspace AI tool
                        },
                        onRepoUrlChanged = { url ->
                            // Update workspace repo URL
                        },
                        onBranchChanged = { branch ->
                            // Update workspace branch
                        },
                        onCiUrlChanged = { url ->
                            // Update workspace CI URL
                        },
                        onPushEncodingToggled = { useBase64 ->
                            // Update workspace encoding preference
                        },
                        onPwaUrlChanged = { url ->
                            // Update workspace PWA URL
                        },
                        onManageWorkspaceClick = {
                            // Open full workspace manager
                        }
                    )
                }
            }

            overlayContainer.addView(sheetView)
        }
    }

    override fun onBackPressed() {
        if (showSettingsSheet) {
            showSettingsSheet = false
            overlayContainer.visibility = View.GONE
        } else {
            currentWebView?.let { webView ->
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    super.onBackPressed()
                }
            } ?: super.onBackPressed()
        }
    }

    override fun onPause() {
        super.onPause()
        // Pause current pane WebView
        currentWebView?.onPaneDeactivated()
    }

    override fun onResume() {
        super.onResume()
        // Resume current pane WebView
        currentWebView?.onPaneActivated()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cleanup all WebViews
        paneManager.cleanup()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        // Handle memory pressure
        paneManager.handleMemoryPressure()
    }
}
