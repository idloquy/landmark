package com.idloquy.landmark.data.repository

import android.util.Log
import com.idloquy.landmark.data.database.dao.SharedMarkGroupDao
import com.idloquy.landmark.data.database.model.Mark
import com.idloquy.landmark.data.database.model.SharedMark
import com.idloquy.landmark.data.database.model.SharedMarkGroup
import com.idloquy.landmark.data.database.model.SharedMarkGroupWithMarks
import com.idloquy.landmark.data.database.model.asNetworkModel
import com.idloquy.landmark.data.database.model.asRequestNetworkModel
import com.idloquy.landmark.data.network.BearerToken
import com.idloquy.landmark.data.network.LandmarkApiService
import com.idloquy.landmark.data.network.model.ApiResponse
import com.idloquy.landmark.data.network.model.RequestMark
import com.idloquy.landmark.data.network.model.RequestSharedMarkGroup
import com.idloquy.landmark.data.network.model.asDatabaseModel
import com.idloquy.landmark.data.network.model.asSharedMarkDatabaseModel
import com.idloquy.landmark.data.repository.exceptions.GroupAlreadyExistsException
import com.idloquy.landmark.data.repository.exceptions.InvalidEditTokenException
import com.idloquy.landmark.data.repository.exceptions.InvalidGroupIdException
import com.idloquy.landmark.data.repository.exceptions.InvalidMarkIdException
import com.idloquy.landmark.di.IoDispatcher
import com.idloquy.landmark.ui.shared_marks.exceptions.InvalidServerResponseException
import com.idloquy.landmark.ui.shared_marks.exceptions.TemporaryServerException
import com.squareup.moshi.JsonDataException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.IOException
import retrofit2.Response
import javax.inject.Inject
import kotlin.math.pow

interface SharedMarkGroupRepository {
    fun getMarkGroups(): Flow<List<SharedMarkGroup>>
    fun getMarkGroup(id: String): Flow<SharedMarkGroup?>
    fun getMarkGroupWithMarks(id: String): Flow<SharedMarkGroupWithMarks?>
    fun getMark(groupId: String, id: Int): Flow<SharedMark?>

    suspend fun importMarkGroup(id: String)
    suspend fun refreshMarkGroup(id: String)
    suspend fun shareMarkGroup(name: String, marks: List<Mark>): String

    suspend fun updateSharedMark(group: SharedMarkGroup, mark: SharedMark)

    suspend fun deleteMarkGroup(group: SharedMarkGroup)
    suspend fun deleteMarkGroups(groups: List<SharedMarkGroup>)
    suspend fun deleteSharedMark(group: SharedMarkGroup, mark: SharedMark)
    suspend fun deleteSharedMarks(group: SharedMarkGroup, marks: List<SharedMark>)

    suspend fun addMarks(group: SharedMarkGroup, marks: List<Mark>)

    suspend fun importEditToken(group: SharedMarkGroup, editToken: String)
}

