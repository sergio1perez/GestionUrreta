package com.beta.gestionurretausuario.data.local.dao

import androidx.room.*
import com.beta.gestionurretausuario.data.local.entity.ItemPedidoEntity
import com.beta.gestionurretausuario.data.local.entity.PedidoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PedidoDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pedido: PedidoEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(pedidos: List<PedidoEntity>)
    
    @Update
    suspend fun update(pedido: PedidoEntity)
    
    @Delete
    suspend fun delete(pedido: PedidoEntity)
    
    @Query("SELECT * FROM pedidos WHERE id = :id")
    suspend fun getById(id: String): PedidoEntity?
    
    @Query("SELECT * FROM pedidos WHERE id = :id")
    fun getByIdFlow(id: String): Flow<PedidoEntity?>
    
    @Query("SELECT * FROM pedidos WHERE usuarioId = :usuarioId ORDER BY fechaPedido DESC")
    fun getByUsuario(usuarioId: String): Flow<List<PedidoEntity>>
    
    @Query("SELECT * FROM pedidos WHERE usuarioId = :usuarioId AND estado = :estado ORDER BY fechaPedido DESC")
    fun getByUsuarioYEstado(usuarioId: String, estado: String): Flow<List<PedidoEntity>>
    
    @Query("SELECT * FROM pedidos WHERE numeroPedido = :numeroPedido LIMIT 1")
    suspend fun getByNumeroPedido(numeroPedido: String): PedidoEntity?
    
    @Query("DELETE FROM pedidos WHERE id = :id")
    suspend fun deleteById(id: String)
    
    @Query("DELETE FROM pedidos")
    suspend fun deleteAll()
    
    // Items del pedido
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ItemPedidoEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<ItemPedidoEntity>)
    
    @Query("SELECT * FROM items_pedido WHERE pedidoId = :pedidoId")
    fun getItemsByPedido(pedidoId: String): Flow<List<ItemPedidoEntity>>
    
    @Query("SELECT * FROM items_pedido WHERE pedidoId = :pedidoId")
    suspend fun getItemsByPedidoSync(pedidoId: String): List<ItemPedidoEntity>
    
    @Query("DELETE FROM items_pedido WHERE pedidoId = :pedidoId")
    suspend fun deleteItemsByPedido(pedidoId: String)
    
    // Transacción para insertar pedido con items
    @Transaction
    suspend fun insertPedidoConItems(pedido: PedidoEntity, items: List<ItemPedidoEntity>) {
        insert(pedido)
        insertItems(items)
    }
}
