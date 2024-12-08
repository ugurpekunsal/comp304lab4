package com.ugur.ugurpekunsal_comp304lab4_ex1

import android.Manifest
import android.os.Bundle
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.work.*
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import java.util.concurrent.TimeUnit
import android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.ui.graphics.Color

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var geofencingHelper: GeofencingHelper
    
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                // Precise location granted
                startLocationUpdates()
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                // Approximate location granted
                startLocationUpdates()
            }
            else -> {
                // No location access granted
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        geofencingClient = LocationServices.getGeofencingClient(this)
        
        geofencingHelper = GeofencingHelper(this)
        
        // Schedule background work
        scheduleBackgroundWork()
        
        // Request location permissions
        requestLocationPermissions()

        setContent {
            MainScreen(
                viewModel = viewModel,
                onGeofenceRequest = { location ->
                    addGeofence(location)
                }
            )
        }
    }

    private fun requestLocationPermissions() {
        locationPermissionRequest.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))
    }

    private fun startLocationUpdates() {
        try {
            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                10000 // 10 seconds
            ).build()

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                mainLooper
            )
        } catch (e: SecurityException) {
            // Handle exception
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            for (location in locationResult.locations) {
                viewModel.updateLocation(LatLng(location.latitude, location.longitude))
            }
        }
    }

    private fun scheduleBackgroundWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val locationSyncRequest = PeriodicWorkRequestBuilder<LocationSyncWorker>(
            15, TimeUnit.MINUTES
        ).setConstraints(constraints).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "locationSync",
            ExistingPeriodicWorkPolicy.UPDATE,
            locationSyncRequest
        )
    }

    private fun addGeofence(location: LatLng) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (checkSelfPermission(ACCESS_BACKGROUND_LOCATION) != 
                PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(ACCESS_BACKGROUND_LOCATION), 
                    BACKGROUND_LOCATION_PERMISSION_CODE)
                return
            }
        }

        try {
            val geofence = geofencingHelper.createGeofence(location)
            val geofencingRequest = geofencingHelper.getGeofencingRequest(geofence)
            val pendingIntent = geofencingHelper.getPendingIntent()

            geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                .addOnSuccessListener {
                    viewModel.setGeofence(location)
                }
                .addOnFailureListener {
                    // Handle failure
                }
        } catch (e: SecurityException) {
            // Handle exception
        }
    }

    companion object {
        private const val BACKGROUND_LOCATION_PERMISSION_CODE = 2
    }
}

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onGeofenceRequest: (LatLng) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        val currentLocation = viewModel.currentLocation
        val markers = viewModel.markers
        val geofenceCircle = viewModel.geofenceCircle
        
        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(
                currentLocation ?: LatLng(43.7851, -79.2266), // Default to Centennial College
                15f
            )
        }

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = currentLocation != null),
            onMapClick = { viewModel.handleMapClick(it) },
            onMapLongClick = { onGeofenceRequest(it) }
        ) {
            // Draw markers
            markers.forEach { markerOptions ->
                Marker(
                    state = MarkerState(position = markerOptions.position),
                    title = markerOptions.title
                )
            }

            // Draw geofence circle if exists
            geofenceCircle?.let { circle ->
                Circle(
                    center = circle.center!!,
                    radius = circle.radius,
                    strokeColor = Color(circle.strokeColor),
                    fillColor = Color(circle.fillColor)
                )
            }

            // Draw route if exists
            viewModel.routePolyline?.let { polyline ->
                Polyline(
                    points = polyline.points,
                    color = Color(polyline.color),
                    width = polyline.width
                )
            }
        }

        // Distance information overlay
        viewModel.routeDistance?.let { distance ->
            Card(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopCenter),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = "Distance: ${String.format("%.2f", distance/1000)} km",
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}