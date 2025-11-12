package pcd.iskahoot.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import pcd.iskahoot.common.Pergunta;

public class ClientGUI extends JFrame {

    private final String ECRA_INICIO = "ECRA_INICIO";
    private final String ECRA_JOGO = "ECRA_JOGO";
    private final String ECRA_ESPERA = "ECRA_ESPERA";

    private CardLayout cardLayout = new CardLayout();
    private JPanel painelPrincipal;
    private StartScreen painelInicio;
    private PainelJogo painelJogo;
    private JPanel painelEspera;

    private ClientNetwork networkHandler;

    public ClientGUI() {
        super("IsKahoot Cliente");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        setResizable(false);

        painelPrincipal = new JPanel(cardLayout);
        

        ActionListener acaoBotaoNext = e -> {
            if (networkHandler != null) {
                cardLayout.show(painelPrincipal, ECRA_ESPERA);
                networkHandler.ligar();
            } else {
                mostrarErro("Erro Interno: Network handler não definido.");
            }
        };


        painelInicio = new StartScreen(acaoBotaoNext);

        painelJogo = new PainelJogo(this::onRespostaSelecionada);
        painelEspera = new JPanel(new BorderLayout());
        painelEspera.add(new JLabel("A ligar ao servidor...", SwingConstants.CENTER), BorderLayout.CENTER);

        painelPrincipal.add(painelInicio, ECRA_INICIO);
        painelPrincipal.add(painelEspera, ECRA_ESPERA);
        painelPrincipal.add(painelJogo, ECRA_JOGO);

        add(painelPrincipal);
        cardLayout.show(painelPrincipal, ECRA_INICIO);
    }


    private void onRespostaSelecionada(int indice) {
        if (networkHandler != null) {
            networkHandler.enviarResposta(indice);
        }
    }



    public void setPergunta(Pergunta p) {

        SwingUtilities.invokeLater(() -> {
            painelJogo.updateQuestion(
                p.getQuestion(), 
                p.getOptions().toArray(new String[0]), 
                30 // TODO: Adicionar tempo à Pergunta
            );
            cardLayout.show(painelPrincipal, ECRA_JOGO);
        });
    }

    public void mostrarErro(String msg) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, msg, "Erro", JOptionPane.ERROR_MESSAGE);
            cardLayout.show(painelPrincipal, ECRA_INICIO);
        });
    }

    public void setNetworkHandler(ClientNetwork networkHandler) {
        this.networkHandler = networkHandler;
    }
}