package pcd.iskahoot.client;

import javax.swing.SwingUtilities;

public class ClientMain {

    public static void main(String[] args) {

        if (args.length < 2) {
            System.out.println("Uso: java clienteKahoot { IP PORT Sala Equipa Username}");
//teste
            args = new String[]{"localhost", "12345", "sala1", "eq1", "user1"};
            System.out.println("A usar valores por defeito (localhost:12345)...");
        }

        final String ip = args[0];
        final int port = Integer.parseInt(args[1]);

        SwingUtilities.invokeLater(() -> {
            ClientGUI gui = new ClientGUI();
            
            ClientNetwork network = new ClientNetwork(ip, port, gui);

            gui.setNetworkHandler(network);

            gui.setVisible(true);
        });
    }
}