package com.beta.gestionurretausuario.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clases")
data class ClaseEntity(
    @PrimaryKey
    val id: String,

    val nombre: String, // "Infantil", "Adultos", "Competición"
    val descripcion: String? = null,
    val instructorId: String,
    val instructorNombre: String, // Desnormalizado para mostrar rápido

    // Horario
    val diaSemana: Int, // 1=Lunes, 2=Martes...7=Domingo
    val horaInicio: String, // "18:00"
    val horaFin: String, // "19:30"

    val nivelRequerido: String? = null, // Cinturón mínimo requerido
    val edadMinima: Int? = null,
    val edadMaxima: Int? = null,
    val capacidadMaxima: Int = 20,

    val activa: Boolean = true,
    val ultimaSincronizacion: Long = System.currentTimeMillis()
)