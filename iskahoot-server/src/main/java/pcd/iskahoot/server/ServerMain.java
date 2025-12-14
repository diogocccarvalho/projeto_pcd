package pcd.iskahoot.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import pcd.iskahoot.common.Pergunta;
import pcd.iskahoot.server.GameConfig; // NEW

public class ServerMain {
    public static final int PORT = 12345;
    
    private ServerSocket serverSocket;
    private volatile boolean running = true;
    private Thread connectionListenerThread;

    // Armazena os vários jogos a decorrer
    private final Map<String, GameState> activeGames = new ConcurrentHashMap<>();
    // Carrega as perguntas uma vez
    private final List<Pergunta> allQuestions;
    private final TUI tui = new TUI();

    public ServerMain() {
        System.out.println("[Servidor] A arrancar...");
        this.allQuestions = QuizLoader.carregarPerguntasDoQuiz("quizzes.json", "PCD-1");
        System.out.println("[Servidor] Quiz 'PCD-1' carregado com " + allQuestions.size() + " perguntas.");
        
        // Inicia o "porteiro" numa thread separada para não bloquear a TUI
        startConnectionListener();

        // Adiciona um shutdown hook para fechar o ServerSocket de forma graciosa
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("[Servidor] A encerrar...");
            running = false;
            try {
                if (serverSocket != null && !serverSocket.isClosed()) {
                    serverSocket.close();
                }
                if (connectionListenerThread != null && connectionListenerThread.isAlive()) {
                    connectionListenerThread.interrupt(); // Interrompe a thread que espera por ligações
                }
            } catch (IOException e) {
                System.err.println("[Servidor] Erro ao fechar o ServerSocket no shutdown hook: " + e.getMessage());
            }
            System.out.println("[Servidor] Encerrado.");
        }));
    }

    private void runTUI() {
        while (running) { // A TUI também deve parar se o servidor estiver a encerrar
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
                while (running) { // Use the running flag
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("[Servidor] Novo cliente (" + clientSocket.getInetAddress() + ") ligou-se.");
                    
                    ClientHandler handler = new ClientHandler(clientSocket, activeGames);
                    new Thread(handler).start();
                }
            } catch (IOException e) {
                if (running) { // Only log as critical if it's not a planned shutdown
                    System.out.println("[Servidor] ERRO CRÍTICO no listener de ligações: " + e.getMessage());
                    e.printStackTrace();
                } else {
                    System.out.println("[Servidor] Listener de ligações encerrado.");
                }
            } finally {
                try {
                    if (serverSocket != null && !serverSocket.isClosed()) {
                        serverSocket.close();
                    }
                } catch (IOException e) {
                    System.err.println("[Servidor] Erro ao fechar ServerSocket no finally: " + e.getMessage());
                }
            }
        });
        connectionListenerThread.start();
    }

    private void createNewGame() {
        String idSala = UUID.randomUUID().toString().substring(0, 8).toUpperCase(); // Gera um ID de 8 caracteres
        // Configurações de jogo padrão por enquanto
        GameConfig defaultConfig = new GameConfig(2, 2, 5); // 2 equipas, 2 jogadores/equipa, 5s por pergunta
        GameState newGame = new GameState(idSala, allQuestions, defaultConfig);
        activeGames.put(idSala, newGame);
        System.out.println("[Servidor] Nova sala criada: " + idSala);
        tui.mostrarMensagem("Sala " + idSala + " criada. Config: " + defaultConfig.numEquipas + " equipas, " + defaultConfig.jogadoresPorEquipa + " jogadores/equipa.");
    }

    public static void main(String[] args) {
        ServerMain server = new ServerMain();
        server.runTUI();
    }
}