package com.cristmejia2006.cmcontroldegastos.ui.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cristmejia2006.cmcontroldegastos.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: FirebaseUser?) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(private val repository: AuthRepository = AuthRepository()) : ViewModel() {

    var authState by mutableStateOf<AuthState>(AuthState.Idle)
        private set

    fun login(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            authState = AuthState.Error("Por favor, completa todos los campos")
            return
        }

        viewModelScope.launch {
            authState = AuthState.Loading
            val result = repository.login(email, password)
            authState = if (result.isSuccess) {
                AuthState.Success(result.getOrNull())
            } else {
                AuthState.Error(result.exceptionOrNull()?.message ?: "Error desconocido")
            }
        }
    }

    fun register(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            authState = AuthState.Error("Por favor, completa todos los campos")
            return
        }

        viewModelScope.launch {
            authState = AuthState.Loading
            val result = repository.register(email, password)
            authState = if (result.isSuccess) {
                AuthState.Success(result.getOrNull())
            } else {
                AuthState.Error(result.exceptionOrNull()?.message ?: "Error desconocido")
            }
        }
    }
}
