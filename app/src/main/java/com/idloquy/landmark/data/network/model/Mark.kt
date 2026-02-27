package com.idloquy.landmark.data.network.model

import com.idloquy.landmark.data.database.model.SharedMark
import com.idloquy.landmark.model.Location

data class MarkResponse(
    val mark: Mark,
) : ResponseData

data class Mark(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val description: String,
)

fun Mark.asSharedMarkDatabaseModel(groupId: String): SharedMark = SharedMark(
    remoteId = id,
    location = Location(
        latitude,
        longitude,
    ),
    description = description,
    groupId = groupId,
)

data class RequestMark(
    val latitude: Double,
    val longitude: Double,
    val description: String,
)