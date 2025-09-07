package com.mohammed_hazem.training.security

import com.mohammed_hazem.training.util.Consts
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatusCode
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.util.*

@Service
class JwtService(@Value("\${jwt.securekey}") private val securetString: String) {

    private val secretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(securetString))
    private val accessTokenLifeDuration = 30L * 60 * 1000
    val refreshTokenLifeDuration = 100L * 24 * 60 * 60 * 1000

    private fun generateToken(userId: String, type: String, expiry: Long): String {
        val now = Date()
        val expiryDate = Date(now.time + expiry)
        return Jwts.builder()
            .subject(userId)
            .claim(Consts.KEY_TYPE, type)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(secretKey, Jwts.SIG.HS256)
            .compact()
    }

    fun generateAccessToken(userId: String) =
        generateToken(userId = userId, type = Consts.ACCESS_CODE_TYPE, expiry = accessTokenLifeDuration)

    fun generateRefreshToken(userId: String) =
        generateToken(userId = userId, type = Consts.REFRESH_CODE_TYPE, expiry = refreshTokenLifeDuration)

    fun verifyAccessToken(token: String): Boolean {
        val claims = getClaims(token) ?: return false
        val type = claims[Consts.KEY_TYPE] as? String ?: return false

        return type == Consts.ACCESS_CODE_TYPE
    }

    fun verifyRefreshToken(token: String): Boolean {
        val claims = getClaims(token) ?: return false
        val type = claims[Consts.KEY_TYPE] as? String ?: return false

        return type == Consts.REFRESH_CODE_TYPE
    }

    fun getUserIdFromToken(token: String): String {
        val claims = getClaims(token) ?: throw ResponseStatusException(HttpStatusCode.valueOf(401), "Invalid Token")
        return claims.subject
    }

    fun getTokenIssueDate(token : String) : Date {
        val claims = getClaims(token) ?: throw ResponseStatusException(HttpStatusCode.valueOf(401), "Invalid Token")
        return claims.issuedAt
    }


    fun getTokenExpirationDate(token : String) : Date {
        val claims = getClaims(token) ?: throw ResponseStatusException(HttpStatusCode.valueOf(401), "Invalid Token")
        return claims.expiration
    }



    private fun getClaims(tokenString: String): Claims? {
        val rawToken = if (tokenString.startsWith("Bearer"))
            tokenString.removePrefix("Bearer ")
        else tokenString

        return try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(rawToken)
                .payload
        } catch (e: Exception) {
            null
        }
    }
}