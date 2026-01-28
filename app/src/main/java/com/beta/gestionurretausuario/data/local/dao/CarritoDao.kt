package com.beta.gestionurretausuario.data.local.dao

import androidx.room.*
import com.beta.gestionurretausuario.data.local.entity.CarritoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CarritoDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: CarritoEntity)
    
    @Update
    suspend fun update(item: CarritoEntity)
    
    @Delete
    suspend fun delete(item: CarritoEntity)
    
    @Query("SELECT * FROM carrito ORDER BY fechaAgregado DESC")
    fun getAll(): Flow<List<CarritoEntity>>
    
    @Query("SELECT * FROM carrito WHERE productoId = :productoId AND talla = :talla AND color = :color")
    suspend fun get(productoId: String, talla: String, color: String): CarritoEntity?
    
    @Query("SELECT COUNT(*) FROM carrito")
    fun getItemCount(): Flow<Int>
    
    @Query("SELECT SUM(cantidad) FROM carrito")
    fun getTotalItems(): Flow<Int?>
    
    @Query("SELECT SUM(precio * cantidad) FROM carrito")
    fun getTotal(): Flow<Double?>
    
    @Query("UPDATE carrito SET cantidad = :cantidad WHERE productoId = :productoId AND talla = :talla AND color = :color")
    suspend fun updateCantidad(productoId: String, talla: String, color: String, cantidad: Int)
    
    @Query("DELETE FROM carrito WHERE productoId = :productoId AND talla = :talla AND color = :color")
    suspend fun delete(productoId: String, talla: String, color: String)
    
    @Query("DELETE FROM carrito")
    suspend fun vaciar()
    
    // Método para añadir o incrementar cantidad
    @Transaction
    suspend fun addOrIncrement(item: CarritoEntity) {
        val existing = get(item.productoId, item.talla, item.color)
        if (existing != null) {
            updateCantidad(item.productoId, item.talla, item.color, existing.cantidad + item.cantidad)
        } else {
            insert(item)
        }
    }
}
