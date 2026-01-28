package com.beta.gestionurretausuario.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "examenes")
data class ExamenEntity(
    @PrimaryKey
    val id: String,

    val titulo: String,
    val descripcion: String? = null,

    val fecha: Long,
    val horaInicio: String,
    val horaFin: String? = null,

    val lugar: String? = null,
    val direccion: String? = null,

    val cinturonObjetivo: String, // "amarillo", "verde", etc.
    val cinturonRequerido: String, // Cinturón mínimo para presentarse

    val precio: Double,
    val plazasMaximas: Int? = null,

    val fechaLimiteInscripcion: Long,
    val requisitosAdicionales: String? = null, // Texto con requisitos

    val estado: String = "abierto", // "abierto", "cerrado", "finalizado", "cancelado"
    val activo: Boolean = true,

    val ultimaSincronizacion: Long = System.currentTimeMillis()
)