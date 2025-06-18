// Окно профиля пользователя

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import javax.swing.JTextField;
import javax.swing.JProgressBar;
import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;

public class UserProfileWindow extends javax.swing.JFrame {
    private UserProfile profile;
    private LocalDate selectedDate = LocalDate.now();
    private int userId; // ID пользователя из профиля
    
    String user = "postgres";
    String pwd = "postgresql";
    String dbUrl = "jdbc:postgresql://localhost:5432/fittrack";

    public UserProfileWindow(UserProfile profile) {
        this.profile = profile;
        this.userId = profile.getId();
        initComponents();
        fillUserData(profile);
        
        DefaultTableModel activityTableModel = new DefaultTableModel(new Object[]{"Активность", "Сожженные калории"}, 0);
        jTable1.setModel(activityTableModel);

        loadActivitiesForDate(selectedDate);
        updateDateLabel();         // Установим дату в jLabel18
        updateNutritionData();     // Загрузим питание за текущий день
    }

    private void fillUserData(UserProfile profile) {
        jLabel2.setText("Фамилия: " + profile.getLastName());
        jLabel3.setText("Имя: " + profile.getFirstName());

        LocalDate birthDate = profile.getBirthDate();
        
        // Форматируем дату в нужный вид
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        String formattedDate = birthDate.format(formatter);
        int age = calculateAge(birthDate);

        jLabel4.setText("Дата рождения: " + formattedDate + " (" + age + " лет)");
        jLabel5.setText("Рост: " + profile.getHeight() + " см");
        jLabel6.setText("Вес: " + profile.getWeight() + " кг");
        jLabel7.setText("Желаемый вес: " + profile.getTargetWeight() + " кг");

        int bmr; // Норма калорий

        if (profile.getGender().equalsIgnoreCase("М")) {
            bmr = (int)(10 * profile.getWeight() + 6.25 * profile.getHeight() - 5 * age + 5);
        } else {
            bmr = (int)(10 * profile.getWeight() + 6.25 * profile.getHeight() - 5 * age - 161);
        }

        jLabel8.setText("Норма калорий: " + bmr + " ккал");
        
        // Расчёт на каждый приём пищи
        int breakfast = (int)(bmr * 0.25);
        int lunch = (int)(bmr * 0.35);
        int snack = (int)(bmr * 0.10);
        int dinner = (int)(bmr * 0.30);
        
        int proteinTarget = (int) Math.round((bmr * 0.30) / 4);
        int fatTarget     = (int) Math.round((bmr * 0.25) / 9);
        int carbTarget    = (int) Math.round((bmr * 0.45) / 4);

        // Настройка прогресс-баров
        setupProgressBar(jProgressBar1, 0, breakfast);
        setupProgressBar(jProgressBar2, 0, lunch);
        setupProgressBar(jProgressBar3, 0, snack);
        setupProgressBar(jProgressBar4, 0, dinner);
        setupProgressBar(jProgressBar5, 0, proteinTarget, "г");
        setupProgressBar(jProgressBar6, 0, fatTarget, "г");
        setupProgressBar(jProgressBar7, 0, carbTarget, "г");
    }
    
    // Расчёт возраста
    private int calculateAge(LocalDate birthDate) {
        return Period.between(birthDate, LocalDate.now()).getYears();
    }
    
    // Настройка прогресс-бара с текстом и единицей измерения
    private void setupProgressBar(JProgressBar bar, int current, int total, String unit) {
        bar.setMinimum(0);
        bar.setMaximum(total);
        bar.setValue(current);
        bar.setStringPainted(true);
        bar.setString(current + " / " + total + " " + unit);
    }
    
    // Перегруженный метод по умолчанию для калорий
    private void setupProgressBar(JProgressBar bar, int current, int total) {
        setupProgressBar(bar, current, total, "ккал");
    }
    
