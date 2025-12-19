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

        setLayout(new GridBagLayout());
        setBackground(new Color(245, 246, 250));

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(new EmptyBorder(30, 40, 30, 40));
        card.setPreferredSize(new Dimension(360, 420));

        JLabel lblTitulo = new JLabel("IsKahoot");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 36));
        lblTitulo.setForeground(new Color(90, 72, 255));
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSubtitulo = new JLabel("Entrar no jogo");
        lblSubtitulo.setFont(new Font("SansSerif", Font.PLAIN, 16));
        lblSubtitulo.setForeground(Color.DARK_GRAY);
        lblSubtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(lblTitulo);
        card.add(Box.createVerticalStrut(10));
        card.add(lblSubtitulo);
        card.add(Box.createVerticalStrut(30));

        JLabel lblUser = criarLabel("Username");
        lblUser.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblUser);

        txtUser = criarCampo("Aluno1");
        card.add(txtUser);

        card.add(Box.createVerticalStrut(15));

        JLabel lblEquipa = criarLabel("Nome da Equipa");
        lblEquipa.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblEquipa);

        txtEquipa = criarCampo("EquipaA");
        card.add(txtEquipa);

        card.add(Box.createVerticalStrut(15));

        JLabel lblSala = criarLabel("Código da Sala");
        lblSala.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblSala);

        txtSala = criarCampo("SALA-1");
        card.add(txtSala);

        card.add(Box.createVerticalStrut(30));

        btnEntrar = new JButton("ENTRAR");
        btnEntrar.setFont(new Font("SansSerif", Font.BOLD, 16));
        btnEntrar.setBackground(new Color(90, 72, 255));
        btnEntrar.setForeground(Color.WHITE);
        btnEntrar.setFocusPainted(false);
        btnEntrar.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        btnEntrar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnEntrar.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnEntrar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnEntrar.setBackground(new Color(70, 52, 235));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnEntrar.setBackground(new Color(90, 72, 255));
            }
        });

        btnEntrar.addActionListener(acaoBotao);
        card.add(btnEntrar);

        add(card); 
    }


    private JLabel criarLabel(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        lbl.setForeground(new Color(80, 80, 80));
        return lbl;
    }

    private JTextField criarCampo(String texto) {
        JTextField campo = new JTextField(texto);
        campo.setFont(new Font("SansSerif", Font.PLAIN, 14));
        campo.setMaximumSize(new Dimension(260, 36)); // 👈 largura fixa
        campo.setAlignmentX(Component.CENTER_ALIGNMENT);
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        return campo;
    }


    public void setDados(String user, String equipa, String sala) {
        txtUser.setText(user);
        txtEquipa.setText(equipa);
        txtSala.setText(sala);
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
