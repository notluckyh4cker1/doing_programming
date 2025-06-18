public class User {
    private String lastName;
    private String firstName;
    private String middleName;
    private String phone;
    private String password;

    // Конструктор
    public User(String lastName, String firstName, String middleName, String phone, String password) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.middleName = middleName;
        this.phone = phone;
        this.password = password;
    }

    // Геттеры и сеттеры
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // Метод для конвертации объекта в строку для записи в файл
    @Override
    public String toString() {
        return lastName + ";" + firstName + ";" + middleName + ";" + phone + ";" + password;
    }

    // Метод для вывода данных пользователя в удобном формате
    public String getFormattedData() {
        return "ФИО: " + lastName + " " + firstName + " " + middleName + ", Телефон: " + phone + ", Пароль: " + password;
    }
}
