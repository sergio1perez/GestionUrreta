package com.beta.gestionurretausuario.data.remote.model

import com.google.firebase.firestore.DocumentId

data class NoticiaFirestore(
    @DocumentId
    val id: String = "",
    val titulo: String = "",
    val contenido: String = "",
    val resumen: String? = null,
    val imagenUrl: String? = null,

    val autorId: String = "",
    val autorNombre: String = "",

    val fechaPublicacion: Long = 0,
    val fechaModificacion: Long? = null,

    val categoria: String? = null,
    val destacada: Boolean = false,
    val activa: Boolean = true,

    // Lista de usuarios que han leído la noticia
    val leidaPor: List<String> = emptyList()
) {
    constructor() : this(id = "")

    fun toEntity(currentUserId: String): com.beta.gestionurretausuario.data.local.entity.NoticiaEntity {
        return com.beta.gestionurretausuario.data.local.entity.NoticiaEntity(
            id = id,
            titulo = titulo,
            contenido = contenido,
            resumen = resumen,
            imagenUrl = imagenUrl,
            autorId = autorId,
            autorNombre = autorNombre,
            fechaPublicacion = fechaPublicacion,
            fechaModificacion = fechaModificacion,
            categoria = categoria,
            destacada = destacada,
            activa = activa,
            leida = leidaPor.contains(currentUserId)
        )
    }

    companion object {
        fun fromEntity(entity: com.beta.gestionurretausuario.data.local.entity.NoticiaEntity): NoticiaFirestore {
            return NoticiaFirestore(
                id = entity.id,
                titulo = entity.titulo,
                contenido = entity.contenido,
                resumen = entity.resumen,
                imagenUrl = entity.imagenUrl,
                autorId = entity.autorId,
                autorNombre = entity.autorNombre,
                fechaPublicacion = entity.fechaPublicacion,
                fechaModificacion = entity.fechaModificacion,
                categoria = entity.categoria,
                destacada = entity.destacada,
                activa = entity.activa
            )
        }
    }
}