    // Обновление метки даты
    private void updateDateLabel() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        jLabel18.setText(selectedDate.format(formatter));
    }
    
    // Загрузка активностей для выбранной даты
    private void loadActivitiesForDate(LocalDate date) {
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        model.setRowCount(0);

        System.out.println("Загружаем активности для даты: " + date);

        try (Connection conn = DatabaseConnector.connect()) {
            String sql = "SELECT description_of_activity, calories_burned FROM activities WHERE user_id = ? AND activity_date::date = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, profile.getId());
                stmt.setDate(2, java.sql.Date.valueOf(date));
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    String desc = rs.getString("description_of_activity");
                    int kcal = rs.getInt("calories_burned");
                    model.addRow(new Object[]{desc, kcal});
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // Загрузка данных питания из БД
    public void updateNutritionData() {
        int breakfastKcal = 0;
        int lunchKcal = 0;
        int snackKcal = 0;
        int dinnerKcal = 0;
        
        int totalProteins = 0;
        int totalFats = 0;
        int totalCarbs = 0;

        try (Connection conn = DatabaseConnector.connect()) {
            String sql = """
                SELECT m.meal_type, SUM(p.calories_per_100g * mp.weight_product / 100.0) AS total_kcal
                FROM meals m
                JOIN meal_products mp ON m.meal_id = mp.meal_id
                JOIN products p ON mp.product_id = p.product_id
                WHERE m.user_id = ? AND m.meal_datetime::date = ?
                GROUP BY m.meal_type
            """;

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, profile.getId());
                ps.setDate(2, java.sql.Date.valueOf(selectedDate));
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    String type = rs.getString("meal_type");
                    int kcal = rs.getInt("total_kcal");
                    switch (type.toLowerCase()) {
                        case "завтрак" -> breakfastKcal = kcal;
                        case "обед" -> lunchKcal = kcal;
                        case "перекус" -> snackKcal = kcal;
                        case "ужин" -> dinnerKcal = kcal;
                    }
                }
            }
            String bjuSql = """
                SELECT SUM(p.proteins_per_100g * mp.weight_product / 100.0) AS total_proteins,
                       SUM(p.fats_per_100g * mp.weight_product / 100.0) AS total_fats,
                       SUM(p.carbs_per_100g * mp.weight_product / 100.0) AS total_carbs
                FROM meals m
                JOIN meal_products mp ON m.meal_id = mp.meal_id
                JOIN products p ON mp.product_id = p.product_id
                WHERE m.user_id = ? AND m.meal_datetime::date = ?
            """;

            try (PreparedStatement ps = conn.prepareStatement(bjuSql)) {
                ps.setInt(1, profile.getId());
                ps.setDate(2, java.sql.Date.valueOf(selectedDate));
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    totalProteins = rs.getInt("total_proteins");
                    totalFats     = rs.getInt("total_fats");
                    totalCarbs    = rs.getInt("total_carbs");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Получение фактического количества выпитой воды за выбранную дату
        int totalWaterMl = 0;
        try (Connection conn = DatabaseConnector.connect()) {
            String waterSql = """
                SELECT SUM(amount_liters)
                FROM waterintake
                WHERE user_id = ? AND intake_date = ?
            """;
            try (PreparedStatement ps = conn.prepareStatement(waterSql)) {
                ps.setInt(1, profile.getId());
                ps.setDate(2, java.sql.Date.valueOf(selectedDate));
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    double totalLiters = rs.getDouble(1);
                    totalWaterMl = (int) Math.round(totalLiters * 1000);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Получаем норму БЖУ и настраиваем прогресс-бары
        LocalDate birthDate = profile.getBirthDate();
        int age = calculateAge(birthDate);
        int bmr;
        
        if (profile.getGender().equalsIgnoreCase("М")) {
            bmr = (int)(10 * profile.getWeight() + 6.25 * profile.getHeight() - 5 * age + 5);
        } else {
            bmr = (int)(10 * profile.getWeight() + 6.25 * profile.getHeight() - 5 * age - 161);
        }
        
        // Общая сумма калорий за день
        int totalCalories = breakfastKcal + lunchKcal + snackKcal + dinnerKcal;

        // Расчёт нормы воды: вес (кг) * 0.03 → результат в литрах
        double waterNormLiters = profile.getWeight() * 0.03;
        int waterTarget = (int) Math.round(waterNormLiters * 1000); // перевод в мл
        
        int proteinTarget = (int) Math.round((bmr * 0.30) / 4); // 30% калорий из белков
        int fatTarget     = (int) Math.round((bmr * 0.25) / 9); // 25% из жиров
        int carbTarget    = (int) Math.round((bmr * 0.45) / 4); // 45% из углеводов
        
        setupProgressBar(jProgressBar1, breakfastKcal, (int)(bmr * 0.25));
        setupProgressBar(jProgressBar2, lunchKcal, (int)(bmr * 0.35));
        setupProgressBar(jProgressBar3, snackKcal, (int)(bmr * 0.10));
        setupProgressBar(jProgressBar4, dinnerKcal, (int)(bmr * 0.30));
        
        setupProgressBar(jProgressBar5, totalProteins, proteinTarget, "г");
        setupProgressBar(jProgressBar6, totalFats, fatTarget, "г");
        setupProgressBar(jProgressBar7, totalCarbs, carbTarget, "г");
        
        setupProgressBar(jProgressBar8, totalCalories, bmr);
        setupProgressBar(jProgressBar9, totalWaterMl, waterTarget, "мл");
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jProgressBar1 = new javax.swing.JProgressBar();
        jProgressBar2 = new javax.swing.JProgressBar();
        jProgressBar3 = new javax.swing.JProgressBar();
        jProgressBar4 = new javax.swing.JProgressBar();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jProgressBar5 = new javax.swing.JProgressBar();
        jProgressBar6 = new javax.swing.JProgressBar();
        jProgressBar7 = new javax.swing.JProgressBar();
        jLabel16 = new javax.swing.JLabel();
        jProgressBar8 = new javax.swing.JProgressBar();
        jLabel17 = new javax.swing.JLabel();
        jProgressBar9 = new javax.swing.JProgressBar();
        jSeparator3 = new javax.swing.JSeparator();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jButton10 = new javax.swing.JButton();
        jSeparator4 = new javax.swing.JSeparator();
        jLabel18 = new javax.swing.JLabel();
        jButton11 = new javax.swing.JButton();
        jButton12 = new javax.swing.JButton();
        jLabel19 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jButton13 = new javax.swing.JButton();
        jButton14 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("Профиль");

        jLabel2.setText("Фамилия:");

        jLabel3.setText("Имя:");

        jLabel4.setText("Дата рождения:");

        jLabel5.setText("Рост:");

        jLabel6.setText("Вес:");

        jLabel7.setText("Желаемый вес:");

        jLabel8.setText("Норма калорий:");

        jButton1.setText("Редактировать профиль");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("Заметки");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setText("Статьи");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton4.setText("Назад");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jLabel9.setText("Завтрак:");

        jLabel10.setText("Обед:");

        jLabel11.setText("Перекус:");

        jLabel12.setText("Ужин:");

        jLabel13.setText("Белки:");

        jLabel14.setText("Жиры:");

        jLabel15.setText("Углеводы:");

        jLabel16.setText("Общее количество калорий:");

        jLabel17.setText("Потребление воды:");

        jButton5.setText("+");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        jButton6.setText("+");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        jButton7.setText("+");
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });

        jButton8.setText("+");
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });

        jButton9.setText("+");
        jButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton9ActionPerformed(evt);
            }
        });

        jButton10.setText("Добавить новый продукт");
        jButton10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton10ActionPerformed(evt);
            }
        });

        jLabel18.setText("День:");

        jButton11.setText(">");
        jButton11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton11ActionPerformed(evt);
            }
        });

        jButton12.setText("<");
        jButton12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton12ActionPerformed(evt);
            }
        });

        jLabel19.setText("Активности");

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        jButton13.setText("Добавить активность");
        jButton13.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton13ActionPerformed(evt);
            }
        });

        jButton14.setText("Назад");
        jButton14.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton14ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(37, 37, 37)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(50, 50, 50)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel7)
                            .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(142, 142, 142))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jButton2)
                                .addGap(45, 45, 45)
                                .addComponent(jButton3)
                                .addGap(45, 45, 45)
                                .addComponent(jButton4))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel9, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel10, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel11, javax.swing.GroupLayout.Alignment.LEADING))
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(18, 18, 18)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(jProgressBar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(jProgressBar2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(jProgressBar3, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addGap(18, 18, 18)
                                        .addComponent(jProgressBar4, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jButton5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jButton6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jButton7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jButton8)))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(109, 109, 109)
                                .addComponent(jButton12, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel18)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton11, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jProgressBar9, javax.swing.GroupLayout.PREFERRED_SIZE, 167, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jButton9))
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                    .addComponent(jLabel16)
                                    .addGap(18, 18, 18)
                                    .addComponent(jProgressBar8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addComponent(jSeparator2, javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addGroup(layout.createSequentialGroup()
                                            .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                            .addComponent(jProgressBar5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(layout.createSequentialGroup()
                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                .addComponent(jProgressBar7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(jProgressBar6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                    .addGap(87, 87, 87)))
                            .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, 360, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 360, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 360, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(134, 134, 134)
                                .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(164, 164, 164))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(73, 73, 73)
                                .addComponent(jButton1))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(91, 91, 91)
                                .addComponent(jButton10))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(148, 148, 148)
                                .addComponent(jLabel19)))
                        .addGap(42, 42, 42))))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 402, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(77, 77, 77)
                        .addComponent(jButton13)
                        .addGap(40, 40, 40)
                        .addComponent(jButton14)))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel8)
                .addGap(18, 18, 18)
                .addComponent(jButton1)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton2)
                    .addComponent(jButton3)
                    .addComponent(jButton4))
                .addGap(18, 18, 18)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel18)
                        .addComponent(jButton11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jProgressBar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(jProgressBar2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel10))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButton7, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(jProgressBar3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel11))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButton8, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jProgressBar4, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jProgressBar5, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel14)
                    .addComponent(jProgressBar6, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jProgressBar7, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel16)
                    .addComponent(jProgressBar8, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel17)
                    .addComponent(jProgressBar9, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton9, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 3, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, 3, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel19)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton13)
                    .addComponent(jButton14))
                .addGap(148, 148, 148))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        dispose();
        new EditUserProfileWindow(profile).setVisible(true);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        new NotesWindow(profile).setVisible(true);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        new ArticlesWindow().setVisible(true);
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        dispose();
    }//GEN-LAST:event_jButton4ActionPerformed

    // Кнопки добавления пищи
    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        AddMealProduct addWindow = new AddMealProduct(this, "завтрак", profile.getId(), selectedDate);
        addWindow.setVisible(true);
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        AddMealProduct addWindow = new AddMealProduct(this, "обед", profile.getId(), selectedDate);
        addWindow.setVisible(true);
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        AddMealProduct addWindow = new AddMealProduct(this, "перекус", profile.getId(), selectedDate);
        addWindow.setVisible(true);
    }//GEN-LAST:event_jButton7ActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        AddMealProduct addWindow = new AddMealProduct(this, "ужин", profile.getId(), selectedDate);
        addWindow.setVisible(true);
    }//GEN-LAST:event_jButton8ActionPerformed

    // Кнопка добавления продуктов в БД
    private void jButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton10ActionPerformed
        AddProductWindow addProductWindow = new AddProductWindow(profile);
        addProductWindow.setVisible(true);
    }//GEN-LAST:event_jButton10ActionPerformed

    private void jButton12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton12ActionPerformed
        selectedDate = selectedDate.minusDays(1);
        loadActivitiesForDate(selectedDate);
        updateDateLabel();
        updateNutritionData();
    }//GEN-LAST:event_jButton12ActionPerformed

    private void jButton11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton11ActionPerformed
        selectedDate = selectedDate.plusDays(1);
        loadActivitiesForDate(selectedDate);
        updateDateLabel();
        updateNutritionData();
    }//GEN-LAST:event_jButton11ActionPerformed

    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
        String input = JOptionPane.showInputDialog(this, "Введите количество выпитой воды (в литрах):", "Добавить воду", JOptionPane.PLAIN_MESSAGE);

        if (input != null && !input.trim().isEmpty()) {
            try {
                double liters = Double.parseDouble(input.trim());
                if (liters <= 0) {
                    JOptionPane.showMessageDialog(this, "Введите положительное число.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Используем выбранную пользователем дату, а не сегодняшнюю
                LocalDate date = selectedDate;

                try (Connection conn = DriverManager.getConnection(dbUrl, user, pwd)) {
                    String sql = "INSERT INTO waterintake (user_id, intake_date, amount_liters) VALUES (?, ?, ?)";
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setInt(1, userId);
                        stmt.setDate(2, java.sql.Date.valueOf(date));
                        stmt.setDouble(3, liters);
                        stmt.executeUpdate();
                    }

                    // После успешного добавления – запросить сумму воды за выбранную дату
                    String sumSql = "SELECT SUM(amount_liters) FROM waterintake WHERE user_id = ? AND intake_date = ?";
                    try (PreparedStatement sumStmt = conn.prepareStatement(sumSql)) {
                        sumStmt.setInt(1, userId);
                        sumStmt.setDate(2, java.sql.Date.valueOf(date));
                        ResultSet rs = sumStmt.executeQuery();
                        double totalWater = 0;
                        if (rs.next()) {
                            totalWater = rs.getDouble(1);
                        }

                        // Рассчитать норму воды (в мл), например вес пользователя * 0.03 * 1000
                        int waterTargetMl = (int) Math.round(profile.getWeight() * 0.03 * 1000);
                        int totalWaterMl = (int) Math.round(totalWater * 1000);

                        // Обновить прогресс-бар воды
                        jProgressBar9.setMaximum(waterTargetMl);
                        jProgressBar9.setValue(totalWaterMl);
                        jProgressBar9.setString(totalWaterMl + " / " + waterTargetMl + " мл");
                        jProgressBar9.setStringPainted(true);
                    }

                    JOptionPane.showMessageDialog(this, "Вода успешно добавлена!", "Успех", JOptionPane.INFORMATION_MESSAGE);

                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Ошибка при добавлении данных в БД.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                }

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Некорректный формат числа.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_jButton9ActionPerformed

    private void jButton13ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton13ActionPerformed
        JTextField activityField = new JTextField();
        JTextField caloriesField = new JTextField();
        Object[] message = {
            "Название активности:", activityField,
            "Сожжённые калории:", caloriesField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Добавить активность", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String activity = activityField.getText().trim();
            String caloriesStr = caloriesField.getText().trim();

            if (!activity.isEmpty() && !caloriesStr.isEmpty()) {
                try {
                    int calories = Integer.parseInt(caloriesStr);
                    LocalDate date = selectedDate; // тот, который выбран в интерфейсе

                    try (Connection conn = DatabaseConnector.connect()) {
                        String sql = "INSERT INTO activities (user_id, activity_date, description_of_activity, calories_burned) VALUES (?, ?, ?, ?)";
                        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                            stmt.setInt(1, profile.getId());
                            stmt.setDate(2, java.sql.Date.valueOf(date));
                            stmt.setString(3, activity);
                            stmt.setInt(4, calories);
                            stmt.executeUpdate();
                        }
                        JOptionPane.showMessageDialog(this, "Активность добавлена.");
                        loadActivitiesForDate(date); // Обновление таблицы
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Введите корректное число калорий.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Ошибка при сохранении активности.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Пожалуйста, заполните все поля.");
            }
        }
    }//GEN-LAST:event_jButton13ActionPerformed

    private void jButton14ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton14ActionPerformed
        dispose();
    }//GEN-LAST:event_jButton14ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton12;
    private javax.swing.JButton jButton13;
    private javax.swing.JButton jButton14;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JProgressBar jProgressBar2;
    private javax.swing.JProgressBar jProgressBar3;
    private javax.swing.JProgressBar jProgressBar4;
    private javax.swing.JProgressBar jProgressBar5;
    private javax.swing.JProgressBar jProgressBar6;
    private javax.swing.JProgressBar jProgressBar7;
    private javax.swing.JProgressBar jProgressBar8;
    private javax.swing.JProgressBar jProgressBar9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables
}
