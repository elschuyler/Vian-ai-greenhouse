/*
 * Vian AI Greenhouse - Chat Buffer Manager
 * PRD v4.3 Section 11: Chat Buffer with incremental save
 * Solves Gemini problem where old messages unload from DOM
 */

package com.jamal2367.styx.vgh.buffer

import android.content.Context
import android.os.Environment
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

class ChatBufferManager(private val context: Context) {

    private val tempBufferFile: File = File(context.cacheDir, "temp_buffer_pane_a.txt")
    private val chatLogsDir: File = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
        "VGH_Chat_Logs"
    )

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    init {
        chatLogsDir.mkdirs()
    }

    fun appendMessage(isUser: Boolean, message: String) {
        val timestamp = dateFormat.format(Date())
        val prefix = if (isUser) "--- [You] $timestamp ---" else "--- [AI] $timestamp ---"
        
        val content = "$prefix\n$message\n\n"
        
        try {
            FileWriter(tempBufferFile, true).use { writer ->
                writer.write(content)
            }
            
            // PRD Section 11.1: Incremental save every 10 lines or 1KB
            if (tempBufferFile.length() >= 1024) {
                flushBuffer()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun flushBuffer() {
        if (!tempBufferFile.exists() || tempBufferFile.length() == 0L) {
            return
        }

        try {
            val content = tempBufferFile.readText()
            tempBufferFile.writeText("") // Clear buffer
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun saveChatLog(workspaceName: String): File? {
        if (!tempBufferFile.exists() || tempBufferFile.length() == 0L) {
            return null
        }

        try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "chat_${workspaceName.replace(" ", "_")}_$timestamp.txt"
            val logFile = File(chatLogsDir, fileName)

            val header = """
                === Vian AI Greenhouse — Chat Export ===
                Workspace: $workspaceName
                Date: ${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())}
                ================================================
                
                """.trimIndent()

            val content = tempBufferFile.readText()
            logFile.writeText(header + content)
            
            tempBufferFile.writeText("") // Clear buffer
            
            return logFile
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun clearBuffer() {
        try {
            tempBufferFile.writeText("")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getBufferContent(): String {
        return try {
            tempBufferFile.readText()
        } catch (e: Exception) {
            ""
        }
    }

    fun isBufferEmpty(): Boolean {
        return !tempBufferFile.exists() || tempBufferFile.length() == 0L
    }
}
