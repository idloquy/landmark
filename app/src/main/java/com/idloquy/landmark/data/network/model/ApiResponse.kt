package com.idloquy.landmark.data.network.model

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

sealed interface ResponseData

sealed class ApiResponse<out T : ResponseData> {
    data class Success<T : ResponseData>(
        val data: T,
    ) : ApiResponse<T>()

    data class Error(
        val message: String,
    ) : ApiResponse<Nothing>()
}

class ApiResponseAdapterFactory : JsonAdapter.Factory {
    override fun create(type: Type, annotations: Set<Annotation?>, moshi: Moshi): JsonAdapter<*>? {
        val raw = Types.getRawType(type)
        if (raw != ApiResponse::class.java) {
            return null
        }

        val parameterized = type as? ParameterizedType
            ?: throw IllegalArgumentException("Expected ApiResponse to be parameterized")

        val typeArgs = parameterized.actualTypeArguments
        if (typeArgs.isEmpty()) throw IllegalArgumentException("Expected ApiResponse to be parameterized")
        if (typeArgs.size > 1) throw IllegalArgumentException("Expected 1 type argument, got ${typeArgs.size}")

        val dataType = typeArgs.first()
        val dataAdapter = moshi.adapter<ResponseData>(dataType)

        return ApiResponseAdapter(dataAdapter)
    }
}

class ApiResponseAdapter<T : ResponseData>(
    val dataAdapter: JsonAdapter<T>,
) : JsonAdapter<ApiResponse<T>>() {
    override fun fromJson(reader: JsonReader): ApiResponse<T>? {
        @Suppress("UNCHECKED_CAST")
        val map = reader.readJsonValue() as? Map<String, Any>
            ?: throw JsonDataException("Expected data to be a JSON object")
        val status = (map["status"] ?: throw JsonDataException("Missing status field")) as? String
            ?: throw JsonDataException("Expected status field to be a string")
        return when (status) {
            "success" -> {
                @Suppress("UNCHECKED_CAST")
                val response = (map["response"] ?: throw JsonDataException("Missing response field"))
                        as? Map<String, Any> ?: throw JsonDataException("Expected response field to be a JSON object")
                dataAdapter.fromJsonValue(response)?.let { ApiResponse.Success(it) }
            }

            "error" -> {
                val message =
                    (map["message"] ?: throw JsonDataException("Missing message field")) as? String
                        ?: throw JsonDataException("Expected message field to be a string")
                ApiResponse.Error(message)
            }

            else -> throw JsonDataException("Unexpected status: $status")
        }
    }

    override fun toJson(writer: JsonWriter, value: ApiResponse<T>?) {
    }
}