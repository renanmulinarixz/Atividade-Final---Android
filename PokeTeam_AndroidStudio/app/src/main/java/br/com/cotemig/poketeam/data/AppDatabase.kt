package br.com.cotemig.poketeam.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.Transaction
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert
    suspend fun insert(user: UserEntity): Long

    @Query("SELECT * FROM usuarios WHERE email = :email LIMIT 1")
    suspend fun byEmail(email: String): UserEntity?

    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    suspend fun byId(id: Long): UserEntity?
}

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favoritos WHERE userId = :userId ORDER BY pokemonId")
    fun byUser(userId: Long): Flow<List<FavoriteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(item: FavoriteEntity)

    @Query("SELECT COUNT(*) FROM favoritos WHERE userId = :userId")
    suspend fun count(userId: Long): Int

    @Transaction
    suspend fun saveIfSpace(item: FavoriteEntity): Boolean {
        if (count(item.userId) >= 6) return false
        save(item)
        return true
    }

    @Query("DELETE FROM favoritos WHERE userId = :userId AND pokemonId = :pokemonId")
    suspend fun remove(userId: Long, pokemonId: Int)
}

@Database(entities = [UserEntity::class, FavoriteEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun favoriteDao(): FavoriteDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "poketeam.db"
            ).build().also { instance = it }
        }
    }
}
