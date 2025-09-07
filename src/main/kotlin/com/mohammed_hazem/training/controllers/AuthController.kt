package com.mohammed_hazem.training.controllers

import com.mohammed_hazem.training.security.AuthService
import com.mohammed_hazem.training.util.Consts
import jakarta.validation.constraints.Email
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(Consts.AUTH_ROUTE)
class AuthController(private val authService: AuthService) {
    data class RefreshRequest(
        val refreshToken: String
    )

    data class RegisterRequest(
        val name: String,
        @field:Email(message = "Not Valid Email, Fuck Off")
        val email: String,
        val password: String
    )

    data class LoginRequest(
        @field:Email(message = "Not Valid Email, Fuck Off")
        val email: String,
        val password: String
    )

    @PostMapping(Consts.REGISTER_ROUTE)
    fun register(@RequestBody body: RegisterRequest) {
        authService.register(
            userName = body.name,
            email = body.email,
            password = body.password
        )
    }

    @PostMapping(Consts.LOGIN_ROUTE)
    fun login(@RequestBody body: LoginRequest): AuthService.TokenPair {
        return authService.login(
            email = body.email,
            password = body.password
        )
    }

    @PostMapping(Consts.REFRESH_ROUTE)
    fun refresh(@RequestBody body: RefreshRequest): AuthService.TokenPair {
        return authService.refresh(
            refreshToken = body.refreshToken
        )
    }


}
