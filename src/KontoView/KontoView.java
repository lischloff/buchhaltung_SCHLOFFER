// Project: Buchhaltung
// Author: Schloffer Lisa
// Date: 23.10.2024

package KontoView;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class KontoView {
    public JPanel panel1;
    private JTable ein_ausgabe;
    private JLabel eingabe;
    private JLabel zahlungsartLabel;
    private JComboBox<String> zahlungsarten;
    private JTextField bezeichnungFeld;
    private JTextField kurzbeschreibungField;
    private JLabel eingabe_oder_ausgabe;
    private JButton saveBtn;
    private JTextField betragField;
    private JRadioButton einnahmeRadioButton;
    private JRadioButton ausgabeRadioButton;
    private JButton filterBnt;
    private JButton deleteBtn;
    private JButton updateBtn;

    private static final String DB_URL = "jdbc:mysql://localhost/buchhaltung_db";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private int selectedId = -1; // Speichert die ID des aktuell ausgewählten Eintrags in der Tabelle

    public KontoView() {
        initializeComponents();
        updateTableData(fetchDataFromDatabase());
        eingabe_oder_ausgabe.setText("Wählen Sie eine Option:");
    }

    // Initialisiert GUI-Komponenten und Aktionen
    private void initializeComponents() {
        initializeTable();

        // Speichern-Button-Aktion
        saveBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveData();
            }
        });

        // Löschen-Button-Aktion
        deleteBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteData();
            }
        });

        // Aktualisieren-Button-Aktion
        updateBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateData();
            }
        });

        deleteBtn.setEnabled(false); // Löschen-Button zu Beginn deaktiviert
        updateBtn.setEnabled(false); // Aktualisieren-Button zu Beginn deaktiviert
    }

    // Tabelle wird initialisiert, sodass nur eine Zeile auswählbar ist
    private void initializeTable() {
        ein_ausgabe.setModel(new NonEditableTableModel(new Object[]{"ID", "Kategorie", "Datum", "Zusatzinfo", "Betrag"}, 0));
        ein_ausgabe.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Listener für Zeilen-Auswahl in der Tabelle
        ein_ausgabe.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting() && ein_ausgabe.getSelectedRow() != -1) {
                    loadSelectedData();
                }
            }
        });
    }

    // Aktualisiert die Daten in der Tabelle
    public void updateTableData(List<Object[]> data) {
        DefaultTableModel model = (DefaultTableModel) ein_ausgabe.getModel();
        model.setRowCount(0);
        for (Object[] row : data) {
            model.addRow(row);
        }
    }

    // Holt alle Buchungen aus der Datenbank und speichert sie in einer Liste
    private List<Object[]> fetchDataFromDatabase() {
        List<Object[]> data = new ArrayList<>();
        try (Connection connect = DriverManager.getConnection(DB_URL, USER, PASSWORD);
             Statement statement = connect.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM Buchung")) {
            while (resultSet.next()) {
                Object[] row = {
                        resultSet.getInt("ID"),
                        resultSet.getInt("Kategorie_ID"),
                        resultSet.getTimestamp("Datum"),
                        resultSet.getString("Zusatzinfo"),
                        resultSet.getDouble("Betrag")
                };
                data.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    // Lädt die Daten des ausgewählten Eintrags aus der Tabelle in die Eingabefelder
    private void loadSelectedData() {
        int row = ein_ausgabe.getSelectedRow();
        if (row != -1) {
            selectedId = (int) ein_ausgabe.getValueAt(row, 0);  // ID aus der Tabelle
            int kategorieId = (int) ein_ausgabe.getValueAt(row, 1) - 1;  // Kategorie-ID in Index konvertieren
            String zusatzinfo = (String) ein_ausgabe.getValueAt(row, 3);
            double betrag = (double) ein_ausgabe.getValueAt(row, 4);

            // Datum aus der Tabelle holen und in LocalDate konvertieren
            Date datum = (Date) ein_ausgabe.getValueAt(row, 2);
            LocalDateTime entryDate = datum.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

            // Aktuelles Datum holen
            LocalDate currentDate = LocalDate.now();

            // Prüfen, ob das Datum des Eintrags mit dem heutigen Datum übereinstimmt
            if (entryDate.equals(currentDate)) {
                zahlungsarten.setSelectedIndex(kategorieId);  // Kategorie in ComboBox auswählen
                kurzbeschreibungField.setText(zusatzinfo);    // Zusatzinfo ins Textfeld
                betragField.setText(String.valueOf(betrag));  // Betrag ins Textfeld
                deleteBtn.setEnabled(true); // Löschen-Button aktivieren
                updateBtn.setEnabled(true); // Aktualisieren-Button aktivieren
            } else {
                // Falls das Datum nicht übereinstimmt, deaktivieren
                JOptionPane.showMessageDialog(panel1, "Sie können nur Einträge vom heutigen Tag bearbeiten.", "Einschränkung", JOptionPane.WARNING_MESSAGE);
                deleteBtn.setEnabled(false); // Löschen-Button deaktivieren
                updateBtn.setEnabled(false); // Aktualisieren-Button deaktivieren
            }
        }
    }

    // Speichert die eingegebenen Daten in der Datenbank
    // Speichert die eingegebenen Daten in der Datenbank
    private void saveData() {
        String kurzbeschreibung = kurzbeschreibungField.getText().trim();
        String betragText = betragField.getText().trim();

        if (kurzbeschreibung.isEmpty() || betragText.isEmpty()) {
            JOptionPane.showMessageDialog(panel1, "Bitte füllen Sie alle Felder aus.", "Eingabefehler", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double betrag;
        try {
            betrag = Double.parseDouble(betragText); // Betrag als Zahl parsen
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(panel1, "Der Betrag muss eine Zahl sein.", "Eingabefehler", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int kategorieId = zahlungsarten.getSelectedIndex() + 1;

        // Aktuellen Zeitpunkt holen
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        try (Connection connect = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            String sql = "INSERT INTO Buchung (Kategorie_ID, Datum, Zusatzinfo, Betrag) VALUES (?, ?, ?, ?)";
            try (PreparedStatement preparedStatement = connect.prepareStatement(sql)) {
                preparedStatement.setInt(1, kategorieId);
                preparedStatement.setTimestamp(2, timestamp); // Timestamp anstelle von Date verwenden
                preparedStatement.setString(3, kurzbeschreibung);
                preparedStatement.setDouble(4, betrag);
                preparedStatement.executeUpdate();
                JOptionPane.showMessageDialog(panel1, "Eintrag erfolgreich gespeichert.");
                updateTableData(fetchDataFromDatabase());
                clearFields();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(panel1, "Fehler beim Speichern des Eintrags: " + e.getMessage(), "Datenbankfehler", JOptionPane.ERROR_MESSAGE);
        }
    }


    // Löscht den ausgewählten Eintrag in der Datenbank mit Bestätigung
    private void deleteData() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(panel1, "Kein Eintrag ausgewählt.", "Fehler", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                panel1,
                "Sind Sie sicher, dass Sie den ausgewählten Eintrag löschen möchten?",
                "Löschen bestätigen",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection connect = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
                String sql = "DELETE FROM Buchung WHERE ID = ?";
                try (PreparedStatement preparedStatement = connect.prepareStatement(sql)) {
                    preparedStatement.setInt(1, selectedId);
                    preparedStatement.executeUpdate();
                    JOptionPane.showMessageDialog(panel1, "Eintrag erfolgreich gelöscht.");
                    updateTableData(fetchDataFromDatabase());
                    clearFields();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(panel1, "Fehler beim Löschen des Eintrags: " + e.getMessage(), "Datenbankfehler", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Aktualisiert den ausgewählten Eintrag in der Datenbank
    private void updateData() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(panel1, "Kein Eintrag ausgewählt.", "Fehler", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String kurzbeschreibung = kurzbeschreibungField.getText().trim();
        String betragText = betragField.getText().trim();

        if (kurzbeschreibung.isEmpty() || betragText.isEmpty()) {
            JOptionPane.showMessageDialog(panel1, "Bitte füllen Sie alle Felder aus.", "Eingabefehler", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double betrag;
        try {
            betrag = Double.parseDouble(betragText); // Betrag als Zahl parsen
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(panel1, "Der Betrag muss eine Zahl sein.", "Eingabefehler", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int kategorieId = zahlungsarten.getSelectedIndex() + 1;

        try (Connection connect = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            String sql = "UPDATE Buchung SET Kategorie_ID = ?, Zusatzinfo = ?, Betrag = ? WHERE ID = ?";
            try (PreparedStatement preparedStatement = connect.prepareStatement(sql)) {
                preparedStatement.setInt(1, kategorieId);
                preparedStatement.setString(2, kurzbeschreibung);
                preparedStatement.setDouble(3, betrag);
                preparedStatement.setInt(4, selectedId);
                preparedStatement.executeUpdate();
                JOptionPane.showMessageDialog(panel1, "Eintrag erfolgreich aktualisiert.");
                updateTableData(fetchDataFromDatabase());
                clearFields();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(panel1, "Fehler beim Aktualisieren des Eintrags: " + e.getMessage(), "Datenbankfehler", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Leert die Eingabefelder und setzt selectedId zurück
    private void clearFields() {
        bezeichnungFeld.setText("");
        kurzbeschreibungField.setText("");
        betragField.setText("");
        selectedId = -1;
        deleteBtn.setEnabled(false);
        updateBtn.setEnabled(false);
    }

    // Benutzerdefiniertes TableModel, um Zellen nicht editierbar zu machen
    private static class NonEditableTableModel extends DefaultTableModel {
        public NonEditableTableModel(Object[] columnNames, int rowCount) {
            super(columnNames, rowCount);
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    }

    // Hauptprogramm zum Erstellen und Anzeigen des GUI-Fensters
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            KontoView view = new KontoView();
            JFrame frame = new JFrame("KontoView");
            frame.setContentPane(view.panel1);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();
            frame.setSize(600, 400);
            frame.setVisible(true);
        });
    }
}
