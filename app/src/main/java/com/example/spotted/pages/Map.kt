package com.example.spotted.pages

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.Color as AndroidColor
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.spotted.R
import com.example.spotted.data.view.MapViewModel
import com.example.spotted.utils.gps.LocationService
import com.example.spotted.utils.gps.PermissionStatus
import com.example.spotted.utils.gps.rememberMultiplePermissions
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.InfoWindow

@Composable
fun MapScreen(
    innerPadding: PaddingValues,
    navigate: (Long) -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val locationService = remember { LocationService(ctx) }
    val coordinates by locationService.coordinates.collectAsStateWithLifecycle()
    val isLoading by locationService.isLoading.collectAsStateWithLifecycle()

    val mapViewModel: MapViewModel = koinViewModel()
    val followedPosts by mapViewModel.followedPosts.collectAsState()

    // Stato reattivo locale per decidere se mostrare la Mappa o la schermata NoGps
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val locationPermissions = rememberMultiplePermissions(
        listOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
    ) { statuses ->
        val granted = statuses.any { it.value.isGranted }
        hasLocationPermission = granted
        if (granted) {
            scope.launch { locationService.getCurrentLocation() }
        }
    }

    // Unico punto di ingresso iniziale controllato
    LaunchedEffect(Unit) {
        if (hasLocationPermission) {
            locationService.getCurrentLocation()
        } else {
            locationPermissions.launchPermissionRequest()
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current

    remember {
        Configuration.getInstance().load(
            ctx,
            ctx.getSharedPreferences("osm_pref", android.content.Context.MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = ctx.packageName
    }

    val mapView = remember {
        MapView(ctx).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)

            controller.setZoom(16.0)

            minZoomLevel = 5.0      // oppure 5.0
            maxZoomLevel = 20.0

            zoomController.setVisibility(
                CustomZoomButtonsController.Visibility.NEVER
            )
        }
    }

    LaunchedEffect(coordinates, followedPosts) {
        mapView.overlays.clear()

        val mapEventsReceiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                InfoWindow.closeAllInfoWindowsOn(mapView)
                return false
            }
            override fun longPressHelper(p: GeoPoint?): Boolean { return false }
        }
        val mapEventsOverlay = MapEventsOverlay(mapEventsReceiver)
        mapView.overlays.add(mapEventsOverlay)

        coordinates?.let { coords ->
            val point = GeoPoint(coords.latitude, coords.longitude)
            mapView.controller.setCenter(point)

            val userIcon = createCircleBitmap(ctx)

            Marker(mapView).apply {
                position = point
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                icon = userIcon
                setInfoWindow(null)
                mapView.overlays.add(this)
            }
        }

        followedPosts.forEach { post ->
            val point = GeoPoint(post.latitude!!, post.longitude!!)
            val dp = ctx.resources.displayMetrics.density
            val bubbleView = LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER_HORIZONTAL
                val hPad = (20 * dp).toInt()
                val vPad = (14 * dp).toInt()
                setPadding(hPad, vPad, hPad, vPad)
                background = GradientDrawable().apply {
                    setColor(AndroidColor.WHITE)
                    cornerRadius = 20 * dp
                }
                elevation = 8 * dp

                addView(TextView(ctx).apply {
                    text = post.title ?: "Spot #${post.id}"
                    textSize = 15f
                    setTypeface(null, android.graphics.Typeface.BOLD)
                    setTextColor(AndroidColor.parseColor("#1C1B1F"))
                    gravity = Gravity.CENTER
                })

                if (!post.description.isNullOrBlank()) {
                    addView(TextView(ctx).apply {
                        text = post.description
                        textSize = 12f
                        setTextColor(AndroidColor.parseColor("#49454F"))
                        gravity = Gravity.CENTER
                        setPadding(0, (4 * dp).toInt(), 0, 0)
                    })
                }

                addView(TextView(ctx).apply {
                    text = "Tocca per aprire la chat →"
                    textSize = 11f
                    setTextColor(AndroidColor.parseColor("#6750A4"))
                    gravity = Gravity.CENTER
                    setPadding(0, (8 * dp).toInt(), 0, 0)
                })
            }

            val infoWindow = object : InfoWindow(bubbleView, mapView) {
                override fun onOpen(item: Any?) {
                    mView.setOnClickListener {
                        close()
                        navigate(post.id)
                    }
                }
                override fun onClose() {}
            }

            Marker(mapView).apply {
                position = point
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                title   = post.title ?: "Spot #${post.id}"
                snippet = post.description
                setInfoWindow(infoWindow)
                setOnMarkerClickListener { marker, _ ->
                    marker.showInfoWindow()
                    true
                }
                mapView.overlays.add(this)
            }
        }
        mapView.invalidate()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME  -> {
                    mapView.onResume()
                    mapViewModel.loadFollowedPostsWithLocation()

                    // Rileva se l'utente ha garantito i permessi tornando dalle impostazioni di sistema
                    val coarseGranted = ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    val fineGranted = ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

                    if (coarseGranted || fineGranted) {
                        hasLocationPermission = true
                        scope.launch { locationService.getCurrentLocation() }
                    }
                }
                Lifecycle.Event.ON_PAUSE   -> mapView.onPause()
                Lifecycle.Event.ON_DESTROY -> mapView.onDetach()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasLocationPermission) {
            // Se i permessi sono attivi, la mappa riempie lo schermo (disegnando anche sotto le barre sfocate)
            AndroidView(
                factory  = { mapView },
                modifier = Modifier.fillMaxSize()
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(
                        end    = 16.dp,
                        bottom = innerPadding.calculateBottomPadding() + 16.dp
                    ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SmallFloatingActionButton(
                    onClick = {
                        coordinates?.let { coords ->
                            mapView.controller.setZoom(16.0)
                            mapView.controller.setCenter(GeoPoint(coords.latitude, coords.longitude))
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Icon(Icons.Filled.LocationOn, contentDescription = "La mia posizione")
                }

                SmallFloatingActionButton(
                    onClick = { mapView.controller.zoomIn() },
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Zoom in")
                }

                SmallFloatingActionButton(
                    onClick = { mapView.controller.zoomOut() },
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Icon(painterResource(R.drawable.remove_24px), contentDescription = "Zoom out")
                }
            }
        } else {
            // Se mancano i permessi, incapsuliamo NoGpsMap dentro i margini dello Scaffold per non sovrapporsi alle barre
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                val isPermanentlyDenied = locationPermissions.statuses.values.all { it == PermissionStatus.PermanentlyDenied }
                NoGpsMap(
                    permanentlyDenied = isPermanentlyDenied,
                    onRequestPermission = { locationPermissions.launchPermissionRequest() }
                )
            }
        }
    }
}

