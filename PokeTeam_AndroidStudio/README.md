# PokéTeam

Aplicativo Android desenvolvido em **Kotlin com Jetpack Compose** que permite consultar Pokémon da **PokéAPI** e criar um time local de até seis favoritos por conta.

## API utilizada

- **PokéAPI:** `https://pokeapi.co/api/v2/`
- Lista paginada: `GET /pokemon/?limit=30&offset=0`
- Detalhes: `GET /pokemon/{id}/`
- As imagens são carregadas dos sprites oficiais mantidos pelo projeto PokéAPI.
- Retrofit + coroutines (`Dispatchers.IO`) e estados de carregando, sucesso e erro.

## Funcionalidades

- Cadastro e login locais com validação do e-mail, senha de no mínimo seis caracteres e e-mail único.
- Senhas salvas como hash SHA-256 com salt aleatório (nunca texto puro).
- Sessão persistente em `SharedPreferences`, inclusive após fechar o aplicativo.
- Três abas: **Pokédex**, **Meu time** e **Perfil**.
- Tela de detalhes com ID passado por rota **type-safe** do Navigation Compose.
- Salva até seis favoritos no **Room**, separados por `userId`.
- Botão de sair que encerra a sessão e retorna ao login.
- Mensagens de erro ao ficar sem conexão e ação de tentar novamente.

## Organização dos pacotes

```text
br.com.cotemig.poketeam/
  network/       PokeApi (Retrofit e DTOs)
  data/          Entities, DAOs, Room, Repository, SessionManager
  viewmodel/     AuthViewModel e PokemonViewModel
  userinterface/ MainActivity, navegação e telas Compose
```

As telas não acessam Retrofit ou Room diretamente: passam sempre pelos ViewModels e pelo Repository.

## Abrir no Android Studio

1. Abra a pasta raiz `PokeTeam_AndroidStudio` em **File > Open**.
2. Como o ZIP não inclui o JAR binário do Gradle Wrapper, execute `PREPARAR_GRADLE_WINDOWS.bat` (Windows) ou `sh PREPARAR_GRADLE_LINUX_MAC.sh` (Linux/macOS) com internet para baixar os três arquivos oficiais do Wrapper 8.11.1. Depois sincronize o Gradle.
3. Use o **JDK 17** e um emulador com **Android 7.0 (API 24)** ou superior.
4. Execute o módulo `app`. A conexão com a internet é necessária para consultar a API.
5. Se o Android Studio solicitar SDK 35, instale-o pelo SDK Manager.

## Prints

**Adicionar capturas reais do emulador antes da entrega**, conforme solicitado no PDF:

- Login
- Cadastro
- Lista da Pokédex
- Detalhes do Pokémon
- Meu time/favoritos
- Estado de erro sem internet

As imagens não foram incluídas como se fossem capturas reais porque não houve execução em emulador neste ambiente.

## Divisão de tarefas

| Integrante | O que fez |
|---|---|
| Preencher integrante 1 | Preencher atividades realizadas |
| Preencher integrante 2 | Preencher atividades realizadas |
| Preencher integrante 3 (se houver) | Preencher atividades realizadas |

## O que aprendemos

- **Integrante 1:** preencher com aprendizado real.
- **Integrante 2:** preencher com aprendizado real.
- **Integrante 3 (se houver):** preencher com aprendizado real.

> Cada participante deve realizar seu próprio commit e preencher o formulário individual de feedback. Não preencher por outras pessoas.

## Roteiro rápido de avaliação

1. Cadastre uma conta e entre com uma senha errada antes de usar a correta.
2. Navegue pelas três abas, abra um Pokémon e salve um favorito.
3. Feche e reabra o aplicativo: a sessão e o favorito devem continuar.
4. Ative o modo avião, toque em atualizar na Pokédex e verifique a mensagem de erro.
5. Saia, crie uma segunda conta e confirme que ela começa sem favoritos.
6. Verifique no Database Inspector que a senha salva está em `senhaHash`, não em texto puro.

## Observação sobre o design

O link recebido para o Figma foi `https://www.figma.com/unsupported_browser`, que não identifica um arquivo de projeto. Por isso, foi criado um layout próprio inspirado no universo Pokémon; ele pode ser adaptado quando o link completo do design estiver disponível.

## Enviar para o GitHub

O repositório de destino é `https://github.com/renanmulinarixz/Atividade-Final---Android`.

- **Windows:** após extrair o ZIP, abra `ENVIAR_PARA_GITHUB.bat`. É necessário ter Git instalado e autenticação com permissão de escrita.
- **Linux/macOS:** execute `sh ENVIAR_PARA_GITHUB.sh` no terminal dentro da pasta.

Os scripts inicializam um repositório local quando necessário e tentam fazer o primeiro commit e push na branch `main`. Eles não usam `--force` e recusam o envio se já existir um remoto diferente.
