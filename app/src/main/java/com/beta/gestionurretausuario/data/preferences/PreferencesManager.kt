package com.beta.gestionurretausuario.data.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * Gestor de SharedPreferences para la aplicación.
 * Maneja todas las preferencias de usuario incluyendo la funcionalidad de "Recordar sesión".
 */
class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    companion object {
        private const val PREFS_NAME = "gestion_urreta_prefs"

        // Claves para autenticación y sesión
        private const val KEY_REMEMBER_SESSION = "remember_session"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_PHOTO_URL = "user_photo_url"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_LOGIN_TIMESTAMP = "login_timestamp"
        private const val KEY_AUTH_PROVIDER = "auth_provider" // "email" o "google"

        // Claves para configuración de la app
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_LANGUAGE = "language"

        // Claves para sincronización
        private const val KEY_LAST_SYNC_USUARIOS = "last_sync_usuarios"
        private const val KEY_LAST_SYNC_CLASES = "last_sync_clases"
        private const val KEY_LAST_SYNC_EVENTOS = "last_sync_eventos"
        private const val KEY_LAST_SYNC_NOTICIAS = "last_sync_noticias"
        private const val KEY_LAST_SYNC_EXAMENES = "last_sync_examenes"
        private const val KEY_LAST_SYNC_PAGOS = "last_sync_pagos"
        private const val KEY_LAST_SYNC_PRODUCTOS = "last_sync_productos"

        // Claves para onboarding
        private const val KEY_FIRST_TIME = "first_time"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"

        // Keys para verificación pendiente
        private const val KEY_PENDING_VERIFICATION_EMAIL = "pending_verification_email"
        private const val KEY_PENDING_VERIFICATION_NAME = "pending_verification_name"

        // Singleton
        @Volatile
        private var INSTANCE: PreferencesManager? = null

        fun getInstance(context: Context): PreferencesManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PreferencesManager(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }

    // =============================================
    // FUNCIONES DE SESIÓN Y AUTENTICACIÓN
    // =============================================

    /**
     * Guarda si el usuario quiere que se recuerde su sesión
     */
    var rememberSession: Boolean
        get() = prefs.getBoolean(KEY_REMEMBER_SESSION, false)
        set(value) = prefs.edit { putBoolean(KEY_REMEMBER_SESSION, value) }

    /**
     * Indica si el usuario está logueado
     */
    var isLoggedIn: Boolean
        get() = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        set(value) = prefs.edit { putBoolean(KEY_IS_LOGGED_IN, value) }

    /**
     * ID del usuario logueado
     */
    var userId: String?
        get() = prefs.getString(KEY_USER_ID, null)
        set(value) = prefs.edit { putString(KEY_USER_ID, value) }

    /**
     * Email del usuario logueado
     */
    var userEmail: String?
        get() = prefs.getString(KEY_USER_EMAIL, null)
        set(value) = prefs.edit { putString(KEY_USER_EMAIL, value) }

    /**
     * Nombre del usuario logueado
     */
    var userName: String?
        get() = prefs.getString(KEY_USER_NAME, null)
        set(value) = prefs.edit { putString(KEY_USER_NAME, value) }

    /**
     * URL de la foto del usuario
     */
    var userPhotoUrl: String?
        get() = prefs.getString(KEY_USER_PHOTO_URL, null)
        set(value) = prefs.edit { putString(KEY_USER_PHOTO_URL, value) }

    /**
     * Timestamp del último login
     */
    var loginTimestamp: Long
        get() = prefs.getLong(KEY_LOGIN_TIMESTAMP, 0)
        set(value) = prefs.edit { putLong(KEY_LOGIN_TIMESTAMP, value) }

    /**
     * Proveedor de autenticación ("email" o "google")
     */
    var authProvider: String?
        get() = prefs.getString(KEY_AUTH_PROVIDER, null)
        set(value) = prefs.edit { putString(KEY_AUTH_PROVIDER, value) }

    /**
     * Guarda los datos de sesión del usuario
     */
    fun saveUserSession(
        userId: String,
        email: String,
        name: String?,
        photoUrl: String?,
        provider: String,
        remember: Boolean
    ) {
        prefs.edit {
            putString(KEY_USER_ID, userId)
            putString(KEY_USER_EMAIL, email)
            putString(KEY_USER_NAME, name)
            putString(KEY_USER_PHOTO_URL, photoUrl)
            putString(KEY_AUTH_PROVIDER, provider)
            putBoolean(KEY_REMEMBER_SESSION, remember)
            putBoolean(KEY_IS_LOGGED_IN, true)
            putLong(KEY_LOGIN_TIMESTAMP, System.currentTimeMillis())
        }
    }

    /**
     * Verifica si se debe mantener la sesión iniciada
     */
    fun shouldKeepSession(): Boolean {
        return rememberSession && isLoggedIn && userId != null
    }

    /**
     * Limpia los datos de sesión (logout)
     */
    fun clearSession() {
        prefs.edit {
            remove(KEY_USER_ID)
            remove(KEY_USER_EMAIL)
            remove(KEY_USER_NAME)
            remove(KEY_USER_PHOTO_URL)
            remove(KEY_AUTH_PROVIDER)
            remove(KEY_LOGIN_TIMESTAMP)
            putBoolean(KEY_IS_LOGGED_IN, false)
            putBoolean(KEY_REMEMBER_SESSION, false)
        }
    }

