package com.mohammed_hazem.training.database.repo

import com.mohammed_hazem.training.database.model.User
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface UsersRepository : MongoRepository<User, ObjectId> {
    fun findByEmail(email : String) : User?
}