package com.mx.app.util

import android.location.Location

object LocationUtils {

    fun distanceBetween(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lng1, lat2, lng2, results)
        return results[0].toDouble()
    }

    fun bearingBetween(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Float {
        val lat1Rad = Math.toRadians(lat1)
        val lat2Rad = Math.toRadians(lat2)
        val dLng = Math.toRadians(lng2 - lng1)

        val y = Math.sin(dLng) * Math.cos(lat2Rad)
        val x = Math.cos(lat1Rad) * Math.sin(lat2Rad) -
                Math.sin(lat1Rad) * Math.cos(lat2Rad) * Math.cos(dLng)

        var bearing = Math.toDegrees(Math.atan2(y, x)).toFloat()
        if (bearing < 0) bearing += 360f
        return bearing
    }

    fun formatCoordinate(value: Double, isLatitude: Boolean): String {
        val direction = if (isLatitude) {
            if (value >= 0) "N" else "S"
        } else {
            if (value >= 0) "E" else "W"
        }
        return "${String.format("%.6f", kotlin.math.abs(value))}° $direction"
    }
}
