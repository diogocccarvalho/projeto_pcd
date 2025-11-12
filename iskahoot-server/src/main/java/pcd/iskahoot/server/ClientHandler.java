package pcd.iskahoot.server;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import pcd.iskahoot.common.Mensagem;

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
//abrir canais
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.in = new ObjectInputStream(socket.getInputStream());

//TODO login
            
            this.idJogador = "jogador_" + socket.getPort(); // ID temporário
            
//adicionar jogador
            boolean adicionado = sala.adicionarJogador(idJogador, "equipaA");
            
//iniciar jogo
            if (sala.getEstado() == GameState.GameStatus.ESPERA_JOGADORES) {
                sala.iniciarJogo();
                System.out.println("[Handler " + idJogador + "] Jogo iniciado!");
            }

//enviar pergunta
            Mensagem msgPergunta = new Mensagem(
                Mensagem.TipoMensagem.NOVA_PERGUNTA, 
                sala.getPerguntaAtual()
            );
            out.writeObject(msgPergunta);

            while (true) {

                Mensagem msgRecebida = (Mensagem) in.readObject();

                if (msgRecebida.getTipo() == Mensagem.TipoMensagem.ENVIAR_RESPOSTA) {
                    int respostaIdx = (Integer) msgRecebida.getPayload();
                    System.out.println("[Handler " + idJogador + "] Recebeu resposta: " + respostaIdx);
                    
                    sala.submeterResposta(idJogador, respostaIdx);
                    
                    // TODO: Avançar para a próxima pergunta
                }
            }

        } catch (Exception e) {

            System.out.println("[Handler " + idJogador + "] Cliente desligou-se: " + e.getMessage());
        } finally {
            // TODO: Limpar recursos
        }
    }
}