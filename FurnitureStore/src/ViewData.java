import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ViewData {
    public static void showDataWindow() {
        JFrame dataFrame = new JFrame("Данные пользователей");
        dataFrame.setSize(600, 400);
        dataFrame.setLayout(new BorderLayout());

        // Создаем текстовую область для отображения данных
        JTextArea dataArea = new JTextArea();
        dataArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(dataArea);
        dataFrame.add(scrollPane, BorderLayout.CENTER);

        // Кнопка для обновления данных
        JButton refreshButton = new JButton("Обновить");
        refreshButton.addActionListener(e -> {
            StringBuilder dataText = new StringBuilder("Данные пользователей:\n");
            int index = 1;

            // Вывод данных администраторов
            dataText.append("Администраторы:\n");
            for (User user : DataStorage.getAdminData().values()) {
                dataText.append(index++).append(". ")
                        .append(user.getLastName()).append(" ")
                        .append(user.getFirstName()).append(" ")
                        .append(user.getMiddleName()).append(", Телефон: ")
                        .append(user.getPhone()).append("\n");
            }

            // Вывод данных клиентов
            dataText.append("\nКлиенты:\n");
            index = 1;
            for (User user : DataStorage.getClientData().values()) {
                dataText.append(index++).append(". ")
                        .append(user.getLastName()).append(" ")
                        .append(user.getFirstName()).append(" ")
                        .append(user.getMiddleName()).append(", Телефон: ")
                        .append(user.getPhone()).append("\n");
            }

            dataArea.setText(dataText.toString());
        });

        // Кнопка для удаления пользователя
        JButton deleteButton = new JButton("Удалить");
        deleteButton.addActionListener(e -> {
            String phoneToDelete = JOptionPane.showInputDialog(dataFrame, "Введите телефон для удаления:");

            if (phoneToDelete != null && !phoneToDelete.isEmpty()) {
                boolean removedFromMemory = false;

                // Удаляем из списка администраторов
                if (DataStorage.getAdminData().remove(phoneToDelete) != null) {
                    DataStorage.saveAdminData(); // Перезаписываем adminData.txt
                    removedFromMemory = true;
                }

                // Удаляем из списка клиентов
                if (DataStorage.getClientData().remove(phoneToDelete) != null) {
                    DataStorage.saveClientData(); // Перезаписываем clientData.txt
                    removedFromMemory = true;
                }

                // Удаляем из базы данных
                boolean removedFromDatabase = deleteClientFromDatabase(phoneToDelete);

                if (removedFromMemory || removedFromDatabase) {
                    JOptionPane.showMessageDialog(dataFrame, "Пользователь с телефоном " + phoneToDelete + " успешно удален.");
                } else {
                    JOptionPane.showMessageDialog(dataFrame, "Пользователь с таким телефоном не найден.");
                }
            }
        });

        // Кнопка для закрытия окна
        JButton closeButton = new JButton("Назад");
        closeButton.addActionListener(e -> dataFrame.dispose());

        // Панель кнопок
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(refreshButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(closeButton);

        dataFrame.add(buttonPanel, BorderLayout.SOUTH);
        dataFrame.setVisible(true);

        // Автоматическое обновление данных при открытии окна
        refreshButton.doClick();
    }

    private static boolean deleteClientFromDatabase(String phone) {
        String query = "DELETE FROM Клиент WHERE Номер_телефона = ?";
        try (Connection connection = DBConnector.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, phone);
            int rowsAffected = preparedStatement.executeUpdate();

            return rowsAffected > 0; // Возвращаем true, если удалена хотя бы одна строка
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Ошибка при удалении из базы данных: " + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
}
