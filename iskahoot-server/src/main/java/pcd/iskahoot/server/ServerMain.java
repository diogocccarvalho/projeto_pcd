package pcd.iskahoot.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import pcd.iskahoot.common.Pergunta;

public class ServerMain {
    public static final int PORT = 12345;
    private final ExecutorService gamePool = Executors.newFixedThreadPool(5);    
    private ServerSocket serverSocket;
    private volatile boolean running = true;
    private Thread connectionListenerThread;

    private final Map<String, GameState> activeGames = new ConcurrentHashMap<>();
    private final List<Pergunta> allQuestions;
    private final TUI tui = new TUI();

    public ServerMain() {
        System.out.println("[Servidor] A arrancar...");
        // Carregar perguntas (ajuste o nome do ficheiro se necessário)
        this.allQuestions = QuizLoader.carregarPerguntasDoQuiz("quizzes.json", "PCD-1");
        System.out.println("[Servidor] Quiz 'PCD-1' carregado com " + allQuestions.size() + " perguntas.");
        
        startConnectionListener();
    }

    private void runTUI() {
        while (running) {
            String command = tui.mainMenu();
            switch (command) {
                case "criarSala":
                    createNewGame();
                    break;
                case "verSalas":
                    tui.mostrarSalas(new java.util.ArrayList<>(activeGames.values()));
                    break;
                default:
                    break;
            }
        }
    }

    private void startConnectionListener() {
        connectionListenerThread = new Thread(() -> {
            try {
                serverSocket = new ServerSocket(PORT);
                System.out.println("[Servidor] À escuta de novas ligações na porta " + PORT + "...");
                while (running) {
                    Socket clientSocket = serverSocket.accept();
                    // Agora passamos apenas as referências necessárias
                    ClientHandler handler = new ClientHandler(clientSocket, activeGames, gamePool);
                    new Thread(handler).start();
                }
            } catch (IOException e) {
                if (running) e.printStackTrace();
            }
        });
        connectionListenerThread.start();
    }

    private void createNewGame() {
        String idSala = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        // Configuração padrão
        GameConfig config = tui.obterConfiguracaoJogo(); 
        if (config == null) config = new GameConfig(2, 2, 5, 30); // Fallback se o TUI falhar

        GameState newGame = new GameState(idSala, allQuestions, config);
        activeGames.put(idSala, newGame);
        System.out.println("[Servidor] Nova sala criada: " + idSala);
    }

    public static void main(String[] args) {
        new ServerMain().runTUI();
    }
}