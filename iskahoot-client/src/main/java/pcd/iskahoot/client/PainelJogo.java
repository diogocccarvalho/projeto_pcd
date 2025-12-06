package pcd.iskahoot.client;

import javax.swing.*;

import pcd.iskahoot.common.TipoPergunta;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.IntConsumer; // Importar o "callback"

public class PainelJogo extends JPanel {
    
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

    // Cores
    private final Color azul = new Color(131, 197, 190);
    private final Color cinza = new Color(239, 241, 237);
    private final Color branco = new Color(255,255,255);
    private final Color preto = new Color(0,0,0); 
    private final Color azulEsc = new Color(0,109,119);
    

    private final IntConsumer onAnswerSelectedCallback;


    public PainelJogo (IntConsumer callback) {
        super(new BorderLayout(10, 10));
        this.onAnswerSelectedCallback = callback;


        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        questionLabel = new JLabel("Aguardando pergunta...", SwingConstants.CENTER);
        questionLabel.setFont(new Font("Calibri", Font.BOLD, 40));

        progressBar = new JProgressBar(0, defaultSeconds);
        progressBar.setValue(defaultSeconds);
        progressBar.setStringPainted(false);
        progressBar.setForeground(azulEsc); 

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setOpaque(false);

        topPanel.add(progressBar);
        topPanel.add(Box.createVerticalStrut(38));
        topPanel.add(questionLabel);
        topPanel.add(Box.createVerticalStrut(25));

        add(topPanel, BorderLayout.NORTH);

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

        add(optionPanel, BorderLayout.CENTER);

        timerLabel = new JLabel(defaultSeconds + "s", SwingConstants.CENTER);
        timerLabel.setFont(new Font("Arial", Font.ITALIC, 18));
        add(timerLabel, BorderLayout.SOUTH);

        timer = new Timer(1000, e -> atualizarTimer());

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

        add(scoreboardPanel, BorderLayout.EAST);
        

    }

    public void updateQuestion(String p, String[] opt, int secQuestion, TipoPergunta type) {
        SwingUtilities.invokeLater(() -> {
            questionLabel.setText("<html><div style='text-align:center;'>" +
                    escapeHtml(p) +
                    "</div></html>");

            for (int i = 0; i<4; i++){
 
                if (opt != null && i < opt.length) {
                    optionButtons[i].setText(opt[i]);
                    optionButtons[i].setEnabled(true);
                } else {
                    optionButtons[i].setText("");
                    optionButtons[i].setEnabled(false);
                }
            }

            podeResponder = true;

            resetTimer(secQuestion);
            startTimer();

        });
    }

    public void updateScoreBoard(String text) {
        SwingUtilities.invokeLater(() -> scoreBoard.setText(text));
    } 

    private void handleOptionSelected(int i) {
        if(!podeResponder) return;

        podeResponder = false;
        stopTimer();


        if (onAnswerSelectedCallback != null) {
            onAnswerSelectedCallback.accept(i);
        }
    }
    
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
        timerLabel.setText(s + "s");
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


}