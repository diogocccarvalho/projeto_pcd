#projeto pcd

C:.
│   pom.xml
│   PROJ_PCD TREE.txt
│   README.md
│
├───iskahoot-client
│   │   pom.xml
│   │
│   ├───src
│   │   └───main
│   │       └───java
│   │           └───pcd
│   │               └───iskahoot
│   │                   └───client
│   │                           ClientAPI.java
│   │                           ClientGUI.java
│   │                           ClientMain.java
│   │                           ClientNetwork.java
│   │                           GameEventListener.java
│   │                           PainelJogo.java
│   │                           StartScreen.java
│   │
│   └───target
│       │   iskahoot-client-1.0-SNAPSHOT.jar
│       │
│       ├───classes
│       │   └───pcd
│       │       └───iskahoot
│       │           └───client
│       │                   ClientAPI.class
│       │                   ClientGUI.class
│       │                   ClientMain.class
│       │                   ClientNetwork.class
│       │                   GameEventListener.class
│       │                   PainelJogo$1.class
│       │                   PainelJogo.class
│       │                   StartScreen.class
│       │
│       ├───maven-archiver
│       │       pom.properties
│       │
│       └───maven-status
│           └───maven-compiler-plugin
│               └───compile
│                   └───default-compile
│                           createdFiles.lst
│                           inputFiles.lst
│
├───iskahoot-common
│   │   pom.xml
│   │
│   ├───src
│   │   └───main
│   │       └───java
│   │           └───pcd
│   │               └───iskahoot
│   │                   └───common
│   │                           Mensagem.java
│   │                           MensagemEnviarResposta.java
│   │                           MensagemFimTempo.java
│   │                           MensagemLogin.java
│   │                           MensagemLoginResultado.java
│   │                           MensagemNovaPergunta.java
│   │                           MensagemPlacar.java
│   │                           Pergunta.java
│   │                           Quiz.java
│   │                           QuizFile.java
│   │                           TipoPergunta.java
│   │
│   └───target
│       │   iskahoot-common-1.0-SNAPSHOT.jar
│       │
│       ├───classes
│       │   └───pcd
│       │       └───iskahoot
│       │           └───common
│       │                   Mensagem.class
│       │                   MensagemEnviarResposta.class
│       │                   MensagemFimTempo.class
│       │                   MensagemLogin.class
│       │                   MensagemLoginResultado.class
│       │                   MensagemNovaPergunta.class
│       │                   MensagemPlacar.class
│       │                   Pergunta.class
│       │                   Quiz.class
│       │                   QuizFile.class
│       │                   TipoPergunta.class
│       │
│       ├───maven-archiver
│       │       pom.properties
│       │
│       └───maven-status
│           └───maven-compiler-plugin
│               └───compile
│                   └───default-compile
│                           createdFiles.lst
│                           inputFiles.lst
│
└───iskahoot-server
    │   pom.xml
    │
    ├───src
    │   └───main
    │       ├───java
    │       │   └───pcd
    │       │       └───iskahoot
    │       │           └───server
    │       │                   ClientHandler.java
    │       │                   GameState.java
    │       │                   QuizLoader.java
    │       │                   ServerMain.java
    │       │                   TUI.java
    │       │
    │       └───resources
    │               quizzes.json
    │
    └───target
        │   iskahoot-server-1.0-SNAPSHOT.jar
        │
        ├───classes
        │   │   quizzes.json
        │   │
        │   └───pcd
        │       └───iskahoot
        │           └───server
        │                   ClientHandler.class
        │                   GameState$GameStatus.class
        │                   GameState$RespostaComTimestamp.class
        │                   GameState.class
        │                   QuizLoader.class
        │                   ServerMain.class
        │                   TUI.class
        │
        ├───maven-archiver
        │       pom.properties
        │
        └───maven-status
            └───maven-compiler-plugin
                └───compile
                    └───default-compile
                            createdFiles.lst
                            inputFiles.lst
