package com.beta.gestionurretausuario.data.remote.model

import com.google.firebase.firestore.DocumentId

data class ExamenFirestore(
    @DocumentId
    val id: String = "",
    val titulo: String = "",
    val descripcion: String? = null,

    val fecha: Long = 0,
    val horaInicio: String = "",
    val horaFin: String? = null,

    val lugar: String? = null,
    val direccion: String? = null,

    val cinturonObjetivo: String = "",
    val cinturonRequerido: String = "",

    val precio: Double = 0.0,
    val plazasMaximas: Int? = null,

    val fechaLimiteInscripcion: Long = 0,
    val requisitosAdicionales: String? = null,

    val estado: String = "abierto",
    val activo: Boolean = true
) {
    constructor() : this(id = "")

    fun toEntity(): com.beta.gestionurretausuario.data.local.entity.ExamenEntity {
        return com.beta.gestionurretausuario.data.local.entity.ExamenEntity(
            id = id,
            titulo = titulo,
            descripcion = descripcion,
            fecha = fecha,
            horaInicio = horaInicio,
            horaFin = horaFin,
            lugar = lugar,
            direccion = direccion,
            cinturonObjetivo = cinturonObjetivo,
            cinturonRequerido = cinturonRequerido,
            precio = precio,
            plazasMaximas = plazasMaximas,
            fechaLimiteInscripcion = fechaLimiteInscripcion,
            requisitosAdicionales = requisitosAdicionales,
            estado = estado,
            activo = activo
        )
    }

    companion object {
        fun fromEntity(entity: com.beta.gestionurretausuario.data.local.entity.ExamenEntity): ExamenFirestore {
            return ExamenFirestore(
                id = entity.id,
                titulo = entity.titulo,
                descripcion = entity.descripcion,
                fecha = entity.fecha,
                horaInicio = entity.horaInicio,
                horaFin = entity.horaFin,
                lugar = entity.lugar,
                direccion = entity.direccion,
                cinturonObjetivo = entity.cinturonObjetivo,
                cinturonRequerido = entity.cinturonRequerido,
                precio = entity.precio,
                plazasMaximas = entity.plazasMaximas,
                fechaLimiteInscripcion = entity.fechaLimiteInscripcion,
                requisitosAdicionales = entity.requisitosAdicionales,
                estado = entity.estado,
                activo = entity.activo
            )
        }
    }
}

data class InscripcionExamenFirestore(
    @DocumentId
    val id: String = "", // examenId_usuarioId
    val examenId: String = "",
    val usuarioId: String = "",

    val fechaInscripcion: Long = 0,
    val estado: String = "pendiente",
    val pagado: Boolean = false,

    val puntuacion: Double? = null,
    val observaciones: String? = null
) {
    constructor() : this(id = "")

    fun toEntity(): com.beta.gestionurretausuario.data.local.entity.InscripcionExamenEntity {
        return com.beta.gestionurretausuario.data.local.entity.InscripcionExamenEntity(
            examenId = examenId,
            usuarioId = usuarioId,
            fechaInscripcion = fechaInscripcion,
            estado = estado,
            pagado = pagado,
            puntuacion = puntuacion,
            observaciones = observaciones
        )
    }
}