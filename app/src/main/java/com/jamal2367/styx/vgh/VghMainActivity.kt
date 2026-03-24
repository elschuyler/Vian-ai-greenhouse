/*
 * Vian AI Greenhouse - Main Activity
 * PRD v4.3: Three-pane architecture entry point
 */

package com.jamal2367.styx.vgh

import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView
import com.jamal2367.styx.R
import com.jamal2367.styx.vgh.pane.PaneTabHeader
import com.jamal2367.styx.vgh.pane.VghPane
import com.jamal2367.styx.vgh.workspace.WorkspaceSettingsBottomSheet
import com.jamal2367.styx.vgh.workspace.WorkspaceState
import com.jamal2367.styx.vgh.workspace.WorkspaceStateDefaults

class VghMainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var headerContainer: ComposeView
    private lateinit var overlayContainer: FrameLayout
    private var currentPane: VghPane = VghPane.AI
    private var showSettingsSheet: Boolean = false

    private val mockWorkspace = WorkspaceState(
        workspaceId = "1",
        workspaceName = "Default Workspace",
        aiToolEntryId = "1",
        aiToolEntryName = "Claude A",
        aiChatUrl = "https://claude.ai",
        repoUrl = "https://github.com/elschuyler/Vian-ai-greenhouse",
        branch = "master",
        pushEncodingBase64 = false
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vgh_main)

        webView = findViewById(R.id.vgh_webview)
        headerContainer = findViewById(R.id.vgh_pane_header)
        overlayContainer = findViewById(R.id.vgh_overlay_container)

        setupWebView()
        setupHeader()
    }

    private fun setupWebView() {
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.loadUrl("https://claude.ai")
    }

    private fun setupHeader() {
        headerContainer.setContent {
            PaneTabHeader(
                currentPane = currentPane,
                onPaneSelected = { pane ->
                    currentPane = pane
                    handlePaneSwitch(pane)
                },
                onWorkspaceSettingsClick = {
                    showSettingsSheet = true
                    renderSettingsSheet()
                }
            )
        }
    }

    private fun handlePaneSwitch(pane: VghPane) {
        when (pane) {
            VghPane.AI -> webView.loadUrl("https://claude.ai")
            VghPane.SOURCE -> webView.loadUrl("https://github.com/elschuyler/Vian-ai-greenhouse")
            VghPane.RESEARCH -> webView.loadUrl("https://duckduckgo.com")
        }
    }

    private fun renderSettingsSheet() {
        if (showSettingsSheet) {
            overlayContainer.visibility = View.VISIBLE
            overlayContainer.removeAllViews()
            
            val sheetView = ComposeView(this).apply {
                setContent {
                    WorkspaceSettingsBottomSheet(
                        workspaceState = mockWorkspace,
                        onDismiss = {
                            showSettingsSheet = false
                            overlayContainer.visibility = View.GONE
                        },
                        onAiToolSelected = {},
                        onRepoUrlChanged = {},
                        onBranchChanged = {},
                        onCiUrlChanged = {},
                        onPushEncodingToggled = {},
                        onPwaUrlChanged = {},
                        onManageWorkspaceClick = {}
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
        } else if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
