package com.idloquy.landmark.data.network.interceptors

import android.util.Log
import com.idloquy.landmark.data.network.BearerToken
import okhttp3.Interceptor
import okhttp3.Response

class BearerTokenInterceptor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        Log.d("bearer token interceptor", "started")
        var newReq = chain.request()
        Log.d("bearer token interceptor", "running for req: $newReq")
        val token = newReq.tag(BearerToken::class.java)
        Log.d("bearer token interceptor", "adding token: $token")
        if (token != null) {
            newReq = newReq.newBuilder().addHeader("Authentication", "Bearer ${token.value}").build()
        }
        Log.d("bearer token interceptor", "updated req: $newReq")
        return chain.proceed(newReq)
    }
}