package com.mohammed_hazem.training.database.model

import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document("JTIs")
data class RefreshToken(
    val userId : ObjectId,
    val issuedAt : Instant,
    @Indexed(expireAfter = "0s")
    val expireAt : Instant,
    val hashedToken : String
)