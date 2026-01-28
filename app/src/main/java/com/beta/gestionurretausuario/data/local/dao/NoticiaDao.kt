package com.beta.gestionurretausuario.data.local.dao

import androidx.room.*
import com.beta.gestionurretausuario.data.local.entity.NoticiaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoticiaDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(noticia: NoticiaEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(noticias: List<NoticiaEntity>)
    
    @Update
    suspend fun update(noticia: NoticiaEntity)
    
    @Delete
    suspend fun delete(noticia: NoticiaEntity)
    
    @Query("SELECT * FROM noticias WHERE id = :id")
    suspend fun getById(id: String): NoticiaEntity?
    
    @Query("SELECT * FROM noticias WHERE id = :id")
    fun getByIdFlow(id: String): Flow<NoticiaEntity?>
    
    @Query("SELECT * FROM noticias WHERE activa = 1 ORDER BY fechaPublicacion DESC")
    fun getAllActivas(): Flow<List<NoticiaEntity>>
    
    @Query("SELECT * FROM noticias WHERE activa = 1 ORDER BY fechaPublicacion DESC LIMIT :limit")
    fun getRecientes(limit: Int = 10): Flow<List<NoticiaEntity>>
    
    @Query("SELECT * FROM noticias WHERE destacada = 1 AND activa = 1 ORDER BY fechaPublicacion DESC")
    fun getDestacadas(): Flow<List<NoticiaEntity>>
    
    @Query("SELECT * FROM noticias WHERE categoria = :categoria AND activa = 1 ORDER BY fechaPublicacion DESC")
    fun getByCategoria(categoria: String): Flow<List<NoticiaEntity>>
    
    @Query("SELECT * FROM noticias WHERE leida = 0 AND activa = 1 ORDER BY fechaPublicacion DESC")
    fun getNoLeidas(): Flow<List<NoticiaEntity>>
    
    @Query("SELECT COUNT(*) FROM noticias WHERE leida = 0 AND activa = 1")
    fun countNoLeidas(): Flow<Int>
    
    @Query("UPDATE noticias SET leida = 1 WHERE id = :id")
    suspend fun marcarComoLeida(id: String)
    
    @Query("UPDATE noticias SET leida = 1")
    suspend fun marcarTodasComoLeidas()
    
    @Query("DELETE FROM noticias WHERE id = :id")
    suspend fun deleteById(id: String)
    
    @Query("DELETE FROM noticias")
    suspend fun deleteAll()
}
