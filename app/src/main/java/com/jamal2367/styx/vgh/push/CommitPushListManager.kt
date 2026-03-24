/*
 * Vian AI Greenhouse - Commit/Push List Manager
 * PRD v4.3 Section 3 + Enhancement: Batch push multiple files at once
 * Lightweight queue management for deferred code pushes
 */

package com.jamal2367.styx.vgh.push

import android.content.Context
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class QueuedFilePush(
    val filePath: String,
    val reason: String,
    val codeContent: String,
    val tier: Int = 1,
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable

sealed class BatchPushResult {
    object Empty : BatchPushResult()
    data class Completed(
        val total: Int,
        val success: Int,
        val failed: Int,
        val results: List<PushResult>
    ) : BatchPushResult()
}

sealed class PushResult {
    data class Success(val filePath: String) : PushResult()
    data class Failure(val filePath: String, val error: String) : PushResult()
}

class CommitPushListManager(private val context: Context) {

    private val queuedFiles = mutableListOf<QueuedFilePush>()
    private var isWindowVisible = false
    private var autoScrollEnabled = true
    private var pushListEnabled = false

    fun addToQueue(filePush: QueuedFilePush) {
        queuedFiles.add(filePush)
        isWindowVisible = true
        notifyQueueUpdated()
    }

    fun removeFromQueue(index: Int) {
        if (index in queuedFiles.indices) {
            queuedFiles.removeAt(index)
            if (queuedFiles.isEmpty()) {
                isWindowVisible = false
            }
            notifyQueueUpdated()
        }
    }

    fun clearQueue() {
        queuedFiles.clear()
        isWindowVisible = false
        notifyQueueUpdated()
    }

    fun getQueuedFiles(): List<QueuedFilePush> = queuedFiles.toList()

    fun getQueueSize(): Int = queuedFiles.size

    fun isWindowVisible(): Boolean = isWindowVisible && queuedFiles.isNotEmpty()

    fun setAutoScrollEnabled(enabled: Boolean) {
        autoScrollEnabled = enabled
    }

    fun isAutoScrollEnabled(): Boolean = autoScrollEnabled

    fun setPushListEnabled(enabled: Boolean) {
        pushListEnabled = enabled
    }

    fun isPushListEnabled(): Boolean = pushListEnabled

    fun shouldShowPushListOption(): Boolean = pushListEnabled

    fun executeBatchPush(): BatchPushResult {
        if (queuedFiles.isEmpty()) {
            return BatchPushResult.Empty
        }

        val results = mutableListOf<PushResult>()
        queuedFiles.forEach { file ->
            val result = executeSinglePush(file)
            results.add(result)
        }

        val successCount = results.count { it is PushResult.Success }
        val failCount = results.count { it is PushResult.Failure }

        clearQueue()

        return BatchPushResult.Completed(
            total = queuedFiles.size,
            success = successCount,
            failed = failCount,
            results = results
        )
    }

    private fun executeSinglePush(filePush: QueuedFilePush): PushResult {
        // TODO: Integrate with Git API push logic from PRD Section 3.7
        // For now, return success placeholder
        return PushResult.Success(filePush.filePath)
    }

    private fun notifyQueueUpdated() {
        // Callback for UI updates - implemented in CommitPushListWindow
    }

    fun shouldAutoNavigateToPaneB(): Boolean {
        // PRD Section 1.3: Disable Pane B auto-navigation when Push List is active
        return !pushListEnabled
    }
}
