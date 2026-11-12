# PokéSpawn — Android

Aplicativo nativo em Java para Android Studio, Android 6.0 ou superior. Sem servidor, Firebase, chave de API ou dependências de interface externas.

## Abrir e rodar

1. Extraia o ZIP em uma pasta de usuário, por exemplo `C:\Users\SeuUsuario\AndroidStudioProjects\PokeSpawn`.
2. No Android Studio, escolha **Open** e selecione a pasta que contém `settings.gradle`.
3. Aguarde o Gradle Sync. Instale **Android SDK Platform 35** e **Build-Tools 35.0.0** se solicitado.
4. Em **Settings → Build, Execution, Deployment → Build Tools → Gradle**, use o JDK 17 ou o JDK integrado do Android Studio compatível com o Gradle 8.9.
5. Escolha um emulador com internet ou um celular conectado por USB e clique em **Run ▶**.
6. Na primeira utilização, informe um nome ou e-mail e uma senha com pelo menos 6 caracteres; toque em **Criar conta**. Depois, use **Entrar**.

Também é possível instalar diretamente o APK de teste entregue junto com este projeto.

Se o SDK estiver em `C:\ProgramData\Android\sdk` e não permitir gravação, configure um SDK em uma pasta do seu usuário, como `%LOCALAPPDATA%\Android\Sdk`, em **Settings → Languages & Frameworks → Android SDK**.

## Funcionamento

- **Login:** contas locais, senha derivada com PBKDF2 e salt aleatório. Aceita nome ou e-mail como identificador; não envia e-mails nem valida uma conta externa. Cada treinador tem sua própria coleção. A sessão permanece conectada até sair.
- **Captura:** seleciona um Pokémon da região escolhida, consulta a PokéAPI e prepara a imagem antes de iniciar os **15 segundos**. O botão captura exatamente o Pokémon mostrado, uma vez por encontro. Espécies repetidas em encontros diferentes aumentam o total de capturas.
- **Pokédex:** catálogo nacional de **1.025 espécies**. Capturados têm sprite colorido e nome; desconhecidos ficam em cinza e com `???`. Toque para ver detalhes e data da captura. Busca por número ou nome de capturado, filtro por região e opção de mostrar apenas capturados.
- **Perfil:** espécies únicas, total de capturas, percentual da Pokédex, progresso por região e últimas descobertas. Toque em uma região para abrir sua Pokédex.

O contador continua ao navegar entre as abas. Ao sair do aplicativo, não há serviço em segundo plano: ao voltar, um encontro vencido é substituído. Ao girar a tela, o encontro e a indicação de captura são preservados. Depois de encerrar totalmente o processo, um novo encontro é criado.

Os dados ficam no armazenamento privado do aplicativo. Sair da conta mantém as capturas. Desinstalar o aplicativo ou limpar seus dados remove contas e progresso. Não há sincronização entre aparelhos. Dados e imagens consultados são guardados localmente; novos encontros e imagens ainda não obtidas precisam de internet. Falhas de conexão exibem um botão para tentar novamente.

## Organização

| Arquivo | Responsabilidade |
| --- | --- |
| `MainActivity.java` | Quatro telas, navegação, ações e ciclo de vida |
| `Encounter.java` | Prazo de 15 segundos, captura única e sorteio |
| `PokemonApi.java` | Consultas HTTPS e cache dos dados |
| `SpriteLoader.java` | Sprites, cache e tratamento em cinza |
| `Pokemon.java` | Conversão do JSON da API |
| `AccountStore.java` | Cadastro e validação de contas locais |
| `GameStore.java` | Capturas, datas e estatísticas por treinador |
| `Regions.java` | Intervalos do número nacional por geração |
| `LoginBanner.java` | Banner e elementos extraídos da tela de login do Figma |
| `PixelScene.java` | Cenário de captura desenhado em pixels |

As regiões correspondem a intervalos do número nacional, e não a todas as espécies disponíveis em cada jogo. O grupo Galar / Hisui inclui os números 810–905.

## Compilar e testar

Windows:

```bat
gradlew.bat assembleDebug testDebugUnitTest lintDebug
```

Linux / macOS:

```sh
chmod +x gradlew
./gradlew assembleDebug testDebugUnitTest lintDebug
```

O APK gerado fica em `app/build/outputs/apk/debug/app-debug.apk`. É uma versão debug, destinada a testes e apresentação escolar.

Os testes verificam o limite exato de 15 segundos, cliques repetidos, restauração de captura, sorteio dentro das regiões, persistência, isolamento entre contas, validação de senha e integração das telas. Os testes de tela usam Robolectric com rede simulada; não representam execução em um aparelho físico. A execução gera imagens em `app/build/reports/screenshots`.

## Referência visual e fontes

Arquivo original: https://www.figma.com/design/JnLlKzThk2rdEcTUDIxeRF/

Foi possível extrair a tela **01 • Login e acesso — EMERALD PIXEL EDITION**, incluindo sua captura, fontes, cores e dois SVGs. A integração do Figma atingiu o limite do plano Starter nas leituras seguintes; as outras telas foram implementadas a partir das funções solicitadas e da identidade visual obtida. Não é uma reprodução integral verificada das demais telas do Figma. A captura obtida está em `referencias/01-login-figma.png`.

- API: https://pokeapi.co/docs/v2/
- Sprites: https://github.com/PokeAPI/sprites
- Fontes Pixelify Sans e Press Start 2P: Google Fonts, licenças OFL incluídas em `assets/fonts`.
- Catálogo de nomes: PokéAPI, 1.025 primeiras espécies, obtido em 12/11/2026. O aplicativo também consulta o catálogo pela API e usa a cópia incluída se a consulta falhar.

Pokémon e seus personagens pertencem aos respectivos titulares. Projeto educativo, sem afiliação oficial.
