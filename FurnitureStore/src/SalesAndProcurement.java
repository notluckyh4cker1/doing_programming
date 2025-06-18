import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ItemEvent;
import java.sql.*;

public class SalesAndProcurement {

    public static void showSalesAndProcurementMenu() {
        JFrame frame = new JFrame("Отслеживание продаж и закупок");
        frame.setSize(900, 500);
        frame.setLayout(null);

        JLabel label = new JLabel("Отслеживание данных:");
        label.setBounds(50, 20, 200, 30);
        label.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 16));

        // Выпадающий список для выбора режима
        String[] modes = {"Закупки", "Продажи"};
        JComboBox<String> modeSelector = new JComboBox<>(modes);
        modeSelector.setBounds(250, 20, 150, 30);

        // Таблица для отображения данных
        JTable table = new JTable();
        DefaultTableModel tableModel = new DefaultTableModel();
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(50, 80, 800, 300);
        frame.add(scrollPane);

        // Кнопка выхода
        JButton backButton = new JButton("Назад");
        backButton.setBounds(700, 400, 150, 40);
        backButton.addActionListener(e -> frame.dispose());

        // Слушатель для изменения режима
        modeSelector.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String selectedMode = (String) modeSelector.getSelectedItem();
                if ("Закупки".equals(selectedMode)) {
                    loadProcurementData(tableModel);
                } else {
                    loadSalesData(tableModel);
                }
            }
        });

        // Инициализация таблицы данными по умолчанию (закупки)
        tableModel.setColumnIdentifiers(new String[]{"ID поставки", "ФИО поставщика", "Название товара", "Количество", "Дата и время поставки"});
        table.setModel(tableModel);
        loadProcurementData(tableModel);

        frame.add(label);
        frame.add(modeSelector);
        frame.add(backButton);
        frame.setVisible(true);
    }

    // Метод для загрузки данных о закупках
    private static void loadProcurementData(DefaultTableModel tableModel) {
        tableModel.setRowCount(0); // Очистка таблицы
        tableModel.setColumnIdentifiers(new String[]{
                "ID поставки", "ФИО поставщика", "Название товара", "Количество", "Стоимость", "Дата и время поставки"
        });

        String query = "SELECT Поставка.ID_поставки, " +
                "CONCAT(Поставщик.Фамилия, ' ', Поставщик.Имя, ' ', Поставщик.Отчество) AS ФИО_поставщика, " +
                "Товар.Название, Состав_поставки.Количество_поставляемых_товаров, " +
                "ROUND(Состав_поставки.Количество_поставляемых_товаров * Товар.Цена_закупки, 2) AS Стоимость, " +
                "TO_CHAR(Поставка.Дата_и_время_поставки, 'YYYY-MM-DD HH24:MI:SS') AS Дата_и_время_поставки " +
                "FROM Состав_поставки " +
                "JOIN Поставка ON Состав_поставки.ID_поставки = Поставка.ID_поставки " +
                "JOIN Товар ON Состав_поставки.ID_товара = Товар.ID_товара " +
                "JOIN Поставщик ON Поставка.ID_поставщика = Поставщик.ID_поставщика " +
                "ORDER BY Поставка.Дата_и_время_поставки ASC";

        try (Connection connection = DBConnector.connect();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                int supplyId = resultSet.getInt("ID_поставки");
                String supplierName = resultSet.getString("ФИО_поставщика");
                String productName = resultSet.getString("Название");
                int quantity = resultSet.getInt("Количество_поставляемых_товаров");
                double cost = resultSet.getDouble("Стоимость");
                String supplyDateTime = resultSet.getString("Дата_и_время_поставки");

                tableModel.addRow(new Object[]{supplyId, supplierName, productName, quantity, cost, supplyDateTime});
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Ошибка при загрузке данных о закупках: " + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // Метод для загрузки данных о продажах
    private static void loadSalesData(DefaultTableModel tableModel) {
        tableModel.setRowCount(0); // Очистка таблицы
        tableModel.setColumnIdentifiers(new String[]{"ФИО клиента", "Название товара", "Количество", "Дата продажи"});

        String query = "SELECT CONCAT(Клиент.Фамилия, ' ', Клиент.Имя, ' ', Клиент.Отчество) AS ФИО_клиента, " +
                "Товар.Название, Состав_продажи.Количество_покупаемых_товаров AS Количество, " +
                "TO_CHAR(Продажа.Дата_и_время_продажи, 'YYYY-MM-DD HH24:MI:SS') AS Дата_продажи " +
                "FROM Продажа " +
                "JOIN Клиент ON Продажа.ID_клиента = Клиент.ID_клиента " +
                "JOIN Состав_продажи ON Продажа.ID_продажи = Состав_продажи.ID_продажи " +
                "JOIN Товар ON Состав_продажи.ID_товара = Товар.ID_товара " +
                "ORDER BY Продажа.Дата_и_время_продажи ASC";

        try (Connection connection = DBConnector.connect();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                String clientName = resultSet.getString("ФИО_клиента");
                String productName = resultSet.getString("Название");
                int quantity = resultSet.getInt("Количество");
                Timestamp saleDateTime = resultSet.getTimestamp("Дата_продажи");

                tableModel.addRow(new Object[]{clientName, productName, quantity, saleDateTime});
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Ошибка при загрузке данных о продажах: " + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}
