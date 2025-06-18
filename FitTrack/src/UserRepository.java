// Класс для заполнения профиля пользователя данными (с помощью UserProfile)

import java.sql.*;
import java.time.LocalDate;

public class UserRepository {

    public static UserProfile getUserProfileById(int userId) {
        try (Connection conn = DatabaseConnector.connect()) {
            String query = "SELECT first_name, last_name, birth_date, gender, height, weight, target_weight FROM users WHERE user_id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                java.sql.Date sqlDate = rs.getDate("birth_date");
                LocalDate birthDate = sqlDate != null ? sqlDate.toLocalDate() : null;
                String gender = rs.getString("gender");
                int height = rs.getInt("height");
                int weight = rs.getInt("weight");
                int targetWeight = rs.getInt("target_weight");

                return new UserProfile(userId, firstName, lastName, birthDate, gender, height, weight, targetWeight);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
