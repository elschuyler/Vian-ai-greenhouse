/*
 * Vian AI Greenhouse - Commit/Push List Floating Window
 * PRD v4.3 Enhancement: Lightweight batch push UI
 * Appears only when queue has items, dormant otherwise
 */

package com.jamal2367.styx.vgh.push

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jamal2367.styx.R

class CommitPushListWindow(private val context: Context) {

    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private var recyclerView: RecyclerView? = null
    private var pushButton: TextView? = null
    private var countLabel: TextView? = null
    private var closeButton: TextView? = null
    private val adapter = QueuedFilesAdapter()
    private var manager: CommitPushListManager? = null

    fun initialize(manager: CommitPushListManager) {
        this.manager = manager
        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    fun show() {
        if (floatingView != null) return

        floatingView = LayoutInflater.from(context).inflate(R.layout.vgh_floating_push_list, null)

        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.END
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
        }

        recyclerView = floatingView?.findViewById(R.id.vgh_queue_recycler)
        pushButton = floatingView?.findViewById(R.id.vgh_batch_push_button)
        countLabel = floatingView?.findViewById(R.id.vgh_queue_count_label)
        closeButton = floatingView?.findViewById(R.id.vgh_close_button)

        recyclerView?.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@CommitPushListWindow.adapter
        }

        pushButton?.setOnClickListener {
            manager?.executeBatchPush()
        }

        closeButton?.setOnClickListener {
            manager?.clearQueue()
            hide()
        }

        updateUI()

        try {
            windowManager?.addView(floatingView, params)
        } catch (e: Exception) {
            // Window already added or context issue
        }
    }

    fun hide() {
        floatingView?.let { view ->
            try {
                windowManager?.removeView(view)
            } catch (e: Exception) {
                // View not attached
            }
            floatingView = null
        }
    }

    fun updateUI() {
        val queueSize = manager?.getQueueSize() ?: 0

        if (queueSize > 0) {
            show()
            countLabel?.text = context.getString(R.string.vgh_queue_count, queueSize)
            adapter.submitList(manager?.getQueuedFiles())

            pushButton?.text = if (queueSize == 1) {
                context.getString(R.string.vgh_push_now)
            } else {
                context.getString(R.string.vgh_batch_push, queueSize)
            }
        } else {
            hide()
        }
    }

    fun destroy() {
        hide()
        manager = null
    }

    private inner class QueuedFilesAdapter : RecyclerView.Adapter<QueuedFilesAdapter.ViewHolder>() {

        private var files: List<QueuedFilePush> = emptyList()

        fun submitList(newFiles: List<QueuedFilePush>) {
            files = newFiles
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.vgh_queue_item, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val file = files[position]
            holder.fileName.text = file.filePath.substringAfterLast('/')
            holder.fileReason.text = file.reason
            holder.removeButton.setOnClickListener {
                manager?.removeFromQueue(position)
                updateUI()
            }
        }

        override fun getItemCount(): Int = files.size

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val fileName: TextView = itemView.findViewById(R.id.vgh_queue_file_name)
            val fileReason: TextView = itemView.findViewById(R.id.vgh_queue_file_reason)
            val removeButton: TextView = itemView.findViewById(R.id.vgh_queue_remove_button)
        }
    }
}
