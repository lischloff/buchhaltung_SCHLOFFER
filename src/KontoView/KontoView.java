package KontoView;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class KontoView {
    public JPanel panel1;
    private JTable ein_ausgabe;
    private JLabel eingabe;
    private JLabel zahlungsartLabel;
    private JComboBox<String> zahlungsarten;  // Verwendung für Kategorien
    private JTextField bezeichnungFeld;
    private JTextField kurzbeschreibungField;
    private JLabel eingabe_oder_ausgabe;
    private JButton saveBtn;
    private JTextField betragField;
    private JRadioButton einnahmeRadioButton;
    private JRadioButton ausgabeRadioButton;
    private JButton filterBnt;
    private JButton deleteBtn;

    private static final String DB_URL = "jdbc:mysql://localhost/buchhaltung_db";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private int selectedId = -1; // Speichert die ID des ausgewählten Eintrags

    public KontoView() {
        initializeComponents();
        updateTableData(fetchDataFromDatabase());
        eingabe_oder_ausgabe.setText("Wählen Sie eine Option:");
    }

    private void initializeComponents() {
        initializeTable();
        initializeCategoryComboBox();
        saveBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveData();
            }
        });
        deleteBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteData();
            }
        });
        deleteBtn.setEnabled(false); // Deaktivieren des Buttons zu Beginn
    }

    private void initializeTable() {
        ein_ausgabe.setModel(new NonEditableTableModel(new Object[]{"ID", "Kategorie", "Datum", "Zusatzinfo", "Betrag"}, 0));
        ein_ausgabe.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ein_ausgabe.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting() && ein_ausgabe.getSelectedRow() != -1) {
                    loadSelectedData();
                }
            }
        });
    }

    private void initializeCategoryComboBox() {
        String[] kategorien = {"Miete", "Gehalt", "Stromkosten", "Verkauf", "Werbung", "Steuererstattung", "Softwarelizenzen"};
        for (String kategorie : kategorien) {
            zahlungsarten.addItem(kategorie);  // Kategorien zur ComboBox hinzufügen
        }
    }

    public void updateTableData(List<Object[]> data) {
        DefaultTableModel model = (DefaultTableModel) ein_ausgabe.getModel();
        model.setRowCount(0);
        for (Object[] row : data) {
            model.addRow(row);
        }
    }

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

    private void loadSelectedData() {
        int row = ein_ausgabe.getSelectedRow();
        if (row != -1) {
            selectedId = (int) ein_ausgabe.getValueAt(row, 0);  // ID aus der Tabelle
            int kategorieId = (int) ein_ausgabe.getValueAt(row, 1) - 1;  // Kategorie-ID in Index konvertieren
            String zusatzinfo = (String) ein_ausgabe.getValueAt(row, 3);
            double betrag = (double) ein_ausgabe.getValueAt(row, 4);
            zahlungsarten.setSelectedIndex(kategorieId);  // Kategorie in ComboBox auswählen
            kurzbeschreibungField.setText(zusatzinfo);    // Zusatzinfo ins Textfeld
            betragField.setText(String.valueOf(betrag));  // Betrag ins Textfeld
            deleteBtn.setEnabled(true); // Löschen-Button aktivieren
        }
    }

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
                    updateTableData(fetchDataFromDatabase());  // Tabelle nach dem Löschen aktualisieren
                    clearFields();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(panel1, "Fehler beim Löschen des Eintrags: " + e.getMessage(), "Datenbankfehler", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearFields() {
        zahlungsarten.setSelectedIndex(0);   // ComboBox zurücksetzen
        kurzbeschreibungField.setText("");   // Textfelder leeren
        betragField.setText("");
        deleteBtn.setEnabled(false);         // Löschen-Button deaktivieren
        selectedId = -1;                     // Zurücksetzen der ausgewählten ID
    }

    private void saveData() {
        String bezeichnung = bezeichnungFeld.getText().trim();
        String kurzbeschreibung = kurzbeschreibungField.getText().trim();
        String betragText = betragField.getText().trim();
        if (bezeichnung.isEmpty() || kurzbeschreibung.isEmpty() || betragText.isEmpty()) {
            JOptionPane.showMessageDialog(panel1, "Bitte füllen Sie alle Felder aus.", "Eingabefehler", JOptionPane.ERROR_MESSAGE);
            return;
        }
        double betrag;
        try {
            betrag = Double.parseDouble(betragText);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(panel1, "Der Betrag muss eine Zahl sein.", "Eingabefehler", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int kategorieId = zahlungsarten.getSelectedIndex() + 1;
        try (Connection connect = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            String sql = "INSERT INTO Buchung (Kategorie_ID, Datum, Zusatzinfo, Betrag) VALUES (?, NOW(), ?, ?)";
            try (PreparedStatement preparedStatement = connect.prepareStatement(sql)) {
                preparedStatement.setInt(1, kategorieId);
                preparedStatement.setString(2, kurzbeschreibung);
                preparedStatement.setDouble(3, betrag);
                preparedStatement.executeUpdate();
                JOptionPane.showMessageDialog(panel1, "Daten erfolgreich gespeichert.");
                updateTableData(fetchDataFromDatabase());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(panel1, "Fehler beim Speichern der Daten: " + e.getMessage(), "Datenbankfehler", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static class NonEditableTableModel extends DefaultTableModel {
        public NonEditableTableModel(Object[] columnNames, int rowCount) {
            super(columnNames, rowCount);
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    }

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
