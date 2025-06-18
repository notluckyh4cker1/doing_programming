import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;

public class ProductInventory {
    public static void showInventoryWindow() {
        JFrame inventoryFrame = new JFrame("Учёт товаров");
        inventoryFrame.setSize(800, 600);
        inventoryFrame.setLayout(null);

        JLabel label = new JLabel("Список товаров");
        label.setBounds(320, 10, 200, 30);
        label.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 16));

        JTable table = new JTable();
        DefaultTableModel tableModel = new DefaultTableModel();
        tableModel.setColumnIdentifiers(new String[]{"ID", "Название", "Категория", "Цена", "Наличие", "Цена закупки"});
        table.setModel(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(50, 100, 700, 300);

        JLabel categoryLabel = new JLabel("Выберите категорию:");
        categoryLabel.setBounds(50, 50, 150, 30);

        JComboBox<String> categoryComboBox = new JComboBox<>(new String[]{"Все"});
        categoryComboBox.setBounds(200, 50, 150, 30);
        loadCategoriesIntoComboBox(categoryComboBox, true);

        JButton sortButton = new JButton("Показать товары");
        sortButton.setBounds(400, 50, 150, 30);
        sortButton.addActionListener(e -> {
            String selectedCategory = (String) categoryComboBox.getSelectedItem();
            loadProductData(tableModel, "Все".equals(selectedCategory) ? null : selectedCategory);
        });

        loadProductData(tableModel, null);

        JButton addButton = new JButton("Добавить товар");
        addButton.setBounds(50, 420, 150, 30);
        addButton.addActionListener(e -> addProduct(tableModel));

        JButton deleteButton = new JButton("Удалить товар");
        deleteButton.setBounds(599, 420, 150, 30);
        deleteButton.addActionListener(e -> deleteProduct(tableModel));

        JButton closeButton = new JButton("Назад");
        closeButton.setBounds(649, 520, 100, 30);
        closeButton.addActionListener(e -> inventoryFrame.dispose());

        inventoryFrame.add(label);
        inventoryFrame.add(scrollPane);
        inventoryFrame.add(categoryLabel);
        inventoryFrame.add(categoryComboBox);
        inventoryFrame.add(sortButton);
        inventoryFrame.add(addButton);
        inventoryFrame.add(deleteButton);
        inventoryFrame.add(closeButton);

        inventoryFrame.setVisible(true);
    }

    private static void loadProductData(DefaultTableModel tableModel, String categoryFilter) {
        tableModel.setRowCount(0);
        String query = "SELECT Товар.ID_товара, Товар.Название, Категории.Название_категории AS Категория, " +
                "Товар.Цена, Товар.Наличие, Товар.Цена_закупки " +
                "FROM Товар " +
                "JOIN Категории ON Товар.Категория = Категории.ID_категории " +
                (categoryFilter != null ? "WHERE Категории.Название_категории = ? " : "") +
                "ORDER BY Товар.ID_товара ASC";

        try (Connection connection = DBConnector.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            if (categoryFilter != null) {
                preparedStatement.setString(1, categoryFilter);
            }

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                tableModel.addRow(new Object[]{
                        resultSet.getInt("ID_товара"),
                        resultSet.getString("Название"),
                        resultSet.getString("Категория"),
                        resultSet.getDouble("Цена"),
                        resultSet.getInt("Наличие"),
                        resultSet.getDouble("Цена_закупки")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Ошибка при загрузке данных: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void addProduct(DefaultTableModel tableModel) {
        JComboBox<String> categoryComboBox = new JComboBox<>();
        loadCategoriesIntoComboBox(categoryComboBox, false);
        categoryComboBox.addItem("Добавить новую категорию...");

        JTextField nameField = new JTextField();
        JTextField priceField = new JTextField();
        JTextField stockField = new JTextField();
        JTextField purchasePriceField = new JTextField();
        JTextField newCategoryField = new JTextField();
        newCategoryField.setEnabled(false);

        categoryComboBox.addActionListener(e -> newCategoryField.setEnabled("Добавить новую категорию...".equals(categoryComboBox.getSelectedItem())));

        Object[] message = {
                "Название товара:", nameField,
                "Категория:", categoryComboBox,
                "Новая категория:", newCategoryField,
                "Цена:", priceField,
                "Наличие:", stockField,
                "Цена закупки:", purchasePriceField
        };

        int option = JOptionPane.showConfirmDialog(null, message, "Добавить товар", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            String name = nameField.getText();
            String category;

            if ("Добавить новую категорию...".equals(categoryComboBox.getSelectedItem())) {
                category = newCategoryField.getText();
                if (category.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Введите новую категорию!", "Ошибка", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                addCategory(category);
            } else {
                category = (String) categoryComboBox.getSelectedItem();
            }

            double price = Double.parseDouble(priceField.getText());
            int stock = Integer.parseInt(stockField.getText());
            double purchasePrice = Double.parseDouble(purchasePriceField.getText());

            try (Connection connection = DBConnector.connect()) {
                String categoryQuery = "SELECT ID_категории FROM Категории WHERE Название_категории = ?";
                PreparedStatement categoryStatement = connection.prepareStatement(categoryQuery);
                categoryStatement.setString(1, category);
                ResultSet categoryResultSet = categoryStatement.executeQuery();

                int categoryId;
                if (categoryResultSet.next()) {
                    categoryId = categoryResultSet.getInt("ID_категории");
                } else {
                    throw new SQLException("Категория не найдена.");
                }

                String query = "INSERT INTO Товар (Название, Категория, Цена, Наличие, Цена_закупки) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, name);
                preparedStatement.setInt(2, categoryId);
                preparedStatement.setDouble(3, price);
                preparedStatement.setInt(4, stock);
                preparedStatement.setDouble(5, purchasePrice);
                preparedStatement.executeUpdate();

                JOptionPane.showMessageDialog(null, "Товар успешно добавлен!", "Успех", JOptionPane.INFORMATION_MESSAGE);
                loadProductData(tableModel, null);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Ошибка при добавлении товара: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static void loadCategoriesIntoComboBox(JComboBox<String> comboBox, boolean includeAll) {
        comboBox.removeAllItems();
        if (includeAll) {
            comboBox.addItem("Все"); // Добавляем категорию "Все" только если это необходимо.
        }
        String query = "SELECT Название_категории FROM Категории ORDER BY Название_категории ASC";

        try (Connection connection = DBConnector.connect();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                comboBox.addItem(resultSet.getString("Название_категории"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Ошибка при загрузке категорий: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void addCategory(String category) {
        String query = "INSERT INTO Категории (Название_категории) VALUES (?)";

        try (Connection connection = DBConnector.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, category);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Ошибка при добавлении новой категории: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void deleteProduct(DefaultTableModel tableModel) {
        JTextField idField = new JTextField();

        Object[] message = {"Введите ID товара для удаления:", idField};

        int option = JOptionPane.showConfirmDialog(null, message, "Удаление товара", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            try {
                int productId = Integer.parseInt(idField.getText());

                int confirmation = JOptionPane.showConfirmDialog(null,
                        "Вы уверены, что хотите удалить товар с ID = " + productId + "?",
                        "Удаление товара", JOptionPane.YES_NO_OPTION);

                if (confirmation == JOptionPane.YES_OPTION) {
                    try (Connection connection = DBConnector.connect()) {
                        // Получаем ID категории товара
                        String getCategoryQuery = "SELECT Категория FROM Товар WHERE ID_товара = ?";
                        PreparedStatement getCategoryStmt = connection.prepareStatement(getCategoryQuery);
                        getCategoryStmt.setInt(1, productId);
                        ResultSet categoryResult = getCategoryStmt.executeQuery();

                        if (categoryResult.next()) {
                            int categoryId = categoryResult.getInt("Категория");

                            // Удаляем товар
                            String deleteProductQuery = "DELETE FROM Товар WHERE ID_товара = ?";
                            PreparedStatement deleteProductStmt = connection.prepareStatement(deleteProductQuery);
                            deleteProductStmt.setInt(1, productId);
                            int rowsAffected = deleteProductStmt.executeUpdate();

                            if (rowsAffected > 0) {
                                // Проверяем, остались ли товары в этой категории
                                String checkCategoryQuery = "SELECT COUNT(*) AS count FROM Товар WHERE Категория = ?";
                                PreparedStatement checkCategoryStmt = connection.prepareStatement(checkCategoryQuery);
                                checkCategoryStmt.setInt(1, categoryId);
                                ResultSet checkResult = checkCategoryStmt.executeQuery();

                                if (checkResult.next() && checkResult.getInt("count") == 0) {
                                    // Если товаров больше нет, удаляем категорию
                                    String deleteCategoryQuery = "DELETE FROM Категории WHERE ID_категории = ?";
                                    PreparedStatement deleteCategoryStmt = connection.prepareStatement(deleteCategoryQuery);
                                    deleteCategoryStmt.setInt(1, categoryId);
                                    deleteCategoryStmt.executeUpdate();
                                }

                                JOptionPane.showMessageDialog(null, "Товар успешно удалён!", "Успех", JOptionPane.INFORMATION_MESSAGE);
                                loadProductData(tableModel, null);
                            } else {
                                JOptionPane.showMessageDialog(null, "Товар с таким ID не найден.", "Ошибка", JOptionPane.WARNING_MESSAGE);
                            }
                        } else {
                            JOptionPane.showMessageDialog(null, "Товар с таким ID не найден.", "Ошибка", JOptionPane.WARNING_MESSAGE);
                        }
                    }
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Введите корректный ID товара.", "Ошибка", JOptionPane.WARNING_MESSAGE);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Ошибка при удалении товара: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
