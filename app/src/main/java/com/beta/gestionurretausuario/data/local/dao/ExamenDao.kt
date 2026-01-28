package com.beta.gestionurretausuario.data.local.dao

import androidx.room.*
import com.beta.gestionurretausuario.data.local.entity.ExamenEntity
import com.beta.gestionurretausuario.data.local.entity.InscripcionExamenEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExamenDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(examen: ExamenEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(examenes: List<ExamenEntity>)
    
    @Update
    suspend fun update(examen: ExamenEntity)
    
    @Delete
    suspend fun delete(examen: ExamenEntity)
    
    @Query("SELECT * FROM examenes WHERE id = :id")
    suspend fun getById(id: String): ExamenEntity?
    
    @Query("SELECT * FROM examenes WHERE id = :id")
    fun getByIdFlow(id: String): Flow<ExamenEntity?>
    
    @Query("SELECT * FROM examenes WHERE activo = 1 ORDER BY fecha DESC")
    fun getAllActivos(): Flow<List<ExamenEntity>>
    
    @Query("SELECT * FROM examenes WHERE estado = 'abierto' AND activo = 1 ORDER BY fecha ASC")
    fun getAbiertos(): Flow<List<ExamenEntity>>
    
    @Query("SELECT * FROM examenes WHERE fecha >= :fechaActual AND activo = 1 ORDER BY fecha ASC")
    fun getProximos(fechaActual: Long = System.currentTimeMillis()): Flow<List<ExamenEntity>>
    
    @Query("SELECT * FROM examenes WHERE cinturonObjetivo = :cinturon AND activo = 1 ORDER BY fecha DESC")
    fun getByCinturon(cinturon: String): Flow<List<ExamenEntity>>
    
    @Query("DELETE FROM examenes WHERE id = :id")
    suspend fun deleteById(id: String)
    
    @Query("DELETE FROM examenes")
    suspend fun deleteAll()
    
    // Inscripciones
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInscripcion(inscripcion: InscripcionExamenEntity)
    
    @Update
    suspend fun updateInscripcion(inscripcion: InscripcionExamenEntity)
    
    @Query("SELECT * FROM inscripciones_examen WHERE examenId = :examenId AND usuarioId = :usuarioId")
    suspend fun getInscripcion(examenId: String, usuarioId: String): InscripcionExamenEntity?
    
    @Query("SELECT * FROM inscripciones_examen WHERE usuarioId = :usuarioId ORDER BY fechaInscripcion DESC")
    fun getInscripcionesByUsuario(usuarioId: String): Flow<List<InscripcionExamenEntity>>
    
    @Query("SELECT * FROM inscripciones_examen WHERE examenId = :examenId")
    fun getInscripcionesByExamen(examenId: String): Flow<List<InscripcionExamenEntity>>
    
    @Query("DELETE FROM inscripciones_examen WHERE examenId = :examenId AND usuarioId = :usuarioId")
    suspend fun deleteInscripcion(examenId: String, usuarioId: String)
}
