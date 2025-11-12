package pcd.iskahoot.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class StartScreen extends JPanel {

    private JButton nextButton;

    public StartScreen(ActionListener nextAction) {
        super(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        JLabel titulo = new JLabel("Bem-vindo ao IsKahoot", SwingConstants.CENTER);
        titulo.setFont(new Font("Calibri", Font.BOLD, 40));
        add(titulo, BorderLayout.CENTER);

        nextButton = new JButton("Entrar (Teste)");
        nextButton.setFont(new Font("Calibri", Font.PLAIN, 20));
        nextButton.addActionListener(nextAction);
        
        JPanel southPanel = new JPanel();
        southPanel.add(nextButton);
        add(southPanel, BorderLayout.SOUTH);
    }
}