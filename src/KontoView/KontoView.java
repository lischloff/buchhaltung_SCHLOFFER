// Project: Buchhaltung
// Author: Schloffer Lisa
// Date: 23.10.2024


package KontoView;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;

public class KontoView {
    // GUI-Komponenten
    public JPanel panel1;
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

    public KontoView() {
        // Initialisiere die JTable mit Spaltennamen
        ein_ausgabe.setModel(new DefaultTableModel(new Object[]{"ID", "Kategorie-ID", "Datum", "Zusatzinfo", "Betrag"}, 0));
    }

    public void updateTableData(List<Object[]> data) {
        DefaultTableModel model = (DefaultTableModel) ein_ausgabe.getModel();
        model.setRowCount(0); // Vorherige Daten löschen

        // Debugging-Ausgabe
        System.out.println("Tabellenaktualisierung gestartet. Anzahl der Zeilen: " + data.size());

        // Neue Daten in die Tabelle einfügen
        for (Object[] row : data) {
            model.addRow(row);
        }

        // Bestätigungsausgabe
        System.out.println("Tabellenaktualisierung abgeschlossen.");
    }
}
