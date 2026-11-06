package com.tukorea.bus.ui.home

import android.app.Activity
import android.content.Context
import android.graphics.PointF
import androidx.annotation.DrawableRes
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.overlay.PathOverlay
import com.naver.maps.map.util.FusedLocationSource
import com.tukorea.bus.R
import com.tukorea.bus.domain.model.LiveBus
import com.tukorea.bus.domain.model.ShuttleDirection
import com.tukorea.bus.domain.model.ShuttleRoute
import com.tukorea.bus.domain.model.ShuttleStop
import com.tukorea.bus.ui.common.boardingRes
import com.tukorea.bus.ui.common.nameRes
import com.tukorea.bus.ui.theme.Black
import com.tukorea.bus.ui.theme.White
import java.util.Locale

private const val CAMERA_PADDING_PX = 160
private const val CAMERA_ANIMATION_MS = 600L
private const val LOCATION_PERMISSION_REQUEST = 1000

/**
 * 선택한 방향의 출발·도착 정류장과 실시간 버스를 보여주는 지도.
 * 지도 컨트롤은 줄이고(우버처럼) 정보는 아래 시트가 맡는다.
 */
@Composable
fun ShuttleMap(
    direction: ShuttleDirection?,
    buses: List<LiveBus>,
    showMyLocation: Boolean,
    bottomPadding: Dp,
    modifier: Modifier = Modifier,
    route: ShuttleRoute? = null,
    headings: Map<String, Double> = emptyMap(),
    selectedBusId: String? = null,
    onBusClick: (String) -> Unit = {},
    onMapClick: () -> Unit = {},
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val isDark = isSystemInDarkTheme()
    val density = LocalDensity.current
    val bottomPaddingPx = with(density) { bottomPadding.roundToPx() }
    // 지도는 상태바 밑까지 깔리므로, 정류장이 상태바에 가리지 않게 위쪽도 비워 둔다.
    val topPaddingPx = WindowInsets.statusBars.getTop(density)
    val busDescription = stringResource(R.string.a11y_bus_marker)

    val mapView = remember { MapView(context) }
    var naverMap by remember { mutableStateOf<NaverMap?>(null) }
    val icons = remember { MapIcons(context) }
    val stopMarkers = remember { mutableListOf<Marker>() }
    val busMarkers = remember { mutableStateMapOf<String, Marker>() }
    val headingMarkers = remember { mutableStateMapOf<String, Marker>() }
    val routeLine = remember { PathOverlay() }
    var mapSize by remember { mutableStateOf(IntSize.Zero) }
    val currentOnBusClick by rememberUpdatedState(onBusClick)
    val currentOnMapClick by rememberUpdatedState(onMapClick)

    DisposableEffect(lifecycle, mapView) {
        // 옵저버를 붙이면 현재 상태까지의 이벤트(ON_CREATE…)를 다시 받는다.
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> mapView.onCreate(null)
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) mapView.onPause()
            if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) mapView.onStop()
            mapView.onDestroy()
        }
    }

    Box(modifier = modifier) {
    AndroidView(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { mapSize = it },
        factory = {
            mapView.apply {
                getMapAsync { map ->
                    map.uiSettings.apply {
                        isZoomControlEnabled = false
                        isCompassEnabled = false
                        isScaleBarEnabled = false
                        isLocationButtonEnabled = false
                    }
                    map.setOnMapClickListener { _, _ -> currentOnMapClick() }
                    // 기기 언어와 상관없이 한글 지명만 보여준다(영문 병기는 라벨을 두 배로 늘린다).
                    map.locale = Locale.KOREA
                    naverMap = map
                }
            }
        },
    )

        if (showMyLocation) {
            LocateMeButton(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = bottomPadding + 16.dp),
                onClick = { naverMap?.locationTrackingMode = LocationTrackingMode.Follow },
            )
        }
    }

    LaunchedEffect(naverMap, isDark) {
        val map = naverMap ?: return@LaunchedEffect
        map.mapType = if (isDark) NaverMap.MapType.Navi else NaverMap.MapType.Basic
        map.isNightModeEnabled = isDark
    }

    LaunchedEffect(naverMap, showMyLocation) {
        val map = naverMap ?: return@LaunchedEffect
        val activity = context as? Activity ?: return@LaunchedEffect
        if (showMyLocation) {
            map.locationSource = FusedLocationSource(activity, LOCATION_PERMISSION_REQUEST)
            map.locationTrackingMode = LocationTrackingMode.NoFollow
        }
    }

    // 출발·도착 정류장
    val originCaption = direction?.let { stringResource(it.origin.boardingRes()) }
    val destinationCaption = direction?.let { stringResource(it.destination.nameRes()) }
    // 시트 높이(bottomPaddingPx)가 정해진 뒤에도 다시 맞춰야 정류장이 시트에 가리지 않는다.
    LaunchedEffect(naverMap, direction, isDark, bottomPaddingPx, topPaddingPx, selectedBusId == null, mapSize) {
        val map = naverMap ?: return@LaunchedEffect
        // 지도 뷰 크기가 정해지기 전에 맞추면(탭을 오가며 지도를 새로 만들 때) 줌이 엉뚱하게 잡힌다.
        if (mapSize.width == 0 || mapSize.height == 0) return@LaunchedEffect
        stopMarkers.forEach { it.map = null }
        stopMarkers.clear()
        direction ?: return@LaunchedEffect
        map.setContentPadding(0, topPaddingPx, 0, bottomPaddingPx)

        stopMarkers += stopMarker(direction.origin, icons.origin, originCaption, isDark).apply { this.map = map }
        stopMarkers += stopMarker(direction.destination, icons.destination, destinationCaption, isDark)
            .apply { this.map = map }

        // 버스를 따라가는 중이면 카메라는 아래 효과가 맡는다.
        if (selectedBusId != null) return@LaunchedEffect
        val bounds = LatLngBounds.Builder()
            .include(direction.origin.latLng)
            .include(direction.destination.latLng)
            .apply { route?.points?.forEach { include(LatLng(it.latitude, it.longitude)) } }
            .build()
        map.moveCamera(
            CameraUpdate.fitBounds(bounds, CAMERA_PADDING_PX).animate(CameraAnimation.Easing, CAMERA_ANIMATION_MS),
        )
    }

    // 노선 경로: 정류장 사이 실제 도로를 따라 가는 선.
    LaunchedEffect(naverMap, route, isDark) {
        val map = naverMap ?: return@LaunchedEffect
        val points = route?.points.orEmpty()
        if (points.size < 2) {
            routeLine.map = null
            return@LaunchedEffect
        }
        routeLine.apply {
            coords = points.map { LatLng(it.latitude, it.longitude) }
            width = with(density) { ROUTE_WIDTH.roundToPx() }
            outlineWidth = with(density) { ROUTE_OUTLINE.roundToPx() }
            color = (if (isDark) White else Black).toArgb()
            outlineColor = (if (isDark) Black else White).toArgb()
            this.map = map
        }
    }

    // 실시간 버스: id별 마커를 재사용하고 사라진 버스는 지운다. 진행 방향 화살표는 별도 마커로 같은 자리에 둔다.
    // 정차 중에는 방향을 모르므로 마지막 방향을 유지한다.
    LaunchedEffect(naverMap, buses, headings) {
        val map = naverMap ?: return@LaunchedEffect
        val ids = buses.map { it.id }.toSet()
        busMarkers.keys.filterNot { it in ids }.forEach { id -> busMarkers.remove(id)?.map = null }
        headingMarkers.keys.filterNot { it in ids }.forEach { id -> headingMarkers.remove(id)?.map = null }
        buses.forEach { bus ->
            val position = LatLng(bus.latitude, bus.longitude)
            val heading = headings[bus.id]
            val arrow = headingMarkers[bus.id]
            if (heading != null) {
                if (arrow == null) {
                    headingMarkers[bus.id] = Marker().apply {
                        this.position = busMarkers[bus.id]?.position ?: position
                        icon = icons.heading
                        anchor = CENTER
                        isFlat = true
                        angle = heading.toFloat()
                        zIndex = 9
                        this.map = map
                    }
                } else {
                    arrow.angle = heading.toFloat()
                }
            }
            val existing = busMarkers[bus.id]
            if (existing != null) {
                // 위치는 몇 초마다 오므로 순간이동 대신 다음 위치까지 미끄러지듯 옮긴다.
                // 새 위치가 오면 이 효과가 다시 시작되며 지금 그려진 자리에서 이어 간다.
                launch { glide(existing, headingMarkers[bus.id], position) }
                return@forEach
            }
            // 좌표를 먼저 넣고 지도에 붙인다(좌표 없이 붙이면 SDK가 InvalidCoordinateException을 던진다).
            busMarkers[bus.id] = Marker().apply {
                this.position = position
                icon = icons.bus
                anchor = CENTER
                zIndex = 10
                subCaptionText = busDescription
                setOnClickListener {
                    currentOnBusClick(bus.id)
                    true
                }
                this.map = map
            }
        }
    }

    // 고른 버스를 따라간다. 마커가 미끄러지는 시간과 같게 카메라도 옮긴다.
    val followed = buses.firstOrNull { it.id == selectedBusId }
    LaunchedEffect(naverMap, followed?.latitude, followed?.longitude) {
        val map = naverMap ?: return@LaunchedEffect
        val bus = followed ?: return@LaunchedEffect
        map.setContentPadding(0, topPaddingPx, 0, bottomPaddingPx)
        val target = LatLng(bus.latitude, bus.longitude)
        val update = if (map.cameraPosition.zoom < FOLLOW_ZOOM - 0.5) {
            CameraUpdate.scrollAndZoomTo(target, FOLLOW_ZOOM).animate(CameraAnimation.Easing, CAMERA_ANIMATION_MS)
        } else {
            CameraUpdate.scrollTo(target).animate(CameraAnimation.Linear, GLIDE_MS)
        }
        map.moveCamera(update)
    }
}

