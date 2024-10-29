// Project: Buchhaltung
// Author: Schloffer Lisa
// Date: 23.10.2024

import KontoView.KontoView;

import javax.swing.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private static Connection connect = null;

    public static void main(String[] args) {
        try {
            // GUI erstellen
            KontoView view = new KontoView();

            // Daten aus der Datenbank holen und in der GUI anzeigen
            List<Object[]> data = fetchDataFromDatabase();
            view.updateTableData(data);

            // GUI in einem JFrame anzeigen
            javax.swing.SwingUtilities.invokeLater(() -> {
                JFrame frame = new JFrame("KontoView");
                frame.setContentPane(view.panel1); // Zugriff auf panel1
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.pack();
                frame.setSize(600, 400); // Festlegen einer Standardgröße
                frame.setVisible(true);
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static List<Object[]> fetchDataFromDatabase() throws SQLException {
        List<Object[]> data = new ArrayList<>();
        try {
            // Verbindung zur MySQL-Datenbank herstellen
            connect = DriverManager.getConnection("jdbc:mysql://localhost/konten_db", "root", "");
            System.out.println("Datenbankverbindung erfolgreich."); // Debugging-Ausgabe

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

            // Ausgabe der Anzahl der geladenen Datensätze
            System.out.println("Geladene Datensätze: " + data.size());
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Verbindung schließen
            if (connect != null) {
                connect.close();
            }
        }
        return data;
    }
}
