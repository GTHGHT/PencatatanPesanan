/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package form;

import com.formdev.flatlaf.FlatLightLaf;
import util.MysqlDataSource;

import javax.sql.DataSource;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Objects;

/**
 *
 * @author FH6CLUE
 */
public class KelolaPesanan extends javax.swing.JFrame {

    Object[] barangColumnNames = new Object[]{"ID Barang Pesanan","Barcode","Nama Barang","Jumlah Barang","Tanggal Expired"};
    Object[] pesananColumnNames = new Object[]{"ID Pesanan", "Tanggal Pesan", "Tanggal Terima", "Status"};
    DataSource ds = MysqlDataSource.getDataSource();

    public KelolaPesanan() {
        initComponents();
        idField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                loadBarang();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });
        loadPesanan();
    }

    public void loadPesanan(){
        String loadPesananSQL = "SELECT p.id_pesanan, p.tanggal_pesan, p.tanggal_terima, p.status FROM pesanan p";
        try (
             Connection con = Objects.requireNonNull(ds).getConnection();
             PreparedStatement preparedStatement = con.prepareStatement(loadPesananSQL);
             ResultSet result = preparedStatement.executeQuery()
        ){
            DefaultTableModel tableModel = new DefaultTableModel(pesananColumnNames, 0){
                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    if(columnIndex == 1 || columnIndex == 2){
                        return Date.class;
                    }else {
                        return String.class;
                    }
                }

                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            while(result.next()){
                tableModel.addRow(new Object[]{
                        result.getString("id_pesanan"),
                        result.getDate("tanggal_pesan"),
                        result.getDate("tanggal_terima"),
                        result.getString("status")
                });
            }
            pesananTabel.setModel(tableModel);
        }catch (SQLException e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Koneksi Ke Database Pesanan Gagal,\nSilahkan Cek Server Database Anda",
                    "Koneksi Gagal",JOptionPane.ERROR_MESSAGE);
        }

    }
    
    public void searchPesanan(){
        String searchPesananSQL =
                "SELECT p.id_pesanan, p.tanggal_pesan, p.tanggal_terima, p.status FROM pesanan p " +
                        "LEFT JOIN barang_pesanan bp USING(id_pesanan) " +
                        "WHERE id_pesanan = ? " +
                        "OR status LIKE ? OR bp.barcode LIKE ? OR bp.nama_barang LIKE ?";
        try (
            Connection con = Objects.requireNonNull(ds).getConnection();
            PreparedStatement preparedStatement = con.prepareStatement(searchPesananSQL)
        ){
            preparedStatement.setString(1, cariField.getText());
            preparedStatement.setString(2, cariField.getText());
            preparedStatement.setString(3, cariField.getText());
            preparedStatement.setString(4, cariField.getText());
            try(ResultSet result = preparedStatement.executeQuery()){
                DefaultTableModel tableModel = new DefaultTableModel(pesananColumnNames, 0){
                    @Override
                    public Class<?> getColumnClass(int columnIndex) {
                        if(columnIndex == 1 || columnIndex == 2){
                            return Date.class;
                        }else {
                            return String.class;
                        }
                    }

                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                };
                while(result.next()){
                    tableModel.addRow(new Object[]{
                            result.getString("id_pesanan"),
                            result.getDate("tanggal_pesan"),
                            result.getDate("tanggal_terima"),
                            result.getString("status")
                    });
                }
                pesananTabel.setModel(tableModel);
            }catch (SQLException ex){
                ex.printStackTrace();
            }
        }catch (SQLException e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Koneksi Ke Database Pesanan Gagal,\nSilahkan Cek Server Database Anda",
                    "Koneksi Gagal",JOptionPane.ERROR_MESSAGE);
        }
    }

    public void loadBarang(){
        String loadBarangSql = "Select bp.id_barang_pesanan, bp.barcode, bp.nama_barang, bp.jumlah_barang, bp.expired " +
                "FROM barang_pesanan bp LEFT JOIN pesanan USING(id_pesanan) WHERE id_pesanan = ?";
        try(
                Connection con = Objects.requireNonNull(ds).getConnection();
                PreparedStatement ps = con.prepareStatement(loadBarangSql)
        ){
            ps.setString(1, idField.getText());
            try (ResultSet result = ps.executeQuery()){
                DefaultTableModel tableModel = new DefaultTableModel(barangColumnNames, 0){
                    @Override
                    public Class<?> getColumnClass(int columnIndex) {
                        return String.class;
                    }

                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                };
                while (result.next()){
                    tableModel.addRow(new Object[]{
                            result.getString("id_barang_pesanan"),
                            result.getString("barcode"),
                            result.getString("nama_barang"),
                            result.getString("jumlah_barang"),
                            result.getString("expired")
                    });
                }
                barangTabel.setModel(tableModel);
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void clearField(){
        cariField.setText("");
        idField.setText("");
        idField.setToolTipText("");
        barangTabel.setModel(new DefaultTableModel(barangColumnNames, 0){
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 4 ? java.util.Date.class : String.class;
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }
        });
    }

    public void fillField(){
        DefaultTableModel tableModel = (DefaultTableModel) pesananTabel.getModel();
        int selectedRow = pesananTabel.getSelectedRow();
        idField.setText((String) tableModel.getValueAt(selectedRow, 0));
        idField.setToolTipText((String) tableModel.getValueAt(selectedRow, 3));
    }
    
    public void rollbackConnection(boolean AutoCommitState){
        try(Connection con = Objects.requireNonNull(ds).getConnection()){
            if(!con.getAutoCommit()){
                con.rollback();
                con.setAutoCommit(AutoCommitState);
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
    }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.JPanel jPanel1 = new javax.swing.JPanel();
        javax.swing.JPanel jPanel3 = new javax.swing.JPanel();
        javax.swing.JLabel jLabel2 = new javax.swing.JLabel();
        kembaliButton = new javax.swing.JButton();
        javax.swing.JPanel jPanel2 = new javax.swing.JPanel();
        javax.swing.JLabel jLabel3 = new javax.swing.JLabel();
        idField = new javax.swing.JTextField();
        javax.swing.JButton clearButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        barangTabel = new javax.swing.JTable();
        javax.swing.JLabel jLabel4 = new javax.swing.JLabel();
        editBarangButton = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        pesananTabel = new javax.swing.JTable();
        javax.swing.JLabel jLabel5 = new javax.swing.JLabel();
        cariField = new javax.swing.JTextField();
        clearCariButton = new javax.swing.JButton();
        cariButton = new javax.swing.JButton();
        javax.swing.JButton hapusButton = new javax.swing.JButton();
        javax.swing.JButton refreshButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 202, 192));

        jPanel3.setBackground(new java.awt.Color(254, 176, 161));
        jPanel3.setPreferredSize(new java.awt.Dimension(374, 50));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Kelola Pesanan");

        kembaliButton.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        kembaliButton.setText("Kembali");
        kembaliButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                kembaliButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(kembaliButton, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 948, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(kembaliButton)
                    .addComponent(jLabel2))
                .addContainerGap())
        );

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setPreferredSize(new java.awt.Dimension(300, 425));

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel3.setText("ID Pesanan");

        idField.setEditable(false);
        idField.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        clearButton.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        clearButton.setText("Clear");
        clearButton.setPreferredSize(new java.awt.Dimension(70, 23));
        clearButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearButtonActionPerformed(evt);
            }
        });

        barangTabel.setModel(new DefaultTableModel(barangColumnNames, 4){
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 4 ? Date.class : String.class;
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }
        });
        jScrollPane1.setViewportView(barangTabel);

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel4.setText("Daftar Barang Pesanan");

        editBarangButton.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        editBarangButton.setText("Edit Barang");
        editBarangButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editBarangButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(clearButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(idField)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 54, Short.MAX_VALUE)
                        .addComponent(editBarangButton, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addGap(25, 25, 25))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(idField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(editBarangButton))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(clearButton, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(23, 23, 23))
        );

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));

        pesananTabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                pesananTabelMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(pesananTabel);

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel5.setText("Cari");

        cariField.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        clearCariButton.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        clearCariButton.setText("Clear");
        clearCariButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearCariButtonActionPerformed(evt);
            }
        });

        cariButton.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        cariButton.setText("Cari");
        cariButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cariButtonActionPerformed(evt);
            }
        });

        hapusButton.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        hapusButton.setText("Hapus Pesanan");
        hapusButton.setPreferredSize(new java.awt.Dimension(70, 23));
        hapusButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hapusButtonActionPerformed(evt);
            }
        });

        refreshButton.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        refreshButton.setText("Refresh");
        refreshButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        refreshButton.setPreferredSize(new java.awt.Dimension(70, 23));
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(cariField, javax.swing.GroupLayout.PREFERRED_SIZE, 450, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(cariButton, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(clearCariButton, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 726, Short.MAX_VALUE)))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(refreshButton, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(hapusButton, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(20, 20, 20))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cariField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5)
                    .addComponent(clearCariButton)
                    .addComponent(cariButton))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 443, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(hapusButton, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(refreshButton, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, 1173, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 377, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 567, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void hapusButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hapusButtonActionPerformed
        String idPesanan;
        if(idField.getText().equals("")){
            TableModel tableModel = pesananTabel.getModel();
            int selectedRow = barangTabel.getSelectedRow();
            if(tableModel.getValueAt(selectedRow, 3).toString().equals("Sudah Diterima")){
                JOptionPane.showMessageDialog(rootPane,
                        "Pesanan Sudah Diterima, Tidak Bisa Diubah Atau Dihapus.",
                        "Penghapusan Gagal", JOptionPane.ERROR_MESSAGE);
                return;
            }else {
                idPesanan = (String) tableModel.getValueAt(selectedRow, 0);
            }
        }else {
            if(idField.getToolTipText().equals("Sudah Diterima")){
                JOptionPane.showMessageDialog(rootPane,
                        "Pesanan Sudah Diterima, Tidak Bisa Diubah Atau Dihapus.",
                        "Penghapusan Gagal", JOptionPane.ERROR_MESSAGE);
                return;
            }else{
                idPesanan = idField.getText();
            }

        }
        rollbackConnection(false);
        String delPesanan = "DELETE barang_pesanan, pesanan FROM barang_pesanan LEFT JOIN pesanan using(id_pesanan) WHERE id_pesanan = ?";
        try(
                Connection con = Objects.requireNonNull(ds).getConnection();
                PreparedStatement ps = con.prepareStatement(delPesanan)
        ){
            ps.setString(1, idPesanan);
            ps.executeUpdate();
            if (!con.getAutoCommit()){
                con.commit();
            }
        }catch (SQLException e){
            e.printStackTrace();
            rollbackConnection(false);
        }
    }//GEN-LAST:event_hapusButtonActionPerformed

    private void clearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearButtonActionPerformed
        clearField();
    }//GEN-LAST:event_clearButtonActionPerformed

    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
        clearCariButtonActionPerformed(null);
    }//GEN-LAST:event_refreshButtonActionPerformed

    private void cariButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cariButtonActionPerformed
        searchPesanan();
    }//GEN-LAST:event_cariButtonActionPerformed

    private void clearCariButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearCariButtonActionPerformed
        cariField.setText("");
        loadPesanan();
    }//GEN-LAST:event_clearCariButtonActionPerformed

    private void kembaliButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_kembaliButtonActionPerformed
        rollbackConnection(true);
        new MainMenu().setVisible(true);
        this.dispose();
    }//GEN-LAST:event_kembaliButtonActionPerformed

    private void pesananTabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pesananTabelMouseClicked
        if(evt.getClickCount() > 1){
            fillField();
            rollbackConnection(false);
        }
    }//GEN-LAST:event_pesananTabelMouseClicked

    private void editBarangButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editBarangButtonActionPerformed
        if (!idField.getText().equals("")) {
            TableModel tableModel = barangTabel.getModel();
            EditBarang editBarang = new EditBarang(this, ds, tableModel, idField.getText());
            editBarang.setVisible(true);
        }else {
            JOptionPane.showMessageDialog(rootPane,
                    "ID Pesanan Kosong, Silahkan Pilih Pesanan Terlebih Dahulu",
                    "Aksi Ditolak",JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_editBarangButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            UIManager.setLookAndFeel("Windows");
        }
        //</editor-fold>


        java.awt.EventQueue.invokeLater(() -> new KelolaPesanan().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable barangTabel;
    private javax.swing.JButton cariButton;
    private javax.swing.JTextField cariField;
    private javax.swing.JButton clearCariButton;
    private javax.swing.JButton editBarangButton;
    private javax.swing.JTextField idField;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JButton kembaliButton;
    private javax.swing.JTable pesananTabel;
    // End of variables declaration//GEN-END:variables
}
