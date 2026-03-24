/*
 * Vian AI Greenhouse - JavaScript Bridge for Tag Detection
 * PRD v4.3 Section 3.4: Unified MutationObserver Implementation
 * Monitors Pane A WebView for VGH tags and injects push buttons
 */

package com.jamal2367.styx.vgh.bridge

import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.jamal2367.styx.vgh.push.CommitPushListManager
import com.jamal2367.styx.vgh.push.QueuedFilePush

class VghJavaScriptBridge(
    private val webView: WebView,
    private val pushListManager: CommitPushListManager
) {

    private val tagParser = VghTagParser()
    private var autoCaptureEnabled = false

    fun enableAutoCapture(enabled: Boolean) {
        autoCaptureEnabled = enabled
        pushListManager.setPushListEnabled(enabled)
        injectMutationObserver()
    }

    fun isAutoCaptureEnabled(): Boolean = autoCaptureEnabled

    @JavascriptInterface
    fun processVghTags(htmlContent: String): String {
        val pushTags = tagParser.parsePushTags(htmlContent)
        val attachTags = tagParser.parseAttachTags(htmlContent)

        val result = StringBuilder()

        pushTags.forEach { tag ->
            val validation = tagParser.validateTagFormat(tag.filePath, tag.codeContent)
            if (validation is VghTagParser.TagValidationResult.Valid) {
                if (pushListManager.shouldShowPushListOption()) {
                    // Add to queue option
                    result.append("<button onclick=\"VghBridge.addToQueue('${tag.filePath}', '${tag.reason}', '${escapeJs(tag.codeContent)}')\">📋 Add to Push List</button>")
                }
                result.append("<button onclick=\"VghBridge.pushNow('${tag.filePath}', '${tag.reason}', '${escapeJs(tag.codeContent)}')\">🔨 Push to: ${tag.filePath}</button>")
            }
        }

        attachTags.forEach { tag ->
            if (autoCaptureEnabled) {
                result.append("<button onclick=\"VghBridge.attachRepo('${tag.url}')\">📎 Attached: ${tag.url}</button>")
            } else {
                result.append("<button onclick=\"VghBridge.attachRepo('${tag.url}')\">📎 Save Link</button>")
            }
        }

        return result.toString()
    }

    @JavascriptInterface
    fun addToQueue(filePath: String, reason: String, codeContent: String) {
        val filePush = QueuedFilePush(
            filePath = filePath,
            reason = reason,
            codeContent = codeContent
        )
        pushListManager.addToQueue(filePush)
    }

    @JavascriptInterface
    fun pushNow(filePath: String, reason: String, codeContent: String) {
        // Immediate push - will be handled by native layer
        // TODO: Integrate with Git API
    }

    @JavascriptInterface
    fun attachRepo(url: String) {
        // TODO: Download and attach repo file
    }

    @JavascriptInterface
    fun getAutoScrollEnabled(): Boolean {
        return pushListManager.isAutoScrollEnabled()
    }

    @JavascriptInterface
    fun shouldAutoNavigateToPaneB(): Boolean {
        return pushListManager.shouldAutoNavigateToPaneB()
    }

    private fun escapeJs(text: String): String {
        return text.replace("\\", "\\\\")
            .replace("'", "\\'")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
    }

    private fun injectMutationObserver() {
        val observerScript = """
            (function() {
                if (window.VghObserverAttached) return;
                window.VghObserverAttached = true;

                const observer = new MutationObserver((mutations) => {
                    let debounceTimer = window.VghDebounceTimer;
                    if (debounceTimer) clearTimeout(debounceTimer);

                    window.VghDebounceTimer = setTimeout(() => {
                        const content = document.body.innerText;
                        const result = VghBridge.processVghTags(content);
                        if (result) {
                            // Inject buttons into DOM
                        }
                    }, 100);
                });

                observer.observe(document.body, {
                    childList: true,
                    subtree: true,
                    characterData: true
                });
            })();
        """

        webView.post {
            webView.evaluateJavascript(observerScript, null)
        }
    }
}
