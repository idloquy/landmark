package com.idloquy.landmark.data.database.model

import android.util.Log
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.idloquy.landmark.data.network.model.Mark
import com.idloquy.landmark.model.Location

@Entity
data class SharedMarkGroup(
    @PrimaryKey
    val id: String,
    val name: String,
    val editToken: String = "",
)

@Entity(foreignKeys = [
    ForeignKey(
        entity = SharedMarkGroup::class,
        parentColumns = ["id"],
        childColumns = ["groupId"],
        onDelete = ForeignKey.CASCADE,
    )
])

data class SharedMark(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val remoteId: String,
    @Embedded
    val location: Location,
    val description: String,
    val groupId: String,
)

fun SharedMark.asNetworkModel(): Mark {
    Log.d("landmark", "converting mark to network model: $this")
    return Mark(
        id = remoteId,
        latitude = location.latitude,
        longitude = location.longitude,
        description = description,
    )
}

data class SharedMarkGroupWithMarks(
    @Embedded
    val sharedMarkGroup: SharedMarkGroup,
    @Relation(
        parentColumn = "id",
        entityColumn = "groupId"
    )
    val marks: List<SharedMark>,
)
