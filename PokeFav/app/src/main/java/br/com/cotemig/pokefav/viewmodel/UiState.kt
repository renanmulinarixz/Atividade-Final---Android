package br.com.cotemig.pokefav.viewmodel

// Estados possíveis de uma tela que busca dados na internet
sealed class UiState<out T> {
    data object Carregando : UiState<Nothing>()
    data class Sucesso<T>(val dados: T) : UiState<T>()
    data class Erro(val mensagem: String) : UiState<Nothing>()
}
