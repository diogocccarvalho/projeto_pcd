package pcd.iskahoot.client;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import pcd.iskahoot.common.Mensagem;
import pcd.iskahoot.common.Pergunta;


public class ClientNetwork implements Runnable {

    private final String ip;
    private final int port;
    private final ClientGUI gui;

    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Socket socket;

    public ClientNetwork(String ip, int port, ClientGUI gui) {
        this.ip = ip;
        this.port = port;
        this.gui = gui;
    }

    public void ligar() {
        try {
  
            this.socket = new Socket(ip, port);
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.in = new ObjectInputStream(socket.getInputStream());
            
            new Thread(this).start();
            
            // TODO LOGIN
        } catch (Exception e) {
            gui.mostrarErro("Não foi possível ligar ao servidor: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {

            while (true) {
                Mensagem msg = (Mensagem) in.readObject();

                switch (msg.getTipo()) {
                    
                    case NOVA_PERGUNTA:
                        Pergunta p = (Pergunta) msg.getPayload();
                        gui.setPergunta(p); 
                        break;
                    
                    case MOSTRAR_PLACAR:
                        // String placar = (String) msg.getPayload();
                        // gui.setPlacar(placar);
                        break;
                    
                    case JOGO_TERMINOU:
                        // gui.mostrarFimDeJogo();
                        break;
                    
                    case LOGIN_FALHOU:
                        // gui.mostrarErro("Login falhou: " + msg.getPayload());
                        break;
                }
            }
        } catch (Exception e) {
            gui.mostrarErro("Ligação ao servidor perdida: " + e.getMessage());
        }
    }


    public void enviarResposta(int indiceResposta) {
        try {
//criar mensagem
            Mensagem msg = new Mensagem(Mensagem.TipoMensagem.ENVIAR_RESPOSTA, indiceResposta);
            out.writeObject(msg);
            out.flush();
        } catch (Exception e) {
            System.err.println("Erro a enviar resposta: " + e.getMessage());
        }
    }
}