package com.beta.gestionurretausuario.data.repository

import com.beta.gestionurretausuario.data.local.dao.ExamenDao
import com.beta.gestionurretausuario.data.local.entity.ExamenEntity
import com.beta.gestionurretausuario.data.local.entity.InscripcionExamenEntity
import com.beta.gestionurretausuario.data.remote.model.ExamenFirestore
import com.beta.gestionurretausuario.data.remote.model.InscripcionExamenFirestore
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class ExamenRepository(
    private val examenDao: ExamenDao,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    companion object {
        private const val COLLECTION_EXAMENES = "examenes"
        private const val COLLECTION_INSCRIPCIONES = "inscripciones_examenes"
    }

    // Operaciones locales
    suspend fun insertLocal(examen: ExamenEntity) = examenDao.insert(examen)
    suspend fun getByIdLocal(id: String): ExamenEntity? = examenDao.getById(id)
    fun getByIdLocalFlow(id: String): Flow<ExamenEntity?> = examenDao.getByIdFlow(id)
    fun getAllActivos(): Flow<List<ExamenEntity>> = examenDao.getAllActivos()
    fun getAbiertos(): Flow<List<ExamenEntity>> = examenDao.getAbiertos()
    fun getProximos(): Flow<List<ExamenEntity>> = examenDao.getProximos()
    fun getByCinturon(cinturon: String): Flow<List<ExamenEntity>> = examenDao.getByCinturon(cinturon)

    // Inscripciones locales
    suspend fun getInscripcion(examenId: String, usuarioId: String) = examenDao.getInscripcion(examenId, usuarioId)
    fun getInscripcionesByUsuario(usuarioId: String) = examenDao.getInscripcionesByUsuario(usuarioId)
    fun getInscripcionesByExamen(examenId: String) = examenDao.getInscripcionesByExamen(examenId)

    // Operaciones remotas
    suspend fun syncFromRemote(): Result<List<ExamenEntity>> {
        return try {
            val documents = firestore.collection(COLLECTION_EXAMENES)
                .whereEqualTo("activo", true)
                .get()
                .await()

            val examenes = documents.mapNotNull { doc ->
                doc.toObject(ExamenFirestore::class.java).toEntity()
            }

            examenDao.insertAll(examenes)
            Result.success(examenes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun inscribirse(examenId: String, usuarioId: String): Result<Unit> {
        return try {
            val inscripcion = InscripcionExamenEntity(
                examenId = examenId,
                usuarioId = usuarioId,
                fechaInscripcion = System.currentTimeMillis()
            )
            examenDao.insertInscripcion(inscripcion)

            // Guardar en Firestore
            val inscripcionFirestore = InscripcionExamenFirestore(
                id = "${examenId}_$usuarioId",
                examenId = examenId,
                usuarioId = usuarioId,
                fechaInscripcion = System.currentTimeMillis()
            )

            firestore.collection(COLLECTION_INSCRIPCIONES)
                .document("${examenId}_$usuarioId")
                .set(inscripcionFirestore)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun cancelarInscripcion(examenId: String, usuarioId: String): Result<Unit> {
        return try {
            examenDao.deleteInscripcion(examenId, usuarioId)

            firestore.collection(COLLECTION_INSCRIPCIONES)
                .document("${examenId}_$usuarioId")
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}