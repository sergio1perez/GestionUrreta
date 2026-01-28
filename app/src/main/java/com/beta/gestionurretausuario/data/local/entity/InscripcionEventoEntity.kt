package com.beta.gestionurretausuario.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "inscripciones_evento",
    primaryKeys = ["eventoId", "usuarioId"],
    foreignKeys = [
        ForeignKey(
            entity = EventoEntity::class,
            parentColumns = ["id"],
            childColumns = ["eventoId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("eventoId"),
        Index("usuarioId")
    ]
)
data class InscripcionEventoEntity(
    val eventoId: String,
    val usuarioId: String,
    val fechaInscripcion: Long,
    val estado: String = "pendiente", // "pendiente", "confirmada", "cancelada"
    val pagado: Boolean = false,
    val ultimaSincronizacion: Long = System.currentTimeMillis()
)