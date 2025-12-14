package pcd.iskahoot.client;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import pcd.iskahoot.common.Pergunta;
import pcd.iskahoot.common.TipoPergunta;

public class ClientGUI extends JFrame implements GameEventListener {

    private final String ECRA_INICIO = "ECRA_INICIO";
    private final String ECRA_JOGO = "ECRA_JOGO";
    private final String ECRA_ESPERA = "ECRA_ESPERA";

    private CardLayout cardLayout = new CardLayout();
    private JPanel painelPrincipal;
    
    private StartScreen painelInicio;
    private PainelJogo painelJogo;
    private JPanel painelEspera;

    private ClientAPI api;
    
    // Internal connection details
    private final String serverIp;
    private final int serverPort;

    public ClientGUI(String serverIp, int serverPort) {
        super("IsKahoot Cliente");
        this.serverIp = serverIp;
        this.serverPort = serverPort;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        setResizable(false);

        painelPrincipal = new JPanel(cardLayout);

        // Action Listener for the Login Button
        painelInicio = new StartScreen(e -> {
            // 1. Get User/Team data from UI
            String user = painelInicio.getUsername();
            String equipa = painelInicio.getEquipa();
            String sala = painelInicio.getSala();

            // 2. Start API with internal IP/Port
            api = new ClientAPI(this.serverIp, this.serverPort, this); 
            api.iniciar();
            
            // 3. Trigger Login
            // Wait slightly for connection or handle in callback? 
            // For simplicity, we queue the login immediately; ClientAPI handles the socket creation.
            // Note: In a robust app, we might wait for 'onConexaoSucesso' before sending login,
            // but ClientAPI.run() does that sequence naturally.
            
            cardLayout.show(painelPrincipal, ECRA_ESPERA);
        });

        painelJogo = new PainelJogo(indice -> {
            if (api != null) api.enviarResposta(indice);
        });

        painelEspera = new JPanel(new BorderLayout());
        painelEspera.add(new JLabel("A conectar...", SwingConstants.CENTER), BorderLayout.CENTER);

        painelPrincipal.add(painelInicio, ECRA_INICIO);
        painelPrincipal.add(painelEspera, ECRA_ESPERA);
        painelPrincipal.add(painelJogo, ECRA_JOGO);

        add(painelPrincipal);
        cardLayout.show(painelPrincipal, ECRA_INICIO);
    }
    
    public void preencherDadosUi(String user, String equipa, String sala) {
        painelInicio.setDados(user, equipa, sala);
    }

    @Override
    public void onConexaoSucesso() {
        // Automatically send login when connected
        api.fazerLogin(
            painelInicio.getUsername(), 
            painelInicio.getEquipa(),
            painelInicio.getSala()
        );
    }

    @Override
    public void onConexaoErro(String erro) {
        mostrarErro("Falha na rede: " + erro);
        cardLayout.show(painelPrincipal, ECRA_INICIO);
    }

    @Override
    public void onLoginSucesso() {
        JLabel lbl = (JLabel) painelEspera.getComponent(0);
        lbl.setText("A aguardar início do jogo...");
        cardLayout.show(painelPrincipal, ECRA_ESPERA);
    }

    @Override
    public void onLoginFalha(String motivo) {
        mostrarErro("Login falhou: " + motivo);
        cardLayout.show(painelPrincipal, ECRA_INICIO);
    }

    @Override
    public void onNovaPergunta(Pergunta p, TipoPergunta tipo, int segundos) {
        painelJogo.updateQuestion(
            p.getQuestion(), 
            p.getOptions().toArray(new String[0]), 
            segundos,
            tipo
        );
        this.setTitle("IsKahoot - RONDA: " + tipo);
        cardLayout.show(painelPrincipal, ECRA_JOGO);
    }

    @Override
    public void onPlacarAtualizado(Map<String, Integer> placar, boolean fimDeJogo) {
        JLabel lbl = (JLabel) painelEspera.getComponent(0);
        if (fimDeJogo) {
             lbl.setText("<html><h1>FIM DO JOGO!</h1>" + placar.toString() + "</html>");
        } else {
             lbl.setText("<html><h2>Placar Atual</h2>" + placar.toString() + "</html>");
        }
        cardLayout.show(painelPrincipal, ECRA_ESPERA);
    }
    
    @Override
    public void onFimTempo() {
        JOptionPane.showMessageDialog(this, "O tempo acabou!");
    }

    @Override
    public void onPlayerJoined(String username) {
        SwingUtilities.invokeLater(() -> painelJogo.addPlayer(username));
    }

    @Override
    public void onPlayerListReceived(java.util.List<String> players) {
        SwingUtilities.invokeLater(() -> painelJogo.setPlayerList(players));
    }

    private void mostrarErro(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Erro", JOptionPane.ERROR_MESSAGE);
    }
}