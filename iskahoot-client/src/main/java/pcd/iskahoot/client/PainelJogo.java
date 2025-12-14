package pcd.iskahoot.client;

import javax.swing.*;
import pcd.iskahoot.common.TipoPergunta;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.IntConsumer;

public class PainelJogo extends JPanel {

    private JLabel questionLabel;
    private JPanel optionPanel;
    private JButton[] optionButtons;

    private JLabel timerLabel;
    private int segundos;
    private Timer timer;
    private JProgressBar progressBar;

    private JTextArea scoreBoard;

    private JList<String> playerList;
    private DefaultListModel<String> playerListModel;

    private boolean podeResponder = false;

    private final Color fundo = new Color(248, 249, 250);
    private final Color branco = Color.WHITE;
    private final Color preto = Color.BLACK;

    private final Color[] kahootColors = {
            new Color(226, 27, 60),
            new Color(19, 104, 206),
            new Color(216, 158, 0),
            new Color(38, 137, 12)
    };

    private final Font titleFont = new Font("Segoe UI", Font.BOLD, 36);
    private final Font optionFont = new Font("Segoe UI", Font.BOLD, 22);
    private final Font smallFont = new Font("Segoe UI", Font.PLAIN, 16);

    private final IntConsumer onAnswerSelectedCallback;

