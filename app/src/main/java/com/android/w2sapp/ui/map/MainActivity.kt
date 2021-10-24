package com.android.w2sapp.ui.map

import android.Manifest
import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.android.w2sapp.R
import com.android.w2sapp.data.models.ResourceStatus
import com.android.w2sapp.databinding.ActivityMainBinding
import com.android.w2sapp.ui.base.BaseActivity
import com.android.w2sapp.ui.posts.PostsDialogFragment
import com.android.w2sapp.utils.action
import com.android.w2sapp.utils.getQueryTextChangeStateFlow
import com.android.w2sapp.utils.hideKeyboard
import com.android.w2sapp.utils.snack
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.progressindicator.CircularProgressIndicator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

@AndroidEntryPoint
class MainActivity : BaseActivity(), OnMapReadyCallback, EasyPermissions.PermissionCallbacks {

    private lateinit var mainViewModel: MainViewModel
    private lateinit var binding: ActivityMainBinding

    private var mMap: GoogleMap? = null
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var locationPermissionGranted = false
    private var lastKnownLocation: LatLng? = null

    private var searchView: SearchView? = null
    private var searchJob: Job? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        mainViewModel =
            ViewModelProvider(this).get(MainViewModel::class.java)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        requestLocationPermission()
        setObservers()
    }

    private fun setObservers() {
        mainViewModel.directions.observe(this) { resource ->
            if (resource != null) {
                searchJob?.cancel()
                when (resource.status) {
                    ResourceStatus.LOADING -> {
                        binding.progressCircular.show()
                    }
                    ResourceStatus.SUCCESS -> {
                        binding.progressCircular.hide()
                        mMap?.clear()
                        val rectLine = PolylineOptions().width(10f).color(
                            ContextCompat.getColor(
                                applicationContext, R.color.colorPrimary
                            )
                        )
                        resource.data?.steps?.forEach {
                            rectLine.add(it)
                        }
                        mMap?.addPolyline(rectLine)

                        mMap?.addMarker(
                            MarkerOptions().title(getString(R.string.origin))
                                .position(resource.data?.steps?.first())
                        )
                        mMap?.addMarker(
                            MarkerOptions().title(getString(R.string.destination))
                                .position(resource.data?.steps?.last())
                        )
                        updateCameraToLastKnowLocation()
                        hideKeyboard()
                    }
                    ResourceStatus.EMPTY -> {
                        binding.progressCircular.hide()
                        mMap?.clear()
                        showSnackBar(getString(R.string.no_routes_found))
                    }
                    ResourceStatus.ERROR -> {
                        binding.progressCircular.hide()
                        mMap?.clear()
                        showSnackBar(getString(R.string.something_went_wrong))
                    }
                    ResourceStatus.OFFLINE_ERROR -> {
                        binding.progressCircular.hide()
                        mMap?.clear()
                        showSnackBar(getString(R.string.you_are_offline))
                    }
                }
            }

        }
    }

    private fun showSnackBar(message: String) {
        binding.root.snack(message) {
            action(getString(R.string.okay), R.color.black) {
                dismiss()
            }
        }
        hideKeyboard()
    }

    private fun initMap() {
        if (mMap == null) {
            val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.getMapAsync(this)
        }
    }

    @FlowPreview
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        searchView = menu?.findItem(R.id.action_search)?.actionView as SearchView?
        setUpSearchListener()
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_posts) {
            showBottomSheet()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showBottomSheet() {
        PostsDialogFragment.newInstance()
            .show(supportFragmentManager, null)
    }

    override fun onBackPressed() {
        if (searchView?.isIconified == false) {
            searchView?.setQuery("", false)
            searchView?.isIconified = true
            return
        }
        super.onBackPressed()
    }

    override fun onStop() {
        searchJob?.cancel()
        super.onStop()
    }

    @FlowPreview
    private fun setUpSearchListener() {
        lifecycleScope.launch {
            searchView?.getQueryTextChangeStateFlow()
                ?.debounce(1000)
                ?.distinctUntilChanged()
                ?.flowOn(Dispatchers.Default)
                ?.collect { q: String? ->
                    if (!q.isNullOrBlank()) {
                        searchJob?.cancel()
                        searchJob = lifecycleScope.launch {
                            val destination =
                                mainViewModel.getLocationFromAddress(Geocoder(this@MainActivity), q)
                            mainViewModel.getDirections(
                                "${lastKnownLocation?.latitude.toString()},${lastKnownLocation?.longitude.toString()}",
                                "${destination?.latitude.toString()},${destination?.longitude.toString()}"
                            )
                        }
                    }
                }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        updateLocationUI()
        getDeviceLocation()
    }

    private fun getDeviceLocation() {
        try {
            if (locationPermissionGranted) {
                val locationResult = fusedLocationProviderClient?.lastLocation
                locationResult?.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful && task.result != null
                        && task.result.latitude > 0.0
                        && task.result.longitude > 0.0
                    ) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = LatLng(task.result.latitude, task.result.longitude)
                        updateCameraToLastKnowLocation()
                    } else {
                        Log.e("sfsf", "")
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    private fun updateCameraToLastKnowLocation() {
        if (lastKnownLocation?.latitude != null
            && lastKnownLocation?.longitude != null
        ) {
            mMap?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        lastKnownLocation?.latitude!!,
                        lastKnownLocation?.longitude!!
                    ), DEFAULT_ZOOM
                )
            )
        }
    }

    private fun updateLocationUI() {
        try {
            if (locationPermissionGranted) {
                mMap?.isMyLocationEnabled = true
                mMap?.uiSettings?.isMyLocationButtonEnabled = true
            } else {
                mMap?.isMyLocationEnabled = false
                mMap?.uiSettings?.isMyLocationButtonEnabled = false
                requestLocationPermission()
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        locationPermissionGranted = true
        initMap()
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            locationPermissionGranted = false
            AppSettingsDialog.Builder(this).build().show()
        }
    }

    @AfterPermissionGranted(REQ_CODE_LOCATION_PERMISSION)
    private fun requestLocationPermission() {
        val perms =
            arrayOf<String>(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        locationPermissionGranted = false
        if (EasyPermissions.hasPermissions(this, *perms)) {
            // Already have permission, do the thing
            locationPermissionGranted = true
            initMap()
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(
                this, getString(R.string.please_provide_loaction_permission),
                REQ_CODE_LOCATION_PERMISSION, *perms
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            requestLocationPermission()
        }
    }

    companion object {
        const val REQ_CODE_LOCATION_PERMISSION = 1213
        const val TAG = "TAG"
        const val DEFAULT_ZOOM = 15f
    }
}