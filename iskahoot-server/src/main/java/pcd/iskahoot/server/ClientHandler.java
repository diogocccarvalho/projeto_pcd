package pcd.iskahoot.server;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;
import pcd.iskahoot.common.*;
import pcd.iskahoot.common.MensagemNovoJogador; // NEW
import pcd.iskahoot.common.MensagemListaJogadores; // NEW

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final Map<String, GameState> activeGames;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    
    private String idJogador;
    private GameState myGame;

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
                
                // Encontra a sala de jogo correta
                this.myGame = activeGames.get(loginMsg.idJogo);

                if (myGame == null) {
                    out.writeObject(new MensagemLoginResultado(false, "A sala '" + loginMsg.idJogo + "' não existe."));
                    return;
                }
                
                if (myGame.adicionarJogador(idJogador, loginMsg.idEquipa)) {
                    System.out.println("[Server] " + idJogador + " entrou na sala " + myGame.getIdSala());
                    myGame.addPlayerOutputStream(idJogador, out); // NEW: Adiciona o output stream do jogador
                    
                    // NEW: Envia o resultado do login (sucesso)
                    out.writeObject(new MensagemLoginResultado(true, null));
                    
                    // NEW: Envia a lista atual de jogadores ao novo jogador
                    out.writeObject(new MensagemListaJogadores(myGame.getPlayersInGame()));
                    
                    // NEW: Broadcast para outros jogadores que um novo jogador entrou
                    myGame.broadcastMessage(new MensagemNovoJogador(idJogador), idJogador);
                    
                    // Verifica se a sala está cheia para começar o jogo
                    if (myGame.getTotalJogadores() == myGame.getMaxEquipas() * myGame.getMaxJogadoresPorEquipa()) {
                        System.out.println("[Server] A sala " + myGame.getIdSala() + " está cheia! O jogo vai começar.");
                        myGame.iniciarJogo();
                    }
                } else {
                    out.writeObject(new MensagemLoginResultado(false, "Não foi possível entrar na sala (verifique se o nome de user já está em uso, se a equipa ou sala estão cheias)."));
                    return;
                }
            } else {
                // Se a primeira mensagem não for de login, ignora o cliente.
                return; 
            }

            // Envia a primeira pergunta se o jogo já estiver a decorrer para esta sala
            if (myGame.getEstado() == GameState.GameStatus.A_DECORRER) {
                enviarPerguntaAtual();
            }

            // --- 2. LOOP DE JOGO (Modo Teste Síncrono) ---
            while (myGame.getEstado() != GameState.GameStatus.FINALIZADO) {
                Object obj = in.readObject();

                if (obj instanceof MensagemEnviarResposta) {
                    MensagemEnviarResposta msg = (MensagemEnviarResposta) obj;
                    
                    // A. Registar resposta
                    System.out.println("[Server] " + idJogador + " respondeu: " + msg.indiceResposta);
                    myGame.submeterResposta(idJogador, msg.indiceResposta);

                    // ==========================================================
                    // O "HACK" DE TESTE: Fazemos tudo seguido sem esperar por ninguém
                    // ==========================================================
                    
                    // B. Calcular os pontos desta ronda imediatamente
                    myGame.processarResultadosDaRonda();

                    // C. Enviar o Placar atualizado (com flag de "não acabou")
                    out.writeObject(new MensagemPlacar(myGame.getPlacar(), false));
                    out.flush();
                    out.reset();

                    // D. Pausa dramática (2s) para veres o placar no Cliente
                    Thread.sleep(2000); 

                    // E. Avançar para a próxima pergunta
                    myGame.avancarParaProximaPergunta();

                    // F. Verificar se o jogo acabou ou mandar nova pergunta
                    if (myGame.jogoTerminou()) {
                        System.out.println("[Server] Jogo terminou para " + idJogador);
                        // Envia placar final com flag true
                        out.writeObject(new MensagemPlacar(myGame.getPlacar(), true));
                        out.flush();
                        break; 
                    } else {
                        // Manda a próxima
                        enviarPerguntaAtual();
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("[Handler] Cliente " + idJogador + " desconectado/com erro: " + e.getMessage());
            // TODO: Adicionar lógica para remover o jogador do myGame
        } finally {
            if (idJogador != null && myGame != null) {
                myGame.removePlayerOutputStream(idJogador);
                System.out.println("[Server] " + idJogador + " saiu da sala " + myGame.getIdSala());
                // TODO: Broadcast to other clients that this player has left
            }
            try { socket.close(); } catch (Exception e) {}
        }
    }

    private void enviarPerguntaAtual() throws Exception {
        out.writeObject(new MensagemNovaPergunta(
            myGame.getPerguntaAtual(),
            myGame.getTipoRondaAtual()
        ));
        out.flush();
        out.reset();
    }
}