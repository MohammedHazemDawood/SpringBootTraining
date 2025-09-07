package com.mohammed_hazem.training.database.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document("notes")
data class Note(
    @Id val id: ObjectId = ObjectId.get(),
    val title: String,
    val color: Long,
    val content : String,
    val createdAt :Instant,
    val ownerId : ObjectId
    )