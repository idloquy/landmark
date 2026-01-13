package com.idloquy.landmark.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.idloquy.landmark.ui.LandmarkViewModel
import com.idloquy.landmark.R
import com.idloquy.landmark.model.Location
import com.idloquy.landmark.model.LocationSaver

@Composable
fun HomeScreen(
    viewModel: LandmarkViewModel = hiltViewModel(),
    onHistory: () -> Unit,
) {
    val context = LocalContext.current

    var hasPermission by rememberSaveable { mutableStateOf(false) }
    var shouldRequestPerms by rememberSaveable { mutableStateOf(true) }

    var locationEnabled by rememberSaveable { mutableStateOf(false) }
    val currentLocation = viewModel.location.collectAsStateWithLifecycle()

    ObservePermissionEffect { isGranted ->
        hasPermission = isGranted
        shouldRequestPerms = true
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        if (permissions.any { it.value }) {
            hasPermission = true
        }
    }
    LaunchedEffect(shouldRequestPerms) {
        if (shouldRequestPerms) {
            launcher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
            shouldRequestPerms = false
        }
    }

    if (hasPermission) {
        ObserveLocationEffect(onLocationDisabled = {
            locationEnabled = false
        }, onLocationEnabled = {
            locationEnabled = true
        }, onLocationChanged = {
            viewModel.updateLocation(it)
        }, onPermissionDisabled = {
            hasPermission = false
        })
    }

    HomeContent(
        currentLocation = currentLocation.value,
        onHistory = onHistory,
        onMark = { location, description ->
            viewModel.markLocation(location, description)
        },
        hasPermission = hasPermission,
        locationEnabled = locationEnabled,
        onSettings = {
            val uri = Uri.fromParts("package", context.packageName, null)
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri)
            context.startActivity(intent)
        },
        onLocationSettings = {
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            context.startActivity(intent)
        },
    )
}

@Composable
fun ObservePermissionEffect(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    onPermission: (Boolean) -> Unit,
) {
    val context = LocalContext.current

    val checkPermission = {
        val coarsePermission =
            context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        val finePermission = context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        coarsePermission == PackageManager.PERMISSION_GRANTED || finePermission == PackageManager.PERMISSION_GRANTED
    }

    val isGranted = checkPermission()
    onPermission(isGranted)

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val isGranted = checkPermission()
                onPermission(isGranted)
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}

@Composable
fun ObserveLocationEffect(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    onLocationEnabled: () -> Unit,
    onLocationDisabled: () -> Unit,
    onLocationChanged: (Location) -> Unit,
    onPermissionDisabled: () -> Unit,
) {
    var locationEnabled by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current

    DisposableEffect(lifecycleOwner, locationEnabled) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val listener = object : LocationListener {
            override fun onLocationChanged(location: android.location.Location) {
                onLocationChanged(Location(location.latitude, location.longitude))
            }

            override fun onProviderEnabled(provider: String) {
                onLocationEnabled()
                locationEnabled = true
            }

            override fun onProviderDisabled(provider: String) {
                onLocationDisabled()
                locationEnabled = false
            }
        }

        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                try {
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        200L,
                        10f,
                        listener,
                    )

                    locationEnabled =
                        locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                    if (locationEnabled) {
                        onLocationEnabled()
                    } else {
                        onLocationDisabled()
                    }
                } catch (_: SecurityException) {
                    onPermissionDisabled()
                }
            } else if (event == Lifecycle.Event.ON_STOP) {
                try {
                    locationManager.removeUpdates(listener)
                } catch (_: SecurityException) {
                    onPermissionDisabled()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        if (locationEnabled) {
            val lastKnownLocation: android.location.Location? = try {
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            } catch (_: SecurityException) {
                onPermissionDisabled()
                return@DisposableEffect onDispose {}
            }

            lastKnownLocation?.let { lastKnownLocation ->
                onLocationChanged(
                    Location(
                        latitude = lastKnownLocation.latitude,
                        longitude = lastKnownLocation.longitude
                    )
                )
            }
        }

        onDispose {
            try {
                locationManager.removeUpdates(listener)
            } catch (_: SecurityException) {
                onPermissionDisabled()
            }
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    currentLocation: Location?,
    onHistory: () -> Unit,
    onMark: (Location, String) -> Unit,
    hasPermission: Boolean,
    locationEnabled: Boolean,
    onSettings: () -> Unit,
    onLocationSettings: () -> Unit,
) {
    var showMarkLocationDialog by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.app_name),
                        fontWeight = FontWeight.Bold,
                    )
                }, colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ), actions = {
                    IconButton(
                        onClick = onHistory,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "History",
                            modifier = Modifier.size(35.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                })
        }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp, 20.dp),
        ) {
            if (!hasPermission) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Location permission required",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(5.dp))

                    Text(
                        text = "Tap Settings, go to Permissions and enable Location",
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(10.dp))

                    TextButton(
                        onClick = onSettings,
                        shape = RectangleShape,
                        colors = ButtonDefaults.buttonColors(),
                    ) {
                        Text("Settings")
                    }
                }

                Spacer(Modifier.weight(1f))
            } else if (!locationEnabled) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Location required",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(5.dp))

                    Text(
                        text = "Please enable location to be able to add marks",
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(15.dp))

                    TextButton(
                        onClick = onLocationSettings,
                        shape = RectangleShape,
                        colors = ButtonDefaults.buttonColors(),
                    ) {
                        Text(
                            text = "Enable",
                        )
                    }
                }
            } else if (currentLocation != null) {
                LocationRow(currentLocation)

                Spacer(Modifier.weight(1f))

                Row(
                    modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(
                        onClick = { showMarkLocationDialog = true },
                        colors = ButtonDefaults.buttonColors(),
                        shape = RectangleShape,
                        elevation = ButtonDefaults.elevatedButtonElevation(),
                    ) {
                        Text(
                            text = "Mark",
                            fontSize = 16.sp,
                            modifier = Modifier.padding(vertical = 5.dp, horizontal = 10.dp),
                        )
                    }
                }

                Spacer(Modifier.weight(1f))

                if (showMarkLocationDialog) {
                    MarkLocationDialog(
                        location = currentLocation,
                        onDismiss = { showMarkLocationDialog = false },
                        onMark = { location, description ->
                            onMark(location, description)
                            showMarkLocationDialog = false

                            Toast(context).apply { setText("Location marked") }.show()
                        },
                    )
                }
            } else {
                Spacer(Modifier.weight(1f))
                Row(
                    modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Determining location...",
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                    )
                }
                Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun LocationRow(
    location: Location,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier) {
        Column {
            Text(
                text = "Latitude:",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Longitude:",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(Modifier.width(10.dp))

        Column(
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${location.latitude}",
                fontSize = 16.sp,
            )
            Text(
                text = "${location.longitude}",
                fontSize = 16.sp,
            )
        }
    }
}

