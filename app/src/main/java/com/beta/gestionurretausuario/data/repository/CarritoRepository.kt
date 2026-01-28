package com.beta.gestionurretausuario.data.repository

import com.beta.gestionurretausuario.data.local.dao.CarritoDao
import com.beta.gestionurretausuario.data.local.entity.CarritoEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio para el carrito de compras.
 * El carrito solo se almacena localmente, no se sincroniza con Firestore.
 */
class CarritoRepository(
    private val carritoDao: CarritoDao
) {

    fun getAll(): Flow<List<CarritoEntity>> = carritoDao.getAll()

    fun getItemCount(): Flow<Int> = carritoDao.getItemCount()

    fun getTotalItems(): Flow<Int?> = carritoDao.getTotalItems()

    fun getTotal(): Flow<Double?> = carritoDao.getTotal()

    suspend fun get(productoId: String, talla: String, color: String): CarritoEntity? {
        return carritoDao.get(productoId, talla, color)
    }

    suspend fun addItem(item: CarritoEntity) {
        carritoDao.addOrIncrement(item)
    }

    suspend fun updateCantidad(productoId: String, talla: String, color: String, cantidad: Int) {
        if (cantidad <= 0) {
            carritoDao.delete(productoId, talla, color)
        } else {
            carritoDao.updateCantidad(productoId, talla, color, cantidad)
        }
    }

    suspend fun removeItem(productoId: String, talla: String, color: String) {
        carritoDao.delete(productoId, talla, color)
    }

    suspend fun vaciar() {
        carritoDao.vaciar()
    }

    /**
     * Añade un producto al carrito
     */
    suspend fun addProducto(
        productoId: String,
        nombreProducto: String,
        imagenUrl: String?,
        precio: Double,
        cantidad: Int = 1,
        talla: String = "",
        color: String = ""
    ) {
        val item = CarritoEntity(
            productoId = productoId,
            nombreProducto = nombreProducto,
            imagenUrl = imagenUrl,
            precio = precio,
            cantidad = cantidad,
            talla = talla,
            color = color
        )
        carritoDao.addOrIncrement(item)
    }
}