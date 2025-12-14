package pcd.iskahoot.client;

import pcd.iskahoot.common.*;
import javax.swing.SwingUtilities;
import java.io.*;
import java.net.Socket;
import pcd.iskahoot.common.MensagemNovoJogador; // NEW
import pcd.iskahoot.common.MensagemListaJogadores; // NEW

public class ClientAPI implements Runnable {

    private final String host;
    private final int port;
    private final GameEventListener listener;
    
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Socket socket;
    private volatile boolean running = true;

    public ClientAPI(String host, int port, GameEventListener listener) {
        this.host = host;
        this.port = port;
        this.listener = listener;
    }

    public void iniciar() {
        new Thread(this).start();
    }

    @Override
    public void run() {
        try {
            socket = new Socket(host, port);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            
            notificarUI(() -> listener.onConexaoSucesso());

            while (running) {
                Object obj = in.readObject();
                tratarMensagem(obj);
            }

        } catch (Exception e) {
            running = false;
            notificarUI(() -> listener.onConexaoErro("Ligação perdida: " + e.getMessage()));
        } finally {
            fecharRecursos();
        }
    }

    private void tratarMensagem(Object obj) {
        SwingUtilities.invokeLater(() -> {
            
            if (obj instanceof MensagemLoginResultado) {
                MensagemLoginResultado msg = (MensagemLoginResultado) obj;
                if (msg.sucesso) listener.onLoginSucesso();
                else listener.onLoginFalha(msg.mensagemErro);
            
            } else if (obj instanceof MensagemNovaPergunta) {
                MensagemNovaPergunta msg = (MensagemNovaPergunta) obj;
                listener.onNovaPergunta(msg.pergunta, msg.tipoRonda, msg.segundos);
            
            } else if (obj instanceof MensagemPlacar) {
                MensagemPlacar msg = (MensagemPlacar) obj;
                listener.onPlacarAtualizado(msg.pontuacoes, msg.jogoAcabou);
            
            } else if (obj instanceof MensagemFimTempo) {
                listener.onFimTempo();
            
            } else if (obj instanceof MensagemNovoJogador) { // NEW
                MensagemNovoJogador msg = (MensagemNovoJogador) obj;
                listener.onPlayerJoined(msg.username);
            
            } else if (obj instanceof MensagemListaJogadores) { // NEW
                MensagemListaJogadores msg = (MensagemListaJogadores) obj;
                listener.onPlayerListReceived(msg.jogadores);
            }
        });
    }


    public void fazerLogin(String user, String equipa, String sala) {
        enviarObjeto(new MensagemLogin(user, equipa, sala));
    }

    public void enviarResposta(int index) {
        enviarObjeto(new MensagemEnviarResposta(index));
    }

    private void enviarObjeto(Object obj) {
        try {
            out.writeObject(obj);
            out.flush();
            out.reset();
        } catch (IOException e) {
            System.err.println("Erro a enviar: " + e.getMessage());
        }
    }

    private void notificarUI(Runnable r) {
        SwingUtilities.invokeLater(r);
    }
    
    private void fecharRecursos() {
        try { if (socket != null) socket.close(); } catch (Exception ignored) {}
    }
}