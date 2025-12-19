package pcd.iskahoot.client;

import javax.swing.SwingUtilities;


/**
 * Ponto de entrada do cliente IsKahoot.
 *
 * Uso:
 *   java ClientMain {IP} {PORT}
 *
 * Se não forem fornecidos argumentos, usa:
 *   IP   = localhost
 *   PORT = 12345
 *
 * O utilizador final NÃO introduz IP nem porto na GUI.
 * Apenas insere username, equipa e código da sala.
 * 
 * O ip é definido no arranque do cliente atraves dos argumentos da main
 */


public class ClientMain {

    public static void main(String[] args) {

        // Default connection values
        String ip = "localhost";
        int port = 12345;
        
        // Defaults for UI fields (optional, just to pre-fill)
        String defaultSala = "SALA-1";
        String defaultEquipa = "EquipaA";
        String defaultUser = "Aluno1";

        // Parse arguments: java ClientMain {IP} {PORT} {SALA} {EQUIPA} {USER}
        if (args.length >= 2) {
            try {
                ip = args[0];
                port = Integer.parseInt(args[1]);
                if (args.length >= 5) {
                    defaultSala = args[2];
                    defaultEquipa = args[3];
                    defaultUser = args[4];
                }
            } catch (NumberFormatException e) {
                System.err.println("Erro: A porta deve ser um número inteiro.");
            }
        } else {
            System.out.println("Aviso: Argumentos insuficientes. A usar valores por defeito (localhost:12345).");
            System.out.println("Uso: java ClientMain {IP} {PORT} [SALA] [EQUIPA] [USER]");
        }

        final String fIp = ip;
        final int fPort = port;
        final String fSala = defaultSala;
        final String fEquipa = defaultEquipa;
        final String fUser = defaultUser;

        SwingUtilities.invokeLater(() -> {
            // Pass IP and Port directly to the GUI "backend", not the UI text fields
            ClientGUI gui = new ClientGUI(fIp, fPort);
            gui.preencherDadosUi(fUser, fEquipa, fSala);
            gui.setVisible(true);
        });
    }
}