package com.beta.gestionurretausuario.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.beta.gestionurretausuario.data.local.dao.*
import com.beta.gestionurretausuario.data.local.entity.*

@Database(
    entities = [
        UsuarioEntity::class,
        ClaseEntity::class,
        AsistenciaEntity::class,
        EventoEntity::class,
        InscripcionEventoEntity::class,
        NoticiaEntity::class,
        ExamenEntity::class,
        InscripcionExamenEntity::class,
        PagoEntity::class,
        ProductoEntity::class,
        PedidoEntity::class,
        ItemPedidoEntity::class,
        CarritoEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    // DAOs
    abstract fun usuarioDao(): UsuarioDao
    abstract fun claseDao(): ClaseDao
    abstract fun asistenciaDao(): AsistenciaDao
    abstract fun eventoDao(): EventoDao
    abstract fun noticiaDao(): NoticiaDao
    abstract fun examenDao(): ExamenDao
    abstract fun pagoDao(): PagoDao
    abstract fun productoDao(): ProductoDao
    abstract fun pedidoDao(): PedidoDao
    abstract fun carritoDao(): CarritoDao

    companion object {
        private const val DATABASE_NAME = "gestion_urreta_db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        // Para testing
        fun getTestInstance(context: Context): AppDatabase {
            return Room.inMemoryDatabaseBuilder(
                context.applicationContext,
                AppDatabase::class.java
            )
                .allowMainThreadQueries()
                .build()
        }
    }
}