package com.utp.finalproject

import android.app.Activity
import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.utp.finalproject.databinding.ActivityLocationPickerBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class LocationPickerActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityLocationPickerBinding
    private var selectedPoint: LatLng? = null
    private var marker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationPickerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.cancelLocationButton.setOnClickListener { finish() }
        binding.confirmLocationButton.setOnClickListener { confirmSelection() }

        if (!BuildConfig.MAPS_API_KEY_CONFIGURED) {
            binding.mapContainer.visibility = View.GONE
            binding.mapConfigurationMessage.visibility = View.VISIBLE
            binding.confirmLocationButton.isEnabled = false
            return
        }

        val fragment = SupportMapFragment.newInstance()
        supportFragmentManager.beginTransaction()
            .replace(R.id.mapContainer, fragment)
            .commit()
        fragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        val initialLatitude = intent.getDoubleExtra(EXTRA_INITIAL_LATITUDE, DEFAULT_LATITUDE)
        val initialLongitude = intent.getDoubleExtra(EXTRA_INITIAL_LONGITUDE, DEFAULT_LONGITUDE)
        val initialPoint = LatLng(initialLatitude, initialLongitude)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(initialPoint, DEFAULT_ZOOM))

        if (intent.hasExtra(EXTRA_INITIAL_LATITUDE) && intent.hasExtra(EXTRA_INITIAL_LONGITUDE)) {
            selectPoint(map, initialPoint)
            binding.locationNameInput.setText(intent.getStringExtra(EXTRA_INITIAL_NAME).orEmpty())
        }

        map.setOnMapClickListener { point -> selectPoint(map, point) }
    }

    private fun selectPoint(map: GoogleMap, point: LatLng) {
        selectedPoint = point
        marker?.remove()
        marker = map.addMarker(MarkerOptions().position(point))
        binding.selectedCoordinatesText.text = getString(
            R.string.selected_coordinates,
            point.latitude,
            point.longitude
        )
        reverseGeocode(point)
    }

    @Suppress("DEPRECATION")
    private fun reverseGeocode(point: LatLng) {
        lifecycleScope.launch {
            val address = withContext(Dispatchers.IO) {
                runCatching {
                    Geocoder(this@LocationPickerActivity, Locale.getDefault())
                        .getFromLocation(point.latitude, point.longitude, 1)
                        ?.firstOrNull()
                        ?.getAddressLine(0)
                }.getOrNull()
            }
            if (!address.isNullOrBlank()) {
                binding.locationNameInput.setText(address)
            }
        }
    }

    private fun confirmSelection() {
        val point = selectedPoint
        if (point == null) {
            Toast.makeText(this, R.string.select_location_first, Toast.LENGTH_SHORT).show()
            return
        }
        val name = binding.locationNameInput.text.toString().trim().ifBlank {
            getString(R.string.location_selected)
        }
        setResult(
            Activity.RESULT_OK,
            Intent().apply {
                putExtra(EXTRA_RESULT_NAME, name)
                putExtra(EXTRA_RESULT_LATITUDE, point.latitude)
                putExtra(EXTRA_RESULT_LONGITUDE, point.longitude)
                putExtra(EXTRA_RESULT_PLACE_ID, "")
            }
        )
        finish()
    }

    companion object {
        const val EXTRA_INITIAL_NAME = "com.utp.finalproject.extra.INITIAL_LOCATION_NAME"
        const val EXTRA_INITIAL_LATITUDE = "com.utp.finalproject.extra.INITIAL_LATITUDE"
        const val EXTRA_INITIAL_LONGITUDE = "com.utp.finalproject.extra.INITIAL_LONGITUDE"
        const val EXTRA_RESULT_NAME = "com.utp.finalproject.extra.LOCATION_NAME"
        const val EXTRA_RESULT_LATITUDE = "com.utp.finalproject.extra.LATITUDE"
        const val EXTRA_RESULT_LONGITUDE = "com.utp.finalproject.extra.LONGITUDE"
        const val EXTRA_RESULT_PLACE_ID = "com.utp.finalproject.extra.PLACE_ID"

        private const val DEFAULT_LATITUDE = 8.9824
        private const val DEFAULT_LONGITUDE = -79.5199
        private const val DEFAULT_ZOOM = 12f
    }
}
