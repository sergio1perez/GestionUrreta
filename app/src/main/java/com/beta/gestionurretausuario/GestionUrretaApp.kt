package com.beta.gestionurretausuario

import android.app.Application
import com.beta.gestionurretausuario.data.local.AppDatabase
import com.beta.gestionurretausuario.data.preferences.PreferencesManager

class GestionUrretaApp : Application() {

    // Lazy initialization de la base de datos
    val database: AppDatabase by lazy {
        AppDatabase.getInstance(this)
    }

    // Lazy initialization del PreferencesManager
    val preferencesManager: PreferencesManager by lazy {
        PreferencesManager.getInstance(this)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        private lateinit var instance: GestionUrretaApp

        fun getInstance(): GestionUrretaApp = instance

        // Acceso fácil a la base de datos
        val database: AppDatabase
            get() = instance.database

        // Acceso fácil al PreferencesManager
        val preferences: PreferencesManager
            get() = instance.preferencesManager
    }
}