package pcd.iskahoot.server;

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
    
    private String idJogador;
    private GameState myGame;
    private volatile boolean running = true;

    public ClientHandler(Socket socket, Map<String, GameState> activeGames) {
        this.socket = socket;
        this.activeGames = activeGames;
    }

    @Override
    public void run() {
        try {
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.in = new ObjectInputStream(socket.getInputStream());

            // --- 1. LOGIN ---
            Object firstMessage = in.readObject();
            if (firstMessage instanceof MensagemLogin) {
                MensagemLogin loginMsg = (MensagemLogin) firstMessage;
                this.idJogador = loginMsg.username;
                
                // Encontra a sala
                this.myGame = activeGames.get(loginMsg.idJogo);

                if (myGame == null) {
                    out.writeObject(new MensagemLoginResultado(false, "A sala não existe."));
                    return;
                }
                
                // Tenta adicionar o jogador (Secção Crítica tratada no GameState)
                // Sincronizamos aqui para garantir que a verificação "isSalaCheia" é atómica com a entrada
                boolean entrou;
                boolean deveIniciar = false;

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
                    
                    // Envia confirmação e lista atual
                    out.writeObject(new MensagemLoginResultado(true, null));
                    out.writeObject(new MensagemListaJogadores(myGame.getPlayersInGame()));
                    
                    // Avisa os outros
                    myGame.broadcastMessage(new MensagemNovoJogador(idJogador), idJogador);
                    
                    // SE a sala encheu com este jogador, arranca o Loop do Jogo numa nova thread
                    if (deveIniciar) {
                        System.out.println("[Server] Sala cheia! Iniciando GameLoop...");
                        new Thread(new GameLoop(myGame)).start();
                    }

                } else {
                    out.writeObject(new MensagemLoginResultado(false, "Sala cheia, jogo a decorrer ou username em uso."));
                    return;
                }
            } else {
                return; // Protocolo errado
            }

            // --- 2. LISTENER LOOP ---
            // Apenas ouve mensagens e encaminha para o GameState.
            // NÃO gere o fluxo do jogo.
            while (running) {
                try {
                    Object obj = in.readObject();

                    if (obj instanceof MensagemEnviarResposta) {
                        MensagemEnviarResposta msg = (MensagemEnviarResposta) obj;
                        // Encaminha a resposta para o estado
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