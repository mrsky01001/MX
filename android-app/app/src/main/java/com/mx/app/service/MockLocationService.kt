package com.mx.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import com.mx.app.MainActivity
import com.mx.app.R
import kotlinx.coroutines.*

class MockLocationService : Service() {

    companion object {
        const val CHANNEL_ID = "mx_mock_location"
        const val NOTIFICATION_ID = 1001
        const val STOPPED_NOTIFICATION_ID = 1002
        const val ACTION_START = "com.mx.app.action.START_MOCK"
        const val ACTION_STOP = "com.mx.app.action.STOP_MOCK"
        private const val TICK_INTERVAL_MS = 200L
    }

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var tickJob: Job? = null
    private var locationManager: LocationManager? = null

    private var isMocking = false
    private var mockLat = 0.0
    private var mockLng = 0.0
    private var mockName = ""

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                mockLat = intent.getDoubleExtra("latitude", 0.0)
                mockLng = intent.getDoubleExtra("longitude", 0.0)
                mockName = intent.getStringExtra("name") ?: ""

                if (mockLat != 0.0 || mockLng != 0.0) {
                    startMocking()
                }
            }
            ACTION_STOP -> {
                stopMocking()
                showStoppedNotification()
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun startMocking() {
        enableTestProviders()
        startForeground(NOTIFICATION_ID, buildActiveNotification())
        startTickLoop()
        isMocking = true
    }

    private fun stopMocking() {
        tickJob?.cancel()
        disableTestProviders()
        isMocking = false
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    private fun enableTestProviders() {
        locationManager?.let { lm ->
            try {
                lm.addTestProvider(
                    LocationManager.GPS_PROVIDER,
                    false, false, false, false,
                    true, true, true,
                    android.location.Criteria.POWER_LOW,
                    android.location.Criteria.ACCURACY_FINE
                )
                lm.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true)
            } catch (e: SecurityException) {
                e.printStackTrace()
            }

            try {
                lm.addTestProvider(
                    "fused",
                    false, false, false, false,
                    true, true, true,
                    android.location.Criteria.POWER_LOW,
                    android.location.Criteria.ACCURACY_FINE
                )
                lm.setTestProviderEnabled("fused", true)
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }

    private fun disableTestProviders() {
        locationManager?.let { lm ->
            try { lm.removeTestProvider(LocationManager.GPS_PROVIDER) } catch (_: Exception) {}
            try { lm.removeTestProvider("fused") } catch (_: Exception) {}
        }
    }

    private fun startTickLoop() {
        tickJob = scope.launch {
            while (isActive) {
                delay(TICK_INTERVAL_MS)
                injectMockLocation()
                updateNotification()
            }
        }
    }

    private fun injectMockLocation() {
        if (!isMocking) return

        val loc = Location(LocationManager.GPS_PROVIDER).apply {
            latitude = mockLat
            longitude = mockLng
            accuracy = 1.0f
            time = System.currentTimeMillis()
            elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
        }

        locationManager?.let { lm ->
            try { lm.setTestProviderLocation(LocationManager.GPS_PROVIDER, loc) } catch (_: Exception) {}
            try { lm.setTestProviderLocation("fused", loc) } catch (_: Exception) {}
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Mock Location",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows mock location status"
                setShowBadge(false)
            }
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
    }

    private fun buildActiveNotification(): Notification {
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val openPending = PendingIntent.getActivity(
            this, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, MockLocationService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPending = PendingIntent.getService(
            this, 1, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val stopAction = NotificationCompat.Action.Builder(
            R.drawable.ic_logo, "Stop", stopPending
        ).build()

        val title = if (mockName.isNotBlank()) "MX: $mockName" else "MX Mock Active"
        val text = String.format("%.6f, %.6f", mockLat, mockLng)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_logo)
            .setContentIntent(openPending)
            .setOngoing(true)
            .setSilent(true)
            .addAction(stopAction)
            .build()
    }

    private fun showStoppedNotification() {
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("show_stopped_dialog", true)
        }
        val openPending = PendingIntent.getActivity(
            this, 2, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("MX Mock Stopped")
            .setContentText("Your fake location has been stopped.")
            .setSmallIcon(R.drawable.ic_logo)
            .setContentIntent(openPending)
            .setAutoCancel(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(STOPPED_NOTIFICATION_ID, notification)
    }

    private fun updateNotification() {
        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(NOTIFICATION_ID, buildActiveNotification())
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
