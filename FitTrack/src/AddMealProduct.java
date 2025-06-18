// Добавление продукта в употребленное

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;

public class AddMealProduct extends javax.swing.JFrame {
    
    private String mealType;
    private int userId;
    private LocalDate date;
    private JFrame parentWindow;
    
    // Модель таблицы и список добавленных продуктов
    private DefaultTableModel addedProductsModel;
    private List<ProductEntry> addedProducts;
    
    // Класс-обёртка
    private static class ProductEntry {
        int productId;
        String name;
        double calories, proteins, fats, carbs, weight;

        public ProductEntry(int productId, String name, double cal, double prot, double fat, double carb, double weight) {
            this.productId = productId;
            this.name = name;
            this.calories = cal;
            this.proteins = prot;
            this.fats = fat;
            this.carbs = carb;
            this.weight = weight;
        }
    }

    public AddMealProduct(JFrame parentWindow, String mealType, int userId, LocalDate date) {
        this.parentWindow = parentWindow;
        this.mealType = mealType;
        this.userId = userId;
        this.date = date;

        initComponents();
        jLabel1.setText("Приём пищи: " + mealType);
        
        // Инициализация модели таблицы добавленных продуктов
        addedProducts = new ArrayList<>();
        addedProductsModel = new DefaultTableModel(new Object[]{"Продукт", "Вес (г/мл)", "Ккал", "Белки", "Жиры", "Углеводы"}, 0);
        jTable2.setModel(addedProductsModel);
        jList1.setModel(new DefaultListModel<>()); // Очистка списка
        jList1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Добавим KeyListener на jTextField1 для поиска продуктов при вводе
        jTextField1.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                String input = jTextField1.getText().trim();
                if (input.length() >= 1) {
                    updateProductSuggestions(input);
                } else {
                    // Очистить список, если пусто
                    jList1.setListData(new String[0]);
                }
            }
        });
        
         // Добавляем MouseListener для клика по элементу списка
        jList1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) { // одиночный клик
                    String selected = jList1.getSelectedValue();
                    if (selected != null) {
                        jTextField1.setText(selected);
                        // Можно очистить список, чтобы убрать подсказки после выбора
                        jList1.setListData(new String[0]);
                    }
                }
            }
        });

        // Добавляем KeyListener для выбора элемента по Enter
        jList1.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String selected = jList1.getSelectedValue();
                    if (selected != null) {
                        jTextField1.setText(selected);
                        jList1.setListData(new String[0]);
                    }
                }
            }
        });
        
        // Удаление продукта из таблицы (ПКМ)
        jTable2.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopup(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopup(e);
                }
            }

            private void showPopup(MouseEvent e) {
                int row = jTable2.rowAtPoint(e.getPoint());
                if (row >= 0 && row < jTable2.getRowCount()) {
                    jTable2.setRowSelectionInterval(row, row); // выделить строку

                    JPopupMenu popup = new JPopupMenu();
                    JMenuItem deleteItem = new JMenuItem("Удалить");
                    deleteItem.addActionListener(event -> {
                        // Удаление из модели таблицы
                        addedProductsModel.removeRow(row);

                        // Удаление из списка addedProducts
                        if (row < addedProducts.size()) {
                            addedProducts.remove(row);
                        }
                    });

                    popup.add(deleteItem);
                    popup.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }

    // Метод обновления списка продуктов
    private void updateProductSuggestions(String input) {
        try (Connection conn = DriverManager.getConnection(dbUrl, user, pwd)) {
            String sql = "SELECT product_name FROM products WHERE LOWER(product_name) LIKE LOWER(?) ORDER BY product_name LIMIT 10";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, "%" + input + "%");
            ResultSet rs = ps.executeQuery();

            List<String> products = new ArrayList<>();
            while (rs.next()) {
                products.add(rs.getString("product_name"));
            }
            jList1.setListData(products.toArray(new String[0]));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jButton2 = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList<>();
        jButton3 = new javax.swing.JButton();

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

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("Время:");

        jLabel2.setText("Добавление продукта:");

        jLabel3.setText("Объём продукта (г/мл):");

        jButton1.setText("Добавить");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jLabel4.setText("Добавленные продукты:");

        jTable2.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane2.setViewportView(jTable2);

        jButton2.setText("Сохранить");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jList1.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        jScrollPane3.setViewportView(jList1);

        jButton3.setText("Назад");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(34, 34, 34)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 1, Short.MAX_VALUE)
                                .addComponent(jLabel3)
                                .addGap(18, 18, 18)
                                .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(127, 127, 127))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 280, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2)
                            .addComponent(jLabel4))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(163, 163, 163)
                        .addComponent(jButton1))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(173, 173, 173)
                        .addComponent(jLabel1))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(19, 19, 19)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 375, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(116, 116, 116)
                        .addComponent(jButton2)
                        .addGap(18, 18, 18)
                        .addComponent(jButton3)))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(7, 7, 7)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 60, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addGap(18, 18, 18)
                .addComponent(jButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton2)
                    .addComponent(jButton3))
                .addGap(46, 46, 46))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    String user = "postgres";
    String pwd = "postgresql";
    String dbUrl = "jdbc:postgresql://localhost:5432/fittrack";
    
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        String selectedProduct = jTextField1.getText().trim();
        String weightText = jTextField2.getText().trim();

        if (selectedProduct == null || selectedProduct.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Выберите продукт.");
            return;
        }

        if (weightText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Введите массу продукта.");
            return;
        }

        try {
            double weight = Double.parseDouble(weightText);

            try (Connection conn = DriverManager.getConnection(dbUrl, user, pwd)) {
                String sql = "SELECT * FROM products WHERE product_name = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, selectedProduct);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    int productId = rs.getInt("product_id");
                    double cal = rs.getDouble("calories_per_100g") * weight / 100;
                    double prot = rs.getDouble("proteins_per_100g") * weight / 100;
                    double fat = rs.getDouble("fats_per_100g") * weight / 100;
                    double carb = rs.getDouble("carbs_per_100g") * weight / 100;

                    // Добавляем в таблицу и список
                    addedProducts.add(new ProductEntry(productId, selectedProduct, cal, prot, fat, carb, weight));
                    addedProductsModel.addRow(new Object[] {
                        selectedProduct, weight, cal, prot, fat, carb
                    });
                } else {
                    JOptionPane.showMessageDialog(this, "Продукт не найден в базе данных.");
                }

            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Ошибка при подключении к БД.");
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Введите корректное число в поле массы.");
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        if (addedProducts.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Список продуктов пуст. Добавьте хотя бы один продукт.");
            return;
        }

        try (Connection conn = DriverManager.getConnection(dbUrl, user, pwd)) {
            conn.setAutoCommit(false);

            // Пример: userId, mealType, date должны быть доступны в твоём классе
            String mealInsert = "INSERT INTO meals (user_id, meal_datetime, meal_type) VALUES (?, ?, ?) RETURNING meal_id";
            PreparedStatement mealStmt = conn.prepareStatement(mealInsert);
            mealStmt.setInt(1, userId);
            mealStmt.setTimestamp(2, Timestamp.valueOf(date.atStartOfDay()));
            
            String normalizedMealType; // Привод mealType к нужному виду
            switch (mealType.toLowerCase()) {
                case "завтрак":
                    normalizedMealType = "Завтрак";
                    break;
                case "обед":
                    normalizedMealType = "Обед";
                    break;
                case "ужин":
                    normalizedMealType = "Ужин";
                    break;
                case "перекус":
                    normalizedMealType = "Перекус";
                    break;
                default:
                    throw new IllegalArgumentException("Неверный тип приёма пищи: " + mealType);
            }

            mealStmt.setString(3, normalizedMealType);
            
            ResultSet rs = mealStmt.executeQuery();

            if (rs.next()) {
                int mealId = rs.getInt("meal_id");

                String productInsert = "INSERT INTO meal_products (meal_id, product_id, weight_product) VALUES (?, ?, ?)";
                PreparedStatement prodStmt = conn.prepareStatement(productInsert);

                for (ProductEntry entry : addedProducts) {
                    prodStmt.setInt(1, mealId);
                    prodStmt.setInt(2, entry.productId);
                    prodStmt.setDouble(3, entry.weight);
                    prodStmt.addBatch();
                }

                prodStmt.executeBatch();
                conn.commit();
                
                if (parentWindow instanceof UserProfileWindow) {
                    ((UserProfileWindow) parentWindow).updateNutritionData();
                }

                JOptionPane.showMessageDialog(this, "Данные успешно сохранены.");
                dispose(); // Закрыть окно
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Ошибка при сохранении данных.");
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        dispose();
    }//GEN-LAST:event_jButton3ActionPerformed

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JList<String> jList1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    // End of variables declaration//GEN-END:variables
}
