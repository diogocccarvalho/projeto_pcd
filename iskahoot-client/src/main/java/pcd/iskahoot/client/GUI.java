// GUI.java → janela principal com pergunta, opções e temporizador.
// ENUNCIADO: As principais interações executadas pelo cliente serão feitas através da
// GUI: exibição de perguntas, cronómetros e placares recebidos, bem como
// enviar respostas durante cada ronda.

package pcd.iskahoot.client;

import javax.swing.*;
import java.awt.*;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


// JFrame : componente principal onde se coloca todos os elementos visuais
public class GUI extends JFrame {
    
    private JLabel questionLabel;
    private JPanel optionPanel;
    JButton [] optionButtons;

    private JLabel timerLabel;
    private int segundos;
    private int defaultSeconds = 30;
    private Timer timer;
    private JProgressBar progressBar;
    
    JTextArea scoreBoard;

    private boolean podeResponder = false;

    private final Color beje = new Color(244, 241, 222);      // Beje?
    private final Color azul = new Color(131, 197, 190);       // Azul?
    private final Color cinza = new Color(239, 241, 237);  // Fundo cinzento-claro
    private final Color branco = new Color(255,255,255);
    private final Color preto = new Color(0,0,0); 
    private final Color azulEsc = new Color(0,109,119);
    

public GUI () {
    super("isKahoot - Cliente");

    // Layout
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(900, 600);
    setLocationRelativeTo(null);
    setResizable(false);

    JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
    mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
    add(mainPanel, BorderLayout.CENTER);

    // Pergunta
    questionLabel = new JLabel("Aguardando pergunta...", SwingConstants.CENTER);
    questionLabel.setFont(new Font("Calibri", Font.BOLD, 40));

    // barra do timer
    progressBar = new JProgressBar(0, defaultSeconds);
    progressBar.setValue(defaultSeconds);
    progressBar.setStringPainted(false); // sem texto
    progressBar.setForeground(azulEsc); 

    // TOP que junta barra + pergunta
    JPanel topPanel = new JPanel();
    topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
    topPanel.setOpaque(false); // mantém fundo igual

    topPanel.add(progressBar);
    topPanel.add(Box.createVerticalStrut(38)); // pequeno espaçamento
    topPanel.add(questionLabel);
    topPanel.add(Box.createVerticalStrut(25));

    mainPanel.add(topPanel, BorderLayout.NORTH);


    // Opções 
    optionPanel = new JPanel(new GridLayout(2, 2, 15, 15));
    optionPanel.setBackground(cinza);
    optionButtons = new JButton[4];

    for (int i = 0; i < 4; i++) {
        final int idx = i;
        JButton btn = new JButton("Opção " + (i + 1));
        btn.setFont(new Font("Arial", Font.PLAIN, 20));
        btn.setFocusPainted(false);
        btn.setBackground(branco);
        btn.setBorder(BorderFactory.createLineBorder(cinza, 2, true));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(azul);
                btn.setForeground(branco);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(branco);
                btn.setForeground(preto);
            }
        });

        btn.addActionListener(e -> handleOptionSelected(idx));

        optionPanel.add(btn);
        optionButtons[i] = btn;
    }

    mainPanel.add(optionPanel, BorderLayout.CENTER);


    // Temporizador
    timerLabel = new JLabel(defaultSeconds + "s", SwingConstants.CENTER);
    timerLabel.setFont(new Font("Arial", Font.ITALIC, 18));
    mainPanel.add(timerLabel, BorderLayout.SOUTH);

    timer = new Timer(1000, e -> atualizarTimer());

    // ScoreBoard
    JPanel scoreboardPanel = new JPanel(new BorderLayout());
    scoreboardPanel.setBackground(branco);
    scoreboardPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(preto, 5),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
    ));
    scoreboardPanel.setPreferredSize(new Dimension(220, 0));

    JLabel scoreboardTitle = new JLabel("ScoreBoard", SwingConstants.CENTER);
    scoreboardTitle.setFont(new Font("Calibri", Font.BOLD | Font.ITALIC, 25));
    scoreboardTitle.setForeground(preto);
    scoreboardPanel.add(scoreboardTitle, BorderLayout.NORTH);

    scoreBoard = new JTextArea();
    scoreBoard.setEditable(false);
    scoreBoard.setFont(new Font("Arial", Font.PLAIN, 14));
    scoreBoard.setBackground(branco);
    scoreBoard.setForeground(Color.DARK_GRAY);
    scoreBoard.setMargin(new Insets(10, 10, 10, 10));

    JScrollPane scroll = new JScrollPane(scoreBoard);
    scroll.setBorder(null);
    scoreboardPanel.add(scroll, BorderLayout.CENTER);

    mainPanel.add(scoreboardPanel, BorderLayout.EAST);

    setVisible(true);
}


    public void updateQuestion(String p, String[] opt, int secQuestion) {
        SwingUtilities.invokeLater(() -> {
            questionLabel.setText("<html><div style='text-align:center;'>" +
                    escapeHtml(p) +
                    "</div></html>");

            for (int i = 0; i<4; i++){
                optionButtons[i].setText(opt[i]);
                optionButtons[i].setEnabled(true);
            }

            podeResponder = true;

            resetTimer(secQuestion);
            startTimer();

        });
    }

    public void updateScoreBoard(String text) {
        SwingUtilities.invokeLater(() -> scoreBoard.setText(text));
    } 

    //Clicar é fixe
    private void handleOptionSelected(int i) {
        if(!podeResponder) return; // ignora o click se o tempo acabar

        podeResponder= false; //evita mais que uma resposta a mesma pergunta
        stopTimer();
    }
    

    // cenas do timer
    private void atualizarTimer() {
        segundos--;
        timerLabel.setText(segundos + "s");
        progressBar.setValue(segundos);

        if (segundos <= 0) {
            timer.stop();
            podeResponder= false;
            timerLabel.setText("O tempo acabou XD");
        }
    }

    private void startTimer() {
        if (!timer.isRunning()) timer.start();
    }

    private void stopTimer() {
        if (timer.isRunning()) timer.stop();
    }

    private void resetTimer(int s){
        segundos = s;
        timerLabel.setText(+ s + "s");
        timer.stop();
        progressBar.setMaximum(s);
        progressBar.setValue(s);
    }

    private String escapeHtml(String a) {
        return a.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\n", "<br/>");
    }


    //main de teste
    public static void main(String[] args) {
        GUI g = new GUI();

        String[] ops = {"A", "B", "C", "D"};
        g.updateQuestion("Qual é a capital de França?", ops, 30);
    }
}
