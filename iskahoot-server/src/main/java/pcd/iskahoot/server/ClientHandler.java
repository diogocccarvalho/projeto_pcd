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

            // 1. FASE DE LOGIN (Obrigatória antes de entrar no loop)
            Object primeiroObjeto = in.readObject();
            
            if (primeiroObjeto instanceof MensagemLogin) {
                MensagemLogin loginMsg = (MensagemLogin) primeiroObjeto;
                this.idJogador = loginMsg.username;
                String equipa = loginMsg.idEquipa;

                // Tentar adicionar ao jogo
                boolean aceito = sala.adicionarJogador(idJogador, equipa);

                if (aceito) {
                    System.out.println("[Handler] Login aceite: " + idJogador);
                    out.writeObject(new MensagemLoginResultado(true, null));
                    
                    // Se o jogo ainda não começou e já temos gente suficiente, arranca (simplificação)
                    sala.iniciarJogo(); 
                } else {
                    System.out.println("[Handler] Login recusado: " + idJogador);
                    out.writeObject(new MensagemLoginResultado(false, "Nome indisponível"));
                    return; // Encerra a thread
                }
            } else {
                // Se a primeira coisa não for login, desliga
                return; 
            }

            // TODO: Loop temporário de testes (ainda não temos a sincronização completa das fases)
            // Enviar a primeira pergunta se o jogo já estiver a decorrer
            if (sala.getEstado() == GameState.GameStatus.A_DECORRER) {
                enviarPerguntaAtual();
            }

            // 2. LOOP DE JOGO
            while (true) {
                Object obj = in.readObject();

                if (obj instanceof MensagemEnviarResposta) {
                    MensagemEnviarResposta msg = (MensagemEnviarResposta) obj;
                    System.out.println("[Handler " + idJogador + "] Resposta: " + msg.indiceResposta);
                    
                    sala.submeterResposta(idJogador, msg.indiceResposta);
                    
                    // NOTA: Aqui, num sistema real, não envias logo a próxima pergunta.
                    // Tens de esperar que TODOS respondam (Barreiras/Latches).
                    // Por agora, isto fica em pausa até implementarmos a concorrência.
                }
            }

        } catch (Exception e) {
            System.out.println("[Handler " + idJogador + "] Desligou-se: " + e.getMessage());
        }
    }

    private void enviarPerguntaAtual() throws Exception {
        out.writeObject(new MensagemNovaPergunta(
            sala.getPerguntaAtual(),
            sala.getTipoRondaAtual()));
        out.flush();
        out.reset();
    }
}