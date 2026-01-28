package com.beta.gestionurretausuario.data.repository

import com.beta.gestionurretausuario.data.local.dao.ProductoDao
import com.beta.gestionurretausuario.data.local.entity.ProductoEntity
import com.beta.gestionurretausuario.data.remote.model.ProductoFirestore
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class ProductoRepository(
    private val productoDao: ProductoDao,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    companion object {
        private const val COLLECTION_PRODUCTOS = "productos"
    }

    // Operaciones locales
    suspend fun insertLocal(producto: ProductoEntity) = productoDao.insert(producto)
    suspend fun getByIdLocal(id: String): ProductoEntity? = productoDao.getById(id)
    fun getByIdLocalFlow(id: String): Flow<ProductoEntity?> = productoDao.getByIdFlow(id)
    fun getAllActivos(): Flow<List<ProductoEntity>> = productoDao.getAllActivos()
    fun getByCategoria(categoria: String): Flow<List<ProductoEntity>> = productoDao.getByCategoria(categoria)
    fun getDestacados(): Flow<List<ProductoEntity>> = productoDao.getDestacados()
    fun getEnOferta(): Flow<List<ProductoEntity>> = productoDao.getEnOferta()
    fun buscar(query: String): Flow<List<ProductoEntity>> = productoDao.buscar(query)
    fun getCategorias(): Flow<List<String>> = productoDao.getCategorias()

    // Operaciones remotas
    suspend fun syncFromRemote(): Result<List<ProductoEntity>> {
        return try {
            val documents = firestore.collection(COLLECTION_PRODUCTOS)
                .whereEqualTo("activo", true)
                .get()
                .await()

            val productos = documents.mapNotNull { doc ->
                doc.toObject(ProductoFirestore::class.java).toEntity()
            }

            productoDao.insertAll(productos)
            Result.success(productos)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getByIdRemote(id: String): Result<ProductoFirestore?> {
        return try {
            val document = firestore.collection(COLLECTION_PRODUCTOS)
                .document(id)
                .get()
                .await()

            if (document.exists()) {
                Result.success(document.toObject(ProductoFirestore::class.java))
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}