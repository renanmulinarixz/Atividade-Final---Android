package br.com.cotemig.pokefav.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Usuario::class, Favorito::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun favoritoDao(): FavoritoDao

    // Garante que o app use sempre o mesmo banco
    companion object {
        @Volatile
        private var instancia: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instancia ?: synchronized(this) {
                val banco = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pokefav.db"
                ).build()
                instancia = banco
                banco
            }
        }
    }
}
