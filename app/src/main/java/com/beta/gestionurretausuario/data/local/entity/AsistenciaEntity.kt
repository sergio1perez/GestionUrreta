package com.beta.gestionurretausuario.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "asistencias",
    primaryKeys = ["claseId", "usuarioId", "fecha"],
    foreignKeys = [
        ForeignKey(
            entity = ClaseEntity::class,
            parentColumns = ["id"],
            childColumns = ["claseId"],
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
        Index("claseId"),
        Index("usuarioId"),
        Index("fecha")
    ]
)
data class AsistenciaEntity(
    val claseId: String,
    val usuarioId: String,
    val fecha: Long, // Solo fecha, sin hora (timestamp a las 00:00)

    val asistio: Boolean = true,
    val justificada: Boolean = false,
    val observaciones: String? = null,

    val ultimaSincronizacion: Long = System.currentTimeMillis()
)