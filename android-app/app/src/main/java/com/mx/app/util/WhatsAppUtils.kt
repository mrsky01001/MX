package com.mx.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri

object WhatsAppUtils {
    fun shareLocation(context: Context, latitude: Double, longitude: Double, label: String = "MX Location") {
        val uri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude($label)")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com/?q=$latitude,$longitude"))
            context.startActivity(mapIntent)
        }
    }
}
