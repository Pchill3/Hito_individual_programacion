package hito_individual;

import javax.swing.*;

public class SoloCrossfitApp extends JFrame {
    private static final long serialVersionUID = 1L;

    public SoloCrossfitApp() {
        super("Registro de usuarios SoloCrossfitApp");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(null);

        SoloCrossfitPanel panel = new SoloCrossfitPanel();
        add(panel);
    }

    public static void main(String[] args) {
        SoloCrossfitApp app = new SoloCrossfitApp();
        app.setVisible(true);
    }
}
