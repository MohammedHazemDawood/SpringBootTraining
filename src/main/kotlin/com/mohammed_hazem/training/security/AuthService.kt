package com.mohammed_hazem.training.security

import com.mohammed_hazem.training.database.model.RefreshToken
import com.mohammed_hazem.training.database.model.User
import com.mohammed_hazem.training.database.repo.RefreshTokenRepository
import com.mohammed_hazem.training.database.repo.UsersRepository
import com.mongodb.DuplicateKeyException
import org.bson.types.ObjectId
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class AuthService(
    private val usersRepository: UsersRepository,
    private val bCrypt: Hash.BCryptHash,
    private val digest: Hash.MassageDigestHash,
    private val jwtService: JwtService,
    private val refreshTokenRepository: RefreshTokenRepository
) {

    fun register(userName: String, email: String, password: String): User {
        return try {
            usersRepository.insert(User(name = userName, email = email, hashedPassword = bCrypt.encode(password)))
        } catch (e: DuplicateKeyException) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "A user with that email \"$email\" is already exists.")
        }
    }

    fun login(email: String, password: String): TokenPair {
        val user = usersRepository.findByEmail(email.trim())
        if (user == null)
            throw BadCredentialsException("Invalid Credentials")
        if (!bCrypt.match(password, user.hashedPassword))
            throw BadCredentialsException("Invalid Credentials")

        val newRefreshToken = jwtService.generateRefreshToken(user.id.toHexString())
        val newAccessToken = jwtService.generateAccessToken(user.id.toHexString())

        storeRefreshToken(user.id, newRefreshToken)

        return TokenPair(
            refreshToken = newRefreshToken,
            accessToken = newAccessToken
        )
    }

    @Transactional
    fun refresh(refreshToken: String): TokenPair {
        if (!jwtService.verifyRefreshToken(refreshToken))
            throw ResponseStatusException(HttpStatusCode.valueOf(401), "Invalid refresh token.")

        val userId = jwtService.getUserIdFromToken(refreshToken)
        val user = usersRepository.findById(ObjectId(userId))
            .orElseThrow { ResponseStatusException(HttpStatusCode.valueOf(401), "Invalid refresh token.") }
        val hashedRefreshToken = digest.encode(refreshToken)

        if (refreshTokenRepository.findByUserIdAndHashedToken(user.id, hashedRefreshToken) == null)
            throw ResponseStatusException(HttpStatusCode.valueOf(401), "Invalid refresh token.")

        refreshTokenRepository.deleteByUserIdAndHashedToken(user.id, hashedRefreshToken)

        val newRefreshToken = jwtService.generateRefreshToken(user.id.toHexString())
        val newAccessToken = jwtService.generateAccessToken(user.id.toHexString())

        storeRefreshToken(user.id, newRefreshToken)

        return TokenPair(
            refreshToken = newRefreshToken,
            accessToken = newAccessToken
        )
    }


    private fun storeRefreshToken(userId: ObjectId, refreshToken: String) {
        val createdAt = jwtService.getTokenIssueDate(refreshToken).toInstant()
        val expiresAt = jwtService.getTokenExpirationDate(refreshToken).toInstant()

        refreshTokenRepository.save(
            RefreshToken(
                userId = userId,
                issuedAt = createdAt,
                expireAt = expiresAt,
                hashedToken = digest.encode(refreshToken)
            )
        )
    }

    data class TokenPair(
        val refreshToken: String,
        val accessToken: String
    )
}