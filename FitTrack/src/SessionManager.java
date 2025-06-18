// Тут запоминаем ID пользователя и используем его в различных целях

public class SessionManager {
    private static int currentUserId = -1;

    public static void setCurrentUserId(int userId) {
        currentUserId = userId;
    }

    public static int getCurrentUserId() {
        return currentUserId;
    }

    public static boolean isUserLoggedIn() {
        return currentUserId != -1;
    }

    public static void logout() {
        currentUserId = -1;
    }
}