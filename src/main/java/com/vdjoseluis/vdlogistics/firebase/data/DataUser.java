package com.vdjoseluis.vdlogistics.firebase.data;

import com.google.cloud.firestore.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.vdjoseluis.vdlogistics.firebase.FirebaseConfig;
import com.vdjoseluis.vdlogistics.models.User;
import com.vdjoseluis.vdlogistics.ui.LoginFrame;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

public class DataUser {

    private static final Firestore db = FirebaseConfig.getFirestore();

    private static final String[] COLUMN_NAMES = {"ID", "Nombre", "Tipo Usuario", "Teléfono", "Email", "Dirección"};

    private static void setColumnModel(JTable table) {
        TableColumnModel model = table.getColumnModel();
        model.getColumn(0).setPreferredWidth(110);
        model.getColumn(1).setPreferredWidth(260);
        model.getColumn(2).setPreferredWidth(150);
        model.getColumn(3).setPreferredWidth(150);
        model.getColumn(4).setPreferredWidth(170);
        model.getColumn(5).setPreferredWidth(260);
        for (int i = 0; i < model.getColumnCount(); i++) {
            model.getColumn(i).setResizable(false);
        }
    }

    public static void loadUsers(JTable table, JLabel loadingLabel, JScrollPane scrollPane) {
        CollectionReference users = db.collection("users");
        Query query = users.orderBy("type", Query.Direction.ASCENDING);

        SwingUtilities.invokeLater(() -> {
            loadingLabel.setBounds(scrollPane.getX() + 640, scrollPane.getY() + 120, 100, 100);
            scrollPane.getParent().add(loadingLabel);
            scrollPane.getParent().setComponentZOrder(loadingLabel, 0);
            loadingLabel.setVisible(true);
            table.setVisible(false);
        });

        query.addSnapshotListener((snapshots, error) -> {
            if (error != null) {
                System.err.println("❌ Error al escuchar cambios en Firestore: " + error.getMessage());
                return;
            }

            DefaultTableModel model = new DefaultTableModel(COLUMN_NAMES, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            if (snapshots != null && !snapshots.isEmpty()) {
                for (QueryDocumentSnapshot document : snapshots) {
                    String userId = document.getId();

                    String fullName = document.getString("firstName") + " " + document.getString("lastName");

                    String operatorType = document.getString("type");

                    String phone = document.getString("phone");

                    String email = document.getString("email");

                    String address = document.getString("address");

                    model.addRow(new Object[]{
                        userId,
                        fullName,
                        operatorType,
                        phone,
                        email,
                        address
                    });
                }
            } else {
                model.addRow(new Object[]{"", "No hay ususarios", "", "", "", ""});
            }

            SwingUtilities.invokeLater(() -> {
                table.setModel(model); // Actualiza la tabla            
                setColumnModel(table);
                loadingLabel.setVisible(false);
                table.setVisible(true);
            });
        });
    }

    public static boolean createUser(User user, String password) {
        try {
            UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                    .setEmail(user.getEmail())
                    .setPassword(password)
                    .setEmailVerified(true)
                    .setDisabled(false);

            UserRecord userRecord = FirebaseAuth.getInstance().createUser(request);
            String userId = userRecord.getUid();

            DocumentReference docRef = db.collection("users").document(userId);
            WriteResult result = docRef.set(user).get();

            System.out.println("✅ Usuario creado en Authentication y Firestore: " + user.getEmail());
            return true;
        } catch (FirebaseAuthException | InterruptedException | ExecutionException e) {
            System.err.println("❌ Error creando usuario: " + e.getMessage());
            return false;
        }
    }

    public static Map<String, String> getDataUser(String userId) {
        Map<String, String> userData = new HashMap<>();
        try {
            DocumentReference userRef = db.collection("users").document(userId);
            DocumentSnapshot document = userRef.get().get();

            if (document.exists()) {
                userData.put("id", userId);
                userData.put("email", document.getString("email"));
                userData.put("firstName", document.getString("firstName"));
                userData.put("lastName", document.getString("lastName"));
                userData.put("phone", document.getString("phone"));
                userData.put("address", document.getString("address"));
                userData.put("type", document.getString("type"));
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return userData;
    }

    public static User getCurrentUser(String email) {
        try {
            String uid = FirebaseAuth.getInstance().getUserByEmail(email).getUid();
            DocumentSnapshot doc = db.collection("users").document(uid).get().get();
            if (doc.exists()) {
                String firstName = doc.getString("firstName");
                String lastName = doc.getString("lastName");
                return new User(firstName, lastName);
            }
        } catch (FirebaseAuthException | InterruptedException | ExecutionException e) {
            System.err.println("❌ Error obteniendo usuario autenticado: " + e.getMessage());
        }
        return null;
    }
    
    public static final Map<String, String> operatorMap = new HashMap<>();

    public static void listenForOperatorNames(JComboBox<String> combo) {
        db.collection("users").whereNotEqualTo("type", "Administrativo")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        System.out.println("Error escuchando cambios: " + e.getMessage());
                        return;
                    }
                    if (snapshots != null) {
                        SwingUtilities.invokeLater(() -> {
                            combo.removeAllItems();
                            operatorMap.clear();
                            for (DocumentSnapshot doc : snapshots.getDocuments()) {
                                String id = doc.getId();
                                String name = doc.getString("firstName") + " " + doc.getString("lastName");
                                combo.addItem(name);
                                operatorMap.put(name, id);
                            }
                        });
                    }
                });
    }    

    public static boolean updateUser(String userId, String firstName, String lastName, String phone, String address, String type) {
        try {
            DocumentReference userRef = db.collection("users").document(userId);
            Map<String, Object> updates = new HashMap<>();
            updates.put("firstName", firstName);
            updates.put("lastName", lastName);
            updates.put("phone", phone);
            updates.put("address", address);
            updates.put("type", type);

            userRef.update(updates);
            System.out.println("Usuario actualizado correctamente");
            return true;
        } catch (Exception e) {
            System.err.println("Error al actualizar usuario: " + e.getMessage());
            return false;
        }
    }

    public static boolean deleteUser(String userId, JFrame currentFrame) {
        try {
            UserRecord currentUser = FirebaseAuth.getInstance().getUser(userId);
            String currentUserId = FirebaseAuth.getInstance().getUserByEmail(currentUser.getEmail()).getUid();

            db.collection("users").document(userId).delete().get();
            FirebaseAuth.getInstance().deleteUser(userId);
            System.out.println("Usuario eliminado correctamente");

            if (currentUserId.equals(userId)) {
                JOptionPane.showMessageDialog(null, "Tu cuenta ha sido eliminada. Se cerrará la sesión.");
                currentFrame.dispose();
                new LoginFrame().setVisible(true);
            }

            return true;
        } catch (Exception e) {
            System.err.println("Error al eliminar usuario: " + e.getMessage());
            return false;
        }
    }

}
