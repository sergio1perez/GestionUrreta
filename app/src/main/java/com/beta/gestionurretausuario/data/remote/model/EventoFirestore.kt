package com.beta.gestionurretausuario.data.remote.model

import com.google.firebase.firestore.DocumentId

data class EventoFirestore(
    @DocumentId
    val id: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val imagenUrl: String? = null,

    val fechaInicio: Long = 0,
    val fechaFin: Long? = null,
    val horaInicio: String? = null,
    val horaFin: String? = null,

    val lugar: String? = null,
    val direccion: String? = null,
    val latitud: Double? = null,
    val longitud: Double? = null,

    val tipoEvento: String = "",
    val requiereInscripcion: Boolean = false,
    val precio: Double? = null,
    val plazasDisponibles: Int? = null,

    // En Firestore podemos guardar la lista de inscritos directamente
    val inscritos: List<String> = emptyList(),

    val activo: Boolean = true
) {
    constructor() : this(id = "")

    fun toEntity(): com.beta.gestionurretausuario.data.local.entity.EventoEntity {
        return com.beta.gestionurretausuario.data.local.entity.EventoEntity(
            id = id,
            titulo = titulo,
            descripcion = descripcion,
            imagenUrl = imagenUrl,
            fechaInicio = fechaInicio,
            fechaFin = fechaFin,
            horaInicio = horaInicio,
            horaFin = horaFin,
            lugar = lugar,
            direccion = direccion,
            latitud = latitud,
            longitud = longitud,
            tipoEvento = tipoEvento,
            requiereInscripcion = requiereInscripcion,
            precio = precio,
            plazasDisponibles = plazasDisponibles,
            activo = activo
        )
    }

    companion object {
        fun fromEntity(entity: com.beta.gestionurretausuario.data.local.entity.EventoEntity): EventoFirestore {
            return EventoFirestore(
                id = entity.id,
                titulo = entity.titulo,
                descripcion = entity.descripcion,
                imagenUrl = entity.imagenUrl,
                fechaInicio = entity.fechaInicio,
                fechaFin = entity.fechaFin,
                horaInicio = entity.horaInicio,
                horaFin = entity.horaFin,
                lugar = entity.lugar,
                direccion = entity.direccion,
                latitud = entity.latitud,
                longitud = entity.longitud,
                tipoEvento = entity.tipoEvento,
                requiereInscripcion = entity.requiereInscripcion,
                precio = entity.precio,
                plazasDisponibles = entity.plazasDisponibles,
                activo = entity.activo
            )
        }
    }
}

data class InscripcionEventoFirestore(
    @DocumentId
    val id: String = "", // eventoId_usuarioId
    val eventoId: String = "",
    val usuarioId: String = "",
    val fechaInscripcion: Long = 0,
    val estado: String = "pendiente",
    val pagado: Boolean = false
) {
    constructor() : this(id = "")

    fun toEntity(): com.beta.gestionurretausuario.data.local.entity.InscripcionEventoEntity {
        return com.beta.gestionurretausuario.data.local.entity.InscripcionEventoEntity(
            eventoId = eventoId,
            usuarioId = usuarioId,
            fechaInscripcion = fechaInscripcion,
            estado = estado,
            pagado = pagado
        )
    }
}