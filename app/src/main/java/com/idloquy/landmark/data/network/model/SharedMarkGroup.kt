package com.idloquy.landmark.data.network.model

import android.util.Log
import com.idloquy.landmark.data.database.model.SharedMark
import com.idloquy.landmark.data.database.model.SharedMarkGroupWithMarks
import com.idloquy.landmark.model.Location
import com.squareup.moshi.Json

data class OwnedSharedMarkGroupResponse(
    val group: SharedMarkGroup,
    @param:Json(name = "edit_token") val editToken: String,
) : ResponseData

data class SharedMarkGroupQueryResponse(
    val group: SharedMarkGroup,
    val editable: Boolean,
) : ResponseData

data class SharedMarkGroup(
    val id: String,
    val name: String,
    val marks: List<Mark>,
)

fun SharedMarkGroup.asDatabaseModel(editToken: String = ""): SharedMarkGroupWithMarks {
    Log.d("landmark", "converting group with edittoken=$editToken to db model: $this")
    return SharedMarkGroupWithMarks(
        sharedMarkGroup = com.idloquy.landmark.data.database.model.SharedMarkGroup(
            id = id,
            name = name,
            editToken = editToken,
        ),
        marks = marks.map {
            SharedMark(
                remoteId = it.id,
                location = Location(
                    it.latitude,
                    it.longitude,
                ),
                description = it.description,
                groupId = id,
            )
        },
    )
}

data class RequestSharedMarkGroup(
    val name: String,
    val marks: List<RequestMark>,
)