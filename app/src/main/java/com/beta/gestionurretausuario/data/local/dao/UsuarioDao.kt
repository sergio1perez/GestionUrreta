package com.beta.gestionurretausuario.data.local.dao

import androidx.room.*
import com.beta.gestionurretausuario.data.local.entity.UsuarioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(usuario: UsuarioEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(usuarios: List<UsuarioEntity>)
    
    @Update
    suspend fun update(usuario: UsuarioEntity)
    
    @Delete
    suspend fun delete(usuario: UsuarioEntity)
    
    @Query("SELECT * FROM usuarios WHERE id = :id")
    suspend fun getById(id: String): UsuarioEntity?
    
    @Query("SELECT * FROM usuarios WHERE id = :id")
    fun getByIdFlow(id: String): Flow<UsuarioEntity?>
    
    @Query("SELECT * FROM usuarios WHERE email = :email LIMIT 1")
    suspend fun getByEmail(email: String): UsuarioEntity?
    
    @Query("SELECT * FROM usuarios WHERE activo = 1")
    fun getAllActivos(): Flow<List<UsuarioEntity>>
    
    @Query("SELECT * FROM usuarios WHERE tipoUsuario = :tipo AND activo = 1")
    fun getByTipo(tipo: String): Flow<List<UsuarioEntity>>
    
    @Query("DELETE FROM usuarios WHERE id = :id")
    suspend fun deleteById(id: String)
    
    @Query("DELETE FROM usuarios")
    suspend fun deleteAll()
    
    @Query("UPDATE usuarios SET ultimaSincronizacion = :timestamp WHERE id = :id")
    suspend fun updateSyncTimestamp(id: String, timestamp: Long = System.currentTimeMillis())
}
