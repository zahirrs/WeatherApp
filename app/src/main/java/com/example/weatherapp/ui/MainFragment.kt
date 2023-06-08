package com.example.weatherapp.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.weatherapp.R
import com.example.weatherapp.data.local.DataStoreManager
import com.example.weatherapp.data.model.CurrentWeatherResponse
import com.example.weatherapp.databinding.FragmentMainBinding
import com.example.weatherapp.utils.RequestState
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import java.util.Locale


class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val viewModel by lazy {
        ViewModelProvider(this)[MainFragmentViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requestPermission()
        setObserver()
        setupSearchAdapter()
    }

    private fun setupSearchAdapter() {
        val languages = resources.getStringArray(R.array.us_cities)
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1, languages
        )
        binding.searchActv.threshold = 1
        binding.searchActv.setAdapter(adapter)

        binding.searchActv.setOnItemClickListener { _, _, position, _ ->
            val selectedText = adapter.getItem(position).toString().trim()

            makeApiCall(selectedText)
        }
    }

    private fun saveLastEntered(selectedText: String) {
        //save this city name to data store
        lifecycleScope.launch {
            DataStoreManager(requireContext()).saveCityName(selectedText)
        }
    }

    private fun makeApiCall(selectedText: String) {
        binding.progressBar.visibility = View.VISIBLE
        viewModel.getCurrentWeatherByCity(selectedText)
    }

    private fun makeApiCallByLocation(lat: Double, lng: Double) {
        binding.progressBar.visibility = View.VISIBLE
        viewModel.getCurrentWeatherByLocation(lat, lng)
    }

    private fun clearSearch() {
        binding.searchActv.setText("")
        binding.searchActv.let {
            val imm =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

    private fun setupValues(response: CurrentWeatherResponse) {
        saveLastEntered(response.name ?: "")
        clearSearch()

        binding.layoutWeatherDisplay.textMain.text =
            response.weather?.firstOrNull()?.description?.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.ROOT
                ) else it.toString()
            }
        binding.layoutWeatherDisplay.textName.text = getString(
            R.string.city_name_and_country,
            response.name,
            response.sys?.country
        )

        binding.layoutWeatherDisplay.textLastUpdated.text =
            "Feels like ${kelvinToCelsius(response.main?.feelsLike!!)}"
        binding.layoutWeatherDisplay.textTemps.text =
            "${kelvinToCelsius(response.main?.tempMin!!)} ~ ${kelvinToCelsius(response.main?.tempMax!!)}"

        val iconUrl =
            "https://openweathermap.org/img/wn/${response.weather?.firstOrNull()?.icon}@2x.png"
        Glide.with(binding.layoutWeatherDisplay.imageIconCityItem)
            .load(
                iconUrl
//                requireContext().getIconDrawableFromCurrentWeather(
//                    weatherIcon = response.weather?.firstOrNull()?.icon
//                )
            )
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(binding.layoutWeatherDisplay.imageIconCityItem)
    }

    private fun setObserver() {
        viewModel.weatherDefinition.observe(viewLifecycleOwner, Observer {
            when (it) {
                is RequestState.SUCCESS<*> -> {
                    binding.progressBar.visibility = View.GONE

                    //parse here
                    //Log.d("tttt", (it.data as CurrentWeatherResponse).name ?: "")
                    setupValues(it.data as CurrentWeatherResponse)
                }

                is RequestState.ERROR -> {
                    binding.progressBar.visibility = View.GONE
                }

                is RequestState.LOADING -> {
                    binding.progressBar.visibility = View.VISIBLE
                }

                else -> {
                    binding.progressBar.visibility = View.GONE
                }
            }
        })

        lifecycleScope.launch {
            DataStoreManager(requireContext()).getCityName.collect {
                if (!it.isEmpty()) {
                    makeApiCall(it)
                }
            }
        }
    }

    private fun kelvinToCelsius(kelvin: Double): Int {
        return (kelvin - 273.15).toInt()
    }

    //permissions
    private fun requestPermission() {
        val locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    // Precise location access granted.
                    lifecycleScope.launch {
                        DataStoreManager(requireContext()).isGranted.collect {
                            if (!it) {
                                getLastLocation()
                            }
                        }
                    }
                }

                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    // Only approximate location access granted.
                    lifecycleScope.launch {
                        DataStoreManager(requireContext()).isGranted.collect {
                            if (!it) {
                                getLastLocation()
                            }
                        }
                    }
                }

                else -> {
                    // No location access granted.
                    lifecycleScope.launch {
                        DataStoreManager(requireContext()).saveIsGranted(false)
                    }
                }
            }
        }

        // Before you perform the actual permission request, check whether your app
        // already has the permissions, and whether your app needs to show a permission
        // rationale dialog. For more details, see Request permissions.
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        //save this city name to data store
        lifecycleScope.launch {
            DataStoreManager(requireContext()).saveIsGranted(true)
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                // Got last known location. In some rare situations this can be null.
                Log.d("tttt", "location >$location")
                makeApiCallByLocation(location?.latitude ?: 0.0, location?.longitude ?: 0.0)
            }
    }
}