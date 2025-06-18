import javax.swing.*;
import java.sql.Connection;

public class Main {
    private static JFrame frame;

    public static void main(String[] args) {
        // Подключение к базе данных PostgreSQL
        Connection connection = DBConnector.connect();
        if (connection != null) {
            System.out.println("Соединение установлено.");
        } else {
            System.out.println("Не удалось подключиться к базе данных.");
        }
        // Вывод главного меню
        showMainMenu();
    }

    public static void showMainMenu() {
        // Создаем главное окно
        frame = new JFrame("Мебельный магазин 'Furniture4U'");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(null);

        // Создаем текст (заголовок)
        JLabel label = new JLabel("Мебельный магазин 'Furniture4U'");
        label.setBounds(270, 40, 400, 200);
        label.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 16));

        JLabel welcome = new JLabel("Добро пожаловать!");
        welcome.setBounds(325, 60, 400, 200);
        welcome.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 16));

        // Кнопка для регистрации
        JButton registerButton = new JButton("Регистрация");
        registerButton.setBounds(250, 200, 300, 50);
        registerButton.addActionListener(e -> Registration.showRoleMenu(frame));

        // Кнопка для входа
        JButton loginButton = new JButton("Вход");
        loginButton.setBounds(250, 260, 300, 50);
        loginButton.addActionListener(e -> Login.showRoleMenu(frame));

        // Кнопка для выхода
        JButton quitButton = new JButton("Выход из программы");
        quitButton.setBounds(250, 320, 300, 50);
        quitButton.addActionListener(e -> {
            frame.dispose();
            System.exit(0);
        });

        // Добавляем элементы в главное окно
        frame.add(label);
        frame.add(welcome);
        frame.add(registerButton);
        frame.add(loginButton);
        frame.add(quitButton);

        // Делаем главное окно видимым
        frame.setVisible(true);
    }

    public static void showAdminPanel() {
        frame.getContentPane().removeAll();
        frame.repaint();

        JLabel label = new JLabel("Мебельный магазин 'Furniture4U'");
        label.setBounds(235, 40, 400, 50);
        label.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 20));

        JLabel label1 = new JLabel("Панель администратора");
        label1.setBounds(280, 80, 400, 50);
        label1.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 20));

        // Кнопка "Посмотреть данные"
        JButton viewDataButton = new JButton("Посмотреть данные пользователей");
        viewDataButton.setBounds(250, 150, 300, 50);
        viewDataButton.addActionListener(e -> ViewData.showDataWindow());

        // Кнопка "Учёт товаров"
        JButton inventoryButton = new JButton("Учёт товаров");
        inventoryButton.setBounds(250, 220, 300, 50);
        inventoryButton.addActionListener(e -> ProductInventory.showInventoryWindow());

        // Кнопка "Планирование закупок"
        JButton procurementButton = new JButton("Организация закупок");
        procurementButton.setBounds(250, 290, 300, 50);
        procurementButton.addActionListener(e -> ProcurementPlanning.showProcurementWindow());

        // Кнопка "Отслеживание продаж и закупок"
        JButton trackSalesAndProcurementButton = new JButton("Отслеживание продаж и закупок");
        trackSalesAndProcurementButton.setBounds(250, 360, 300, 50);
        trackSalesAndProcurementButton.addActionListener(e -> SalesAndProcurement.showSalesAndProcurementMenu());

        // Кнопка выхода
        JButton logoutButton = new JButton("Выход");
        logoutButton.setBounds(250, 430, 300, 50);
        logoutButton.addActionListener(e -> {
            frame.dispose();
            showMainMenu();
        });

        frame.add(label);
        frame.add(label1);
        frame.add(viewDataButton);
        frame.add(inventoryButton);
        frame.add(procurementButton);
        frame.add(trackSalesAndProcurementButton);
        frame.add(logoutButton);

        frame.setVisible(true);
    }

    public static void showClientPanel() {
        frame.getContentPane().removeAll();
        frame.repaint();

        JLabel label = new JLabel("Мебельный магазин 'Furniture4U'");
        label.setBounds(235, 40, 400, 50);
        label.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 20));

        JLabel label1 = new JLabel("Панель клиента");
        label1.setBounds(320, 80, 400, 50);
        label1.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 20));

        // Кнопка "Заказ мебели"
        JButton orderFurnitureButton = new JButton("Заказ мебели");
        orderFurnitureButton.setBounds(250, 150, 300, 50);
        orderFurnitureButton.addActionListener(e -> FurnitureOrder.showFurnitureOrderMenu());

        // Кнопка "Мои заказы"
        JButton myOrdersButton = new JButton("Мои заказы");
        myOrdersButton.setBounds(250, 220, 300, 50);
        myOrdersButton.addActionListener(e -> MyOrders.showOrdersMenu());

        // Кнопка выхода
        JButton logoutButton = new JButton("Выход");
        logoutButton.setBounds(250, 290, 300, 50);
        logoutButton.addActionListener(e -> {
            frame.dispose();
            showMainMenu();
        });

        frame.add(label);
        frame.add(label1);
        frame.add(orderFurnitureButton);
        frame.add(myOrdersButton);
        frame.add(logoutButton);

        frame.setVisible(true);
    }
}
