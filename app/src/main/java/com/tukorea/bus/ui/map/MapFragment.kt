package com.tukorea.bus.ui.map

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.util.FusedLocationSource
import com.tukorea.bus.databinding.FragmentMapBinding
import android.widget.Toast
import com.tukorea.bus.domain.util.LocationUtils
import com.naver.maps.map.LocationTrackingMode
import com.tukorea.bus.domain.model.MapLocation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MapFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private val vm: MapViewModel by viewModels()
    private var naverMap: NaverMap? = null
    private lateinit var locationSource: FusedLocationSource
    private var lastUpdatedLocation: MapLocation? = null
    
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)
        
        // 맵 뷰 초기화 및 비동기 맵 로드
        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync(this)
        
        // 현재 위치 이동
        binding.fabLocation.setOnClickListener {
            vm.loadCurrentLocation()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.state.collectLatest { state ->
                    handleState(state)
                }
            }
        }
    }

    /**
     * 네이버 맵이 준비되었을 때 호출됩니다.
     * 맵 설정 및 초기 위치 로드를 수행합니다.
     */
    override fun onMapReady(map: NaverMap) {
        naverMap = map
        map.locationSource = locationSource
        map.uiSettings.isLocationButtonEnabled = true
        map.uiSettings.isZoomControlEnabled = true

        if (vm.state.value.isLocationPermissionGranted) {
            vm.loadCurrentLocation()
        }
    }

    /**
     * ViewModel의 상태를 받아 UI를 업데이트합니다.
     * @param state 현재 맵 UI 상태
     */
    private fun handleState(state: MapUiState) {
        binding.progressIndicator.visibility = 
            if (state.isLoading) View.VISIBLE else View.GONE

        // 현재 위치가 업데이트되면 맵 카메라를 해당 위치로 이동
        state.currentLocation?.let { location ->
            val latLng = LatLng(location.latitude, location.longitude)
            
            // 중복 업데이트 방지
            val shouldUpdate = lastUpdatedLocation?.let { last ->
                LocationUtils.isDistanceGreaterThan(
                    from = last,
                    to = location,
                    thresholdMeters = 2.0
                )
            } ?: true
            
            if (shouldUpdate) {
                naverMap?.let { map ->
                    // 카메라 부드럽게 이동
                    val cameraUpdate = CameraUpdate.scrollTo(latLng)
                        .animate(CameraAnimation.Easing, 500)
                    map.moveCamera(cameraUpdate)
                    lastUpdatedLocation = location
                }
            }
        }

        state.error?.let { error ->
            Toast.makeText(
                requireContext(),
                error,
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    

    /**
     * 위치 권한 요청 결과를 처리합니다.
     * 
     * 네이버 맵 SDK의 FusedLocationSource 호환성을 위해 유지합니다.
     */
    @Suppress("DEPRECATION")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            if (!locationSource.isActivated) {
                naverMap?.locationTrackingMode = LocationTrackingMode.None
            }
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        // 커스텀 권한 요청 코드 처리
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && 
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                vm.onLocationPermissionGranted()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.mapView.onDestroy()
        _binding = null
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }
}

