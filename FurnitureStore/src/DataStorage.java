import java.io.*;
import java.util.*;

public class DataStorage {
    private static final String ADMIN_FILE = "adminData.txt";
    private static final String CLIENT_FILE = "clientData.txt";

    private static Map<String, User> adminData = new HashMap<>();
    private static Map<String, User> clientData = new HashMap<>();

    static {
        loadAdminData();
        loadClientData();
    }

    public static Map<String, User> getAdminData() {
        return adminData;
    }

    public static Map<String, User> getClientData() {
        return clientData;
    }

    public static void saveAdminData() {
        saveData(ADMIN_FILE, adminData);
    }

    public static void saveClientData() {
        saveData(CLIENT_FILE, clientData);
    }

    private static void saveData(String fileName, Map<String, User> data) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            for (User user : data.values()) {
                writer.write(user.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadAdminData() {
        adminData = loadData(ADMIN_FILE);
    }

    private static void loadClientData() {
        clientData = loadData(CLIENT_FILE);
    }

    private static Map<String, User> loadData(String fileName) {
        Map<String, User> data = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length == 5) {
                    // Создаем объект User и добавляем его в Map
                    User user = new User(parts[0], parts[1], parts[2], parts[3], parts[4]);
                    data.put(parts[3], user); // Ключ — телефон
                }
            }
        } catch (FileNotFoundException e) {
            // Если файл не найден, возвращаем пустую Map
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }
}
