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

| Matheus | network e data (Retrofit, Room, Repository, usuários) |
| Manuella| viewmodel e userinterface (telas e navegação) |
| Renan | interfaces figma e apresentação

## O que aprendemos

Todos nós aprendemos sobre a implantação de Api em kotlin.
