package com.idloquy.landmark.data.database.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.idloquy.landmark.data.network.model.RequestMark
import com.idloquy.landmark.model.Location

@Entity
data class Mark(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @Embedded
    val location: Location,
    val description: String,
)

fun Mark.asRequestNetworkModel(): RequestMark {
    return RequestMark(
        location.latitude,
        location.longitude,
        description,
    )
}