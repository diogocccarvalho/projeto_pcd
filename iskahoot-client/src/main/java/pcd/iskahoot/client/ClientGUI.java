package pcd.iskahoot.client;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import pcd.iskahoot.common.Pergunta;
import pcd.iskahoot.common.TipoPergunta;

// 1. Implementa o Listener para receber avisos da API
public class ClientGUI extends JFrame implements GameEventListener {

    private final String ECRA_INICIO = "ECRA_INICIO";
    private final String ECRA_JOGO = "ECRA_JOGO";
    private final String ECRA_ESPERA = "ECRA_ESPERA";

    private CardLayout cardLayout = new CardLayout();
    private JPanel painelPrincipal;
    
    // Os teus painéis originais
    private StartScreen painelInicio;
    private PainelJogo painelJogo;
    private JPanel painelEspera;

    private ClientAPI api;

    public ClientGUI() {
        super("IsKahoot Cliente");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        setResizable(false);

        painelPrincipal = new JPanel(cardLayout);

        painelInicio = new StartScreen(e -> {
            String ip = "localhost";
            int port = 12345;;

            api = new ClientAPI(ip, port, this); 
            api.iniciar();
            
            cardLayout.show(painelPrincipal, ECRA_ESPERA);
        });

        painelJogo = new PainelJogo(indice -> {
            if (api != null) api.enviarResposta(indice);
        });

        // Painel simples de espera/placar
        painelEspera = new JPanel(new BorderLayout());
        painelEspera.add(new JLabel("A aguardar...", SwingConstants.CENTER), BorderLayout.CENTER);

        painelPrincipal.add(painelInicio, ECRA_INICIO);
        painelPrincipal.add(painelEspera, ECRA_ESPERA);
        painelPrincipal.add(painelJogo, ECRA_JOGO);

        add(painelPrincipal);
        cardLayout.show(painelPrincipal, ECRA_INICIO);
    }


    @Override
    public void onConexaoSucesso() {
        api.fazerLogin("UserTeste", "EquipaA", "SALA-1");
    }

    @Override
    public void onConexaoErro(String erro) {
        mostrarErro("Falha na rede: " + erro);
        cardLayout.show(painelPrincipal, ECRA_INICIO);
    }

    @Override
    public void onLoginSucesso() {
        // Fica no ecrã de espera até vir a primeira pergunta
        cardLayout.show(painelPrincipal, ECRA_ESPERA);
    }

    @Override
    public void onLoginFalha(String motivo) {
        mostrarErro("Login falhou: " + motivo);
        cardLayout.show(painelPrincipal, ECRA_INICIO);
    }

    @Override
    public void onNovaPergunta(Pergunta p, TipoPergunta tipo) {
        // Atualiza o teu painel de jogo antigo
        painelJogo.updateQuestion(
            p.getQuestion(), 
            p.getOptions().toArray(new String[0]), 
            30 
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

    private void mostrarErro(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Erro", JOptionPane.ERROR_MESSAGE);
    }

    public void preencherDadosIniciais(String ip, int port, String user, String equipa, String sala) {
        // Chama o método que criámos acima na StartScreen
        painelInicio.setDados(ip, port, user, equipa, sala);
    } 
}