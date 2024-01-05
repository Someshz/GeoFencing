package com.example.geofencingproject

import android.Manifest
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    lateinit var fab: FloatingActionButton
    var geoLocationList = mutableListOf<GeoLocation>()
    lateinit var fragmentManager: FragmentManager
    lateinit var transaction: FragmentTransaction
    lateinit var geofencingClient: GeofencingClient
    lateinit var locationManager: LocationManager
    lateinit var locationIntent: Intent
    val FINE_PERMISSION_CODE: Int = 1
    val BACKGROUND_PERMISSION_CODE: Int = 2
    val NOTIFICATION_PERMISSION_CODE: Int = 3
    lateinit var serviceIntent: Intent


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        geofencingClient = LocationServices.getGeofencingClient(this)
        fragmentManager = supportFragmentManager
        serviceIntent = Intent(this, GeofenceLocationService::class.java)
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        locationIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        val fragment = ListFragment()
        openMyFragment(fragment, 0)
        fab = findViewById(R.id.fab)
        fab.setOnClickListener {
            val fragment1 = AddLocation()
            openMyFragment(fragment1, 1)
            fab.visibility = View.GONE
        }

        if (checkPermission()) {
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                startActivity(locationIntent)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                startForegroundService(serviceIntent)
            else
                startService(serviceIntent)
        } else {
            requestForFineLocation()
        }
    }

    override fun onResume() {
        super.onResume()
        CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    startActivity(locationIntent)
                    break
                }
                delay(5000)
            }
        }
    }

    private fun checkPermission(): Boolean {
        if ((ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
                    ) && (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
                    ) && (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
                    )
        ) {
            return true
        }
        return false
    }

    private fun openMyFragment(fragment: Fragment, flag: Int) {
        transaction = fragmentManager.beginTransaction()
        if (flag == 0) {
            transaction.replace(R.id.framelayout, fragment)
            transaction.addToBackStack(null)
        } else {
            transaction.replace(R.id.framelayout, fragment)
            transaction.addToBackStack(null)
        }
        transaction.commit()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        fab.visibility = View.VISIBLE
        val fragment = ListFragment()
        openMyFragment(fragment, 0)
    }

    fun addDataToList(geoLocation: GeoLocation) {
        geoLocationList.add(geoLocation)

        val geoFence = Geofence.Builder()
            .setRequestId(geoLocation.latitude)
            .setCircularRegion(
                geoLocation.latitude.toDouble(),
                geoLocation.longitude.toDouble(),
                geoLocation.radius.toFloat()
            )
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .build()

        val geoFencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_EXIT or GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geoFence)
            .build()

        val broadcastintent = Intent(this, GeoFencingBroadcastReceiver::class.java)

        val bundle = Bundle()
        Log.d(
            "hey1234",
            geoLocation.latitude + " " + geoLocation.longitude + " " + geoLocation.radius
        )
        bundle.putString("lat", geoLocation.latitude)
        bundle.putString("lon", geoLocation.longitude)
        bundle.putString("radius", geoLocation.radius)
        broadcastintent.putExtras(bundle)
        val pendingIntent = getGeofencePendingIntent(broadcastintent)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            geofencingClient.addGeofences(geoFencingRequest, pendingIntent)
                .addOnSuccessListener {

                }
                .addOnFailureListener {

                }
        }

        geoLocationList.forEach { geoLocation ->
            Log.d(
                "hey12",
                "${geoLocation.latitude} ${geoLocation.longitude} ${geoLocation.radius}"
            )
        }
    }

    private fun getGeofencePendingIntent(broadcastintent: Intent): PendingIntent {

        return PendingIntent.getBroadcast(
            this,
            (Math.random() * 1000 + 1).toInt(),
            broadcastintent,
            PendingIntent.FLAG_MUTABLE
        )
    }

    private fun requestForFineLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                FINE_PERMISSION_CODE
            )
        } else {
            requestForBackgroundLocation()
        }
    }


    private fun requestForBackgroundLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                    val builder =
                        AlertDialog.Builder(this)
                    builder.setTitle("This app needs background location access")
                    builder.setMessage("Please grant location access so this app can detect beacons in the background. or close the app and open again")
                    builder.setPositiveButton(android.R.string.ok, null)
                    builder.setOnDismissListener {
                        requestPermissions(
                            arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                            BACKGROUND_PERMISSION_CODE
                        )
                    }
                    builder.show()
                }
            } else {
                requestForNotification()
            }
        }
    }

    private fun requestForNotification() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                NOTIFICATION_PERMISSION_CODE
            )

        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            FINE_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        startActivity(locationIntent)
                    }
                    requestForBackgroundLocation()
                } else {
                    requestForNotification()
                }

            }

            BACKGROUND_PERMISSION_CODE -> {
                requestForNotification()
            }

            NOTIFICATION_PERMISSION_CODE -> {
                if (checkPermission()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        startForegroundService(serviceIntent)
                    else
                        startService(serviceIntent)
                } else {
                    val builder =
                        AlertDialog.Builder(this)
                    builder.setTitle("Functionality limited")
                    builder.setMessage("Please open this app again since you haven't given proper permission. this app will not work properly")
                    builder.setPositiveButton(android.R.string.ok, null)
                    builder.setOnDismissListener {
                        finish()
                    }
                    builder.show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        geofencingClient.removeGeofences(geoLocationList.map { it.latitude })
            .run {
                addOnSuccessListener {

                }
                addOnFailureListener {

                }
            }

        stopService(serviceIntent)
    }


}