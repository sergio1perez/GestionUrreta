package com.beta.gestionurretausuario.data.remote.model

import com.google.firebase.firestore.DocumentId

data class AsistenciaFirestore(
    @DocumentId
    val id: String = "", // claseId_usuarioId_fecha
    val claseId: String = "",
    val usuarioId: String = "",
    val fecha: Long = 0,

    val asistio: Boolean = true,
    val justificada: Boolean = false,
    val observaciones: String? = null
) {
    constructor() : this(id = "")

    fun toEntity(): com.beta.gestionurretausuario.data.local.entity.AsistenciaEntity {
        return com.beta.gestionurretausuario.data.local.entity.AsistenciaEntity(
            claseId = claseId,
            usuarioId = usuarioId,
            fecha = fecha,
            asistio = asistio,
            justificada = justificada,
            observaciones = observaciones
        )
    }

    companion object {
        fun fromEntity(entity: com.beta.gestionurretausuario.data.local.entity.AsistenciaEntity): AsistenciaFirestore {
            return AsistenciaFirestore(
                id = "${entity.claseId}_${entity.usuarioId}_${entity.fecha}",
                claseId = entity.claseId,
                usuarioId = entity.usuarioId,
                fecha = entity.fecha,
                asistio = entity.asistio,
                justificada = entity.justificada,
                observaciones = entity.observaciones
            )
        }
    }
}