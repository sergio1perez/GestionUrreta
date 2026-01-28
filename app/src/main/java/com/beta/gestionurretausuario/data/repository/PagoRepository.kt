package com.beta.gestionurretausuario.data.repository

import com.beta.gestionurretausuario.data.local.dao.PagoDao
import com.beta.gestionurretausuario.data.local.entity.PagoEntity
import com.beta.gestionurretausuario.data.remote.model.PagoFirestore
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class PagoRepository(
    private val pagoDao: PagoDao,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    companion object {
        private const val COLLECTION_PAGOS = "pagos"
    }

    // Operaciones locales
    suspend fun insertLocal(pago: PagoEntity) = pagoDao.insert(pago)
    suspend fun updateLocal(pago: PagoEntity) = pagoDao.update(pago)
    suspend fun getByIdLocal(id: String): PagoEntity? = pagoDao.getById(id)
    fun getByIdLocalFlow(id: String): Flow<PagoEntity?> = pagoDao.getByIdFlow(id)
    fun getByUsuario(usuarioId: String): Flow<List<PagoEntity>> = pagoDao.getByUsuario(usuarioId)
    fun getPendientesByUsuario(usuarioId: String): Flow<List<PagoEntity>> = pagoDao.getPendientesByUsuario(usuarioId)
    fun getPagadosByUsuario(usuarioId: String): Flow<List<PagoEntity>> = pagoDao.getPagadosByUsuario(usuarioId)
    fun getVencidosByUsuario(usuarioId: String): Flow<List<PagoEntity>> = pagoDao.getVencidosByUsuario(usuarioId)
    suspend fun getTotalPendiente(usuarioId: String): Double = pagoDao.getTotalPendiente(usuarioId) ?: 0.0
    suspend fun marcarVencidos() = pagoDao.marcarVencidos()

    // Operaciones remotas
    suspend fun syncFromRemote(usuarioId: String): Result<List<PagoEntity>> {
        return try {
            val documents = firestore.collection(COLLECTION_PAGOS)
                .whereEqualTo("usuarioId", usuarioId)
                .get()
                .await()

            val pagos = documents.mapNotNull { doc ->
                doc.toObject(PagoFirestore::class.java).toEntity()
            }

            pagoDao.insertAll(pagos)
            Result.success(pagos)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun confirmarPago(pagoId: String, metodoPago: String): Result<Unit> {
        return try {
            val fechaPago = System.currentTimeMillis()

            // Actualizar local
            val pago = pagoDao.getById(pagoId)
            pago?.let {
                val pagoActualizado = it.copy(
                    estado = "pagado",
                    metodoPago = metodoPago,
                    fechaPago = fechaPago
                )
                pagoDao.update(pagoActualizado)
            }

            // Actualizar remoto
            firestore.collection(COLLECTION_PAGOS)
                .document(pagoId)
                .update(
                    mapOf(
                        "estado" to "pagado",
                        "metodoPago" to metodoPago,
                        "fechaPago" to fechaPago
                    )
                )
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}