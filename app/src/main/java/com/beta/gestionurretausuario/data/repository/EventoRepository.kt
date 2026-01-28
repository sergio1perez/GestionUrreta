package com.beta.gestionurretausuario.data.repository

import com.beta.gestionurretausuario.data.local.dao.EventoDao
import com.beta.gestionurretausuario.data.local.entity.EventoEntity
import com.beta.gestionurretausuario.data.local.entity.InscripcionEventoEntity
import com.beta.gestionurretausuario.data.remote.model.EventoFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class EventoRepository(
    private val eventoDao: EventoDao,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    companion object {
        private const val COLLECTION_EVENTOS = "eventos"
        private const val COLLECTION_INSCRIPCIONES = "inscripciones_eventos"
    }

    // Operaciones locales
    suspend fun insertLocal(evento: EventoEntity) = eventoDao.insert(evento)
    suspend fun getByIdLocal(id: String): EventoEntity? = eventoDao.getById(id)
    fun getByIdLocalFlow(id: String): Flow<EventoEntity?> = eventoDao.getByIdFlow(id)
    fun getAllActivos(): Flow<List<EventoEntity>> = eventoDao.getAllActivos()
    fun getProximos(): Flow<List<EventoEntity>> = eventoDao.getProximos()
    fun getPasados(): Flow<List<EventoEntity>> = eventoDao.getPasados()
    fun getByTipo(tipo: String): Flow<List<EventoEntity>> = eventoDao.getByTipo(tipo)

    // Inscripciones locales
    suspend fun getInscripcion(eventoId: String, usuarioId: String) = eventoDao.getInscripcion(eventoId, usuarioId)
    fun getInscripcionesByUsuario(usuarioId: String) = eventoDao.getInscripcionesByUsuario(usuarioId)

    // Operaciones remotas
    suspend fun syncFromRemote(): Result<List<EventoEntity>> {
        return try {
            val documents = firestore.collection(COLLECTION_EVENTOS)
                .whereEqualTo("activo", true)
                .get()
                .await()

            val eventos = documents.mapNotNull { doc ->
                doc.toObject(EventoFirestore::class.java).toEntity()
            }

            eventoDao.insertAll(eventos)
            Result.success(eventos)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun inscribirse(eventoId: String, usuarioId: String): Result<Unit> {
        return try {
            // Guardar inscripción local
            val inscripcion = InscripcionEventoEntity(
                eventoId = eventoId,
                usuarioId = usuarioId,
                fechaInscripcion = System.currentTimeMillis()
            )
            eventoDao.insertInscripcion(inscripcion)

            // Actualizar en Firestore (añadir usuario a lista de inscritos)
            firestore.collection(COLLECTION_EVENTOS)
                .document(eventoId)
                .update("inscritos", FieldValue.arrayUnion(usuarioId))
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun cancelarInscripcion(eventoId: String, usuarioId: String): Result<Unit> {
        return try {
            eventoDao.deleteInscripcion(eventoId, usuarioId)

            firestore.collection(COLLECTION_EVENTOS)
                .document(eventoId)
                .update("inscritos", FieldValue.arrayRemove(usuarioId))
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}