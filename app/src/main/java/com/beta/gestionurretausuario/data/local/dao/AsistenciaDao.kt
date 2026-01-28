package com.beta.gestionurretausuario.data.local.dao

import androidx.room.*
import com.beta.gestionurretausuario.data.local.entity.AsistenciaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AsistenciaDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(asistencia: AsistenciaEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(asistencias: List<AsistenciaEntity>)
    
    @Update
    suspend fun update(asistencia: AsistenciaEntity)
    
    @Delete
    suspend fun delete(asistencia: AsistenciaEntity)
    
    @Query("SELECT * FROM asistencias WHERE claseId = :claseId AND usuarioId = :usuarioId AND fecha = :fecha")
    suspend fun get(claseId: String, usuarioId: String, fecha: Long): AsistenciaEntity?
    
    @Query("SELECT * FROM asistencias WHERE usuarioId = :usuarioId ORDER BY fecha DESC")
    fun getByUsuario(usuarioId: String): Flow<List<AsistenciaEntity>>
    
    @Query("SELECT * FROM asistencias WHERE claseId = :claseId ORDER BY fecha DESC")
    fun getByClase(claseId: String): Flow<List<AsistenciaEntity>>
    
    @Query("SELECT * FROM asistencias WHERE usuarioId = :usuarioId AND fecha BETWEEN :fechaInicio AND :fechaFin ORDER BY fecha DESC")
    fun getByUsuarioEntreFechas(usuarioId: String, fechaInicio: Long, fechaFin: Long): Flow<List<AsistenciaEntity>>
    
    @Query("SELECT COUNT(*) FROM asistencias WHERE usuarioId = :usuarioId AND asistio = 1")
    suspend fun countAsistencias(usuarioId: String): Int
    
    @Query("SELECT COUNT(*) FROM asistencias WHERE usuarioId = :usuarioId AND asistio = 0")
    suspend fun countFaltas(usuarioId: String): Int
    
    @Query("DELETE FROM asistencias WHERE claseId = :claseId AND usuarioId = :usuarioId AND fecha = :fecha")
    suspend fun delete(claseId: String, usuarioId: String, fecha: Long)
    
    @Query("DELETE FROM asistencias")
    suspend fun deleteAll()
}
