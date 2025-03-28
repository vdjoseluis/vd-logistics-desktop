package com.vdjoseluis.vdlogistics.firebase.data;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.toedter.calendar.JDateChooser;
import com.vdjoseluis.vdlogistics.firebase.FirebaseConfig;
import com.vdjoseluis.vdlogistics.models.Service;
import com.vdjoseluis.vdlogistics.models.User;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

public class DataLogs {

    private static final Firestore db = FirebaseConfig.getFirestore();

    private static final String[] COLUMN_NAMES = {"ID", "Fecha", "Operario", "Acción Realizada", "Servicio"};

    private static void setColumnModel(JTable table) {
        TableColumnModel model = table.getColumnModel();
        model.getColumn(0).setPreferredWidth(100);
        model.getColumn(1).setPreferredWidth(150);
        model.getColumn(2).setPreferredWidth(240);
        model.getColumn(3).setPreferredWidth(130);
        model.getColumn(4).setPreferredWidth(100);
        for (int i = 0; i < model.getColumnCount(); i++) {
            model.getColumn(i).setResizable(false);
        }
    }

    public static void loadLogs(JTable table, JLabel loadingLabel, JScrollPane scrollPane) {
        CollectionReference logs = db.collection("logs");
        Query query = logs.orderBy("date", Query.Direction.ASCENDING);

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
                    String logId = document.getId();
                    String date = getFormattedDate(document.getDate("date"));

                    DocumentReference operatorRef = (DocumentReference) document.get("refOperator");
                    String operator = (operatorRef != null) ? getFullName(operatorRef) : "Sin operario";
                    
                    String action = document.getString("action");

                    DocumentReference serviceRef = (DocumentReference) document.get("refService");
                    String service = (serviceRef != null) ? serviceRef.getId() : "Sin servicio";

                    model.addRow(new Object[]{
                        logId,
                        date,
                        operator,
                        action,
                        service
                    });
                }
            } else {
                model.addRow(new Object[]{"", "No hay actividad", "", ""});
            }

            SwingUtilities.invokeLater(() -> {
                table.setModel(model); 
                setColumnModel(table);
                loadingLabel.setVisible(false);
                table.setVisible(true);
            });
        });
    }

    private static String getFormattedDate(Date date) {
        SimpleDateFormat formatDate = new SimpleDateFormat("dd-MM-yyyy   /  HH:mm");
        return formatDate.format(date);
    }

    private static String getFullName(DocumentReference ref) {
        try {
            DocumentSnapshot doc = ref.get().get();
            String firstName = doc.getString("firstName");
            String lastName = doc.getString("lastName");
            return (firstName != null && lastName != null) ? firstName + " " + lastName : "Desconocido";
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return "Desconocido";
        }
    }
    
    public static boolean registerLog(String email, String action, String serviceId) {
        try {
            DocumentReference docRef = db.collection("logs").document();
            
            String userId = FirebaseAuth.getInstance().getUserByEmail(email).getUid();
                        
            Map<String, Object> data = new HashMap<>();
            data.put("date", Timestamp.now());
            data.put("refOperator", db.collection("users").document(userId));
            data.put("action", action);
            data.put("refService", db.collection("services").document(serviceId));

            docRef.set(data);
            return true;
        } catch (FirebaseAuthException e) {
            System.err.println("❌ Error creando servicio: " + e.getMessage());
            return false;
        }
    }

}
