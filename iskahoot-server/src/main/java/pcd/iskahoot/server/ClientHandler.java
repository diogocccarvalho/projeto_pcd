package pcd.iskahoot.server;

import java.util.concurrent.ExecutorService;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;
import pcd.iskahoot.common.*;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final Map<String, GameState> activeGames;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private final ExecutorService gamePool;

    private String idJogador;
    private GameState myGame;
    private volatile boolean running = true;

    public ClientHandler(Socket socket, Map<String, GameState> activeGames, ExecutorService gamePool) {
        this.socket = socket;
        this.activeGames = activeGames;
        this.gamePool = gamePool;
    }

    @Override
    public void run() {
        try {
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.out.flush(); // GOOD PRACTICE: Ensure header is written immediately
            this.in = new ObjectInputStream(socket.getInputStream());

            // --- 1. LOGIN ---
            Object firstMessage = in.readObject();
            if (firstMessage instanceof MensagemLogin) {
                MensagemLogin loginMsg = (MensagemLogin) firstMessage;
                this.idJogador = loginMsg.username;
                
                this.myGame = activeGames.get(loginMsg.idJogo);

                if (myGame == null) {
                    out.writeObject(new MensagemLoginResultado(false, "A sala não existe."));
                    out.flush();
                    return;
                }
                
                boolean entrou;
                boolean deveIniciar = false;

                // Sync to ensure atomic check of 'isSalaCheia'
                synchronized(myGame) {
                    entrou = myGame.adicionarJogador(idJogador, loginMsg.idEquipa);
                    if (entrou) {
                        myGame.addPlayerOutputStream(idJogador, out);
                        if (myGame.isSalaCheia()) {
                            deveIniciar = true;
                        }
                    }
                }

                if (entrou) {
                    System.out.println("[Server] " + idJogador + " entrou na sala " + myGame.getIdSala());
                    
                    // FIX: Synchronize on myGame to prevent race condition.
                    // O 'out' já foi publicado em 'addPlayerOutputStream'. Se não sincronizarmos aqui,
                    // um broadcast de outra thread (ex: outro jogador a entrar ou GameLoop) pode tentar
                    // escrever neste 'out' ao mesmo tempo que estamos a escrever o LoginResultado.
                    synchronized(myGame) {
                        // Envia confirmação e lista atual
                        out.writeObject(new MensagemLoginResultado(true, null));
                        out.writeObject(new MensagemListaJogadores(myGame.getPlayersInGame()));
                        out.flush(); 
                        out.reset(); 
                    }
                    
                    // Avisa os outros
                    myGame.broadcastMessage(new MensagemNovoJogador(idJogador), idJogador);
                    
                    // Start Game if full
                    if (deveIniciar) {
                        System.out.println("[Server] Sala cheia! Submetendo GameLoop à ThreadPool...");
                        gamePool.submit(new GameLoop(myGame));
                    }

                } else {
                    out.writeObject(new MensagemLoginResultado(false, "Erro: Sala cheia/Jogo decorrer/Nome duplicado."));
                    out.flush();
                    return;
                }
            } else {
                return; 
            }

            // --- 2. LISTENER LOOP ---
            while (running) {
                try {
                    Object obj = in.readObject();

                    if (obj instanceof MensagemEnviarResposta) {
                        MensagemEnviarResposta msg = (MensagemEnviarResposta) obj;
                        myGame.registarResposta(idJogador, msg.indiceResposta);
                    }
                } catch (Exception e) {
                    System.out.println("[Handler] Erro na conexão com " + idJogador + ": " + e.getMessage());
                    running = false;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    private void closeConnection() {
        if (myGame != null && idJogador != null) {
            myGame.removePlayerOutputStream(idJogador);
            System.out.println("[Server] " + idJogador + " desconectado.");
        }
        try { socket.close(); } catch (Exception e) {}
    }
}