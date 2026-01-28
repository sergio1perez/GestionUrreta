package com.beta.gestionurretausuario.data.repository

import com.beta.gestionurretausuario.data.local.dao.ClaseDao
import com.beta.gestionurretausuario.data.local.entity.ClaseEntity
import com.beta.gestionurretausuario.data.remote.model.ClaseFirestore
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class ClaseRepository(
    private val claseDao: ClaseDao,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    companion object {
        private const val COLLECTION_CLASES = "clases"
    }

    // Operaciones locales
    suspend fun insertLocal(clase: ClaseEntity) = claseDao.insert(clase)
    suspend fun updateLocal(clase: ClaseEntity) = claseDao.update(clase)
    suspend fun deleteLocal(clase: ClaseEntity) = claseDao.delete(clase)
    suspend fun getByIdLocal(id: String): ClaseEntity? = claseDao.getById(id)
    fun getByIdLocalFlow(id: String): Flow<ClaseEntity?> = claseDao.getByIdFlow(id)
    fun getAllActivas(): Flow<List<ClaseEntity>> = claseDao.getAllActivas()
    fun getByDia(dia: Int): Flow<List<ClaseEntity>> = claseDao.getByDia(dia)
    fun getByInstructor(instructorId: String): Flow<List<ClaseEntity>> = claseDao.getByInstructor(instructorId)

    // Operaciones remotas
    suspend fun syncFromRemote(): Result<List<ClaseEntity>> {
        return try {
            val documents = firestore.collection(COLLECTION_CLASES)
                .whereEqualTo("activa", true)
                .get()
                .await()

            val clases = documents.mapNotNull { doc ->
                doc.toObject(ClaseFirestore::class.java).toEntity()
            }

            // Guardar todas en local
            claseDao.insertAll(clases)

            Result.success(clases)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getByIdRemote(id: String): Result<ClaseFirestore?> {
        return try {
            val document = firestore.collection(COLLECTION_CLASES)
                .document(id)
                .get()
                .await()

            if (document.exists()) {
                Result.success(document.toObject(ClaseFirestore::class.java))
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}