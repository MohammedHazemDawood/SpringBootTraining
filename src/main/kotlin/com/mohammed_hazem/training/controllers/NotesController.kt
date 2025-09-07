package com.mohammed_hazem.training.controllers

import com.mohammed_hazem.training.database.model.Note
import com.mohammed_hazem.training.database.repo.NotesRepository
import org.bson.types.ObjectId
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import java.time.Instant

@RestController
@RequestMapping("/notes")
class NotesController(private val repository: NotesRepository) {
    data class NoteRequest(
        val id: String?,
        val title: String,
        val color: Long,
        val content: String,
    )

    data class NoteResponse(
        val id: String,
        val title: String,
        val color: Long,
        val content: String,
        val createdAt: Instant
    )

    @GetMapping
    fun getNotesByUserId(@RequestParam(required = true) user: String): List<NoteResponse> =
        repository.findByOwnerId(ObjectId(SecurityContextHolder.getContext().authentication.principal as String))
            .map(::noteToResponse)

    @PostMapping
    fun saveNote(@RequestBody note: NoteRequest) {
        val ownerIdStr = SecurityContextHolder.getContext().authentication.principal as String
        if (!ObjectId.isValid(ownerIdStr)) {
            throw IllegalArgumentException("Invalid ownerId")
        }

        val ownerId = ObjectId(ownerIdStr)
        val noteId = note.id?.takeIf { ObjectId.isValid(it) }?.let { ObjectId(it) } ?: ObjectId.get()

        repository.save(
            Note(
                id = noteId,
                title = note.title,
                color = note.color,
                content = note.content,
                ownerId = ownerId,
                createdAt = Instant.now()
            )
        )
    }


    @DeleteMapping(path = ["/{id}"])
    fun deleteNote(@PathVariable id: String) {
        val ownerId = SecurityContextHolder.getContext().authentication.principal as String
        val note = repository.findById(ObjectId(id)).orElseThrow { IllegalArgumentException("Note not found") }
        if (ObjectId(ownerId) == note.ownerId)
            repository.delete(note)
    }

    private fun noteToResponse(note: Note): NoteResponse = NoteResponse(
        id = note.id.toHexString(),
        title = note.title,
        color = note.color,
        content = note.content,
        createdAt = note.createdAt
    )
}