import javax.swing.*;

public class AdminRegistration {
    public static void openAdminRegistration() {
        JFrame adminFrame = new JFrame("Регистрация администратора");
        adminFrame.setSize(400, 400);
        adminFrame.setLayout(null);

        JLabel usersurnameLabel = new JLabel("Фамилия:");
        usersurnameLabel.setBounds(20, 20, 150, 30);

        JTextField usersurnameField = new JTextField();
        usersurnameField.setBounds(130, 20, 200, 30);

        JLabel usernameLabel = new JLabel("Имя:");
        usernameLabel.setBounds(20, 60, 150, 30);

        JTextField usernameField = new JTextField();
        usernameField.setBounds(130, 60, 200, 30);

        JLabel usersurnameFLabel = new JLabel("Отчество:");
        usersurnameFLabel.setBounds(20, 100, 150, 30);

        JTextField usersurnameFField = new JTextField();
        usersurnameFField.setBounds(130, 100, 200, 30);

        JLabel userphoneLabel = new JLabel("Телефон:");
        userphoneLabel.setBounds(20, 140, 150, 30);

        JTextField userphoneField = new JTextField();
        userphoneField.setBounds(130, 140, 200, 30);

        JLabel passwordLabel = new JLabel("Пароль:");
        passwordLabel.setBounds(20, 180, 150, 30);

        JPasswordField passwordField = new JPasswordField();
        passwordField.setBounds(130, 180, 200, 30);

        JButton registerButton = new JButton("Зарегистрироваться");
        registerButton.setBounds(90, 240, 200, 40);
        registerButton.addActionListener(e -> {
            String username = usernameField.getText();
            String surname = usersurnameField.getText();
            String surnamef = usersurnameFField.getText();
            String phone = userphoneField.getText();
            String password = new String(passwordField.getPassword());

            if (DataStorage.getAdminData().containsKey(phone)) {
                // Если аккаунт с таким номером уже зарегистрирован
                JOptionPane.showMessageDialog(adminFrame, "Аккаунт с таким номером уже зарегистрирован!");
            } else {
                // Если аккаунта с таким номером нет, продолжаем регистрацию
                User user = new User(surname, username, surnamef, phone, password);

                DataStorage.getAdminData().put(phone, user); // Сохраняем объект User в Map
                DataStorage.saveAdminData();

                JOptionPane.showMessageDialog(adminFrame,
                        "Администратор зарегистрирован:\nФИО: " + surname + " " + username + " " + surnamef
                                + "\nТелефон: " + phone);
                adminFrame.dispose();
            }
        });

        adminFrame.add(usernameLabel);
        adminFrame.add(usernameField);
        adminFrame.add(usersurnameLabel);
        adminFrame.add(usersurnameField);
        adminFrame.add(usersurnameFLabel);
        adminFrame.add(usersurnameFField);
        adminFrame.add(userphoneLabel);
        adminFrame.add(userphoneField);
        adminFrame.add(passwordLabel);
        adminFrame.add(passwordField);
        adminFrame.add(registerButton);

        adminFrame.setVisible(true);
    }
}
