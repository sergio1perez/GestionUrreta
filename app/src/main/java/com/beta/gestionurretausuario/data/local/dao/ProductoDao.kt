package com.beta.gestionurretausuario.data.local.dao

import androidx.room.*
import com.beta.gestionurretausuario.data.local.entity.ProductoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductoDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(producto: ProductoEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(productos: List<ProductoEntity>)
    
    @Update
    suspend fun update(producto: ProductoEntity)
    
    @Delete
    suspend fun delete(producto: ProductoEntity)
    
    @Query("SELECT * FROM productos WHERE id = :id")
    suspend fun getById(id: String): ProductoEntity?
    
    @Query("SELECT * FROM productos WHERE id = :id")
    fun getByIdFlow(id: String): Flow<ProductoEntity?>
    
    @Query("SELECT * FROM productos WHERE activo = 1 ORDER BY nombre ASC")
    fun getAllActivos(): Flow<List<ProductoEntity>>
    
    @Query("SELECT * FROM productos WHERE categoria = :categoria AND activo = 1 ORDER BY nombre ASC")
    fun getByCategoria(categoria: String): Flow<List<ProductoEntity>>
    
    @Query("SELECT * FROM productos WHERE destacado = 1 AND activo = 1 ORDER BY nombre ASC")
    fun getDestacados(): Flow<List<ProductoEntity>>
    
    @Query("SELECT * FROM productos WHERE precioOferta IS NOT NULL AND activo = 1 ORDER BY nombre ASC")
    fun getEnOferta(): Flow<List<ProductoEntity>>
    
    @Query("SELECT * FROM productos WHERE activo = 1 AND (nombre LIKE '%' || :query || '%' OR descripcion LIKE '%' || :query || '%') ORDER BY nombre ASC")
    fun buscar(query: String): Flow<List<ProductoEntity>>
    
    @Query("SELECT * FROM productos WHERE stock <= stockMinimo AND activo = 1")
    fun getConStockBajo(): Flow<List<ProductoEntity>>
    
    @Query("SELECT DISTINCT categoria FROM productos WHERE activo = 1 ORDER BY categoria ASC")
    fun getCategorias(): Flow<List<String>>
    
    @Query("DELETE FROM productos WHERE id = :id")
    suspend fun deleteById(id: String)
    
    @Query("DELETE FROM productos")
    suspend fun deleteAll()
}
