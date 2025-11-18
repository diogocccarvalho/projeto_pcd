package pcd.iskahoot.server;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import pcd.iskahoot.common.*;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final GameState sala;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    
    private String idJogador;

    public ClientHandler(Socket socket, GameState sala) {
        this.socket = socket;
        this.sala = sala;
    }

    @Override
    public void run() {
        try {
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.in = new ObjectInputStream(socket.getInputStream());

            // --- 1. LOGIN ---
            Object primeiroObjeto = in.readObject();
            if (primeiroObjeto instanceof MensagemLogin) {
                MensagemLogin loginMsg = (MensagemLogin) primeiroObjeto;
                this.idJogador = loginMsg.username;
                
                if (sala.adicionarJogador(idJogador, loginMsg.idEquipa)) {
                    System.out.println("[Server] " + idJogador + " entrou na sala.");
                    out.writeObject(new MensagemLoginResultado(true, null));
                    
                    // PARA TESTE: Arranca logo mal o primeiro entra
                    sala.iniciarJogo(); 
                } else {
                    out.writeObject(new MensagemLoginResultado(false, "Nome ocupado"));
                    return;
                }
            } else {
                return; 
            }

            // Envia a primeira pergunta
            if (sala.getEstado() == GameState.GameStatus.A_DECORRER) {
                enviarPerguntaAtual();
            }

            // --- 2. LOOP DE JOGO (Modo Teste Síncrono) ---
            while (true) {
                Object obj = in.readObject();

                if (obj instanceof MensagemEnviarResposta) {
                    MensagemEnviarResposta msg = (MensagemEnviarResposta) obj;
                    
                    // A. Registar resposta
                    System.out.println("[Server] " + idJogador + " respondeu: " + msg.indiceResposta);
                    sala.submeterResposta(idJogador, msg.indiceResposta);

                    // ==========================================================
                    // O "HACK" DE TESTE: Fazemos tudo seguido sem esperar por ninguém
                    // ==========================================================
                    
                    // B. Calcular os pontos desta ronda imediatamente
                    sala.processarResultadosDaRonda();

                    // C. Enviar o Placar atualizado (com flag de "não acabou")
                    out.writeObject(new MensagemPlacar(sala.getPlacar(), false));
                    out.flush();
                    out.reset();

                    // D. Pausa dramática (2s) para veres o placar no Cliente
                    Thread.sleep(2000); 

                    // E. Avançar para a próxima pergunta
                    sala.avancarParaProximaPergunta();

                    // F. Verificar se o jogo acabou ou mandar nova pergunta
                    if (sala.jogoTerminou()) {
                        System.out.println("[Server] Jogo terminou para " + idJogador);
                        // Envia placar final com flag true
                        out.writeObject(new MensagemPlacar(sala.getPlacar(), true));
                        out.flush();
                        break; // Sai do loop
                    } else {
                        // Manda a próxima
                        enviarPerguntaAtual();
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("[Handler] Erro/Desconexão: " + e.getMessage());
        } finally {
            try { socket.close(); } catch (Exception e) {}
        }
    }

    private void enviarPerguntaAtual() throws Exception {
        out.writeObject(new MensagemNovaPergunta(
            sala.getPerguntaAtual(),
            sala.getTipoRondaAtual()
        ));
        out.flush();
        out.reset();
    }
}