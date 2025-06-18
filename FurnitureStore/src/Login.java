import javax.swing.*;

public class Login {
    public static void showRoleMenu(JFrame frame) {
        JPopupMenu roleMenu = new JPopupMenu();
        JMenuItem adminLogin = new JMenuItem("Войти как администратор");
        JMenuItem clientLogin = new JMenuItem("Войти как клиент");

        // Добавляем действия для выбора роли
        adminLogin.addActionListener(e -> AdminLogin.openAdminLogin());
        clientLogin.addActionListener(e -> ClientLogin.openClientLogin());

        // Добавляем элементы в контекстное меню
        roleMenu.add(adminLogin);
        roleMenu.add(clientLogin);

        // Показываем контекстное меню
        roleMenu.show(frame, 325, 340);
    }
}