    /**
     * Limpia solo el estado de login pero mantiene preferencias
     */
    fun clearLoginState() {
        prefs.edit {
            putBoolean(KEY_IS_LOGGED_IN, false)
        }
    }

    // =============================================
    // FUNCIONES DE CONFIGURACIÓN
    // =============================================

    /**
     * Notificaciones habilitadas
     */
    var notificationsEnabled: Boolean
        get() = prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
        set(value) = prefs.edit { putBoolean(KEY_NOTIFICATIONS_ENABLED, value) }

    /**
     * Modo oscuro
     */
    var darkMode: Boolean
        get() = prefs.getBoolean(KEY_DARK_MODE, true) // Por defecto activado
        set(value) = prefs.edit { putBoolean(KEY_DARK_MODE, value) }

    /**
     * Idioma de la app
     */
    var language: String
        get() = prefs.getString(KEY_LANGUAGE, "es") ?: "es"
        set(value) = prefs.edit { putString(KEY_LANGUAGE, value) }

    // =============================================
    // FUNCIONES DE SINCRONIZACIÓN
    // =============================================

    fun getLastSync(key: String): Long = prefs.getLong(key, 0)

    fun setLastSync(key: String, timestamp: Long = System.currentTimeMillis()) {
        prefs.edit { putLong(key, timestamp) }
    }

    var lastSyncUsuarios: Long
        get() = prefs.getLong(KEY_LAST_SYNC_USUARIOS, 0)
        set(value) = prefs.edit { putLong(KEY_LAST_SYNC_USUARIOS, value) }

    var lastSyncClases: Long
        get() = prefs.getLong(KEY_LAST_SYNC_CLASES, 0)
        set(value) = prefs.edit { putLong(KEY_LAST_SYNC_CLASES, value) }

    var lastSyncEventos: Long
        get() = prefs.getLong(KEY_LAST_SYNC_EVENTOS, 0)
        set(value) = prefs.edit { putLong(KEY_LAST_SYNC_EVENTOS, value) }

    var lastSyncNoticias: Long
        get() = prefs.getLong(KEY_LAST_SYNC_NOTICIAS, 0)
        set(value) = prefs.edit { putLong(KEY_LAST_SYNC_NOTICIAS, value) }

    var lastSyncExamenes: Long
        get() = prefs.getLong(KEY_LAST_SYNC_EXAMENES, 0)
        set(value) = prefs.edit { putLong(KEY_LAST_SYNC_EXAMENES, value) }

    var lastSyncPagos: Long
        get() = prefs.getLong(KEY_LAST_SYNC_PAGOS, 0)
        set(value) = prefs.edit { putLong(KEY_LAST_SYNC_PAGOS, value) }

    var lastSyncProductos: Long
        get() = prefs.getLong(KEY_LAST_SYNC_PRODUCTOS, 0)
        set(value) = prefs.edit { putLong(KEY_LAST_SYNC_PRODUCTOS, value) }

    /**
     * Resetea todos los timestamps de sincronización
     */
    fun resetAllSyncTimestamps() {
        prefs.edit {
            putLong(KEY_LAST_SYNC_USUARIOS, 0)
            putLong(KEY_LAST_SYNC_CLASES, 0)
            putLong(KEY_LAST_SYNC_EVENTOS, 0)
            putLong(KEY_LAST_SYNC_NOTICIAS, 0)
            putLong(KEY_LAST_SYNC_EXAMENES, 0)
            putLong(KEY_LAST_SYNC_PAGOS, 0)
            putLong(KEY_LAST_SYNC_PRODUCTOS, 0)
        }
    }

    // =============================================
    // FUNCIONES DE ONBOARDING
    // =============================================

    /**
     * Es la primera vez que se abre la app
     */
    var isFirstTime: Boolean
        get() = prefs.getBoolean(KEY_FIRST_TIME, true)
        set(value) = prefs.edit { putBoolean(KEY_FIRST_TIME, value) }

    /**
     * Se ha completado el onboarding
     */
    var onboardingCompleted: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
        set(value) = prefs.edit { putBoolean(KEY_ONBOARDING_COMPLETED, value) }

    /**
     * Email pendiente de verificación
     */
    var pendingVerificationEmail: String?
        get() = prefs.getString(KEY_PENDING_VERIFICATION_EMAIL, null)
        set(value) = prefs.edit { putString(KEY_PENDING_VERIFICATION_EMAIL, value) }

    /**
     * Nombre del usuario pendiente de verificación
     */
    var pendingVerificationName: String?
        get() = prefs.getString(KEY_PENDING_VERIFICATION_NAME, null)
        set(value) = prefs.edit { putString(KEY_PENDING_VERIFICATION_NAME, value) }

    /**
     * Limpia los datos de verificación pendiente
     */
    fun clearPendingVerification() {
        prefs.edit {
            remove(KEY_PENDING_VERIFICATION_EMAIL)
            remove(KEY_PENDING_VERIFICATION_NAME)
        }
    }

    // =============================================
    // FUNCIONES GENERALES
    // =============================================

    /**
     * Limpia todas las preferencias (reset completo)
     */
    fun clearAll() {
        prefs.edit { clear() }
    }

    /**
     * Obtiene todas las preferencias como Map (para debug)
     */
    fun getAllPreferences(): Map<String, *> {
        return prefs.all
    }
}