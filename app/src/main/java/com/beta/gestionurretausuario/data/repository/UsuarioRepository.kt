package com.beta.gestionurretausuario.data.repository

import com.beta.gestionurretausuario.data.local.dao.UsuarioDao
import com.beta.gestionurretausuario.data.local.entity.UsuarioEntity
import com.beta.gestionurretausuario.data.remote.model.UsuarioFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class UsuarioRepository(
    private val usuarioDao: UsuarioDao,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    companion object {
        private const val COLLECTION_USUARIOS = "usuarios"
    }

    // =============================================
    // OPERACIONES LOCALES (ROOM)
    // =============================================

    suspend fun insertLocal(usuario: UsuarioEntity) {
        usuarioDao.insert(usuario)
    }

    suspend fun updateLocal(usuario: UsuarioEntity) {
        usuarioDao.update(usuario)
    }

    suspend fun deleteLocal(usuario: UsuarioEntity) {
        usuarioDao.delete(usuario)
    }

    suspend fun getByIdLocal(id: String): UsuarioEntity? {
        return usuarioDao.getById(id)
    }

    fun getByIdLocalFlow(id: String): Flow<UsuarioEntity?> {
        return usuarioDao.getByIdFlow(id)
    }

    fun getAllActivos(): Flow<List<UsuarioEntity>> {
        return usuarioDao.getAllActivos()
    }

    // =============================================
    // OPERACIONES REMOTAS (FIRESTORE)
    // =============================================

    /**
     * Obtiene un usuario de Firestore por su ID
     */
    suspend fun getByIdRemote(userId: String): Result<UsuarioFirestore?> {
        return try {
            val document = firestore.collection(COLLECTION_USUARIOS)
                .document(userId)
                .get()
                .await()

            if (document.exists()) {
                val usuario = document.toObject(UsuarioFirestore::class.java)
                Result.success(usuario)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Guarda o actualiza un usuario en Firestore
     */
    suspend fun saveToRemote(usuario: UsuarioFirestore): Result<Unit> {
        return try {
            firestore.collection(COLLECTION_USUARIOS)
                .document(usuario.id)
                .set(usuario)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Actualiza campos específicos de un usuario en Firestore
     */
    suspend fun updateFieldsRemote(userId: String, fields: Map<String, Any>): Result<Unit> {
        return try {
            firestore.collection(COLLECTION_USUARIOS)
                .document(userId)
                .update(fields)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Crea un usuario en Firestore a partir de FirebaseUser (después de registro)
     */
    suspend fun createUserFromFirebaseUser(user: FirebaseUser): Result<UsuarioEntity> {
        return try {
            val usuarioFirestore = UsuarioFirestore(
                id = user.uid,
                email = user.email ?: "",
                nombre = user.displayName ?: "",
                imagenUrl = user.photoUrl?.toString(),
                cinturon = "Blanco",
                gup = "10° Kup",
                activo = true,
                tipoUsuario = "alumno"
            )

            // Guardar en Firestore
            firestore.collection(COLLECTION_USUARIOS)
                .document(user.uid)
                .set(usuarioFirestore)
                .await()

            // Convertir a entidad local
            val entity = usuarioFirestore.toEntity()

            // Guardar en local
            usuarioDao.insert(entity)

            Result.success(entity)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // =============================================
    // SINCRONIZACIÓN
    // =============================================

    /**
     * Sincroniza un usuario desde Firestore a Room
     */
    suspend fun syncFromRemote(userId: String): Result<UsuarioEntity?> {
        return try {
            val remoteResult = getByIdRemote(userId)

            remoteResult.getOrNull()?.let { remoteUser ->
                val localEntity = remoteUser.toEntity()
                usuarioDao.insert(localEntity)
                Result.success(localEntity)
            } ?: Result.success(null)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sincroniza un usuario desde Room a Firestore
     */
    suspend fun syncToRemote(userId: String): Result<Unit> {
        return try {
            val localUser = usuarioDao.getById(userId)
            localUser?.let {
                val firestoreUser = UsuarioFirestore.fromEntity(it)
                saveToRemote(firestoreUser)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene el usuario actual (de Firebase Auth)
     */
    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    /**
     * Obtiene el ID del usuario actual
     */
    fun getCurrentUserId(): String? = auth.currentUser?.uid

    /**
     * Obtiene el usuario actual de la base de datos local
     */
    suspend fun getCurrentUserLocal(): UsuarioEntity? {
        val userId = getCurrentUserId() ?: return null
        return usuarioDao.getById(userId)
    }

    /**
     * Obtiene el usuario actual como Flow
     */
    fun getCurrentUserLocalFlow(): Flow<UsuarioEntity?>? {
        val userId = getCurrentUserId() ?: return null
        return usuarioDao.getByIdFlow(userId)
    }
}