// Project: Buchhaltung
// Author: Schloffer Lisa
// Date: 23.10.2024

import java.sql.*;

public class Main {

    private static Connection connect = null;
    private static Statement statement = null;
    private static PreparedStatement preparedStatement = null;
    private static ResultSet resultSet = null;

    public static void main(String[] args) throws Exception {
        try {
            // This will load the MySQL driver, each DB has its own driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Setup the connection with the DB
            connect = DriverManager
                    .getConnection("jdbc:mysql://localhost/konten_db", "root", "");

            // Fetching data from kostenkategorie table
            System.out.println("Kostenkategorien:");
            statement = connect.createStatement();
            resultSet = statement.executeQuery("SELECT * FROM kostenkategorie");
            writeKostenkategorieResultSet(resultSet);

            // Fetching data from Buchung table
            System.out.println("\nBuchungen:");
            resultSet = statement.executeQuery("SELECT * FROM Buchung");
            writeBuchungResultSet(resultSet);

        } catch (Exception e) {
            throw e;
        } finally {
            close();
        }
    }

    private static void writeKostenkategorieResultSet(ResultSet resultSet) throws SQLException {
        // ResultSet for kostenkategorie table
        while (resultSet.next()) {
            int id = resultSet.getInt("id");
            String bezeichnung = resultSet.getString("bezeichnung");
            String kurzbeschreibung = resultSet.getString("kurzbeschreibung");
            int ein_ausgabe = resultSet.getInt("ein_ausgabe");

            System.out.println("ID: " + id);
            System.out.println("bezeichnung: " + bezeichnung);
            System.out.println("kurzbeschreibung: " + kurzbeschreibung);
            System.out.println("Ein/Ausgabe: " + (ein_ausgabe == 1 ? "Eingabe" : "Ausgabe"));
            System.out.println("------------------");
        }
    }

    private static void writeBuchungResultSet(ResultSet resultSet) throws SQLException {
        // ResultSet for Buchung table
        while (resultSet.next()) {
            int id = resultSet.getInt("id");
            int kategorieId = resultSet.getInt("kategorie_id");
            Timestamp datum = resultSet.getTimestamp("datum");
            String zusatzinfo = resultSet.getString("zusatzinfo");
            double betrag = resultSet.getDouble("Betrag");

            System.out.println("Buchungs-ID: " + id);
            System.out.println("Kategorie-ID: " + kategorieId);
            System.out.println("Datum: " + datum);
            System.out.println("Zusatzinfo: " + zusatzinfo);
            System.out.println("Betrag: " + betrag);
            System.out.println("------------------");
        }
    }

    // You need to close the resultSet
    private static void close() {
        try {
            if (resultSet != null) {
                resultSet.close();
            }

            if (statement != null) {
                statement.close();
            }

            if (connect != null) {
                connect.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
