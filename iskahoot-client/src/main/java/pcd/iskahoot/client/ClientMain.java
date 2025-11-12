package pcd.iskahoot.client;

import javax.swing.SwingUtilities;

public class ClientMain {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            new ClientGUI().setVisible(true);
        });
    }
}