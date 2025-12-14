package pcd.iskahoot.client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;

public class StartScreen extends JPanel {

    private JTextField txtUser;
    private JTextField txtEquipa;
    private JTextField txtSala;
    private JButton btnEntrar;

    public StartScreen(ActionListener acaoBotao) {
        this.setLayout(new GridBagLayout());
        this.setBackground(new Color(240, 240, 240));

        // Reduced grid rows since IP/Port are gone
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        formPanel.setBackground(Color.WHITE);

        // --- Dados do Jogador/Jogo ---
        formPanel.add(new JLabel("Username:"));
        txtUser = new JTextField("Aluno1");
        formPanel.add(txtUser);

        formPanel.add(new JLabel("Nome Equipa:"));
        txtEquipa = new JTextField("EquipaA");
        formPanel.add(txtEquipa);

        formPanel.add(new JLabel("Código Sala:"));
        txtSala = new JTextField("SALA-1");
        formPanel.add(txtSala);

        // --- Botão ---
        formPanel.add(new JLabel("")); 
        
        btnEntrar = new JButton("ENTRAR");
        btnEntrar.setBackground(new Color(70, 130, 180));
        btnEntrar.setForeground(Color.WHITE);
        btnEntrar.setFocusPainted(false);
        
        btnEntrar.addActionListener(acaoBotao);
        formPanel.add(btnEntrar);

        this.add(formPanel);
    }

    public void setDados(String user, String equipa, String sala) {
        this.txtUser.setText(user);
        this.txtEquipa.setText(equipa);
        this.txtSala.setText(sala);
    }

    public String getUsername() {
        return txtUser.getText().trim();
    }

    public String getEquipa() {
        return txtEquipa.getText().trim();
    }

    public String getSala() {
        return txtSala.getText().trim();
    }
}