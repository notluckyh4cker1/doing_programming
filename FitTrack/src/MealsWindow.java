// Окно отметок приема пищи

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Date;

public class MealsWindow extends javax.swing.JFrame {
    
    private Date selectedDate;
    private UserProfile profile;
    private UserProfileWindow userprofile;

    public MealsWindow(Date selectedDate, UserProfile profile) {
        this.selectedDate = selectedDate;
        this.profile = profile;
        initComponents();
        updateDateLabel();
        updateNutritionData();
        
        userprofile = new UserProfileWindow(profile);
        userprofile.updateNutritionData();
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        setTitle("Питание на " + sdf.format(selectedDate));
    }
    
    // Данные от БД
    String user = "postgres";
    String pwd = "postgresql";
    String dbUrl = "jdbc:postgresql://localhost:5432/fittrack";
    
    private void updateDateLabel() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        jLabel1.setText(sdf.format(selectedDate));
    }
    
    private void setupProgressBar(JProgressBar bar, int min, int max) {
        bar.setMinimum(min);
        bar.setMaximum(max);
        bar.setValue(min);
        bar.setStringPainted(true);
        bar.setString(min + " / " + max);
    }

    private void setupProgressBar(JProgressBar bar, int current, int max, String unit) {
        bar.setMinimum(0);
        bar.setMaximum(max);
        bar.setValue(current);
        bar.setStringPainted(true);
        bar.setString(current + " " + unit + " / " + max + " " + unit);
    }

    private void updateProgressBar(JProgressBar bar, int current, int max, String unit) {
        bar.setMaximum(max);
        bar.setValue(current);
        bar.setString(current + " " + unit + " / " + max + " " + unit);
    }
    

    private int calculateAge(LocalDate birthDate) {
        return Period.between(birthDate, LocalDate.now()).getYears();
    }
    
    public void updateNutritionData() {
        int breakfastKcal = 0;
        int lunchKcal = 0;
        int snackKcal = 0;
        int dinnerKcal = 0;

        int totalProteins = 0;
        int totalFats = 0;
        int totalCarbs = 0;

        LocalDate localDate = selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        try (Connection conn = DatabaseConnector.connect()) {
            String kcalSql = """
                SELECT m.meal_type, SUM(p.calories_per_100g * mp.weight_product / 100.0) AS total_kcal
                FROM meals m
                JOIN meal_products mp ON m.meal_id = mp.meal_id
                JOIN products p ON mp.product_id = p.product_id
                WHERE m.user_id = ? AND m.meal_datetime::date = ?
                GROUP BY m.meal_type
            """;

            try (PreparedStatement ps = conn.prepareStatement(kcalSql)) {
                ps.setInt(1, profile.getId());
                ps.setDate(2, java.sql.Date.valueOf(localDate));
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
                ps.setDate(2, java.sql.Date.valueOf(localDate));
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    totalProteins = rs.getInt("total_proteins");
                    totalFats = rs.getInt("total_fats");
                    totalCarbs = rs.getInt("total_carbs");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        int age = calculateAge(profile.getBirthDate());
        int bmr;
        if (profile.getGender().equalsIgnoreCase("М")) {
            bmr = (int)(10 * profile.getWeight() + 6.25 * profile.getHeight() - 5 * age + 5);
        } else {
            bmr = (int)(10 * profile.getWeight() + 6.25 * profile.getHeight() - 5 * age - 161);
        }

        int totalCalories = breakfastKcal + lunchKcal + snackKcal + dinnerKcal;

        double waterNormLiters = profile.getWeight() * 0.03;
        int waterTarget = (int) Math.round(waterNormLiters * 1000);

        int proteinTarget = (int) Math.round((bmr * 0.30) / 4);
        int fatTarget     = (int) Math.round((bmr * 0.25) / 9);
        int carbTarget    = (int) Math.round((bmr * 0.45) / 4);

        int totalWaterMl = 0;
        try (Connection conn = DatabaseConnector.connect()) {
            String waterSql = """
                SELECT SUM(amount_liters)
                FROM waterintake
                WHERE user_id = ? AND intake_date = ?
            """;
            try (PreparedStatement ps = conn.prepareStatement(waterSql)) {
                ps.setInt(1, profile.getId());
                ps.setDate(2, java.sql.Date.valueOf(localDate));
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    double totalLiters = rs.getDouble(1);
                    totalWaterMl = (int) Math.round(totalLiters * 1000);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Обновление прогресс-баров
        setupProgressBar(jProgressBar1, breakfastKcal, (int)(bmr * 0.25), "ккал");
        setupProgressBar(jProgressBar2, lunchKcal, (int)(bmr * 0.35), "ккал");
        setupProgressBar(jProgressBar3, snackKcal, (int)(bmr * 0.10), "ккал");
        setupProgressBar(jProgressBar4, dinnerKcal, (int)(bmr * 0.30), "ккал");

        setupProgressBar(jProgressBar5, totalProteins, proteinTarget, "г");
        setupProgressBar(jProgressBar6, totalFats, fatTarget, "г");
        setupProgressBar(jProgressBar7, totalCarbs, carbTarget, "г");

        setupProgressBar(jProgressBar8, totalCalories, bmr, "ккал");
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
        jProgressBar1 = new javax.swing.JProgressBar();
        jProgressBar2 = new javax.swing.JProgressBar();
        jProgressBar3 = new javax.swing.JProgressBar();
        jProgressBar4 = new javax.swing.JProgressBar();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jProgressBar5 = new javax.swing.JProgressBar();
        jProgressBar6 = new javax.swing.JProgressBar();
        jProgressBar7 = new javax.swing.JProgressBar();
        jLabel9 = new javax.swing.JLabel();
        jProgressBar8 = new javax.swing.JProgressBar();
        jLabel10 = new javax.swing.JLabel();
        jProgressBar9 = new javax.swing.JProgressBar();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("Дата:");

        jLabel2.setText("Завтрак:");

        jLabel3.setText("Обед:");

        jLabel4.setText("Перекус:");

        jLabel5.setText("Ужин:");

        jLabel6.setText("Белки:");

        jLabel7.setText("Жиры:");

        jLabel8.setText("Углеводы:");

        jLabel9.setText("Общее количество калорий:");

        jLabel10.setText("Потребление воды:");

        jButton1.setText("+");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("+");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setText("+");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton4.setText("+");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jButton5.setText("+");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        jButton6.setText("Назад");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(45, 45, 45)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel9)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jProgressBar8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, 70, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jProgressBar5, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE)
                                    .addComponent(jProgressBar6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jProgressBar7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jButton6)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jProgressBar9, javax.swing.GroupLayout.PREFERRED_SIZE, 169, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jButton5))))))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(31, 31, 31)
                        .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(46, 46, 46)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, 70, Short.MAX_VALUE)
                            .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jProgressBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE)
                            .addComponent(jProgressBar2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jProgressBar3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jProgressBar4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButton4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(182, 182, 182)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(25, 25, 25))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jProgressBar2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jProgressBar3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jProgressBar4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6)
                    .addComponent(jProgressBar5, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7)
                    .addComponent(jProgressBar6, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jProgressBar7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jProgressBar8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jProgressBar9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(jButton6)
                .addContainerGap(52, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        dispose();
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        LocalDate localDate = selectedDate.toInstant()
                          .atZone(ZoneId.systemDefault())
                          .toLocalDate();
        AddMealProduct addWindow = new AddMealProduct(this, "завтрак", profile.getId(), localDate);
        addWindow.setVisible(true);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        LocalDate localDate = selectedDate.toInstant()
                          .atZone(ZoneId.systemDefault())
                          .toLocalDate();
        AddMealProduct addWindow = new AddMealProduct(this, "обед", profile.getId(), localDate);
        addWindow.setVisible(true);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        LocalDate localDate = selectedDate.toInstant()
                          .atZone(ZoneId.systemDefault())
                          .toLocalDate();
        AddMealProduct addWindow = new AddMealProduct(this, "перекус", profile.getId(), localDate);
        addWindow.setVisible(true);
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        LocalDate localDate = selectedDate.toInstant()
                          .atZone(ZoneId.systemDefault())
                          .toLocalDate();
        AddMealProduct addWindow = new AddMealProduct(this, "ужин", profile.getId(), localDate);
        addWindow.setVisible(true);
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        String input = JOptionPane.showInputDialog(this, "Введите количество выпитой воды (в литрах):", "Добавить воду", JOptionPane.PLAIN_MESSAGE);

        if (input != null && !input.trim().isEmpty()) {
            try {
                double liters = Double.parseDouble(input.trim());
                if (liters <= 0) {
                    JOptionPane.showMessageDialog(this, "Введите положительное число.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                LocalDate date = selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

                try (Connection conn = DatabaseConnector.connect()) {
                    // Вставка новой записи
                    String insertSql = "INSERT INTO waterintake (user_id, intake_date, amount_liters) VALUES (?, ?, ?)";
                    try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                        stmt.setInt(1, profile.getId());
                        stmt.setDate(2, java.sql.Date.valueOf(date));
                        stmt.setDouble(3, liters);
                        stmt.executeUpdate();
                    }

                    // После успешного добавления — обновим весь прогресс
                    updateNutritionData();

                    JOptionPane.showMessageDialog(this, "Вода успешно добавлена!", "Успех", JOptionPane.INFORMATION_MESSAGE);

                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Ошибка при добавлении данных в БД.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                }

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Некорректный формат числа.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_jButton5ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
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
    private javax.swing.JSeparator jSeparator1;
    // End of variables declaration//GEN-END:variables
}