    public PainelJogo(IntConsumer callback) {
        super(new BorderLayout(15, 15));
        this.onAnswerSelectedCallback = callback;

        setBackground(fundo);
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        questionLabel = new JLabel("Aguardando pergunta...", SwingConstants.CENTER);
        questionLabel.setFont(titleFont);

        progressBar = new JProgressBar();
        progressBar.setPreferredSize(new Dimension(0, 18));
        progressBar.setBorderPainted(false);
        progressBar.setMinimum(0);
        progressBar.setMaximum(30);
        progressBar.setValue(30);


        timerLabel = new JLabel("", SwingConstants.CENTER);
        timerLabel.setFont(smallFont);

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setOpaque(false);

        topPanel.add(progressBar);
        topPanel.add(Box.createVerticalStrut(30));
        topPanel.add(questionLabel);
        topPanel.add(Box.createVerticalStrut(15));

        add(topPanel, BorderLayout.NORTH);

        optionPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        optionPanel.setOpaque(false);
        optionButtons = new JButton[4];

        for (int i = 0; i < 4; i++) {
            final int idx = i;

            JButton btn = new RoundedButton("Opção " + (i + 1));
            btn.setFont(optionFont);
            btn.setBackground(kahootColors[i]);
            btn.setForeground(Color.WHITE);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            btn.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    if (btn.isEnabled())
                        btn.setBackground(kahootColors[idx].darker());
                }

                public void mouseExited(MouseEvent e) {
                    if (btn.isEnabled())
                        btn.setBackground(kahootColors[idx]);
                }
            });

            btn.addActionListener(e -> handleOptionSelected(idx));

            optionPanel.add(btn);
            optionButtons[i] = btn;
        }

        add(optionPanel, BorderLayout.CENTER);

        add(timerLabel, BorderLayout.SOUTH);

        timer = new Timer(1000, e -> atualizarTimer());

        JPanel scorePanel = criarPainelLateral("ScoreBoard");
        scoreBoard = new JTextArea();
        scoreBoard.setEditable(false);
        scoreBoard.setFont(smallFont);
        scoreBoard.setBackground(branco);
        scorePanel.add(new JScrollPane(scoreBoard), BorderLayout.CENTER);
        add(scorePanel, BorderLayout.EAST);

        JPanel playerPanel = criarPainelLateral("Jogadores");
        playerListModel = new DefaultListModel<>();
        playerList = new JList<>(playerListModel);
        playerList.setFont(smallFont);
        playerList.setCellRenderer(new PlayerRenderer());
        playerPanel.add(new JScrollPane(playerList), BorderLayout.CENTER);
        add(playerPanel, BorderLayout.WEST);
    }

    public void updateQuestion(String q, String[] opt, int sec, TipoPergunta type) {
        SwingUtilities.invokeLater(() -> {
            questionLabel.setText("<html><div style='text-align:center;'>"
                    + escapeHtml(q) + "</div></html>");

            for (int i = 0; i < optionButtons.length; i++) {
                if (opt != null && i < opt.length) {
                    optionButtons[i].setText(opt[i]);
                    optionButtons[i].setEnabled(true);
                    optionButtons[i].setBackground(kahootColors[i]);
                    optionButtons[i].setForeground(Color.WHITE);
                } else {
                    optionButtons[i].setEnabled(false);
                }
            }

            podeResponder = true;
            timer.stop();
            resetTimer(sec);
            timer.start();

        });
    }

    private void handleOptionSelected(int i) {
        if (!podeResponder) return;

        podeResponder = false;
        timer.stop();

        for (int j = 0; j < optionButtons.length; j++) {
            JButton b = optionButtons[j];
            b.setEnabled(false);
            if (j == i) {
                b.setBackground(Color.WHITE);
                b.setForeground(kahootColors[i]);
            } else {
                b.setBackground(new Color(220, 220, 220));
            }
        }

        onAnswerSelectedCallback.accept(i);
    }

    private void atualizarTimer() {
        segundos--;
        timerLabel.setText(segundos + "s");
        progressBar.setValue(segundos);

        if (segundos <= 5) {
            progressBar.setForeground(kahootColors[0]);
            progressBar.setVisible(segundos % 2 == 0);
        } else if (segundos <= 10) {
            progressBar.setForeground(kahootColors[2]);
        }

        if (segundos <= 0) {
            timer.stop();
            podeResponder = false;
            timerLabel.setText("O tempo acabou!");
        }
    }

    private void resetTimer(int s) {
        segundos = s;
        timerLabel.setText(s + "s");
        progressBar.setMaximum(s);
        progressBar.setValue(s);
        progressBar.setForeground(kahootColors[3]);
        progressBar.setVisible(true);
    }

    public void updateScoreBoard(String txt) {
        SwingUtilities.invokeLater(() -> scoreBoard.setText(txt));
    }

    public void addPlayer(String p) {
        SwingUtilities.invokeLater(() -> {
            if (!playerListModel.contains(p))
                playerListModel.addElement(p);
        });
    }

    public void setPlayerList(java.util.List<String> players) {
        SwingUtilities.invokeLater(() -> {
            playerListModel.clear();
            players.forEach(playerListModel::addElement);
        });
    }

    private JPanel criarPainelLateral(String titulo) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(branco);
        panel.setPreferredSize(new Dimension(220, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel label = new JLabel(titulo, SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 20));
        panel.add(label, BorderLayout.NORTH);

        return panel;
    }

    private String escapeHtml(String s) {
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\n", "<br/>");
    }



    static class RoundedButton extends JButton {
        public RoundedButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
        }

        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
            super.paintComponent(g);
        }
    }

    static class PlayerRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(
                JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {

            JLabel lbl = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);

            lbl.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            lbl.setBackground(isSelected
                    ? new Color(19, 104, 206)
                    : index % 2 == 0 ? Color.WHITE : new Color(245, 245, 245));
            lbl.setForeground(isSelected ? Color.WHITE : Color.BLACK);
            return lbl;
        }
    }

/*     public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
        JFrame frame = new JFrame("IsKahoot - Teste GUI");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 800);
        frame.setLocationRelativeTo(null);

        PainelJogo painel = new PainelJogo(
                resposta -> System.out.println("Resposta escolhida: " + resposta)
        );

        frame.setContentPane(painel);
        frame.setVisible(true);

        // 🔽 Dados de teste
        painel.setPlayerList(java.util.List.of(
                "Alice", "Bruno", "Carla", "Diogo", "Eva"
        ));



        painel.updateQuestion(
                "Qual destas estruturas NÃO é bloqueante?",
                new String[]{
                        "join()",
                        "sleep()",
                        "interrupted()",
                        "wait()"
                },
                30,
                null
        );
    });
} */

}


