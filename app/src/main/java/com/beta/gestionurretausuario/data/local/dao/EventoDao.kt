package com.beta.gestionurretausuario.data.local.dao

import androidx.room.*
import com.beta.gestionurretausuario.data.local.entity.EventoEntity
import com.beta.gestionurretausuario.data.local.entity.InscripcionEventoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EventoDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(evento: EventoEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(eventos: List<EventoEntity>)
    
    @Update
    suspend fun update(evento: EventoEntity)
    
    @Delete
    suspend fun delete(evento: EventoEntity)
    
    @Query("SELECT * FROM eventos WHERE id = :id")
    suspend fun getById(id: String): EventoEntity?
    
    @Query("SELECT * FROM eventos WHERE id = :id")
    fun getByIdFlow(id: String): Flow<EventoEntity?>
    
    @Query("SELECT * FROM eventos WHERE activo = 1 ORDER BY fechaInicio DESC")
    fun getAllActivos(): Flow<List<EventoEntity>>
    
    @Query("SELECT * FROM eventos WHERE activo = 1 AND fechaInicio >= :fechaActual ORDER BY fechaInicio ASC")
    fun getProximos(fechaActual: Long = System.currentTimeMillis()): Flow<List<EventoEntity>>
    
    @Query("SELECT * FROM eventos WHERE activo = 1 AND fechaInicio < :fechaActual ORDER BY fechaInicio DESC")
    fun getPasados(fechaActual: Long = System.currentTimeMillis()): Flow<List<EventoEntity>>
    
    @Query("SELECT * FROM eventos WHERE tipoEvento = :tipo AND activo = 1 ORDER BY fechaInicio DESC")
    fun getByTipo(tipo: String): Flow<List<EventoEntity>>
    
    @Query("DELETE FROM eventos WHERE id = :id")
    suspend fun deleteById(id: String)
    
    @Query("DELETE FROM eventos")
    suspend fun deleteAll()
    
    // Inscripciones
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInscripcion(inscripcion: InscripcionEventoEntity)
    
    @Query("SELECT * FROM inscripciones_evento WHERE eventoId = :eventoId AND usuarioId = :usuarioId")
    suspend fun getInscripcion(eventoId: String, usuarioId: String): InscripcionEventoEntity?
    
    @Query("SELECT * FROM inscripciones_evento WHERE usuarioId = :usuarioId")
    fun getInscripcionesByUsuario(usuarioId: String): Flow<List<InscripcionEventoEntity>>
    
    @Query("DELETE FROM inscripciones_evento WHERE eventoId = :eventoId AND usuarioId = :usuarioId")
    suspend fun deleteInscripcion(eventoId: String, usuarioId: String)
}
