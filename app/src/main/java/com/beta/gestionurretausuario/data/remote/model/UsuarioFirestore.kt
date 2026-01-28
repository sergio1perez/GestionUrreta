package com.beta.gestionurretausuario.data.remote.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class UsuarioFirestore(
    @DocumentId
    val id: String = "",
    val email: String = "",
    val nombre: String = "",
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
    @ServerTimestamp
    val fechaRegistro: Date? = null,
    val activo: Boolean = true,
    val tipoUsuario: String = "alumno",

    // FCM Token para notificaciones push
    val fcmToken: String? = null
) {
    // Constructor vacío requerido por Firestore
    constructor() : this(id = "")

    // Método para convertir a entidad local
    fun toEntity(): com.beta.gestionurretausuario.data.local.entity.UsuarioEntity {
        return com.beta.gestionurretausuario.data.local.entity.UsuarioEntity(
            id = id,
            email = email,
            nombre = nombre,
            apellidos = apellidos,
            telefono = telefono,
            fechaNacimiento = fechaNacimiento,
            imagenUrl = imagenUrl,
            cinturon = cinturon,
            gup = gup,
            fechaInicioPractica = fechaInicioPractica,
            numeroLicencia = numeroLicencia,
            direccion = direccion,
            ciudad = ciudad,
            codigoPostal = codigoPostal,
            fechaRegistro = fechaRegistro?.time ?: System.currentTimeMillis(),
            activo = activo,
            tipoUsuario = tipoUsuario
        )
    }

    companion object {
        // Método para crear desde entidad local
        fun fromEntity(entity: com.beta.gestionurretausuario.data.local.entity.UsuarioEntity): UsuarioFirestore {
            return UsuarioFirestore(
                id = entity.id,
                email = entity.email,
                nombre = entity.nombre,
                apellidos = entity.apellidos,
                telefono = entity.telefono,
                fechaNacimiento = entity.fechaNacimiento,
                imagenUrl = entity.imagenUrl,
                cinturon = entity.cinturon,
                gup = entity.gup,
                fechaInicioPractica = entity.fechaInicioPractica,
                numeroLicencia = entity.numeroLicencia,
                direccion = entity.direccion,
                ciudad = entity.ciudad,
                codigoPostal = entity.codigoPostal,
                activo = entity.activo,
                tipoUsuario = entity.tipoUsuario
            )
        }
    }
}