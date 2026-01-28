package com.beta.gestionurretausuario.data.remote.model

import com.google.firebase.firestore.DocumentId

data class PagoFirestore(
    @DocumentId
    val id: String = "",
    val usuarioId: String = "",

    val concepto: String = "",
    val descripcion: String? = null,

    val importe: Double = 0.0,
    val fechaPago: Long? = null,
    val fechaVencimiento: Long? = null,

    val tipoPago: String = "",
    val referenciaId: String? = null,

    val metodoPago: String? = null,
    val estado: String = "pendiente",

    val numeroRecibo: String? = null
) {
    constructor() : this(id = "")

    fun toEntity(): com.beta.gestionurretausuario.data.local.entity.PagoEntity {
        return com.beta.gestionurretausuario.data.local.entity.PagoEntity(
            id = id,
            usuarioId = usuarioId,
            concepto = concepto,
            descripcion = descripcion,
            importe = importe,
            fechaPago = fechaPago,
            fechaVencimiento = fechaVencimiento,
            tipoPago = tipoPago,
            referenciaId = referenciaId,
            metodoPago = metodoPago,
            estado = estado,
            numeroRecibo = numeroRecibo
        )
    }

    companion object {
        fun fromEntity(entity: com.beta.gestionurretausuario.data.local.entity.PagoEntity): PagoFirestore {
            return PagoFirestore(
                id = entity.id,
                usuarioId = entity.usuarioId,
                concepto = entity.concepto,
                descripcion = entity.descripcion,
                importe = entity.importe,
                fechaPago = entity.fechaPago,
                fechaVencimiento = entity.fechaVencimiento,
                tipoPago = entity.tipoPago,
                referenciaId = entity.referenciaId,
                metodoPago = entity.metodoPago,
                estado = entity.estado,
                numeroRecibo = entity.numeroRecibo
            )
        }
    }
}