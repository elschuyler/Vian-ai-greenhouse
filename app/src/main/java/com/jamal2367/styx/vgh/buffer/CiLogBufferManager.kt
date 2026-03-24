/*
 * Vian AI Greenhouse - CI Log Buffer Manager
 * PRD v4.3 Section 2.2: CI Log Buffering with 3MB chunking
 */

package com.jamal2367.styx.vgh.buffer

import android.content.Context
import android.os.Environment
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

class CiLogBufferManager(private val context: Context) {

    private val tempBufferFile: File = File(context.cacheDir, "temp_buffer_ci.txt")
    private val ciLogsDir: File = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
        "VGH_CI_Logs"
    )

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private var currentChunkSize: Long = 0
    private val MAX_CHUNK_SIZE = 3 * 1024 * 1024 // 3MB

    init {
        ciLogsDir.mkdirs()
    }

    fun appendLogLine(line: String) {
        try {
            FileWriter(tempBufferFile, true).use { writer ->
                writer.write(line + "\n")
                currentChunkSize += line.length + 1
            }

            // PRD Section 2.2: 3MB CI Log Chunking
            if (currentChunkSize >= MAX_CHUNK_SIZE) {
                saveAndRotateChunk()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveAndRotateChunk() {
        if (!tempBufferFile.exists() || tempBufferFile.length() == 0L) {
            return
        }

        try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val chunkNumber = getChunkCount() + 1
            val fileName = "ci_log_part${chunkNumber}_$timestamp.txt"
            val chunkFile = File(ciLogsDir, fileName)

            val content = tempBufferFile.readText()
            chunkFile.writeText(content)

            tempBufferFile.writeText("") // Clear buffer
            currentChunkSize = 0
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun saveCiLog(): File? {
        if (!tempBufferFile.exists() || tempBufferFile.length() == 0L) {
            return null
        }

        try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "ci_log_final_$timestamp.txt"
            val logFile = File(ciLogsDir, fileName)

            val content = tempBufferFile.readText()
            logFile.writeText(content)

            tempBufferFile.writeText("") // Clear buffer
            currentChunkSize = 0

            return logFile
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun getChunkCount(): Int {
        return ciLogsDir.listFiles()?.count { it.name.startsWith("ci_log_part") } ?: 0
    }

    fun clearBuffer() {
        try {
            tempBufferFile.writeText("")
            currentChunkSize = 0
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
}
