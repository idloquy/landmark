package com.idloquy.landmark.data.network

import com.idloquy.landmark.data.network.model.ApiResponse
import com.idloquy.landmark.data.network.model.Mark
import com.idloquy.landmark.data.network.model.MarkResponse
import com.idloquy.landmark.data.network.model.OwnedSharedMarkGroupResponse
import com.idloquy.landmark.data.network.model.RequestMark
import com.idloquy.landmark.data.network.model.RequestSharedMarkGroup
import com.idloquy.landmark.data.network.model.SharedMarkGroupQueryResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Tag

interface LandmarkApiService {
    @POST("/v1/mark-groups")
    suspend fun createMarkGroup(@Body markGroup: RequestSharedMarkGroup): Response<ApiResponse<OwnedSharedMarkGroupResponse>>

    @GET("/v1/mark-groups/{id}")
    suspend fun getMarkGroup(@Path("id") id: String, @Tag editToken: BearerToken? = null): Response<ApiResponse<SharedMarkGroupQueryResponse>>

    @DELETE("/v1/mark-groups/{id}")
    suspend fun deleteMarkGroup(
        @Path("id") id: String,
        @Tag token: BearerToken,
    ): Response<ApiResponse.Error?>

    @DELETE("/v1/mark-groups/{groupId}/marks/{id}")
    suspend fun deleteMark(
        @Path("groupId") groupId: String,
        @Tag token: BearerToken,
        @Path("id") id: String,
    ): Response<ApiResponse.Error?>

    @PUT("/v1/mark-groups/{groupId}/marks/{id}")
    suspend fun updateMark(
        @Path("groupId") groupId: String,
        @Tag token: BearerToken,
        @Path("id") id: String,
        @Body mark: Mark,
    ): Response<ApiResponse.Error?>

    @POST("/v1/mark-groups/{groupId}/marks")
    suspend fun addMark(
        @Path("groupId") groupId: String,
        @Tag token: BearerToken,
        @Body mark: RequestMark,
    ): Response<ApiResponse<MarkResponse>>
}