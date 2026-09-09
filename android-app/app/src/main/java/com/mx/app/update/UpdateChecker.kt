package com.mx.app.update

import android.content.Context
import android.content.pm.PackageManager
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

data class GitHubRelease(
    @SerializedName("tag_name") val tagName: String,
    @SerializedName("assets") val assets: List<GitHubAsset>
)

data class GitHubAsset(
    @SerializedName("name") val name: String,
    @SerializedName("browser_download_url") val downloadUrl: String
)

class UpdateChecker(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    companion object {
        private const val REPO_OWNER = "mrsky01001"
        private const val REPO_NAME = "MX"
        private const val APK_FILE_NAME = "app-debug.apk"
    }

    private fun getCurrentVersionCode(): Int {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                packageInfo.longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode
            }
        } catch (e: PackageManager.NameNotFoundException) {
            1
        }
    }

    private fun extractVersionFromTag(tag: String): Int {
        val cleaned = tag.trim().removePrefix("v").removePrefix("V")
        val parts = cleaned.split(".")
        return when {
            parts.size >= 3 -> {
                val major = parts[0].toIntOrNull() ?: 0
                val minor = parts[1].toIntOrNull() ?: 0
                val patch = parts[2].toIntOrNull() ?: 0
                major * 10000 + minor * 100 + patch
            }
            parts.size == 2 -> {
                val major = parts[0].toIntOrNull() ?: 0
                val minor = parts[1].toIntOrNull() ?: 0
                major * 100 + minor
            }
            parts.size == 1 -> parts[0].toIntOrNull() ?: 1
            else -> 1
        }
    }

    suspend fun checkForUpdate(): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("https://api.github.com/repos/$REPO_OWNER/$REPO_NAME/releases/latest")
                .header("Accept", "application/vnd.github.v3+json")
                .build()

            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val body = response.body?.string() ?: return@withContext null
                val release = gson.fromJson(body, GitHubRelease::class.java)

                val remoteVersionCode = extractVersionFromTag(release.tagName)
                val currentVersionCode = getCurrentVersionCode()

                val apkAsset = release.assets.find { it.name == APK_FILE_NAME }

                if (remoteVersionCode > currentVersionCode && apkAsset != null) {
                    UpdateInfo(
                        versionName = release.tagName,
                        versionCode = remoteVersionCode,
                        downloadUrl = apkAsset.downloadUrl,
                        currentVersionCode = currentVersionCode
                    )
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}

data class UpdateInfo(
    val versionName: String,
    val versionCode: Int,
    val downloadUrl: String,
    val currentVersionCode: Int
)
