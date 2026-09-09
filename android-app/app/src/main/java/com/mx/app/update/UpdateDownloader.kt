package com.mx.app.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class UpdateDownloader(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val APK_DIR_NAME = "mx_updates"
        private const val APK_FILE_NAME = "mx-update.apk"
    }

    private fun getUpdateDir(): File {
        val dir = File(context.cacheDir, APK_DIR_NAME)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    private fun getApkFile(): File {
        return File(getUpdateDir(), APK_FILE_NAME)
    }

    fun getDownloadedApkFile(): File? {
        val file = getApkFile()
        return if (file.exists() && file.length() > 0) file else null
    }

    fun deleteDownloadedApk() {
        val file = getApkFile()
        if (file.exists()) file.delete()
    }

    suspend fun downloadApk(
        url: String,
        onProgress: (Int) -> Unit
    ): File? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) return@withContext null

            val body = response.body ?: return@withContext null
            val contentLength = body.contentLength()

            val file = getApkFile()
            if (file.exists()) file.delete()

            val inputStream = body.byteStream()
            val outputStream = FileOutputStream(file)

            val buffer = ByteArray(8192)
            var bytesRead: Int
            var totalBytesRead = 0L

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                totalBytesRead += bytesRead

                if (contentLength > 0) {
                    val progress = ((totalBytesRead * 100) / contentLength).toInt()
                    onProgress(progress)
                }
            }

            outputStream.flush()
            outputStream.close()
            inputStream.close()

            if (file.exists() && file.length() > 0) {
                file
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun installApk(file: File): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(
                    Uri.fromFile(file),
                    "application/vnd.android.package-archive"
                )
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }
}
