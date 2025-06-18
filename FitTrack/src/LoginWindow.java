// Окно авторизации пользователя

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import java.time.LocalDate;

public class LoginWindow extends javax.swing.JFrame {

    public LoginWindow() {
        initComponents();
        
        // Обработчик для чекбокса "Показать пароль"
        jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                char echoChar = jCheckBox1.isSelected() ? (char) 0 : '•';
                jPasswordField1.setEchoChar(echoChar);
            }
        });
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jFormattedTextField1 = new javax.swing.JFormattedTextField();
        jLabel3 = new javax.swing.JLabel();
        jPasswordField1 = new javax.swing.JPasswordField();
        jCheckBox1 = new javax.swing.JCheckBox();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("Вход");

        jLabel2.setText("Номер телефона:");

        jLabel3.setText("Пароль:");

        jCheckBox1.setText("Показать пароль");

        jButton1.setText("Авторизация");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("Назад");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(39, 39, 39)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(28, 28, 28)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jCheckBox1, javax.swing.GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE)
                            .addComponent(jFormattedTextField1)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPasswordField1)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(86, 86, 86)
                        .addComponent(jButton1)
                        .addGap(29, 29, 29)
                        .addComponent(jButton2)))
                .addContainerGap(92, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(31, 31, 31)
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jFormattedTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPasswordField1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jCheckBox1)
                .addGap(61, 61, 61)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2))
                .addContainerGap(69, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        String phone = jFormattedTextField1.getText().trim();
        String password = new String(jPasswordField1.getPassword());

        // Проверка валидности номера телефона
        if (!phone.matches("\\+7\\d{10}")) {
            JOptionPane.showMessageDialog(this,
                "Введите номер телефона в формате +7XXXXXXXXXX",
                "Неверный формат", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Проверка заполненности пароля
        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Введите пароль",
                "Пустой пароль", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Подключение к БД
        String url = "jdbc:postgresql://localhost:5432/fittrack";
        String user = "postgres";
        String dbPassword = "postgresql";

        try (Connection conn = DriverManager.getConnection(url, user, dbPassword)) {
            String query = "SELECT user_id, first_name, last_name, birth_date, gender, height, weight, target_weight "
             + "FROM users WHERE phone = ? AND password = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, phone);
                stmt.setString(2, password);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        int userId = rs.getInt("user_id");
                        String firstName = rs.getString("first_name");
                        String lastName = rs.getString("last_name");

                        // Дата рождения из java.sql.Date в java.time.LocalDate
                        java.sql.Date birthDateSql = rs.getDate("birth_date");
                        LocalDate birthDate = birthDateSql != null ? birthDateSql.toLocalDate() : null;

                        String gender = rs.getString("gender");
                        int height = rs.getInt("height");
                        int weight = rs.getInt("weight");
                        int targetWeight = rs.getInt("target_weight");

                        UserProfile profile = new UserProfile(userId, firstName, lastName, birthDate,
                                                              gender, height, weight, targetWeight);

                        SessionManager.setCurrentUserId(userId);

                        // Успешная авторизация
                        JOptionPane.showMessageDialog(this,
                            "Добро пожаловать, " + firstName + "!",
                            "Успешный вход", JOptionPane.INFORMATION_MESSAGE);

                        dispose();

                        // Передаем профиль в CalendarWindow
                        new CalendarWindow(profile).setVisible(true);

                    } else {
                        JOptionPane.showMessageDialog(this,
                            "Неверный номер телефона или пароль!",
                            "Ошибка входа", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Ошибка подключения к базе данных:\n" + ex.getMessage(),
                "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        dispose();
        MainWindow mainWindow = new MainWindow();
        mainWindow.setVisible(true);
    }//GEN-LAST:event_jButton2ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JFormattedTextField jFormattedTextField1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPasswordField jPasswordField1;
    // End of variables declaration//GEN-END:variables
}