private val ShuttleStop.latLng: LatLng get() = LatLng(latitude, longitude)

/** 위치 갱신 간격(2초)보다 조금 짧게 잡아 다음 위치가 오기 전에 도착하게 한다. */
private const val GLIDE_MS = 1_900L

/** 너무 멀리 떨어졌으면(재연결 등) 미끄러지지 않고 바로 옮긴다. */
private const val GLIDE_MAX_METERS = 500.0

private suspend fun glide(marker: Marker, arrow: Marker?, target: LatLng) {
    val start = marker.position
    if (start.distanceTo(target) > GLIDE_MAX_METERS) {
        marker.position = target
        arrow?.position = target
        return
    }
    val startNanos = withFrameNanos { it }
    while (true) {
        val elapsedMs = (withFrameNanos { it } - startNanos) / 1_000_000
        val t = (elapsedMs.toDouble() / GLIDE_MS).coerceAtMost(1.0)
        val p = LatLng(
            start.latitude + (target.latitude - start.latitude) * t,
            start.longitude + (target.longitude - start.longitude) * t,
        )
        marker.position = p
        arrow?.position = p
        if (t >= 1.0) break
    }
}

private val CENTER = PointF(0.5f, 0.5f)
private val ROUTE_WIDTH = 5.dp
private val ROUTE_OUTLINE = 1.5.dp
private const val FOLLOW_ZOOM = 16.0

