// Project: Buchhaltung
// Author: Schloffer Lisa
// Date: 23.10.2024

package KontoView;

import javax.swing.*;

public class KontoView {
    private JPanel panel1;
    private JTable ein_ausgabe;
    private JLabel eingabe;
    private JLabel zahlungsartLabel;
    private JComboBox zahlungsarten;
    private JTextField bezeichnungFeld;
    private JTextField kurzbeschreibungField;
    private JLabel eingabe_oder_ausgabe;
    private JButton saveBtn;
    private JTextField betragField;
    private JRadioButton einnahmeRadioButton;
    private JRadioButton ausgabeRadioButton;

    public static void main(String[] args) {
        JFrame frame = new JFrame("KontoView");
        frame.setContentPane(new KontoView().panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
