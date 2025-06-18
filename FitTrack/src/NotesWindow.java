import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.*;
import java.sql.*;

public class NotesWindow extends javax.swing.JFrame {

    private UserProfile currentUserProfile;

    public NotesWindow(UserProfile profile) {
        initComponents();
        this.currentUserProfile = profile;
        loadNotesFromDatabase();
        this.setupTablePopupMenu();
    }
    
    // Данные от БД
    String user = "postgres";
    String pwd = "postgresql";
    String dbUrl = "jdbc:postgresql://localhost:5432/fittrack";
    
    // Загрузка заметок из БД
    private void loadNotesFromDatabase() {
        DefaultTableModel model = new DefaultTableModel(new Object[]{"Номер заметки", "Текст заметки"}, 0);
        jTable1.setModel(model);

        String query = "SELECT note_id, note_text FROM notes WHERE user_id = ?";
        try (Connection conn = DriverManager.getConnection(dbUrl, user, pwd);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, currentUserProfile.getId());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int noteId = rs.getInt("note_id");
                String noteText = rs.getString("note_text");
                model.addRow(new Object[]{noteId, noteText});
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Ошибка загрузки заметок");
        }
    }
    
    private void setupTablePopupMenu() {
        jTable1.addMouseListener(new MouseAdapter() {
            private void showPopup(MouseEvent e) {
                int row = jTable1.rowAtPoint(e.getPoint());
                if (row >= 0 && row < jTable1.getRowCount()) {
                    jTable1.setRowSelectionInterval(row, row); // выделить строку

                    JPopupMenu popup = new JPopupMenu();

                    JMenuItem deleteItem = new JMenuItem("Удалить");
                    deleteItem.addActionListener(event -> {
                        int noteId = (int) jTable1.getValueAt(row, 0);
                        deleteNoteById(noteId);
                    });

                    JMenuItem editItem = new JMenuItem("Изменить");
                    editItem.addActionListener(event -> {
                        int noteId = (int) jTable1.getValueAt(row, 0);
                        String oldText = (String) jTable1.getValueAt(row, 1);

                        String newText = JOptionPane.showInputDialog(
                            NotesWindow.this,
                            "Измените текст заметки:",
                            oldText
                        );

                        if (newText != null && !newText.trim().isEmpty() && !newText.equals(oldText)) {
                            updateNoteText(noteId, newText.trim());
                        }
                    });

                    popup.add(editItem);
                    popup.add(deleteItem);
                    popup.show(e.getComponent(), e.getX(), e.getY());
                }
            }

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
        });
    }
    
    private void deleteNoteById(int noteId) {
        String query = "DELETE FROM notes WHERE note_id = ?";
        try (Connection conn = DriverManager.getConnection(dbUrl, user, pwd);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, noteId);
            stmt.executeUpdate();

            loadNotesFromDatabase();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Ошибка при удалении заметки");
        }
    }
    
    private void updateNoteText(int noteId, String newText) {
        String query = "UPDATE notes SET note_text = ? WHERE note_id = ?";
        try (Connection conn = DriverManager.getConnection(dbUrl, user, pwd);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, newText);
            stmt.setInt(2, noteId);
            stmt.executeUpdate();

            loadNotesFromDatabase();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Ошибка при обновлении заметки");
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jButton1 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("Заметки");

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

        jButton1.setText("Добавить заметку");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton4.setText("Назад");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(170, 170, 170)
                        .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(166, 166, 166))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(76, 76, 76)
                        .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(30, 30, 30)
                        .addComponent(jButton4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(64, 64, 64))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(19, 19, 19)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
                .addGap(18, 18, 18))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 185, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(32, 32, 32))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        String noteText = JOptionPane.showInputDialog(this, "Введите текст заметки");
        if (noteText == null || noteText.trim().isEmpty()) {
            return; // отмена или пусто
        }

        String query = "INSERT INTO notes (user_id, note_text, note_date) VALUES (?, ?, CURRENT_TIMESTAMP)";
        try (Connection conn = DriverManager.getConnection(dbUrl, user, pwd);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, currentUserProfile.getId());
            stmt.setString(2, noteText.trim());
            stmt.executeUpdate();

            loadNotesFromDatabase();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Ошибка при добавлении заметки");
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        dispose();
    }//GEN-LAST:event_jButton4ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables
}
