package co.edu.unicauca.sistema.desktop;

import javax.swing.SwingUtilities;

/** Starts the Swing application on the Event Dispatch Thread. */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        ClientConfig config = ClientConfig.fromSystemProperties();
        SwingUtilities.invokeLater(() -> new MainFrame(config).setVisible(true));
    }
}
