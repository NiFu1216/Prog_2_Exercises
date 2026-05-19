package at.ac.fhcampuswien.utils;

import java.sql.*;

public class DatabaseUtil {

    private static final String JDBC_URL = "jdbc:h2:~/MovieDB";
    private static final String USER = "user";
    private static final String PASSWORD = "pw";

    public static void initializeDatabase() {

        try (Connection connection = getConnection()) {
            // Create table
            String createTableStmt = """
                CREATE TABLE IF NOT EXISTS movies (
                    id UUID PRIMARY KEY,
                    title VARCHAR(255) NOT NULL,
                    genre VARCHAR(100) NOT NULL,
                    release_year INT NOT NULL
                );
                """;

            try (PreparedStatement preparedStatement = connection.prepareStatement(createTableStmt)) {
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
    }

}
