/*
 * Vian AI Greenhouse - CI Log Downloader
 * PRD v4.3 Section 2.2: Simplified raw log download (NO buffering)
 * Detects GitHub Actions raw log URLs and auto-downloads them
 */

package com.jamal2367.styx.vgh.buffer

import android.content.Context
import android.os.Environment
import android.webkit.WebView
import android.webkit.WebViewClient
import okhttp3.*
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class CiLogDownloader(private val context: Context) {

    private val ciLogsDir: File = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
        "VGH/CI_Logs"
    )

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    init {
        ciLogsDir.mkdirs()
    }

    // Detect raw CI log URLs anywhere in the app
    fun shouldDownloadCiLog(url: String): Boolean {
        return url.contains("blob.core.windows.net") &&
               url.contains("logs") &&
               url.contains(".txt") &&
               url.contains("actions-results")
    }

    fun downloadCiLog(url: String, onComplete: (File?) -> Unit) {
        if (!shouldDownloadCiLog(url)) {
            onComplete(null)
            return
        }

        val request = Request.Builder()
            .url(url)
            .build()

        httpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onComplete(null)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    onComplete(null)
                    return
                }

                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val fileName = "ci_log_github_$timestamp.txt"
                val logFile = File(ciLogsDir, fileName)

                try {
                    response.body?.byteStream()?.use { input ->
                        FileOutputStream(logFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                    onComplete(logFile)
                } catch (e: Exception) {
                    onComplete(null)
                }
            }
        })
    }

    // WebViewClient to detect and auto-download raw logs
    fun createWebViewClient(): WebViewClient {
        return object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                url?.let {
                    if (shouldDownloadCiLog(it)) {
                        downloadCiLog(it) { file ->
                            if (file != null) {
                                // Show toast: "CI log saved to Downloads/VGH/CI_Logs"
                            }
                        }
                    }
                }
            }
        }
    }
}
