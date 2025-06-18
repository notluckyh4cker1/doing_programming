import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class FurnitureOrder {
    private static final Map<Integer, Integer> cart = new HashMap<>(); // Карта для хранения ID товара и количества

    public static void showFurnitureOrderMenu() {
        JFrame orderFrame = new JFrame("Заказ мебели");
        orderFrame.setSize(800, 600);
        orderFrame.setLayout(null);

        JLabel label = new JLabel("Выберите мебель для заказа");
        label.setBounds(250, 10, 300, 30);
        label.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 16));

        // Выпадающий список категорий
        JLabel categoryLabel = new JLabel("Категория:");
        categoryLabel.setBounds(50, 50, 100, 30);
        JComboBox<String> categoryComboBox = new JComboBox<>();
        categoryComboBox.setBounds(150, 50, 200, 30);

        // Таблица для отображения мебели
        JTable table = new JTable();
        DefaultTableModel tableModel = new DefaultTableModel();
        tableModel.setColumnIdentifiers(new String[]{"ID", "Название", "Категория", "Цена", "Наличие"});
        table.setModel(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(50, 100, 700, 300);

        // Загрузка категорий в выпадающий список
        loadCategories(categoryComboBox);

        // Загрузка данных в таблицу при выборе категории
        categoryComboBox.addActionListener(e -> {
            String selectedCategory = (String) categoryComboBox.getSelectedItem();
            loadFurnitureData(tableModel, selectedCategory);
        });

        // Поле ввода ID мебели
        JLabel productIdLabel = new JLabel("ID мебели:");
        productIdLabel.setBounds(50, 420, 100, 30);
        JTextField productIdField = new JTextField();
        productIdField.setBounds(150, 420, 100, 30);

        // Поле ввода количества
        JLabel quantityLabel = new JLabel("Количество:");
        quantityLabel.setBounds(50, 460, 100, 30);
        JTextField quantityField = new JTextField();
        quantityField.setBounds(150, 460, 100, 30);

        // Кнопка добавления в корзину
        JButton addToCartButton = new JButton("Добавить в корзину");
        addToCartButton.setBounds(50, 500, 150, 30);
        addToCartButton.addActionListener(e -> {
            try {
                int productId = Integer.parseInt(productIdField.getText());
                int quantity = Integer.parseInt(quantityField.getText());
                cart.put(productId, cart.getOrDefault(productId, 0) + quantity);
                JOptionPane.showMessageDialog(orderFrame, "Мебель добавлена в корзину!");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(orderFrame, "Введите корректные данные!",
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Кнопка просмотра корзины
        JButton viewCartButton = new JButton("Корзина");
        viewCartButton.setBounds(200, 500, 120, 30);
        viewCartButton.addActionListener(e -> showCartContents());

        // Кнопка оформления заказа
        JButton confirmButton = new JButton("Оформить заказ");
        confirmButton.setBounds(549, 420, 200, 30);
        confirmButton.addActionListener(e -> {
            processOrder();
            String selectedCategory = (String) categoryComboBox.getSelectedItem();
            loadFurnitureData(tableModel, selectedCategory); // Обновляем данные в таблице
        });

        // Кнопка закрытия
        JButton closeButton = new JButton("Назад");
        closeButton.setBounds(649, 500, 100, 30);
        closeButton.addActionListener(e -> orderFrame.dispose());

        orderFrame.add(label);
        orderFrame.add(categoryLabel);
        orderFrame.add(categoryComboBox);
        orderFrame.add(scrollPane);
        orderFrame.add(productIdLabel);
        orderFrame.add(productIdField);
        orderFrame.add(quantityLabel);
        orderFrame.add(quantityField);
        orderFrame.add(addToCartButton);
        orderFrame.add(viewCartButton);
        orderFrame.add(confirmButton);
        orderFrame.add(closeButton);

        // Загружаем начальные данные для первой категории
        if (categoryComboBox.getItemCount() > 0) {
            categoryComboBox.setSelectedIndex(0);
        }

        orderFrame.setVisible(true);
    }

    private static void loadCategories(JComboBox<String> categoryComboBox) {
        categoryComboBox.removeAllItems();
        categoryComboBox.addItem("Все"); // Добавляем опцию "Все"

        String query = "SELECT Название_категории FROM Категории"; // Исправьте на корректное имя столбца

        try (Connection connection = DBConnector.connect();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                String category = resultSet.getString("Название_категории"); // Используйте актуальное имя столбца
                categoryComboBox.addItem(category);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Ошибка при загрузке категорий: " + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private static void loadFurnitureData(DefaultTableModel tableModel, String category) {
        tableModel.setRowCount(0); // Очищаем таблицу

        String query;
        if ("Все".equals(category)) {
            query = """
            SELECT Товар.ID_товара, Товар.Название, Категории.Название_категории AS Категория, 
                   Товар.Наличие, Товар.Цена
            FROM Товар
            JOIN Категории ON Товар.Категория = Категории.ID_категории
            ORDER BY Товар.ID_товара ASC
            """;
        } else {
            query = """
            SELECT Товар.ID_товара, Товар.Название, Категории.Название_категории AS Категория, 
                   Товар.Наличие, Товар.Цена
            FROM Товар
            JOIN Категории ON Товар.Категория = Категории.ID_категории
            WHERE Категории.Название_категории = ?  -- Используем правильное имя столбца
            ORDER BY Товар.ID_товара ASC
            """;
        }

        try (Connection connection = DBConnector.connect();
             PreparedStatement statement = connection.prepareStatement(query)) {

            if (!"Все".equals(category)) {
                statement.setString(1, category);
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int id = resultSet.getInt("ID_товара");
                    String name = resultSet.getString("Название");
                    String categoryName = resultSet.getString("Категория");
                    double price = resultSet.getDouble("Цена");
                    int stock = resultSet.getInt("Наличие");

                    tableModel.addRow(new Object[]{id, name, categoryName, price, stock});
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Ошибка при загрузке данных мебели: " + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private static void processOrder() {
        Connection connection = null; // Объявляем переменную connection заранее
        try {
            connection = DBConnector.connect(); // Получаем соединение
            connection.setAutoCommit(false); // Отключаем автокоммит для выполнения транзакции

            // 1. Создаем запись в таблице "Продажа"
            String insertSaleQuery = "INSERT INTO Продажа (ID_клиента, Дата_и_время_продажи) VALUES (?, ?) RETURNING ID_продажи";
            int saleId = -1;

            try (PreparedStatement insertSaleStatement = connection.prepareStatement(insertSaleQuery)) {
                insertSaleStatement.setInt(1, 1); // Здесь 1 - это ID клиента (для примера, замените на реальный ID клиента)
                insertSaleStatement.setTimestamp(2, new Timestamp(System.currentTimeMillis())); // Текущая дата и время
                ResultSet resultSet = insertSaleStatement.executeQuery();

                if (resultSet.next()) {
                    saleId = resultSet.getInt("ID_продажи");
                }
            }

            if (saleId == -1) {
                throw new SQLException("Не удалось создать запись о продаже.");
            }

            // 2. Добавляем записи в таблицу "Состав_продажи"
            String insertSaleItemQuery = "INSERT INTO Состав_продажи (ID_продажи, ID_товара, Количество_покупаемых_товаров) VALUES (?, ?, ?)";

            try (PreparedStatement insertSaleItemStatement = connection.prepareStatement(insertSaleItemQuery)) {
                for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
                    int productId = entry.getKey();
                    int quantity = entry.getValue();

                    insertSaleItemStatement.setInt(1, saleId); // Используем ID продажи
                    insertSaleItemStatement.setInt(2, productId); // ID товара
                    insertSaleItemStatement.setInt(3, quantity); // Количество

                    insertSaleItemStatement.addBatch(); // Добавляем в пакет
                }

                insertSaleItemStatement.executeBatch(); // Выполняем пакет
            }

            // 3. Обновляем таблицу "Товар" для уменьшения количества на складе
            String updateStockQuery = "UPDATE Товар SET Наличие = Наличие - ? WHERE ID_товара = ? AND Наличие >= ?";
            try (PreparedStatement updateStockStatement = connection.prepareStatement(updateStockQuery)) {
                for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
                    int productId = entry.getKey();
                    int quantity = entry.getValue();

                    updateStockStatement.setInt(1, quantity); // Количество товаров для уменьшения
                    updateStockStatement.setInt(2, productId); // ID товара
                    updateStockStatement.setInt(3, quantity); // Проверка, что товаров достаточно на складе

                    updateStockStatement.addBatch(); // Добавляем в пакет
                }

                updateStockStatement.executeBatch(); // Выполняем пакет
            }

            // Подтверждаем транзакцию
            connection.commit();
            JOptionPane.showMessageDialog(null, "Заказ оформлен успешно!");
            cart.clear(); // Очищаем корзину
        } catch (SQLException e) {
            try {
                if (connection != null) {
                    connection.rollback(); // Если произошла ошибка, откатываем транзакцию
                }
            } catch (SQLException rollbackEx) {
                JOptionPane.showMessageDialog(null, "Ошибка при откате транзакции: " + rollbackEx.getMessage(),
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
                rollbackEx.printStackTrace();
            }
            JOptionPane.showMessageDialog(null, "Ошибка при оформлении заказа: " + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } finally {
            // Закрытие соединения после завершения операции
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(null, "Ошибка при закрытии соединения: " + e.getMessage(),
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            }
        }
    }

    private static void showCartContents() {
        JFrame cartFrame = new JFrame("Корзина");
        cartFrame.setSize(600, 400);
        cartFrame.setLayout(null);

        JLabel label = new JLabel("Корзина товаров");
        label.setBounds(200, 10, 200, 30);
        label.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 16));

        JTable cartTable = new JTable();
        DefaultTableModel cartTableModel = new DefaultTableModel();
        cartTableModel.setColumnIdentifiers(new String[]{"Название", "Количество", "Сумма"});
        cartTable.setModel(cartTableModel);
        JScrollPane scrollPane = new JScrollPane(cartTable);
        scrollPane.setBounds(50, 50, 500, 200);

        JLabel totalSumLabel = new JLabel("Общая сумма: ");
        totalSumLabel.setBounds(50, 270, 400, 30);
        totalSumLabel.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 14));

        double totalSum = 0;

        String query = "SELECT Название, Цена FROM Товар WHERE ID_товара = ?";
        try (Connection connection = DBConnector.connect();
             PreparedStatement statement = connection.prepareStatement(query)) {

            for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
                int productId = entry.getKey();
                int quantity = entry.getValue();

                statement.setInt(1, productId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        String productName = resultSet.getString("Название");
                        double price = resultSet.getDouble("Цена");
                        double totalItemSum = price * quantity;

                        totalSum += totalItemSum;
                        cartTableModel.addRow(new Object[]{productName, quantity, totalItemSum});
                    }
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(cartFrame, "Ошибка при загрузке корзины: " + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        totalSumLabel.setText("Общая сумма заказа: " + String.format("%.2f", totalSum));

        JButton closeButton = new JButton("Закрыть");
        closeButton.setBounds(400, 310, 100, 30);
        closeButton.addActionListener(e -> cartFrame.dispose());

        cartFrame.add(label);
        cartFrame.add(scrollPane);
        cartFrame.add(totalSumLabel);
        cartFrame.add(closeButton);

        cartFrame.setVisible(true);
    }
}
