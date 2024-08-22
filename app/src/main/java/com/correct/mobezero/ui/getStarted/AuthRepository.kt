package com.correct.mobezero.ui.getStarted

import com.correct.mobezero.data.LoginResponse
import com.correct.mobezero.retrofit.APIService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class AuthRepository(private val apiService: APIService) {
    suspend fun login(id: String): Response<LoginResponse> = withContext(Dispatchers.IO) {
        apiService.login(id)
    }
}