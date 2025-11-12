package pcd.iskahoot.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class ClientGUI extends JFrame {

    private final String ECRA_INICIO = "ECRA_INICIO";
    private final String ECRA_JOGO = "ECRA_JOGO";

    private CardLayout cardLayout = new CardLayout();
    private JPanel painelPrincipal;

    private StartScreen painelInicio;
    private PainelJogo painelJogo;

    public ClientGUI() {
        super("IsKahoot Cliente");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        setResizable(false);

        painelPrincipal = new JPanel(cardLayout);
        
        ActionListener acaoBotaoNext = e -> {
            cardLayout.show(painelPrincipal, ECRA_JOGO);
            
            String[] ops = {"A", "B", "C", "D"};
            painelJogo.updateQuestion("Qual é a capital de França?", ops, 30);
        };

        painelInicio = new StartScreen(acaoBotaoNext);
        painelJogo = new PainelJogo();

        painelPrincipal.add(painelInicio, ECRA_INICIO);
        painelPrincipal.add(painelJogo, ECRA_JOGO);

        add(painelPrincipal);
        
        cardLayout.show(painelPrincipal, ECRA_INICIO);
    }
}