package pcd.iskahoot.client;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

import javax.swing.*;
import java.awt.*;

public class PainelPlacar extends JPanel {
    
    private JLabel titulo;
    private JTable tabela;
    private DefaultTableModel modelo;

        public PainelPlacar() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 246, 250));

        titulo = new JLabel("Placar", SwingConstants.CENTER);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 26));
        titulo.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        modelo = new DefaultTableModel(
                new Object[]{"Posição", "Equipa", "Pontuação"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabela = new JTable(modelo);
        tabela.setFont(new Font("SansSerif", Font.PLAIN, 16));
        tabela.setRowHeight(28);
        tabela.getTableHeader().setFont(
                new Font("SansSerif", Font.BOLD, 16));

        JScrollPane scroll = new JScrollPane(tabela);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 40, 40, 40));

        add(titulo, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    public void atualizarPlacar(Map<String, Integer> placar, boolean fimDeJogo) {
        modelo.setRowCount(0);

        titulo.setText(fimDeJogo ? "🏆 FIM DO JOGO" : "Placar Atual");

        List<Map.Entry<String, Integer>> lista =
                new ArrayList<>(placar.entrySet());

        lista.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

        int pos = 1;
        for (Map.Entry<String, Integer> e : lista) {
            modelo.addRow(new Object[]{
                    pos++, e.getKey(), e.getValue()
            });
        }
    }
}

