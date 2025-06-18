import javax.swing.*;

public class AdminLogin {
    public static void openAdminLogin() {
        JFrame loginFrame = new JFrame("Вход администратора");
        loginFrame.setSize(400, 220);
        loginFrame.setLayout(null);

        JLabel phoneLabel = new JLabel("Телефон:");
        phoneLabel.setBounds(20, 20, 150, 30);

        JTextField phoneField = new JTextField();
        phoneField.setBounds(130, 20, 200, 30);

        JLabel passwordLabel = new JLabel("Пароль:");
        passwordLabel.setBounds(20, 60, 150, 30);

        JPasswordField passwordField = new JPasswordField();
        passwordField.setBounds(130, 60, 200, 30);

        JButton loginButton = new JButton("Войти");
        loginButton.setBounds(90, 120, 200, 40);
        loginButton.addActionListener(e -> {
            String phone = phoneField.getText().trim(); // Убираем лишние пробелы
            String password = new String(passwordField.getPassword()).trim();

            if (DataStorage.getAdminData().containsKey(phone)) {
                User user = DataStorage.getAdminData().get(phone); // Получаем объект данных пользователя
                String storedPassword = user.getPassword(); // Получаем сохранённый пароль
                if (storedPassword.equals(password)) {
                    String firstName = user.getFirstName(); // Извлекаем имя администратора
                    JOptionPane.showMessageDialog(loginFrame, "Добро пожаловать, " + firstName + "!");
                    loginFrame.dispose();
                    Main.showAdminPanel();
                } else {
                    JOptionPane.showMessageDialog(loginFrame, "Неверный телефон или пароль!");
                }
            }
        });

        loginFrame.add(phoneLabel);
        loginFrame.add(phoneField);
        loginFrame.add(passwordLabel);
        loginFrame.add(passwordField);
        loginFrame.add(loginButton);

        loginFrame.setVisible(true);
    }
}
