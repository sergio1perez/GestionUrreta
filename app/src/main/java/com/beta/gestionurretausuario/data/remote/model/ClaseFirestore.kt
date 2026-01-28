package com.beta.gestionurretausuario.data.remote.model

import com.google.firebase.firestore.DocumentId

data class ClaseFirestore(
    @DocumentId
    val id: String = "",
    val nombre: String = "",
    val descripcion: String? = null,
    val instructorId: String = "",
    val instructorNombre: String = "",

    val diaSemana: Int = 1,
    val horaInicio: String = "",
    val horaFin: String = "",

    val nivelRequerido: String? = null,
    val edadMinima: Int? = null,
    val edadMaxima: Int? = null,
    val capacidadMaxima: Int = 20,

    val activa: Boolean = true
) {
    constructor() : this(id = "")

    fun toEntity(): com.beta.gestionurretausuario.data.local.entity.ClaseEntity {
        return com.beta.gestionurretausuario.data.local.entity.ClaseEntity(
            id = id,
            nombre = nombre,
            descripcion = descripcion,
            instructorId = instructorId,
            instructorNombre = instructorNombre,
            diaSemana = diaSemana,
            horaInicio = horaInicio,
            horaFin = horaFin,
            nivelRequerido = nivelRequerido,
            edadMinima = edadMinima,
            edadMaxima = edadMaxima,
            capacidadMaxima = capacidadMaxima,
            activa = activa
        )
    }

    companion object {
        fun fromEntity(entity: com.beta.gestionurretausuario.data.local.entity.ClaseEntity): ClaseFirestore {
            return ClaseFirestore(
                id = entity.id,
                nombre = entity.nombre,
                descripcion = entity.descripcion,
                instructorId = entity.instructorId,
                instructorNombre = entity.instructorNombre,
                diaSemana = entity.diaSemana,
                horaInicio = entity.horaInicio,
                horaFin = entity.horaFin,
                nivelRequerido = entity.nivelRequerido,
                edadMinima = entity.edadMinima,
                edadMaxima = entity.edadMaxima,
                capacidadMaxima = entity.capacidadMaxima,
                activa = entity.activa
            )
        }
    }
}