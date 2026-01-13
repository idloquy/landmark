package com.idloquy.landmark.model

import android.os.Bundle
import androidx.compose.runtime.saveable.Saver
import kotlinx.serialization.Serializable

@Serializable
data class Location(
    val latitude: Double,
    val longitude: Double,
)

private fun locationToBundle(location: Location): Bundle {
    return Bundle().apply {
        putDouble("latitude", location.latitude)
        putDouble("longitude", location.longitude)
    }
}

private fun bundleToLocation(bundle: Bundle): Location {
    return Location(
        latitude = bundle.getDouble("latitude"),
        longitude = bundle.getDouble("longitude"),
    )
}

val LocationSaver = Saver<Location, Bundle>(save = {
    locationToBundle(it)
}, restore = {
    bundleToLocation(it)
})