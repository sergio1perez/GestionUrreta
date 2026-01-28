package com.beta.gestionurretausuario.data.local.dao

import androidx.room.*
import com.beta.gestionurretausuario.data.local.entity.PagoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PagoDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pago: PagoEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(pagos: List<PagoEntity>)
    
    @Update
    suspend fun update(pago: PagoEntity)
    
    @Delete
    suspend fun delete(pago: PagoEntity)
    
    @Query("SELECT * FROM pagos WHERE id = :id")
    suspend fun getById(id: String): PagoEntity?
    
    @Query("SELECT * FROM pagos WHERE id = :id")
    fun getByIdFlow(id: String): Flow<PagoEntity?>
    
    @Query("SELECT * FROM pagos WHERE usuarioId = :usuarioId ORDER BY fechaVencimiento DESC")
    fun getByUsuario(usuarioId: String): Flow<List<PagoEntity>>
    
    @Query("SELECT * FROM pagos WHERE usuarioId = :usuarioId AND estado = 'pendiente' ORDER BY fechaVencimiento ASC")
    fun getPendientesByUsuario(usuarioId: String): Flow<List<PagoEntity>>
    
    @Query("SELECT * FROM pagos WHERE usuarioId = :usuarioId AND estado = 'pagado' ORDER BY fechaPago DESC")
    fun getPagadosByUsuario(usuarioId: String): Flow<List<PagoEntity>>
    
    @Query("SELECT * FROM pagos WHERE usuarioId = :usuarioId AND estado = 'vencido' ORDER BY fechaVencimiento ASC")
    fun getVencidosByUsuario(usuarioId: String): Flow<List<PagoEntity>>
    
    @Query("SELECT * FROM pagos WHERE estado = 'pendiente' AND fechaVencimiento < :fechaActual")
    suspend fun getPagosVencidos(fechaActual: Long = System.currentTimeMillis()): List<PagoEntity>
    
    @Query("UPDATE pagos SET estado = 'vencido' WHERE estado = 'pendiente' AND fechaVencimiento < :fechaActual")
    suspend fun marcarVencidos(fechaActual: Long = System.currentTimeMillis())
    
    @Query("SELECT SUM(importe) FROM pagos WHERE usuarioId = :usuarioId AND estado = 'pendiente'")
    suspend fun getTotalPendiente(usuarioId: String): Double?
    
    @Query("DELETE FROM pagos WHERE id = :id")
    suspend fun deleteById(id: String)
    
    @Query("DELETE FROM pagos")
    suspend fun deleteAll()
}
