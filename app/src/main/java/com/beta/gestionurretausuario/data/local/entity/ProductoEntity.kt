package com.beta.gestionurretausuario.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "productos")
data class ProductoEntity(
    @PrimaryKey
    val id: String,

    val nombre: String,
    val descripcion: String? = null,
    val imagenUrl: String? = null,
    val imagenesAdicionales: String? = null, // JSON array de URLs

    val precio: Double,
    val precioOferta: Double? = null, // Si está en oferta

    val categoria: String, // "dobok", "protecciones", "accesorios", "equipamiento"
    val subcategoria: String? = null,

    val tallas: String? = null, // JSON array: ["S", "M", "L", "XL"]
    val colores: String? = null, // JSON array: ["blanco", "negro"]

    val stock: Int = 0,
    val stockMinimo: Int = 5,

    val activo: Boolean = true,
    val destacado: Boolean = false,

    val ultimaSincronizacion: Long = System.currentTimeMillis()
)