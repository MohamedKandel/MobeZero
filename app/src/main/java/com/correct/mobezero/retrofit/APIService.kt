package com.correct.mobezero.retrofit


import com.correct.mobezero.data.LoginResponse
import retrofit2.Response
import retrofit2.http.POST
import retrofit2.http.Query

interface APIService {
    @POST("android_symlex")
    suspend fun login(@Query("id") id:String): Response<LoginResponse>
}