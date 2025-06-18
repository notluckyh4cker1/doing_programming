// Класс взаимодействия добавления продукта и БД

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AddProductConnector {
    public boolean addProduct(String name, double calories, double proteins, double fats, double carbs) {
        String sql = "INSERT INTO products (product_name, calories_per_100g, proteins_per_100g, fats_per_100g, carbs_per_100g) " +
                     "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setDouble(2, calories);
            stmt.setDouble(3, proteins);
            stmt.setDouble(4, fats);
            stmt.setDouble(5, carbs);

            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
