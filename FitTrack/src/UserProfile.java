// Класс данных пользователя

import java.time.LocalDate;

public class UserProfile {
    private int id;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private String gender;
    private int height;
    private int weight;
    private int targetWeight;

    public UserProfile(int id, String firstName, String lastName, LocalDate birthDate,
                       String gender, int height, int weight, int targetWeight) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.gender = gender;
        this.height = height;
        this.weight = weight;
        this.targetWeight = targetWeight;
    }

    // Геттеры
    public int getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public LocalDate getBirthDate() { return birthDate; }
    public String getGender() { return gender; }
    public int getHeight() { return height; }
    public int getWeight() { return weight; }
    public int getTargetWeight() { return targetWeight; }
    
    // Сеттеры
    public void setId(int id) { this.id = id; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
    public void setGender(String gender) { this.gender = gender; }
    public void setHeight(int height) { this.height = height; }
    public void setWeight(int weight) { this.weight = weight; }
    public void setTargetWeight(int targetWeight) { this.targetWeight = targetWeight; }
}
