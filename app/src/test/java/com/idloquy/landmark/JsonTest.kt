package com.idloquy.landmark

import com.idloquy.landmark.data.network.model.ApiResponse
import com.idloquy.landmark.data.network.model.ApiResponseAdapterFactory
import com.idloquy.landmark.data.network.model.Mark
import com.idloquy.landmark.data.network.model.RequestMark
import com.idloquy.landmark.data.network.model.RequestSharedMarkGroup
import com.idloquy.landmark.data.network.model.SharedMarkGroup
import com.idloquy.landmark.data.network.model.SharedMarkGroupQueryResponse
import com.idloquy.landmark.ui.shared_marks.exceptions.InvalidServerResponseException
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Before
import org.junit.Test

class JsonTest {
    private lateinit var moshi: Moshi

    @Before
    fun initMoshi() {
        moshi = Moshi.Builder()
            .add(ApiResponseAdapterFactory())
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    @Test
    fun deserializeResponse_InvalidJson_Exception() {
        val type =
            Types.newParameterizedType(ApiResponse::class.java, SharedMarkGroupQueryResponse::class.java)
        val adapter = moshi.adapter<ApiResponse<SharedMarkGroupQueryResponse>>(type)

        val exception = runCatching {
            adapter.fromJson("foo")
        }.exceptionOrNull()
        assert(exception != null)
    }

    @Test
    fun deserializeResponse_EmptyJson_Exception() {
        val type =
            Types.newParameterizedType(ApiResponse::class.java, SharedMarkGroupQueryResponse::class.java)
        val adapter = moshi.adapter<ApiResponse<SharedMarkGroupQueryResponse>>(type)

        val exception = runCatching {
            adapter.fromJson("")
        }.exceptionOrNull()
        assert(exception != null)
    }

    @Test
    fun deserializeResponse_InvalidStatus_JsonDataException() {
        val type =
            Types.newParameterizedType(ApiResponse::class.java, SharedMarkGroupQueryResponse::class.java)
        val adapter = moshi.adapter<ApiResponse<SharedMarkGroupQueryResponse>>(type)

        val exception = runCatching {
            adapter.fromJson("{\"status\":\"foo\",\"response\":{}}")
        }.exceptionOrNull()
        assert(exception is JsonDataException)
    }

    @Test
    fun deserializeResponseSharedMarkGroup_ValidSuccess_IsSuccess() {
        val mark =
            Mark(
                id = 1,
                latitude = 0.12345,
                longitude = 0.12345,
                description = "lorem ipsum"
            )
        val group = SharedMarkGroup(
            id = "0000-0000",
            name = "group1",
            marks = listOf(mark),
        )

        val type =
            Types.newParameterizedType(ApiResponse::class.java, SharedMarkGroupQueryResponse::class.java)
        val adapter = moshi.adapter<ApiResponse<SharedMarkGroupQueryResponse>>(type)
        val res = adapter.fromJson(
            "{\"status\":\"success\",\"response\":{\"group\":{\"id\":\"${group.id}\",\"name\":\"${group.name}\",\"marks\":[{\"id\":${mark.id},\"latitude\":${mark.latitude},\"longitude\":${mark.longitude},\"description\":\"${mark.description}\"}]}}}"
        )
        assert(res is ApiResponse.Success)

        val successRes = res as ApiResponse.Success
        assert(successRes.data.group == group)
    }

    @Test
    fun deserializeApiResponse_ValidError_IsSuccess() {
        val message = "lorem ipsum"

        val type =
            Types.newParameterizedType(ApiResponse::class.java, SharedMarkGroupQueryResponse::class.java)
        val adapter = moshi.adapter<ApiResponse<SharedMarkGroupQueryResponse>>(type)
        val res = adapter.fromJson(
            "{\"status\":\"error\",\"message\":\"$message\"}"
        )
        assert(res is ApiResponse.Error)
        val errorRes = res as ApiResponse.Error
        assert(errorRes.message == message)
    }

    @Test
    fun serializeRequestSharedMarkGroup_JsonIsValid() {
        val mark = RequestMark(
            latitude = 0.12345,
            longitude = 0.12345,
            description = "lorem ipsum",
        )
        val group = RequestSharedMarkGroup(
            name = "group",
            marks = listOf(
                RequestMark(
                    latitude = 0.12345,
                    longitude = 0.12345,
                    description = "lorem ipsum",
                )
            ),
        )
        val expected = "{\"name\":\"${group.name}\",\"marks\":[{\"latitude\":${mark.latitude},\"longitude\":${mark.longitude},\"description\":\"${mark.description}\"}]}"

        val adapter = moshi.adapter(RequestSharedMarkGroup::class.java)

        val res = adapter.toJson(group)
        assert(res == expected)
    }
}