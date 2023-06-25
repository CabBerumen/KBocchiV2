package com.example.kbocchiv2

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import java.io.IOException
import java.util.*

class Maps : AppCompatActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var originEditText: TextView
    private lateinit var destinationEditText: EditText
    private lateinit var ubicacionActualBtn: Button
    private var isLocationPermissionGranted = false

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        originEditText = findViewById(R.id.originEditText)
        destinationEditText = findViewById(R.id.destinationEditText)
        val searchButton: Button = findViewById(R.id.searchButton)
        ubicacionActualBtn = findViewById(R.id.currentLocationButton)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createLocationRequest()

        searchButton.setOnClickListener { searchRoute() }
        ubicacionActualBtn.setOnClickListener { requestLocationPermission() }
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult?.lastLocation?.let { lastLocation ->
                    getAddressFromLocation(lastLocation)
                    stopLocationUpdates()
                }
            }
        }
    }

    private fun searchRoute() {
        val origin = originEditText.text.toString().trim()
        val destination = destinationEditText.text.toString().trim()

        if (origin.isEmpty() || destination.isEmpty()) {
            Toast.makeText(this, "Ingresa una ubicación de origen y destino", Toast.LENGTH_SHORT).show()
            return
        }
        val uri = Uri.parse("https://www.google.com/maps/dir/$origin/$destination")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.google.android.apps.maps")
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)

        if (intent.resolveActivity(packageManager) == null) {
            Toast.makeText(this, "No se encontró la aplicación Google Maps", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isLocationPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        if (isLocationPermissionGranted()) {
            startLocationUpdates()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun showDialogForPermission() {
        AlertDialog.Builder(this)
            .setTitle("Permiso de ubicación")
            .setMessage("Se necesita el permiso de ubicación para proporcionar la ubicación actual")
            .setPositiveButton("Aceptar") { dialog, _ ->
                dialog.dismiss()
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
                Toast.makeText(
                    this,
                    "No se otorgó el permiso de ubicación",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .show()
    }

    private fun startLocationUpdates() {
        if (isLocationPermissionGranted()) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationClient.lastLocation?.addOnSuccessListener { location ->
                    if (location != null) {
                        getAddressFromLocation(location)
                    }
                }
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
            } else {
                showDialogForPermission()
            }
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun getAddressFromLocation(location: Location) {
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses: List<Address>? = try {
            geocoder.getFromLocation(location.latitude, location.longitude, 1)
        } catch (e: IOException) {
            e.printStackTrace()
            emptyList()
        }

        if (addresses != null && addresses.isNotEmpty()) {
            val address: Address = addresses[0]
            val addressText = address.getAddressLine(0)
            runOnUiThread {
                originEditText.text = addressText
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (isLocationPermissionGranted) {
            startLocationUpdates()
        }
    }

    override fun onStop() {
        super.onStop()
        stopLocationUpdates()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                isLocationPermissionGranted = true
                startLocationUpdates()
            } else {
                Toast.makeText(
                    this,
                    "No se otorgó el permiso de ubicación. Puedes otorgarlo en la configuración de la aplicación.",
                    Toast.LENGTH_LONG
                ).show()

                val appSettingsIntent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + packageName)
                )
                startActivity(appSettingsIntent)
            }
        }
    }
}