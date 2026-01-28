package com.beta.gestionurretausuario.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuarios")
data class UsuarioEntity(
    @PrimaryKey
    val id: String, // UID de Firebase Auth

    val email: String,
    val nombre: String,
    val apellidos: String = "",
    val telefono: String? = null,
    val fechaNacimiento: Long? = null,
    val imagenUrl: String? = null,

    // Datos de Taekwondo
    val cinturon: String = "Blanco",
    val gup: String = "10° Kup",
    val fechaInicioPractica: Long? = null,
    val numeroLicencia: String? = null,

    // Dirección
    val direccion: String? = null,
    val ciudad: String? = null,
    val codigoPostal: String? = null,

    // Metadata
    val fechaRegistro: Long = System.currentTimeMillis(),
    val activo: Boolean = true,
    val tipoUsuario: String = "alumno", // "alumno", "instructor", "admin"

    // Sincronización
    val ultimaSincronizacion: Long = System.currentTimeMillis()
)