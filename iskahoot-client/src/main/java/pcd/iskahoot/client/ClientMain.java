package pcd.iskahoot.client;

import javax.swing.SwingUtilities;

public class ClientMain {

    public static void main(String[] args) {

        // Valores por defeito (caso não sejam passados argumentos)
        String ip = "localhost";
        int port = 12345;
        String sala = "SALA-1";
        String equipa = "EquipaA";
        String username = "Jogador1";

        // 1. Parse dos Argumentos (Requisito do Enunciado: IP PORT Jogo Equipa Username)
        if (args.length >= 5) {
            try {
                ip = args[0];
                port = Integer.parseInt(args[1]);
                sala = args[2];
                equipa = args[3];
                username = args[4];
            } catch (NumberFormatException e) {
                System.err.println("Erro: A porta deve ser um número inteiro.");
            }
        } else {
            System.out.println("Aviso: Argumentos insuficientes. A usar valores por defeito.");
            System.out.println("Uso correto: java ClientMain {IP} {PORT} {SALA} {EQUIPA} {USER}");
        }

        // Variáveis finais para usar no lambda
        final String fIp = ip;
        final int fPort = port;
        final String fSala = sala;
        final String fEquipa = equipa;
        final String fUser = username;

        // 2. Arrancar a Interface Gráfica
        SwingUtilities.invokeLater(() -> {
            ClientGUI gui = new ClientGUI();
            
            gui.preencherDadosIniciais(fIp, fPort, fUser, fEquipa, fSala);

            gui.setVisible(true);
        });
    }
}