package br.com.cotemig.pokefav.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// Tabela de usuários. O e-mail é único e a senha fica salva só como hash
@Entity(tableName = "usuarios", indices = [Index(value = ["email"], unique = true)])
data class Usuario(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nome: String,
    val email: String,
    val senhaHash: String
)
