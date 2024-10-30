package KontoView;

import javax.swing.*;
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

    private static final String DB_URL = "jdbc:mysql://localhost/konten_db";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public KontoView() {
        initializeComponents();
        updateTableData(fetchDataFromDatabase());
        eingabe_oder_ausgabe.setText("Wählen Sie eine Option:");
    }

    private void initializeComponents() {
        initializeTable();
        initializeCategoryComboBox();  // Initialisiert die Kategorien in der ComboBox zahlungsarten

        saveBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveData();
            }
        });
    }

    private void initializeTable() {
        ein_ausgabe.setModel(new NonEditableTableModel(new Object[]{"ID", "Kategorie", "Datum", "Zusatzinfo", "Betrag"}, 0));
        ein_ausgabe.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    private void initializeCategoryComboBox() {
        String[] kategorien = {"Miete", "Gehalt", "Stromkosten", "Einnahmen aus Verkäufen", "Wartung", "Beratung", "Zinsen", "Werbung", "Steuererstattung", "Softwarelizenzen"};
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

        int kategorieId = zahlungsarten.getSelectedIndex() + 1;  // Auswahl in eine Kategorie-ID umwandeln

        try (Connection connect = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            String sql = "INSERT INTO Buchung (kategorie_id, datum, zusatzinfo, betrag) VALUES (?, NOW(), ?, ?)";
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
