import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class ProcurementPlanning {
    private static final Map<Integer, Integer> cart = new HashMap<>(); // Карта для хранения ID товара и количества

    public static void showProcurementWindow() {
        JFrame procurementFrame = new JFrame("Планирование закупок");
        procurementFrame.setSize(800, 600);
        procurementFrame.setLayout(null);

        JLabel label = new JLabel("Закупка товаров");
        label.setBounds(320, 10, 200, 30);
        label.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 16));

        // Таблица для отображения товаров
        JTable table = new JTable();
        DefaultTableModel tableModel = new DefaultTableModel();
        tableModel.setColumnIdentifiers(new String[]{"ID", "Название", "Категория", "Цена", "Наличие"});
        table.setModel(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(50, 100, 700, 300);

        // Загрузка данных в таблицу
        loadProductData(tableModel);

        // Поле ввода ID товара
        JLabel productIdLabel = new JLabel("ID товара:");
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
        addToCartButton.setBounds(300, 420, 150, 30);
        addToCartButton.addActionListener(e -> {
            try {
                int productId = Integer.parseInt(productIdField.getText());
                int quantity = Integer.parseInt(quantityField.getText());
                if (quantity <= 0) {
                    throw new NumberFormatException();
                }
                cart.put(productId, cart.getOrDefault(productId, 0) + quantity);
                JOptionPane.showMessageDialog(procurementFrame, "Товар добавлен в корзину!");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(procurementFrame, "Введите корректные данные!",
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Кнопка просмотра корзины
        JButton viewCartButton = new JButton("Корзина");
        viewCartButton.setBounds(300, 460, 120, 30);
        viewCartButton.addActionListener(e -> showCartContents());

        // Выбор поставщика
        JLabel supplierLabel = new JLabel("Выберите поставщика:");
        supplierLabel.setBounds(50, 500, 150, 30);

        JComboBox<String> supplierComboBox = new JComboBox<>();
        supplierComboBox.setBounds(200, 500, 200, 30);

        loadSuppliers(supplierComboBox);

        // Кнопка оформления закупки
        JButton confirmButton = new JButton("Оформить закупку");
        confirmButton.setBounds(599, 420, 150, 30);
        confirmButton.addActionListener(e -> {
            if (supplierComboBox.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(procurementFrame, "Выберите поставщика!",
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }

            processProcurement(supplierComboBox);
            loadProductData(tableModel);
        });

        JButton closeButton = new JButton("Назад");
        closeButton.setBounds(649, 500, 100, 30);
        closeButton.addActionListener(e -> procurementFrame.dispose());

        procurementFrame.add(label);
        procurementFrame.add(scrollPane);
        procurementFrame.add(productIdLabel);
        procurementFrame.add(productIdField);
        procurementFrame.add(quantityLabel);
        procurementFrame.add(quantityField);
        procurementFrame.add(addToCartButton);
        procurementFrame.add(viewCartButton);
        procurementFrame.add(supplierLabel);
        procurementFrame.add(supplierComboBox);
        procurementFrame.add(confirmButton);
        procurementFrame.add(closeButton);

        procurementFrame.setVisible(true);
    }

    private static void loadProductData(DefaultTableModel tableModel) {
        tableModel.setRowCount(0);
        String query = "SELECT Товар.ID_товара, Товар.Название, Категории.Название_категории, " +
                "Товар.Наличие, Товар.Цена_закупки " +
                "FROM Товар " +
                "JOIN Категории ON Товар.Категория = Категории.ID_категории " +
                "ORDER BY Товар.ID_товара ASC";

        try (Connection connection = DBConnector.connect();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                int id = resultSet.getInt("ID_товара");
                String name = resultSet.getString("Название");
                String categoryName = resultSet.getString("Название_категории");
                double price = resultSet.getDouble("Цена_закупки");
                int stock = resultSet.getInt("Наличие");

                tableModel.addRow(new Object[]{id, name, categoryName, price, stock});
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Ошибка при загрузке данных товаров: " + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private static void loadSuppliers(JComboBox<String> supplierComboBox) {
        supplierComboBox.removeAllItems();
        String query = "SELECT Фамилия, Имя, Отчество FROM Поставщик ORDER BY Фамилия ASC";

        try (Connection connection = DBConnector.connect();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                String fullName = resultSet.getString("Фамилия") + " " +
                        resultSet.getString("Имя") + " " +
                        resultSet.getString("Отчество");
                supplierComboBox.addItem(fullName);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Ошибка при загрузке данных поставщиков: " + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private static void processProcurement(JComboBox<String> supplierComboBox) {
        try (Connection connection = DBConnector.connect()) {
            connection.setAutoCommit(false);

            String selectedSupplier = (String) supplierComboBox.getSelectedItem();
            String[] supplierParts = selectedSupplier.split(" ");
            String lastName = supplierParts[0];
            String firstName = supplierParts[1];
            String middleName = supplierParts.length > 2 ? supplierParts[2] : "";

            String supplierQuery = "SELECT ID_поставщика FROM Поставщик WHERE Фамилия = ? AND Имя = ? AND Отчество = ?";
            try (PreparedStatement supplierStatement = connection.prepareStatement(supplierQuery)) {
                supplierStatement.setString(1, lastName);
                supplierStatement.setString(2, firstName);
                supplierStatement.setString(3, middleName);
                ResultSet supplierResult = supplierStatement.executeQuery();

                if (supplierResult.next()) {
                    int supplierId = supplierResult.getInt("ID_поставщика");

                    String insertProcurementQuery = "INSERT INTO Поставка (ID_поставщика, Дата_и_время_поставки) VALUES (?, NOW())";
                    try (PreparedStatement insertProcurementStatement = connection.prepareStatement(insertProcurementQuery, Statement.RETURN_GENERATED_KEYS)) {
                        insertProcurementStatement.setInt(1, supplierId);
                        insertProcurementStatement.executeUpdate();

                        ResultSet generatedKeys = insertProcurementStatement.getGeneratedKeys();
                        int procurementId = 0;
                        if (generatedKeys.next()) {
                            procurementId = generatedKeys.getInt(1);
                        }

                        String updateQuery = "UPDATE Товар SET Наличие = Наличие + ? WHERE ID_товара = ?";
                        try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
                            for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
                                int productId = entry.getKey();
                                int quantity = entry.getValue();

                                updateStatement.setInt(1, quantity);
                                updateStatement.setInt(2, productId);
                                updateStatement.addBatch();
                            }
                            updateStatement.executeBatch();
                        }

                        String insertSupplyContentQuery = "INSERT INTO Состав_поставки (ID_поставки, ID_товара, Количество_поставляемых_товаров) VALUES (?, ?, ?)";
                        try (PreparedStatement insertSupplyContentStatement = connection.prepareStatement(insertSupplyContentQuery)) {
                            for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
                                insertSupplyContentStatement.setInt(1, procurementId);
                                insertSupplyContentStatement.setInt(2, entry.getKey());
                                insertSupplyContentStatement.setInt(3, entry.getValue());
                                insertSupplyContentStatement.addBatch();
                            }
                            insertSupplyContentStatement.executeBatch();
                        }

                        connection.commit();
                        JOptionPane.showMessageDialog(null, "Закупка оформлена успешно!");
                        cart.clear();
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Поставщик не найден!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Ошибка при оформлении закупки: " + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private static void showCartContents() {
        JFrame cartFrame = new JFrame("Просмотр корзины");
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
        String query = "SELECT Название, Цена_закупки FROM Товар WHERE ID_товара = ?";
        try (Connection connection = DBConnector.connect();
             PreparedStatement statement = connection.prepareStatement(query)) {

            for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
                int productId = entry.getKey();
                int quantity = entry.getValue();

                statement.setInt(1, productId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        String productName = resultSet.getString("Название");
                        double purchasePrice = resultSet.getDouble("Цена_закупки");
                        double totalItemSum = purchasePrice * quantity;

                        totalSum += totalItemSum;

                        cartTableModel.addRow(new Object[]{productName, quantity, totalItemSum});
                    }
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(cartFrame, "Ошибка при загрузке данных корзины: " + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        totalSumLabel.setText("Общая сумма закупки: " + String.format("%.2f", totalSum));

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
