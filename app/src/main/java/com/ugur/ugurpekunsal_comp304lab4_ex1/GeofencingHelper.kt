package com.ugur.ugurpekunsal_comp304lab4_ex1

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng

class GeofencingHelper(private val context: Context) {
    private val geofencingClient = LocationServices.getGeofencingClient(context)

    fun createGeofence(
        location: LatLng,
        radius: Float = 100f, // 100 meters
        id: String = "GEOFENCE_ID"
    ): Geofence {
        return Geofence.Builder()
            .setCircularRegion(location.latitude, location.longitude, radius)
            .setRequestId(id)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .build()
    }

    fun getGeofencingRequest(geofence: Geofence): GeofencingRequest {
        return GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()
    }

    fun getPendingIntent(): PendingIntent {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }
} 