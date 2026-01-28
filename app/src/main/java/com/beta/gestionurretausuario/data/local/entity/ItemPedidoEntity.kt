package com.beta.gestionurretausuario.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "items_pedido",
    primaryKeys = ["pedidoId", "productoId", "talla", "color"],
    foreignKeys = [
        ForeignKey(
            entity = PedidoEntity::class,
            parentColumns = ["id"],
            childColumns = ["pedidoId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProductoEntity::class,
            parentColumns = ["id"],
            childColumns = ["productoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("pedidoId"),
        Index("productoId")
    ]
)
data class ItemPedidoEntity(
    val pedidoId: String,
    val productoId: String,

    val nombreProducto: String, // Desnormalizado
    val cantidad: Int,
    val precioUnitario: Double,
    val subtotal: Double,

    val talla: String = "",
    val color: String = ""
)