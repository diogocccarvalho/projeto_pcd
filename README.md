# IsKahoot!

IsKahoot! is a multiplayer quiz game, inspired by Kahoot!, where players connect to a server to answer questions in real-time. This project is built using Java and Maven, and it demonstrates a client-server architecture with a focus on concurrent programming patterns to handle multiple players and game state synchronization.

## How to Run

The project is a Maven multi-module application. All commands should be run from the project's root directory.

### Server

1.  **Compile the entire project:**
    ```bash
    mvn clean install
    ```
2.  **Run the server:**
    Use the `-pl` flag to specify the `iskahoot-server` module.
    ```bash
    mvn -pl iskahoot-server exec:java
    ```
    The server will start and wait for clients to connect on port `12345`.

### Client

1.  **Run the client:**
    For each player, open a new terminal. Use the `-pl` flag for the `iskahoot-client` module and pass the player's name as an argument.
    ```bash
    mvn -pl iskahoot-client exec:java -Dexec.args="<username>"
    ```
    Replace `<username>` with the desired player name.

2.  **Run the client (with optional host and port):**
    You can also specify the host and port to connect to.
    ```bash
    mvn -pl iskahoot-client exec:java -Dexec.args="<username> <host> <port>"
    ```
    If not provided, the client defaults to connecting to `localhost:12345`.

## Architecture

The application follows a classic client-server model.

### Server

The server is responsible for managing the game logic, player connections, and overall game state.

*   **`ServerMain`**: The entry point for the server. It starts a `ServerSocket` and listens for incoming client connections.
*   **Threading Model**: The server employs a **thread-per-client** model. For each connecting client, `ServerMain` spawns a new `ClientHandler` thread. This allows the server to handle multiple clients concurrently.
*   **`ClientHandler`**: Each `ClientHandler` thread is dedicated to a single client. It manages the communication with that client, including the login process and receiving answers to questions.
*   **`GameLoop`**: The core of the game's logic resides in the `GameLoop`. It runs in a separate thread and controls the flow of the game: sending new questions, waiting for the round to end, calculating scores, and sending updated placars.
*   **`GameState`**: This class acts as a centralized container for all the state of a single game, including the list of players, their scores, the current quiz, and the current question. It is the primary mechanism for synchronization between the `GameLoop` and the various `ClientHandler` threads.

### Client

The client provides the user interface for players to join and play the game.

*   **`ClientMain`**: The entry point for the client application. It parses command-line arguments and starts the GUI.
*   **`ClientAPI`**: This class handles all the networking for the client. It establishes a connection to the server and manages the serialization and deserialization of messages.
*   **`ClientGUI`**: The main class for the client's Swing-based user interface.
*   **`StartScreen` & `PainelJogo`**: These are the two main panels in the GUI. `StartScreen` is shown on startup to allow the user to log in. Once logged in, `PainelJogo` is displayed, which shows the game's progress (questions, placar, etc.).

### Common

The `iskahoot-common` module contains the code shared between the server and the client. This primarily consists of the `Mensagem` classes, which represent the various messages exchanged between the client and server (e.g., `MensagemLogin`, `MensagemNovaPergunta`, `MensagemPlacar`). It also includes the data structures for the quiz itself, such as `Quiz`, `Pergunta`, and `TipoPergunta`.

## Synchronization

The server uses a combination of standard and custom synchronization primitives to manage the game state and coordinate the `GameLoop` with the `ClientHandler` threads.

### `ModifiedCountdownLatch`

This is a custom synchronization primitive used for **individual** question rounds. It's a variation of a `CountDownLatch` that includes a built-in timer and a bonus multiplier for early answers.

*   **How it works**:
    1.  When a new individual question round starts, the `GameLoop` creates a `ModifiedCountdownLatch` initialized with the number of players.
    2.  The `GameLoop` then calls `await()` on the latch, which blocks it until the latch's count reaches zero or the timer runs out.
    3.  When a player sends an answer, their `ClientHandler` thread calls `countdown()` on the latch. This method is `synchronized` and decrements the count.
    4.  The first player to answer correctly gets the highest bonus, the second gets a slightly lower bonus, and so on.
    5.  If all players answer before the timer expires, the latch's count reaches zero and the `GameLoop` unblocks.
    6.  If the timer expires before all players have answered, the `await()` method also returns, and the `GameLoop` proceeds. This ensures the game doesn't stall if a player disconnects or doesn't answer.

### `TeamBarrier`

This is a custom synchronization primitive used for **team**-based question rounds. It functions as a classic cyclic barrier with a timeout.