class DefaultSharedMarkGroupRepository @Inject constructor(
    private val sharedMarkGroupDao: SharedMarkGroupDao,
    private val httpClient: LandmarkApiService,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : SharedMarkGroupRepository {
    override fun getMarkGroups(): Flow<List<SharedMarkGroup>> {
        return sharedMarkGroupDao.getAll().flowOn(ioDispatcher)
    }

    override fun getMarkGroup(id: String): Flow<SharedMarkGroup?> {
        return sharedMarkGroupDao.getSharedMarkGroupByIdFlow(id).flowOn(ioDispatcher)
    }

    override fun getMarkGroupWithMarks(id: String): Flow<SharedMarkGroupWithMarks?> {
        return sharedMarkGroupDao.getSharedMarkGroupWithMarksByIdFlow(id).flowOn(ioDispatcher)
    }

    override fun getMark(groupId: String, id: Int): Flow<SharedMark?> {
        return sharedMarkGroupDao.getSharedMarkByIdFlow(groupId, id)
    }

    private suspend fun <T> retriedRequest(
        maxRetries: Int = 3,
        initialDelay: Long = 1000L,
        f: suspend () -> Response<T>,
    ): Response<T> {
        var numRetries = 0
        lateinit var lastException: Exception

        while (numRetries < maxRetries) {
            if (numRetries > 0) {
                delay(initialDelay * 2.0.pow(numRetries).toLong())
            }
            val res = try {
                f()
            } catch (e: Exception) {
                when (e) {
                    is IOException, is TemporaryServerException -> {
                        numRetries += 1
                        lastException = e
                        continue
                    }

                    else -> throw e
                }
            }

            return res
        }

        throw lastException
    }

    private suspend fun refreshMarkGroup(id: String, create: Boolean) {
        retriedRequest {
            val res = try {
                httpClient.getMarkGroup(id)
            } catch (e: JsonDataException) {
                throw InvalidServerResponseException(
                    message = "Invalid response format", cause = e
                )
            } catch (e: IOException) {
                throw TemporaryServerException(cause = e)
            }

            when (res.code()) {
                200 -> {
                    val data = res.body() ?: run {
                        throw InvalidServerResponseException("Expected non-empty body")
                    }
                    when (data) {
                        is ApiResponse.Success -> {
                            val groupWithMarks = data.data.group.asDatabaseModel()
                            if (create) {
                                sharedMarkGroupDao.insertAllSharedMarkGroupsWithMarks(groupWithMarks)
                            } else {
                                sharedMarkGroupDao.updateSharedMarkGroupMarks(groupWithMarks.sharedMarkGroup, groupWithMarks.marks)
                            }
                        }

                        is ApiResponse.Error -> {
                            throw InvalidServerResponseException("Error response on success status")
                        }
                    }
                }

                404 -> {
                    throw InvalidGroupIdException("Group not found")
                }

                500 -> {
                    throw TemporaryServerException("Internal server error")
                }

                else -> {
                    throw InvalidServerResponseException("Unexpected status code: ${res.code()}")
                }
            }

            res
        }
    }

    override suspend fun importMarkGroup(id: String) {
        withContext(ioDispatcher) {
            if (sharedMarkGroupDao.getSharedMarkGroupById(id) != null) {
                throw GroupAlreadyExistsException()
            }

            refreshMarkGroup(
                id = id,
                create = true,
            )
        }
    }

    override suspend fun refreshMarkGroup(id: String) {
        return refreshMarkGroup(
            id = id,
            create = false,
        )
    }

    override suspend fun shareMarkGroup(name: String, marks: List<Mark>): String {
        require(marks.isNotEmpty())

        return withContext(ioDispatcher) {
            val res = retriedRequest {
                val group = RequestSharedMarkGroup(
                    name = name,
                    marks = marks.map {
                        RequestMark(
                            latitude = it.location.latitude,
                            longitude = it.location.longitude,
                            description = it.description,
                        )
                    },
                )

                val res = try {
                    httpClient.createMarkGroup(group)
                } catch (e: JsonDataException) {
                    throw InvalidServerResponseException(
                        message = "Invalid response format",
                        cause = e
                    )
                } catch (e: IOException) {
                    throw TemporaryServerException(cause = e)
                }

                when (res.code()) {
                    201 -> res

                    500 -> {
                        throw TemporaryServerException("Internal server error")
                    }

                    else -> {
                        throw InvalidServerResponseException("Unexpected status code: ${res.code()}")
                    }
                }
            }

            val data = res.body() ?: run {
                throw InvalidServerResponseException("Expected non-empty body")
            }
            when (data) {
                is ApiResponse.Success -> {
                    val group = data.data.group.asDatabaseModel(data.data.editToken)
                    sharedMarkGroupDao.insertAllSharedMarkGroupsWithMarks(group)
                    data.data.group.id
                }

                is ApiResponse.Error -> {
                    throw InvalidServerResponseException("Error response on success status")
                }
            }
        }
    }

    override suspend fun updateSharedMark(group: SharedMarkGroup, mark: SharedMark) {
        withContext(ioDispatcher) {
            updateSharedMarkOnServer(group, mark)
            sharedMarkGroupDao.updateSharedMarks(mark)
        }
    }

    private suspend fun updateSharedMarkOnServer(group: SharedMarkGroup, mark: SharedMark) {
        retriedRequest {
            val res = try {
                httpClient.updateMark(
                    group.id,
                    BearerToken(group.editToken),
                    mark.remoteId,
                    mark.asNetworkModel()
                )
            } catch (e: JsonDataException) {
                throw InvalidServerResponseException(
                    message = "Invalid response format", cause = e,
                )
            } catch (e: IOException) {
                throw TemporaryServerException(cause = e)
            }

            when (res.code()) {
                204 -> Unit
                404 -> {
                    throw InvalidMarkIdException("Mark not found")
                }

                500 -> {
                    throw TemporaryServerException("Internal server error")
                }

                else -> {
                    throw InvalidServerResponseException("Unexpected status code: ${res.code()}")
                }
            }

            res
        }
    }

    override suspend fun deleteMarkGroup(group: SharedMarkGroup) {
        withContext(ioDispatcher) {
            if (group.editToken.isNotEmpty()) deleteMarkGroupFromServer(group)
            sharedMarkGroupDao.deleteAll(group)
        }
    }

    override suspend fun deleteMarkGroups(groups: List<SharedMarkGroup>) {
        withContext(ioDispatcher) {
            // Delete groups one by one so failures deleting the group remotely can be more easily handled.
            Log.d("landmark", "deleting groups: $groups")
            for (group in groups) {
                Log.d("landmark", "deleting group $group from remote server if required")
                if (group.editToken.isNotEmpty()) deleteMarkGroupFromServer(group)
                Log.d("landmark", "deleting group $group from db")
                sharedMarkGroupDao.deleteAll(group)
                Log.d("landmark", "done deleting group $group")
            }
        }
    }

    private suspend fun deleteMarkGroupFromServer(group: SharedMarkGroup) {
        retriedRequest {
            val res = try {
                httpClient.deleteMarkGroup(group.id, BearerToken(group.editToken))
            } catch (e: JsonDataException) {
                throw InvalidServerResponseException(
                    message = "Invalid response format", cause = e
                )
            } catch (e: IOException) {
                throw TemporaryServerException(cause = e)
            }

            when (res.code()) {
                204 -> Unit
                404 -> Unit
                500 -> {
                    throw TemporaryServerException("Internal server error")
                }

                else -> {
                    throw InvalidServerResponseException("Unexpected status code: ${res.code()}")
                }
            }
            res
        }
    }

    override suspend fun deleteSharedMark(group: SharedMarkGroup, mark: SharedMark) {
        withContext(ioDispatcher) {
            if (sharedMarkGroupDao.sharedMarksCount(group.id) == 1) {
                deleteMarkGroupFromServer(group)
                sharedMarkGroupDao.deleteSharedMarkGroup(group)
                return@withContext
            }

            deleteSharedMarkFromServer(group, mark)
            sharedMarkGroupDao.deleteSharedMark(group.id, mark.id)
        }
    }

    override suspend fun deleteSharedMarks(
        group: SharedMarkGroup, marks: List<SharedMark>
    ) {
        withContext(ioDispatcher) {
            if (marks.size == sharedMarkGroupDao.sharedMarksCount(group.id)) {
                deleteMarkGroupFromServer(group)
                sharedMarkGroupDao.deleteSharedMarkGroup(group)
                return@withContext
            }

            for (mark in marks) {
                deleteSharedMarkFromServer(group, mark)
                sharedMarkGroupDao.deleteSharedMark(group.id, mark.id)
            }
        }
    }

    private suspend fun deleteSharedMarkFromServer(group: SharedMarkGroup, sharedMark: SharedMark) {
        retriedRequest {
            val res = try {
                httpClient.deleteMark(group.id, BearerToken(group.editToken), sharedMark.remoteId)
            } catch (e: JsonDataException) {
                throw InvalidServerResponseException(
                    message = "Invalid response format", cause = e
                )
            } catch (e: IOException) {
                throw TemporaryServerException(cause = e)
            }

            when (res.code()) {
                204 -> Unit
                404 -> Unit
                500 -> {
                    throw TemporaryServerException("Internal server error")
                }

                else -> {
                    throw InvalidServerResponseException("Unexpected status code: ${res.code()}")
                }
            }
            res
        }
    }

    override suspend fun addMarks(group: SharedMarkGroup, marks: List<Mark>) {
        withContext(ioDispatcher) {
            for (mark in marks) {
                val mark = addMarkToServer(group, mark)
                sharedMarkGroupDao.insertAllSharedMarks(mark.asSharedMarkDatabaseModel(group.id))
            }
        }
    }

    private suspend fun addMarkToServer(
        group: SharedMarkGroup,
        mark: Mark
    ): com.idloquy.landmark.data.network.model.Mark {
        val res = retriedRequest {
            val res = try {
                httpClient.addMark(group.id, BearerToken(group.editToken), mark.asRequestNetworkModel())
            } catch (e: JsonDataException) {
                throw InvalidServerResponseException(
                    message = "Invalid response format", cause = e,
                )
            } catch (e: IOException) {
                throw TemporaryServerException(cause = e)
            }

            when (res.code()) {
                201 -> res
                404 -> throw InvalidMarkIdException("Mark not found")
                500 -> throw TemporaryServerException("Internal server error")
                else -> throw InvalidServerResponseException("Unexpected status code: ${res.code()}")
            }
        }

        val data = res.body() ?: throw InvalidServerResponseException("Expected non-empty body")
        when (data) {
            is ApiResponse.Success -> {
                return data.data.mark
            }

            is ApiResponse.Error -> {
                throw InvalidServerResponseException("Error response on success status")
            }
        }
    }

    private suspend fun validateEditToken(group: SharedMarkGroup, editToken: String) {
        val res = retriedRequest {
            val res = try {
                httpClient.getMarkGroup(group.id, BearerToken(editToken))
            } catch (e: JsonDataException) {
                throw InvalidServerResponseException(
                    message = "Invalid response format", cause = e,
                )
            } catch (e: IOException) {
                throw TemporaryServerException(cause = e)
            }

            when (res.code()) {
                200 -> Unit
                404 -> throw InvalidGroupIdException("Group not found")
                500 -> throw TemporaryServerException("Internal server error")
            }

            res
        }

        val data = res.body() ?: throw InvalidServerResponseException("Expected non-empty body")
        when (data) {
            is ApiResponse.Success -> if (!data.data.editable) {
                throw InvalidEditTokenException()
            }
            is ApiResponse.Error -> {
                throw InvalidServerResponseException("Error response on success status")
            }
        }
    }

    override suspend fun importEditToken(group: SharedMarkGroup, editToken: String) {
        withContext(ioDispatcher) {
            validateEditToken(group, editToken)
            val group = group.copy(editToken = editToken)
            sharedMarkGroupDao.updateSharedMarkGroups(group)
        }
    }
}