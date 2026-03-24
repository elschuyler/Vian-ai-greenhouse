/*
 * Vian AI Greenhouse - Custom WebView for Pane Architecture
 * PRD v4.3 Section 21: WebView Isolation
 * Isolated context per pane, memory-efficient lifecycle
 */

package com.jamal2367.styx.vgh.pane

import android.content.Context
import android.util.AttributeSet
import android.webkit.WebView

class VghWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.webViewStyle
) : WebView(context, attrs, defStyleAttr) {

    private var paneType: VghPane = VghPane.AI
    private var isPaneActive = false

    init {
        setupWebView()
    }

    private fun setupWebView() {
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
        settings.allowFileAccess = false
        settings.allowContentAccess = false

        // Memory optimization for low-end devices (PRD v4.3)
        settings.databaseEnabled = false
        settings.appCacheEnabled = false
        settings.geolocationEnabled = false
    }

    fun setPaneType(pane: VghPane) {
        this.paneType = pane
    }

    fun getPaneType(): VghPane = paneType

    fun onPaneActivated() {
        isPaneActive = true
        onResume()
    }

    fun onPaneDeactivated() {
        isPaneActive = false
        onPause()
    }

    fun onPaneDestroyed() {
        onPaneDeactivated()
        destroy()
    }

    fun saveState(): ByteArray? {
        return saveState(android.os.Bundle()).let { bundle ->
            bundle.toByteArray()
        }
    }

    fun restoreState(data: ByteArray) {
        val bundle = android.os.Bundle()
        data.copyInto(bundle.byteArray)
        restoreState(bundle)
    }

    override fun onPause() {
        super.onPause()
        // Pause timers, animations, etc.
    }

    override fun onResume() {
        super.onResume()
        // Resume timers, animations, etc.
    }

    override fun destroy() {
        // Clear history to free memory
        clearHistory()
        clearCache(true)
        loadUrl("about:blank")
        super.destroy()
    }
}
