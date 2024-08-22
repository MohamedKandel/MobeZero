package com.correct.mobezero.ui.getStarted

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.correct.mobezero.data.LoginResponse
import com.correct.mobezero.retrofit.APIService
import com.correct.mobezero.retrofit.RetrofitClient
import kotlinx.coroutines.launch

class AuthViewModel(application: Application): AndroidViewModel(application) {

    private val authRepo = AuthRepository(
        RetrofitClient.getClient().create(APIService::class.java)
    )

    private val _loginResponse = MutableLiveData<LoginResponse>()

    val loginResponse: LiveData<LoginResponse> get() = _loginResponse

    fun login(id: String) = viewModelScope.launch {
        val result = authRepo.login(id)
        if (result.isSuccessful) {
            _loginResponse.postValue(result.body())
        }
    }
}