# PokéFav

App Android que lista os pokémons da primeira geração e deixa cada usuário salvar os seus favoritos.

## API utilizada

**PokéAPI**, endereço base `https://pokeapi.co/api/v2/`

- `GET pokemon?limit=151`: lista de pokémons mostrada na aba Pokédex
- `GET pokemon/{id}`: tipos, altura, peso e atributos mostrados na tela de detalhe
- As imagens vêm do repositório de sprites da PokéAPI

## Prints

| Login | Cadastro | Pokédex |
|-------|----------|---------|
| ![Login](prints/login.png) | ![Cadastro](prints/cadastro.png) | ![Pokédex](prints/lista.png) |

| Detalhe | Favoritos | Erro sem internet |
|---------|-----------|-------------------|
| ![Detalhe](prints/detalhe.png) | ![Favoritos](prints/favoritos.png) | ![Erro](prints/erro.png) |

## Divisão de tarefas

| Integrante | O que fez |
|------------|-----------|
| Integrante 1 | network e data (Retrofit, Room, Repository, usuários) |
| Integrante 2 | viewmodel e userinterface (telas e navegação) |

## O que aprendemos

- **Integrante 1:** 
- **Integrante 2:** 
