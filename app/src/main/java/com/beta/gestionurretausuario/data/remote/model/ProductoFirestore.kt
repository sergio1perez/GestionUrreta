package com.beta.gestionurretausuario.data.remote.model

import com.google.firebase.firestore.DocumentId
import com.google.gson.Gson

data class ProductoFirestore(
    @DocumentId
    val id: String = "",
    val nombre: String = "",
    val descripcion: String? = null,
    val imagenUrl: String? = null,
    val imagenesAdicionales: List<String> = emptyList(),

    val precio: Double = 0.0,
    val precioOferta: Double? = null,

    val categoria: String = "",
    val subcategoria: String? = null,

    val tallas: List<String> = emptyList(),
    val colores: List<String> = emptyList(),

    val stock: Int = 0,
    val stockMinimo: Int = 5,

    val activo: Boolean = true,
    val destacado: Boolean = false
) {
    constructor() : this(id = "")

    fun toEntity(): com.beta.gestionurretausuario.data.local.entity.ProductoEntity {
        val gson = Gson()
        return com.beta.gestionurretausuario.data.local.entity.ProductoEntity(
            id = id,
            nombre = nombre,
            descripcion = descripcion,
            imagenUrl = imagenUrl,
            imagenesAdicionales = if (imagenesAdicionales.isNotEmpty()) gson.toJson(imagenesAdicionales) else null,
            precio = precio,
            precioOferta = precioOferta,
            categoria = categoria,
            subcategoria = subcategoria,
            tallas = if (tallas.isNotEmpty()) gson.toJson(tallas) else null,
            colores = if (colores.isNotEmpty()) gson.toJson(colores) else null,
            stock = stock,
            stockMinimo = stockMinimo,
            activo = activo,
            destacado = destacado
        )
    }

    companion object {
        fun fromEntity(entity: com.beta.gestionurretausuario.data.local.entity.ProductoEntity): ProductoFirestore {
            val gson = Gson()
            return ProductoFirestore(
                id = entity.id,
                nombre = entity.nombre,
                descripcion = entity.descripcion,
                imagenUrl = entity.imagenUrl,
                imagenesAdicionales = entity.imagenesAdicionales?.let {
                    gson.fromJson(it, Array<String>::class.java).toList()
                } ?: emptyList(),
                precio = entity.precio,
                precioOferta = entity.precioOferta,
                categoria = entity.categoria,
                subcategoria = entity.subcategoria,
                tallas = entity.tallas?.let {
                    gson.fromJson(it, Array<String>::class.java).toList()
                } ?: emptyList(),
                colores = entity.colores?.let {
                    gson.fromJson(it, Array<String>::class.java).toList()
                } ?: emptyList(),
                stock = entity.stock,
                stockMinimo = entity.stockMinimo,
                activo = entity.activo,
                destacado = entity.destacado
            )
        }
    }
}