/** 지도 위 내 위치·버스보다 위에 그려 정류장이 가려지지 않게 한다. */
private const val STOP_GLOBAL_Z = 300_001

@Composable
private fun LocateMeButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.size(48.dp),
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Outlined.MyLocation,
                contentDescription = stringResource(R.string.a11y_locate_me),
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

private fun stopMarker(stop: ShuttleStop, image: OverlayImage, caption: String?, isDark: Boolean): Marker =
    Marker().apply {
        position = stop.latLng
        icon = image
        globalZIndex = STOP_GLOBAL_Z
        captionText = caption.orEmpty()
        captionTextSize = 14f
        captionColor = (if (isDark) White else Black).toArgb()
        captionHaloColor = (if (isDark) Black else White).toArgb()
        isHideCollidedCaptions = false
        // 정류장과 겹치는 지도 기본 심벌(POI 라벨)은 숨겨 정류장 이름이 묻히지 않게 한다.
        isHideCollidedSymbols = true
    }

/** 벡터 리소스를 지도 마커용 비트맵으로 한 번만 만들어 둔다. */
private class MapIcons(context: Context) {
    val bus = context.overlay(R.drawable.ic_map_bus)
    val origin = context.overlay(R.drawable.ic_map_stop_origin)
    val destination = context.overlay(R.drawable.ic_map_stop_destination)
    val heading = context.overlay(R.drawable.ic_map_bus_heading)

    private fun Context.overlay(@DrawableRes id: Int): OverlayImage =
        OverlayImage.fromBitmap(requireNotNull(ContextCompat.getDrawable(this, id)).toBitmap())
}
