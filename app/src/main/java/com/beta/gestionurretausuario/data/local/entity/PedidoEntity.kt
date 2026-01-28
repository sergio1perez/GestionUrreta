package com.beta.gestionurretausuario.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pedidos",
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
data class PedidoEntity(
    @PrimaryKey
    val id: String,

    val usuarioId: String,
    val numeroPedido: String, // Número legible: "PED-2024-001"

    val fechaPedido: Long,
    val fechaEnvio: Long? = null,
    val fechaEntrega: Long? = null,

    val subtotal: Double,
    val descuento: Double = 0.0,
    val gastosEnvio: Double = 0.0,
    val total: Double,

    val estado: String = "pendiente", // "pendiente", "confirmado", "preparando", "enviado", "entregado", "cancelado"

    val direccionEnvio: String? = null,
    val ciudadEnvio: String? = null,
    val codigoPostalEnvio: String? = null,

    val metodoPago: String? = null,
    val pagado: Boolean = false,

    val observaciones: String? = null,

    val ultimaSincronizacion: Long = System.currentTimeMillis()
)