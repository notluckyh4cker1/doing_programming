import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.util.Vector;

public class MyOrders {

    public static void showOrdersMenu() {
        // Создаем новое окно для отображения заказов
        JFrame ordersFrame = new JFrame("Мои заказы");
        ordersFrame.setSize(800, 600);
        ordersFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        ordersFrame.setLayout(null);

        // Извлекаем данные о клиенте и его заказах
        String clientName = "";
        DefaultTableModel tableModel = new DefaultTableModel();

        // Устанавливаем имена столбцов
        tableModel.addColumn("ID заказа");
        tableModel.addColumn("Название товара");
        tableModel.addColumn("Количество");
        tableModel.addColumn("Стоимость");
        tableModel.addColumn("Дата и время продажи");

        // Получаем информацию о клиенте (ФИО) и его заказах
        try (Connection connection = DBConnector.connect()) {
            // Предположим, что ID клиента = 1 (можно заменить на реальный ID клиента)
            int clientId = 1;

            // Получаем ФИО клиента
            String clientQuery = "SELECT CONCAT(Фамилия, ' ', Имя, ' ', COALESCE(Отчество, '')) AS \"ФИО клиента\" FROM Клиент WHERE ID_клиента = ?";
            try (PreparedStatement clientStmt = connection.prepareStatement(clientQuery)) {
                clientStmt.setInt(1, clientId);
                ResultSet clientResult = clientStmt.executeQuery();
                if (clientResult.next()) {
                    clientName = clientResult.getString("ФИО клиента");
                }
            }

            // Получаем заказы клиента
            String ordersQuery = "SELECT p.ID_продажи AS \"ID заказа\", t.Название AS \"Название товара\", " +
                    "sp.Количество_покупаемых_товаров AS \"Количество\", " +
                    "(sp.Количество_покупаемых_товаров * t.Цена) AS \"Стоимость\", " +
                    "p.Дата_и_время_продажи AS \"Дата и время продажи\" " +
                    "FROM Продажа p " +
                    "JOIN Состав_продажи sp ON p.ID_продажи = sp.ID_продажи " +
                    "JOIN Товар t ON sp.ID_товара = t.ID_товара " +
                    "WHERE p.ID_клиента = ?";
            try (PreparedStatement ordersStmt = connection.prepareStatement(ordersQuery)) {
                ordersStmt.setInt(1, clientId);
                ResultSet ordersResult = ordersStmt.executeQuery();

                while (ordersResult.next()) {
                    // Добавляем данные о заказе в таблицу
                    Object[] row = new Object[5];
                    row[0] = ordersResult.getInt("ID заказа");
                    row[1] = ordersResult.getString("Название товара");
                    row[2] = ordersResult.getInt("Количество");
                    row[3] = ordersResult.getDouble("Стоимость");
                    row[4] = ordersResult.getTimestamp("Дата и время продажи").toString().substring(0, 19); // Убираем миллисекунды

                    tableModel.addRow(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Ошибка при извлечении данных о заказах: " + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }

        // Создаем таблицу для отображения заказов
        JTable ordersTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(ordersTable);
        scrollPane.setBounds(50, 150, 700, 300);

        // Создаем метку с ФИО клиента
        JLabel clientLabel = new JLabel("Клиент: " + clientName);
        clientLabel.setBounds(50, 50, 400, 50);
        clientLabel.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 16));

        // Кнопка закрытия окна
        JButton closeButton = new JButton("Закрыть");
        closeButton.setBounds(350, 500, 100, 50);
        closeButton.addActionListener(e -> ordersFrame.dispose());

        // Добавляем элементы в окно
        ordersFrame.add(clientLabel);
        ordersFrame.add(scrollPane);
        ordersFrame.add(closeButton);
        ordersFrame.setVisible(true);
    }
}
