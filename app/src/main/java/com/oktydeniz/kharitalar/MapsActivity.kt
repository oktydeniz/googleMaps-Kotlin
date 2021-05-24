package com.oktydeniz.kharitalar

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.util.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        supportActionBar?.hide()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMapLongClickListener(myListener)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                val sharedPreferences =
                    this@MapsActivity.getSharedPreferences(
                        "com.oktydeniz.kharitalar",
                        Context.MODE_PRIVATE
                    )
                val firstTimeCheck = sharedPreferences.getBoolean("notFirstTime", false)
                if (!firstTimeCheck) {

                    mMap.clear()
                    val latLng = LatLng(location.latitude, location.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                    mMap.addMarker(MarkerOptions().position(latLng).title("You are Here"))
                    sharedPreferences.edit().putBoolean("notFirstTime", true).apply()
                }
            }

        }
        permissions()
    }

    private fun permissions() {
        if (ContextCompat.checkSelfPermission(
                applicationContext,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        } else {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                2,
                2f,
                locationListener
            )
            val intent = intent
            val status = intent.getStringExtra("status")
            if (status!! == "new") {
                val lastLocation =
                    locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (lastLocation != null) {
                    val lastLocationLatLing = LatLng(lastLocation.latitude, lastLocation.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLocationLatLing, 15f))

                }
            } else {
                mMap.clear()
                val selectedPlace = intent.getSerializableExtra("place") as Place
                val location = LatLng(selectedPlace.latitude!!, selectedPlace.longitude!!)
                mMap.addMarker(MarkerOptions().title(selectedPlace.address).position(location))
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))

            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty()) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    2,
                    2f,
                    locationListener
                )
            }
        }
    }

    private val myListener = object : GoogleMap.OnMapLongClickListener {
        override fun onMapLongClick(p0: LatLng?) {
            val geoCoder = Geocoder(this@MapsActivity, Locale.getDefault())
            var address = ""
            if (p0 != null) {
                try {
                    val addressList = geoCoder.getFromLocation(p0.latitude, p0.longitude, 1)
                    if (addressList != null && addressList.size > 0) {
                        if (addressList[0].thoroughfare != null) {
                            address += addressList[0].thoroughfare
                            if (addressList[0].subThoroughfare != null) {
                                address += " "
                                address += addressList[0].subThoroughfare
                            }
                        }
                    } else {
                        address = "New Place"
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                mMap.clear()
                mMap.addMarker(MarkerOptions().position(p0).title(address))
                val place = Place(address, p0.latitude, p0.longitude)
                val dialogBuilder = AlertDialog.Builder(this@MapsActivity)
                dialogBuilder.setCancelable(false)
                dialogBuilder.setMessage("Place : ${place.address} will saved")
                dialogBuilder.setTitle("Save This Place")
                dialogBuilder.setNegativeButton("No") { _, _ ->
                    Toast.makeText(applicationContext, "Canceled", Toast.LENGTH_SHORT).show()
                }
                dialogBuilder.setPositiveButton("Yes") { _, _ ->
                    try {
                        val db = openOrCreateDatabase("Places", Context.MODE_PRIVATE, null)
                        db.execSQL("CREATE TABLE IF NOT EXISTS places (address VARCHAR,latitude DOUBLE,longitude DOUBLE)")
                        val toCompiler =
                            "INSERT INTO places (address,latitude,longitude) VALUES (?,?,?)"
                        val sqLiteStatement = db.compileStatement(toCompiler)
                        sqLiteStatement.bindString(1, place.address)
                        sqLiteStatement.bindDouble(2, place.latitude!!)
                        sqLiteStatement.bindDouble(3, place.longitude!!)
                        sqLiteStatement.execute()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    Toast.makeText(applicationContext, "Done", Toast.LENGTH_SHORT).show()
                }
                dialogBuilder.show()
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }
}