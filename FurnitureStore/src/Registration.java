import javax.swing.*;

public class Registration {
    public static void showRoleMenu(JFrame frame) {
        JPopupMenu roleMenu = new JPopupMenu();
        JMenuItem adminItem = new JMenuItem("Зарегистрироваться как администратор");
        JMenuItem clientItem = new JMenuItem("Зарегистрироваться как клиент");

        // Добавляем действия для выбора роли
        adminItem.addActionListener(e -> AdminRegistration.openAdminRegistration());
        clientItem.addActionListener(e -> ClientRegistration.openClientRegistration());

        // Добавляем элементы в контекстное меню
        roleMenu.add(adminItem);
        roleMenu.add(clientItem);

        // Показываем контекстное меню
        roleMenu.show(frame, 280, 280);
    }
}
