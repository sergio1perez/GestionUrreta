package com.beta.gestionurretausuario.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pagos",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("usuarioId")]
)
data class PagoEntity(
    @PrimaryKey
    val id: String,

    val usuarioId: String,

    val concepto: String, // "Mensualidad Enero 2024", "Examen cinturón verde", etc.
    val descripcion: String? = null,

    val importe: Double,
    val fechaPago: Long? = null,
    val fechaVencimiento: Long? = null,

    val tipoPago: String, // "mensualidad", "examen", "evento", "tienda", "otro"
    val referenciaId: String? = null, // ID del examen, evento o pedido relacionado

    val metodoPago: String? = null, // "efectivo", "tarjeta", "transferencia", "domiciliacion"
    val estado: String = "pendiente", // "pendiente", "pagado", "vencido", "cancelado"

    val numeroRecibo: String? = null,

    val ultimaSincronizacion: Long = System.currentTimeMillis()
)