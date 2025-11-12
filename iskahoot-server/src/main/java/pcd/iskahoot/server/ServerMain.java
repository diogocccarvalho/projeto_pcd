package pcd.iskahoot.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import pcd.iskahoot.common.Pergunta;

public class ServerMain {
    public static final int PORT = 12345;

    public static void main(String[] args) {
        System.out.println("[Servidor] A arrancar...");

        try {
            // 1. Carregar o quiz UMA VEZ no arranque
            List<Pergunta> perguntas = QuizLoader.carregarPerguntasDoQuiz("quizzes.json", "PCD-1");
            System.out.println("[Servidor] Quiz 'PCD-1' carregado com " + perguntas.size() + " perguntas.");
            
            // 2. Criar a sala de jogo UMA VEZ
            GameState sala = new GameState("SALA-PRINCIPAL", perguntas);
            
            // 3. Abrir a porta ("Central de Correios")
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("[Servidor] À escuta na porta " + PORT + "...");

            // 4. O loop do "Porteiro"
            while (true) {
                // Bloqueia aqui, à espera que alguém toque à campainha
                Socket clientSocket = serverSocket.accept(); 
                
                System.out.println("[Servidor] Novo cliente (" + clientSocket.getInetAddress() + ") ligou-se.");
                
                // 5. Entregar o cliente a um "Assistente" (ClientHandler)
                // e lançá-lo numa nova Thread
                ClientHandler handler = new ClientHandler(clientSocket, sala);
                new Thread(handler).start();
            }

        } catch (Exception e) {
            System.out.println("[Servidor] ERRO CRÍTICO: " + e.getMessage());
            e.printStackTrace();
        }
    }
}