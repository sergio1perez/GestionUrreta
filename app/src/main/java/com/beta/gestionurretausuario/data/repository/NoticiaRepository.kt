package com.beta.gestionurretausuario.data.repository

import com.beta.gestionurretausuario.data.local.dao.NoticiaDao
import com.beta.gestionurretausuario.data.local.entity.NoticiaEntity
import com.beta.gestionurretausuario.data.remote.model.NoticiaFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class NoticiaRepository(
    private val noticiaDao: NoticiaDao,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    companion object {
        private const val COLLECTION_NOTICIAS = "noticias"
    }

    // Operaciones locales
    suspend fun insertLocal(noticia: NoticiaEntity) = noticiaDao.insert(noticia)
    suspend fun getByIdLocal(id: String): NoticiaEntity? = noticiaDao.getById(id)
    fun getByIdLocalFlow(id: String): Flow<NoticiaEntity?> = noticiaDao.getByIdFlow(id)
    fun getAllActivas(): Flow<List<NoticiaEntity>> = noticiaDao.getAllActivas()
    fun getRecientes(limit: Int = 10): Flow<List<NoticiaEntity>> = noticiaDao.getRecientes(limit)
    fun getDestacadas(): Flow<List<NoticiaEntity>> = noticiaDao.getDestacadas()
    fun getByCategoria(categoria: String): Flow<List<NoticiaEntity>> = noticiaDao.getByCategoria(categoria)
    fun getNoLeidas(): Flow<List<NoticiaEntity>> = noticiaDao.getNoLeidas()
    fun countNoLeidas(): Flow<Int> = noticiaDao.countNoLeidas()

    suspend fun marcarComoLeida(id: String) = noticiaDao.marcarComoLeida(id)
    suspend fun marcarTodasComoLeidas() = noticiaDao.marcarTodasComoLeidas()

    // Operaciones remotas
    suspend fun syncFromRemote(currentUserId: String): Result<List<NoticiaEntity>> {
        return try {
            val documents = firestore.collection(COLLECTION_NOTICIAS)
                .whereEqualTo("activa", true)
                .orderBy("fechaPublicacion", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .await()

            val noticias = documents.mapNotNull { doc ->
                doc.toObject(NoticiaFirestore::class.java).toEntity(currentUserId)
            }

            noticiaDao.insertAll(noticias)
            Result.success(noticias)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun marcarComoLeidaRemote(noticiaId: String, usuarioId: String): Result<Unit> {
        return try {
            // Actualizar local
            noticiaDao.marcarComoLeida(noticiaId)

            // Actualizar remoto
            firestore.collection(COLLECTION_NOTICIAS)
                .document(noticiaId)
                .update("leidaPor", FieldValue.arrayUnion(usuarioId))
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}