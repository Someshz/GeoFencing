package com.example.geofencingproject


import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class GeofenceLocationService : Service() {

    companion object {
        private const val CHANNEL_ID = "2"
        private const val NOTIFICATION_ID=12
    }
    private val mBinder: IBinder = MyBinder()
    override fun onBind(intent: Intent): IBinder? {
        return mBinder
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        requestLocationUpdates()
        buildNotification()
    }

    private fun buildNotification() {
//        val stop = "stop"
//        val broadcastIntent = PendingIntent.getBroadcast(
//            this, 0, Intent(stop), PendingIntent.FLAG_MUTABLE
//        )

        // Create the persistent notification
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("BackGround Location")
            .setContentText("Location tracking is working")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//            .setContentIntent(broadcastIntent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Location",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.setShowBadge(false)
            channel.description = "Location tracking In Background"
            channel.setSound(null, null)
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
        startForeground(NOTIFICATION_ID, builder.build())
    }

    private fun requestLocationUpdates() {

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY,1000).apply {
            setWaitForAccurateLocation(true)
        }.build()
//        request.interval = 1000
//        request.fastestInterval = 3000
//        request.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        val client = LocationServices.getFusedLocationProviderClient(this)
        val permission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (permission == PackageManager.PERMISSION_GRANTED) {
            client.requestLocationUpdates(request, object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    val location = "Latitude : " + locationResult.lastLocation!!
                        .latitude +
                            "\nLongitude : " + locationResult.lastLocation!!.longitude
                Toast.makeText(this@GeofenceLocationService, location, Toast.LENGTH_SHORT).show()
                }
            }, null)
        } else {
            stopSelf()
        }
    }

    inner class MyBinder : Binder() {
        val service: GeofenceLocationService
            get() = this@GeofenceLocationService
    }


}