fun createCircleBitmap(
    context: Context,
    radiusDp: Float = 10f
): BitmapDrawable {

    val density = context.resources.displayMetrics.density

    val radius = radiusDp * density
    val haloRadius = radius * 2.6f

    val size = (haloRadius * 2 + 16 * density).toInt()

    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val cx = size / 2.5f
    val cy = size / 2.5f

    // -----------------------
    // Alone esterno (azzurro trasparente)
    // -----------------------
    val haloPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.parseColor("#4D4285F4") // circa 30% alpha
        style = Paint.Style.FILL
    }
    canvas.drawCircle(cx, cy, haloRadius, haloPaint)

    // -----------------------
    // Ombra morbida
    // -----------------------
    val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.BLACK
        alpha = 35
        maskFilter = android.graphics.BlurMaskFilter(
            6 * density,
            android.graphics.BlurMaskFilter.Blur.NORMAL
        )
    }

    canvas.drawCircle(cx, cy + density, radius, shadowPaint)

    // Necessario per disegnare BlurMaskFilter
    bitmap.setHasAlpha(true)

    // -----------------------
    // Cerchio blu
    // -----------------------
    val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.parseColor("#1A73E8")
        style = Paint.Style.FILL
    }

    canvas.drawCircle(cx, cy, radius, fillPaint)

    // -----------------------
    // Bordo bianco
    // -----------------------
    val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.WHITE
        style = Paint.Style.STROKE
        strokeWidth = radius * 0.28f
    }

    canvas.drawCircle(
        cx,
        cy,
        radius - borderPaint.strokeWidth / 2,
        borderPaint
    )

    return BitmapDrawable(context.resources, bitmap)
}