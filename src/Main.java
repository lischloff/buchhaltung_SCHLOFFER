// Project: Buchhaltung
// Author: Schloffer Lisa
// Date: 23.10.2024

import KontoView.KontoView;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // GUI in einem JFrame anzeigen
        javax.swing.SwingUtilities.invokeLater(() -> {
            KontoView view = new KontoView();
            JFrame frame = new JFrame("KontoView");
            frame.setContentPane(view.panel1); // Zugriff auf panel1, da es jetzt 'public' ist
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();
            frame.setSize(600, 400); // Festlegen einer Standardgröße
            frame.setVisible(true);
        });
    }
}
