package KontoView;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class KontoView {
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
    private JButton filterBnt;

    // Datenbankverbindungsdetails
    private static final String DB_URL = "jdbc:mysql://localhost/konten_db";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public KontoView() {
        // Initialisiere die JTable mit Spaltennamen und setze das Model
        ein_ausgabe.setModel(new NonEditableTableModel(new Object[]{"ID", "Kategorie-ID", "Datum", "Zusatzinfo", "Betrag"}, 0));
        ein_ausgabe.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Deaktiviert Mehrfachselektion
        ein_ausgabe.setCellSelectionEnabled(false); // Deaktiviert Zellenselektion
        ein_ausgabe.setRowSelectionAllowed(false); // Deaktiviert Reihenselektion
        ein_ausgabe.setColumnSelectionAllowed(false); // Deaktiviert Spaltenselektion

        // Daten aus der Datenbank abrufen und die Tabelle aktualisieren
        List<Object[]> data = fetchDataFromDatabase();
        updateTableData(data);
    }

    public void updateTableData(List<Object[]> data) {
        DefaultTableModel model = (DefaultTableModel) ein_ausgabe.getModel();
        model.setRowCount(0); // Vorherige Daten löschen
        // Neue Daten in die Tabelle einfügen
        for (Object[] row : data) {
            model.addRow(row);
        }
    }

    private List<Object[]> fetchDataFromDatabase() {
        List<Object[]> data = new ArrayList<>();
        try (Connection connect = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            // Datenabfrage durchführen
            Statement statement = connect.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM Buchung");
            // Zeilenweise Daten in Liste einfügen
            while (resultSet.next()) {
                Object[] row = {
                        resultSet.getInt("id"),
                        resultSet.getInt("kategorie_id"),
                        resultSet.getTimestamp("datum"),
                        resultSet.getString("zusatzinfo"),
                        resultSet.getDouble("betrag")
                };
                data.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    // Eine benutzerdefinierte Klasse für das nicht editierbare Modell
    private static class NonEditableTableModel extends DefaultTableModel {
        public NonEditableTableModel(Object[] columnNames, int rowCount) {
            super(columnNames, rowCount);
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false; // Keine Zelle ist editierbar
        }
    }

    // Hauptmethode zum Starten der Anwendung
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