*   **How it works**:
    1.  For a team round, the `GameLoop` creates a `TeamBarrier` initialized with the number of players on the team.
    2.  The `GameLoop` then calls `await()`, which blocks it until all players on the team have "arrived" at the barrier or the timer expires.
    3.  When a player on the team answers, their `ClientHandler` calls `arrive()` on the barrier.
    4.  Once all players on the team have called `arrive()`, the barrier is broken, and the `GameLoop` unblocks and can process the team's answer.
    5.  Similar to the latch, the barrier has a timeout to prevent the game from stalling.

## Concurrency Model

The server's concurrency model is designed to separate I/O-bound tasks from CPU-bound tasks, ensuring high responsiveness and stability. It achieves this using two distinct types of threads.

### 1. Client Handler Threads (Thread-per-Client Pattern)

*   **Role**: For each client that connects, the `ServerMain` spawns a new `ClientHandler` thread. This thread is dedicated exclusively to that one client for its entire lifecycle. Its primary responsibility is to block on the network socket, waiting to read incoming messages from the client.
*   **Purpose**: This model excels at handling I/O-bound operations. Since the thread spends most of its time waiting for network input, dedicating a thread to each client ensures that the server is always responsive to every client. An incoming message from any client can be read immediately without being blocked by other operations on the server.

### 2. Game Loop Thread Pool (`gameExecutor`)

*   **Role**: A fixed-size thread pool (`Executors.newFixedThreadPool(10)`) is created when the server starts. This pool is responsible for executing the core game logic contained within the `GameLoop` runnable. When a game is ready to start, its `GameLoop` is submitted to this pool.
*   **Purpose**: This design pattern provides two key benefits:
    1.  **Efficiency and Resource Management**: Creating new threads is resource-intensive. By using a thread pool, a fixed number of threads are created once and then reused to run multiple `GameLoop`s. This avoids the overhead of thread creation/destruction for every game. More importantly, it acts as a gatekeeper, limiting the number of concurrently active games to the size of the pool (10). This prevents the server from being overwhelmed by an unbounded number of game threads, which could exhaust memory and CPU resources.
    2.  **Separation of Concerns**: It decouples the core game logic from network I/O. The `GameLoop` can perform potentially long-running or CPU-intensive tasks (calculating scores, waiting on timers) without blocking the `ClientHandler` threads from receiving new messages.

### Interaction and Synchronization

The two types of threads work in concert, synchronized via the shared `GameState` object:

1.  A `ClientHandler` thread receives a message (e.g., an answer) from its client. This is a quick, non-blocking I/O operation.
2.  The `ClientHandler` thread does not process the game logic itself. It immediately forwards the answer to the thread-safe `GameState` object.
3.  The `GameLoop` thread, running from the `gameExecutor` pool, is waiting on a synchronization primitive (a `ModifiedCountdownLatch` or `TeamBarrier`) within that same `GameState` object.
4.  The submission of an answer updates the state of the latch or barrier, which in turn unblocks the `GameLoop` thread when the condition (e.g., all players have answered or time is up) is met.

This clear separation ensures the server remains responsive (fast I/O handling) and stable (controlled execution of game logic), which is a robust and scalable architecture for this type of application.

## Project Structure
```
├───pom.xml
├───README.md
├───.git/...
├───iskahoot-client/
│   ├───pom.xml
│   └───src/
│       └───main/
│           └───java/
│               └───pcd/
│                   └───iskahoot/
│                       └───client/
│                           ├───ClientAPI.java
│                           ├───ClientGUI.java
│                           ├───ClientMain.java
│                           ├───GameEventListener.java
│                           ├───PainelJogo.java
│                           └───StartScreen.java
├───iskahoot-common/
│   ├───pom.xml
│   └───src/
│       └───main/
│           └───java/
│               └───pcd/
│                   └───iskahoot/
│                       └───common/
│                           ├───Mensagem.java
│                           ├───MensagemEnviarResposta.java
│                           ├───MensagemFimTempo.java
│                           ├───MensagemListaJogadores.java
│                           ├───MensagemLogin.java
│                           ├───MensagemLoginResultado.java
│                           ├───MensagemNovaPergunta.java
│                           ├───MensagemNovoJogador.java
│                           ├───MensagemPlacar.java
│                           ├───Pergunta.java
│                           ├───Quiz.java
│                           ├───QuizFile.java
│                           └───TipoPergunta.java
└───iskahoot-server/
    ├───pom.xml
    └───src/
        ├───main/
        │   ├───java/
        │   │   └───pcd/
        │   │       └───iskahoot/
        │   │           └───server/
        │   │               ├───ClientHandler.java
        │   │               ├───GameConfig.java
        │   │               ├───GameLoop.java
        │   │               ├───GameState.java
        │   │               ├───ModifiedCountdownLatch.java
        │   │               ├───QuizLoader.java
        │   │               ├───ServerMain.java
        │   │               ├───TeamBarrier.java
        │   │               └───TUI.java
        │   └───resources/
        │       └───quizzes.json
```