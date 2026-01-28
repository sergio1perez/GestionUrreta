package com.beta.gestionurretausuario.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "noticias")
data class NoticiaEntity(
    @PrimaryKey
    val id: String,

    val titulo: String,
    val contenido: String,
    val resumen: String? = null, // Preview corto
    val imagenUrl: String? = null,

    val autorId: String,
    val autorNombre: String,

    val fechaPublicacion: Long,
    val fechaModificacion: Long? = null,

    val categoria: String? = null, // "general", "competicion", "horarios", "importante"
    val destacada: Boolean = false,
    val activa: Boolean = true,

    // Para saber si el usuario la ha leído
    val leida: Boolean = false,

    val ultimaSincronizacion: Long = System.currentTimeMillis()
)