/*
 * Vian AI Greenhouse - Main Activity
 * PRD v4.3: Three-pane architecture with Notes + Chat Buffer
 * UPDATED: Phase 4 - Simplified CI log download (no buffering)
 */

package com.jamal2367.styx.vgh

import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView
import com.jamal2367.styx.R
import com.jamal2367.styx.vgh.buffer.ChatBufferManager
import com.jamal2367.styx.vgh.buffer.CiLogDownloader
import com.jamal2367.styx.vgh.menu.GridMenuManager
import com.jamal2367.styx.vgh.notes.NotesDatabase
import com.jamal2367.styx.vgh.notes.NotesRepository
import com.jamal2367.styx.vgh.pane.PaneManager
import com.jamal2367.styx.vgh.pane.PaneTabHeader
import com.jamal2367.styx.vgh.pane.VghPane
import com.jamal2367.styx.vgh.pane.VghWebView
import com.jamal2367.styx.vgh.push.CommitPushListManager
import com.jamal2367.styx.vgh.push.CommitPushListWindow
import com.jamal2367.styx.vgh.workspace.WorkspaceManager
import com.jamal2367.styx.vgh.workspace.WorkspaceSettingsBottomSheet
import com.jamal2367.styx.vgh.workspace.WorkspaceState

class VghMainActivity : AppCompatActivity() {

    private lateinit var headerContainer: ComposeView
    private lateinit var overlayContainer: FrameLayout
    private lateinit var webViewContainer: FrameLayout

    private lateinit var paneManager: PaneManager
    private lateinit var workspaceManager: WorkspaceManager
    private lateinit var pushListManager: CommitPushListManager
    private lateinit var pushListWindow: CommitPushListWindow
    private lateinit var gridMenuManager: GridMenuManager
    private lateinit var chatBufferManager: ChatBufferManager
    private lateinit var ciLogDownloader: CiLogDownloader
    private lateinit var notesRepository: NotesRepository

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
        pushListManager = CommitPushListManager(this)
        pushListWindow = CommitPushListWindow(this)
        gridMenuManager = GridMenuManager(this, pushListManager)
        chatBufferManager = ChatBufferManager(this)
        ciLogDownloader = CiLogDownloader(this)

        // Initialize Notes Database
        val notesDb = NotesDatabase.getInstance(this)
        notesRepository = NotesRepository(notesDb.noteDao())

        // Load current workspace
        workspaceManager.getCurrentWorkspace()?.let { workspace ->
            paneManager.setWorkspaceState(workspace)
        }

        // Initialize Push List
        pushListWindow.initialize(pushListManager)
        gridMenuManager.initialize()

        setupHeader()
        setupWebView()
        setupBottomToolbar()
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

        // Set WebViewClient for CI log detection
        currentWebView?.webViewClient = ciLogDownloader.createWebViewClient()

        // Load URL for current pane
        paneManager.getPaneState(currentPane)?.url?.let { url ->
            currentWebView?.loadUrl(url)
        }
    }

    private fun setupBottomToolbar() {
        val attachButton = findViewById<View>(R.id.vgh_slot_attach)
        val saveButton = findViewById<View>(R.id.vgh_slot_save)
        val menuButton = findViewById<View>(R.id.vgh_slot_menu)

        attachButton.setOnClickListener {
            handleAttachRepo()
        }

        saveButton.setOnClickListener {
            handleSaveAction()
        }

        menuButton.setOnClickListener {
            handleMenuAction()
        }
    }

    private fun handleAttachRepo() {
        // PRD Section 14.1: Open bottom sheet with attached files
        // TODO: Implement bottom sheet
    }

    private fun handleSaveAction() {
        val currentPane = paneManager.getCurrentPane()
        
        when (currentPane) {
            VghPane.AI -> {
                // PRD Section 14.2: Save chat buffer
                workspaceManager.getCurrentWorkspace()?.let { workspace ->
                    val file = chatBufferManager.saveChatLog(workspace.workspaceName)
                    if (file != null) {
                        Toast.makeText(
                            this,
                            getString(R.string.vgh_chat_log_saved),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this,
                            getString(R.string.vgh_chat_log_empty),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            else -> {
                // Default: Save chat buffer
                workspaceManager.getCurrentWorkspace()?.let { workspace ->
                    val file = chatBufferManager.saveChatLog(workspace.workspaceName)
                    if (file != null) {
                        Toast.makeText(
                            this,
                            getString(R.string.vgh_chat_log_saved),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun handleMenuAction() {
        // PRD Section 14.3: Open Grid Menu
        // TODO: Implement Grid Menu dialog
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
            targetWebView?.webViewClient = ciLogDownloader.createWebViewClient()
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
        // Flush chat buffer
        chatBufferManager.flushBuffer()
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
        pushListWindow.destroy()
        // Save any remaining buffer content
        chatBufferManager.saveChatLog("Unsaved")
    }

    override fun onLowMemory() {
        super.onLowMemory()
        // Handle memory pressure
        paneManager.handleMemoryPressure()
    }
}
