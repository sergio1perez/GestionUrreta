package com.beta.gestionurretausuario.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "inscripciones_examen",
    primaryKeys = ["examenId", "usuarioId"],
    foreignKeys = [
        ForeignKey(
            entity = ExamenEntity::class,
            parentColumns = ["id"],
            childColumns = ["examenId"],
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
        Index("examenId"),
        Index("usuarioId")
    ]
)
data class InscripcionExamenEntity(
    val examenId: String,
    val usuarioId: String,

    val fechaInscripcion: Long,
    val estado: String = "pendiente", // "pendiente", "confirmada", "aprobado", "suspendido", "no_presentado"
    val pagado: Boolean = false,

    val puntuacion: Double? = null, // Nota del examen si aplica
    val observaciones: String? = null,

    val ultimaSincronizacion: Long = System.currentTimeMillis()
)