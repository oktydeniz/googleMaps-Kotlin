package com.oktydeniz.kharitalar

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ListView

class MainActivity : AppCompatActivity() {
    private var modelList = ArrayList<Place>()
    private lateinit var customAdapter: CustomAdapter
    private lateinit var listView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        listView = findViewById(R.id.listViewMain)
        getAllData()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.add_place) {
            val intent = Intent(this@MainActivity, MapsActivity::class.java)
            intent.putExtra("status", "new")
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getAllData() {
        try {
            val db = openOrCreateDatabase("Places", Context.MODE_PRIVATE, null)
            val cursor = db.rawQuery("SELECT * FROM places ", null)
            val addressIndex = cursor.getColumnIndex("address")
            val latitudeIndex = cursor.getColumnIndex("latitude")
            val longitudeIndex = cursor.getColumnIndex("longitude")
            while (cursor.moveToNext()) {
                val address = cursor.getString(addressIndex)
                val latitude = cursor.getDouble(latitudeIndex)
                val longitude = cursor.getDouble(longitudeIndex)
                val place = Place(address, latitude, longitude)
                modelList.add(place)
            }
            cursor.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        customAdapter = CustomAdapter(modelList, this)
        listView.adapter = customAdapter
        listView.setOnItemClickListener { parent, view, position, id ->
            val intent = Intent(this@MainActivity, MapsActivity::class.java)
            intent.putExtra("status", "old")
            intent.putExtra("place", modelList[position])
            startActivity(intent)
        }
    }
}