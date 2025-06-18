import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnector {
    private static final String URL = "jdbc:postgresql://localhost:5432/furniture_store"; // URL базы данных
    private static final String USER = "postgres"; // Пользователь
    private static final String PASSWORD = "postgresql"; // Пароль

    public static Connection connect() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Успешное подключение к базе данных!");
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Ошибка подключения к базе данных.");
        }
        return connection;
    }
}