@Composable
fun MarkLocationDialog(
    location: Location,
    description: String = "",
    onDismiss: () -> Unit,
    onMark: (Location, String) -> Unit,
) {
    val currentLocation = rememberSaveable(saver = LocationSaver) { location }

    var description by rememberSaveable { mutableStateOf(description) }
    var isError by rememberSaveable { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
    ) {
        Surface(shape = RoundedCornerShape(8.dp)) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(10.dp)
                    .padding(top = 10.dp),
            ) {
                OutlinedTextField(
                    label = { Text("Latitude") },
                    value = "${currentLocation.latitude}",
                    enabled = false,
                    onValueChange = {},
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledLabelColor = OutlinedTextFieldDefaults.colors().unfocusedLabelColor,
                        disabledTextColor = OutlinedTextFieldDefaults.colors().unfocusedTextColor,
                    )
                )
                OutlinedTextField(
                    label = { Text("Longitude") },
                    value = "${currentLocation.longitude}",
                    enabled = false,
                    onValueChange = {},
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledLabelColor = OutlinedTextFieldDefaults.colors().unfocusedLabelColor,
                        disabledTextColor = OutlinedTextFieldDefaults.colors().unfocusedTextColor,
                    )
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    supportingText = if (isError) {
                        {
                            Text("A description is required")
                        }
                    } else {
                        null
                    },
                    isError = isError,
                )

                Spacer(Modifier.height(5.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    TextButton(
                        modifier = Modifier.weight(1f),
                        onClick = onDismiss,
                    ) {
                        Text("Cancel")
                    }

                    Spacer(Modifier.weight(1f))

                    TextButton(
                        modifier = Modifier.weight(1f), onClick = {
                            if (description.isNotEmpty()) {
                                onMark(currentLocation, description)
                            } else {
                                isError = true
                            }
                        }) {
                        Text("Mark")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    HomeContent(
        currentLocation = Location(
            latitude = 0.123451234512345,
            longitude = 0.123451234512345,
        ),
        onHistory = {},
        onMark = { _, _ -> },
        hasPermission = true,
        locationEnabled = true,
        onSettings = {},
        onLocationSettings = {},
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenLoadingPreview() {
    HomeContent(
        currentLocation = null,
        onHistory = {},
        onMark = { _, _ -> },
        hasPermission = true,
        locationEnabled = true,
        onSettings = {},
        onLocationSettings = {},
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenNoPermissionPreview() {
    HomeContent(
        currentLocation = null,
        onHistory = {},
        onMark = { _, _ -> },
        hasPermission = false,
        locationEnabled = true,
        onSettings = {},
        onLocationSettings = {},
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenNoLocationPreview() {
    HomeContent(
        currentLocation = null,
        onHistory = {},
        onMark = { _, _ -> },
        hasPermission = true,
        locationEnabled = false,
        onSettings = {},
        onLocationSettings = {},
    )
}