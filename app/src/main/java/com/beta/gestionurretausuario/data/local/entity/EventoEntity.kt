package com.beta.gestionurretausuario.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "eventos")
data class EventoEntity(
    @PrimaryKey
    val id: String,

    val titulo: String,
    val descripcion: String,
    val imagenUrl: String? = null,

    val fechaInicio: Long,
    val fechaFin: Long? = null,
    val horaInicio: String? = null,
    val horaFin: String? = null,

    val lugar: String? = null,
    val direccion: String? = null,
    val latitud: Double? = null,
    val longitud: Double? = null,

    val tipoEvento: String, // "competicion", "exhibicion", "seminario", "social"
    val requiereInscripcion: Boolean = false,
    val precio: Double? = null,
    val plazasDisponibles: Int? = null,

    val activo: Boolean = true,
    val ultimaSincronizacion: Long = System.currentTimeMillis()
)