package com.mohammed_hazem.training.security

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component
import java.security.MessageDigest
import java.util.Base64

class Hash {

    @Component
    class BCryptHash {
        private val bcrypt = BCryptPasswordEncoder()

        fun encode(password: String) = bcrypt.encode(password)

        fun match(rawPassword: String, hashedPassword: String) = bcrypt.matches(rawPassword, hashedPassword)
    }

    @Component
    class MassageDigestHash {
        fun encode(text : String) : String{
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(text.encodeToByteArray())

            return Base64.getEncoder().encodeToString(hashBytes)
        }

        fun match(rawText : String, hashedText :String) : Boolean = encode(rawText) == hashedText

    }
}