/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.vdjoseluis.vdlogistics.ui;

import com.vdjoseluis.vdlogistics.firebase.FirebaseAuthService;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 *
 * @author vdjos
 */
public class LoginFrame extends javax.swing.JFrame {

    /**
     * Creates new form LoginFrame
     */
    public LoginFrame() {
        initComponents();
        this.setLocationRelativeTo(null);
        SwingUtilities.invokeLater(() -> jTextFieldEmail.requestFocusInWindow());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButtonExit = new javax.swing.JButton();
        logo = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jTextFieldEmail = new javax.swing.JTextField();
        jPasswordField = new javax.swing.JPasswordField();
        jLabel3 = new javax.swing.JLabel();
        jButtonLogin = new javax.swing.JButton();
        background = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jButtonExit.setBackground(new java.awt.Color(9, 25, 77));
        jButtonExit.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 12)); // NOI18N
        jButtonExit.setForeground(new java.awt.Color(255, 255, 255));
        jButtonExit.setText("Salir");
        jButtonExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonExitActionPerformed(evt);
            }
        });
        getContentPane().add(jButtonExit, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 10, -1, -1));

        logo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        logo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/logo.png"))); // NOI18N
        logo.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        logo.setMaximumSize(new java.awt.Dimension(367, 392));
        logo.setMinimumSize(new java.awt.Dimension(367, 392));
        logo.setPreferredSize(new java.awt.Dimension(367, 392));
        getContentPane().add(logo, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 20, 180, 200));

        jLabel1.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 14)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Email:");
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 280, -1, -1));

        jTextFieldEmail.setBackground(new java.awt.Color(9, 25, 77));
        jTextFieldEmail.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 13)); // NOI18N
        jTextFieldEmail.setForeground(new java.awt.Color(255, 255, 255));
        jTextFieldEmail.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jTextFieldEmail.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        jTextFieldEmail.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldEmailActionPerformed(evt);
            }
        });
        getContentPane().add(jTextFieldEmail, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 270, 250, 30));

        jPasswordField.setBackground(new java.awt.Color(9, 25, 77));
        jPasswordField.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 13)); // NOI18N
        jPasswordField.setForeground(new java.awt.Color(255, 255, 255));
        jPasswordField.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        getContentPane().add(jPasswordField, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 320, 250, 30));

        jLabel3.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 14)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("Contraseña:");
        getContentPane().add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 330, -1, -1));

        jButtonLogin.setBackground(new java.awt.Color(0, 68, 85));
        jButtonLogin.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 12)); // NOI18N
        jButtonLogin.setForeground(new java.awt.Color(255, 255, 255));
        jButtonLogin.setText("Iniciar sesión");
        jButtonLogin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonLoginActionPerformed(evt);
            }
        });
        getContentPane().add(jButtonLogin, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 380, -1, 30));

        background.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/background.png"))); // NOI18N
        background.setMaximumSize(new java.awt.Dimension(4000, 2250));
        background.setName(""); // NOI18N
        background.setPreferredSize(new java.awt.Dimension(4000, 2250));
        getContentPane().add(background, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 450, 450));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExitActionPerformed
        System.exit(0);
    }//GEN-LAST:event_jButtonExitActionPerformed

    private void jTextFieldEmailActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldEmailActionPerformed
    }//GEN-LAST:event_jTextFieldEmailActionPerformed

    private void jButtonLoginActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonLoginActionPerformed
        this.loginUser();
    }//GEN-LAST:event_jButtonLoginActionPerformed

    private void loginUser() {
        String email = this.jTextFieldEmail.getText();
        String password = new String (this.jPasswordField.getPassword());
       if (FirebaseAuthService.loginUser(email, password)) {
            //JOptionPane.showMessageDialog(this, "Login exitoso!");
            dispose();
            MainFrame home = new MainFrame();
                home.setVisible(true);
                this.dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Error de autenticación");
        }     
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(LoginFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(LoginFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(LoginFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(LoginFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new LoginFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel background;
    private javax.swing.JButton jButtonExit;
    private javax.swing.JButton jButtonLogin;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPasswordField jPasswordField;
    private javax.swing.JTextField jTextFieldEmail;
    private javax.swing.JLabel logo;
    // End of variables declaration//GEN-END:variables
}
