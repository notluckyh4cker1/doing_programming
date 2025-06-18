// Устанавливаем соединение с базой данных (БД)

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector {
    public static Connection connect() {
        // Эти три поля менять в случае если у вас другие данные
        String user = "postgres";
        String pwd = "postgresql";
        String dbUrl = "jdbc:postgresql://localhost:5432/fittrack";
        try {
            Connection conn = DriverManager.getConnection(dbUrl, user, pwd);
            System.out.println("Successfully connected to database!");
            return conn;
        } catch (SQLException e) {
            System.out.println("Error! The connection to database is failed.");
            e.printStackTrace();
            return null;
        }
    }
}