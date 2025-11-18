package pcd.iskahoot.client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;

public class StartScreen extends JPanel {

    private JTextField txtIp;
    private JTextField txtPort;
    private JTextField txtUser;
    private JTextField txtEquipa;
    private JTextField txtSala;
    private JButton btnEntrar;

    public StartScreen(ActionListener acaoBotao) {
        // Layout para centrar o formulário no ecrã
        this.setLayout(new GridBagLayout());
        this.setBackground(new Color(240, 240, 240)); // Cinzento claro

        // Painel interior com grelha (Labels à esquerda, Inputs à direita)
        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20)); // Margem
        formPanel.setBackground(Color.WHITE);

        // --- 1. Configuração de Rede ---
        formPanel.add(new JLabel("IP Servidor:"));
        txtIp = new JTextField("localhost"); // Valor default para facilitar
        formPanel.add(txtIp);

        formPanel.add(new JLabel("Porta:"));
        txtPort = new JTextField("12345");
        formPanel.add(txtPort);

        // --- 2. Dados do Jogador/Jogo (Obrigatórios pelo enunciado) ---
        formPanel.add(new JLabel("Username:"));
        txtUser = new JTextField("Aluno1");
        formPanel.add(txtUser);

        formPanel.add(new JLabel("Nome Equipa:"));
        txtEquipa = new JTextField("EquipaA");
        formPanel.add(txtEquipa);

        formPanel.add(new JLabel("Código Sala:"));
        txtSala = new JTextField("SALA-1");
        formPanel.add(txtSala);

        // --- 3. Botão ---
        // Espaço vazio para alinhar o botão à direita na grelha
        formPanel.add(new JLabel("")); 
        
        btnEntrar = new JButton("ENTRAR");
        btnEntrar.setBackground(new Color(70, 130, 180));
        btnEntrar.setForeground(Color.WHITE);
        btnEntrar.setFocusPainted(false);
        
        // Liga a ação que vem do ClientGUI
        btnEntrar.addActionListener(acaoBotao);
        formPanel.add(btnEntrar);

        // Adiciona o formulário ao painel principal
        this.add(formPanel);
    }

    public void setDados(String ip, int port, String user, String equipa, String sala) {
        this.txtIp.setText(ip);
        this.txtPort.setText(String.valueOf(port));
        this.txtUser.setText(user);
        this.txtEquipa.setText(equipa);
        this.txtSala.setText(sala);
    }

    public String getIp() {
        return txtIp.getText().trim();
    }

    public int getPort() {
        try {
            return Integer.parseInt(txtPort.getText().trim());
        } catch (NumberFormatException e) {
            return 12345; // Default seguro em caso de erro
        }
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