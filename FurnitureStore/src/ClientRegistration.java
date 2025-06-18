import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ClientRegistration {
    public static void openClientRegistration() {
        JFrame clientFrame = new JFrame("Регистрация клиента");
        clientFrame.setSize(400, 400);
        clientFrame.setLayout(null);

        // Метки и поля ввода для данных клиента
        JLabel surnameLabel = new JLabel("Фамилия:");
        surnameLabel.setBounds(20, 20, 150, 30);
        JTextField surnameField = new JTextField();
        surnameField.setBounds(130, 20, 200, 30);

        JLabel nameLabel = new JLabel("Имя:");
        nameLabel.setBounds(20, 60, 150, 30);
        JTextField nameField = new JTextField();
        nameField.setBounds(130, 60, 200, 30);

        JLabel surnameFLabel = new JLabel("Отчество:");
        surnameFLabel.setBounds(20, 100, 150, 30);
        JTextField surnameFField = new JTextField();
        surnameFField.setBounds(130, 100, 200, 30);

        JLabel phoneLabel = new JLabel("Телефон:");
        phoneLabel.setBounds(20, 140, 150, 30);
        JTextField phoneField = new JTextField();
        phoneField.setBounds(130, 140, 200, 30);

        JLabel passLabel = new JLabel("Пароль:");
        passLabel.setBounds(20, 180, 150, 30);
        JTextField passField = new JTextField();
        passField.setBounds(130, 180, 200, 30);

        // Кнопка для регистрации
        JButton registerButton = new JButton("Регистрация");
        registerButton.setBounds(90, 240, 200, 40);

        registerButton.addActionListener(e -> {
            // Получение данных из полей
            String name = nameField.getText().trim();
            String surname = surnameField.getText().trim();
            String surnameF = surnameFField.getText().trim();
            String phone = phoneField.getText().trim();
            String pass = passField.getText().trim();

            if (name.isEmpty() || surname.isEmpty() || phone.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(clientFrame, "Пожалуйста, заполните все обязательные поля!",
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (DataStorage.getClientData().containsKey(phone)) {
                // Проверка на существующего клиента
                JOptionPane.showMessageDialog(clientFrame, "Аккаунт с таким номером уже зарегистрирован!");
            } else {
                // Регистрация клиента в Map
                User user = new User(surname, name, surnameF, phone, pass);
                DataStorage.getClientData().put(phone, user); // Сохранение в Map
                DataStorage.saveClientData();

                // Добавление клиента в базу данных
                if (registerClientInDatabase(surname, name, surnameF, phone)) {
                    JOptionPane.showMessageDialog(clientFrame,
                            "Клиент зарегистрирован:\nФИО: " + surname + " " + name + " " + surnameF
                                    + "\nТелефон: " + phone);
                    clientFrame.dispose();
                }
            }
        });

        // Добавление компонентов в окно
        clientFrame.add(nameLabel);
        clientFrame.add(nameField);
        clientFrame.add(surnameLabel);
        clientFrame.add(surnameField);
        clientFrame.add(surnameFLabel);
        clientFrame.add(surnameFField);
        clientFrame.add(phoneLabel);
        clientFrame.add(phoneField);
        clientFrame.add(passLabel);
        clientFrame.add(passField);
        clientFrame.add(registerButton);

        clientFrame.setVisible(true);
    }

    private static boolean registerClientInDatabase(String surname, String name, String surnameF, String phone) {
        // SQL-запрос для добавления клиента
        String query = "INSERT INTO Клиент (Фамилия, Имя, Отчество, Номер_телефона) VALUES (?, ?, ?, ?)";

        try (Connection connection = DBConnector.connect();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // Установка параметров в запрос
            statement.setString(1, surname);
            statement.setString(2, name);
            statement.setString(3, surnameF.isEmpty() ? null : surnameF);
            statement.setString(4, phone);

            statement.executeUpdate(); // Выполнение запроса
            return true; // Успешное добавление

        } catch (SQLException ex) {
            // Обработка ошибок базы данных
            JOptionPane.showMessageDialog(null,
                    "Ошибка при добавлении клиента в базу данных: " + ex.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            return false; // Ошибка
        }
    }
}
