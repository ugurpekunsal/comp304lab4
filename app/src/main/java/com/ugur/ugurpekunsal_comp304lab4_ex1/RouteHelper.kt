package com.ugur.ugurpekunsal_comp304lab4_ex1

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import android.graphics.Color

class RouteHelper {
    fun createRoute(start: LatLng, end: LatLng): PolylineOptions {
        // In a real app, you'd use the Google Directions API here
        // For this demo, we'll just draw a straight line
        return PolylineOptions()
            .add(start)
            .add(end)
            .width(5f)
            .color(android.graphics.Color.BLUE)
    }

    fun calculateDistance(start: LatLng, end: LatLng): Double {
        val R = 6371e3 // Earth's radius in meters
        val φ1 = Math.toRadians(start.latitude)
        val φ2 = Math.toRadians(end.latitude)
        val Δφ = Math.toRadians(end.latitude - start.latitude)
        val Δλ = Math.toRadians(end.longitude - start.longitude)

        val a = Math.sin(Δφ/2) * Math.sin(Δφ/2) +
                Math.cos(φ1) * Math.cos(φ2) *
                Math.sin(Δλ/2) * Math.sin(Δλ/2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a))

        return R * c // distance in meters
    }
} 