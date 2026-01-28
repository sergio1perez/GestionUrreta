package com.beta.gestionurretausuario.data.local.dao

import androidx.room.*
import com.beta.gestionurretausuario.data.local.entity.ClaseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClaseDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(clase: ClaseEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(clases: List<ClaseEntity>)
    
    @Update
    suspend fun update(clase: ClaseEntity)
    
    @Delete
    suspend fun delete(clase: ClaseEntity)
    
    @Query("SELECT * FROM clases WHERE id = :id")
    suspend fun getById(id: String): ClaseEntity?
    
    @Query("SELECT * FROM clases WHERE id = :id")
    fun getByIdFlow(id: String): Flow<ClaseEntity?>
    
    @Query("SELECT * FROM clases WHERE activa = 1 ORDER BY diaSemana, horaInicio")
    fun getAllActivas(): Flow<List<ClaseEntity>>
    
    @Query("SELECT * FROM clases WHERE diaSemana = :dia AND activa = 1 ORDER BY horaInicio")
    fun getByDia(dia: Int): Flow<List<ClaseEntity>>
    
    @Query("SELECT * FROM clases WHERE instructorId = :instructorId AND activa = 1")
    fun getByInstructor(instructorId: String): Flow<List<ClaseEntity>>
    
    @Query("DELETE FROM clases WHERE id = :id")
    suspend fun deleteById(id: String)
    
    @Query("DELETE FROM clases")
    suspend fun deleteAll()
}
