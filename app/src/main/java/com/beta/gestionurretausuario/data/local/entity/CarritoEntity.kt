package com.beta.gestionurretausuario.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "carrito",
    primaryKeys = ["productoId", "talla", "color"]
)
data class CarritoEntity(
    val productoId: String,
    val nombreProducto: String,
    val imagenUrl: String? = null,
    val precio: Double,
    val cantidad: Int,
    val talla: String = "",
    val color: String = "",
    val fechaAgregado: Long = System.currentTimeMillis()
)