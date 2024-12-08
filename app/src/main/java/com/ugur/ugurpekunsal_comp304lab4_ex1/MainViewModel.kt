package com.ugur.ugurpekunsal_comp304lab4_ex1

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.CircleOptions
import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.PolylineOptions

class MainViewModel : ViewModel() {
    var currentLocation by mutableStateOf<LatLng?>(null)
        private set
    
    var markers by mutableStateOf<List<MarkerOptions>>(emptyList())
        private set
        
    var geofenceCircle by mutableStateOf<CircleOptions?>(null)
        private set
    
    var routePolyline by mutableStateOf<PolylineOptions?>(null)
        private set
    
    var routeDistance by mutableStateOf<Double?>(null)
        private set
    
    private var routeStartPoint: LatLng? = null
    private val routeHelper = RouteHelper()

    fun updateLocation(location: LatLng) {
        currentLocation = location
    }

    fun addMarker(position: LatLng, title: String = "Marker") {
        markers = markers + MarkerOptions().position(position).title(title)
    }

    fun setGeofence(center: LatLng, radius: Double = 100.0) {
        geofenceCircle = CircleOptions()
            .center(center)
            .radius(radius)
            .strokeColor(android.graphics.Color.RED)
            .fillColor(android.graphics.Color.argb(70, 255, 0, 0))
    }

    fun handleMapClick(position: LatLng) {
        if (routeStartPoint == null) {
            routeStartPoint = position
            addMarker(position, "Start")
        } else {
            addMarker(position, "End")
            // Create route between points
            routePolyline = routeHelper.createRoute(routeStartPoint!!, position)
            routeDistance = routeHelper.calculateDistance(routeStartPoint!!, position)
            routeStartPoint = null
        }
    }
} 