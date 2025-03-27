package com.vdjoseluis.vdlogistics.firebase.data;

import com.google.cloud.firestore.*;
import com.vdjoseluis.vdlogistics.firebase.FirebaseConfig;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

public class DataIncident {

    private static final Firestore db = FirebaseConfig.getFirestore();

    private static final String[] COLUMN_NAMES = {"ID", "Fecha", "Operario", "Cliente", "Descripción"};

    private static void setColumnModel(JTable table) {
        TableColumnModel model = table.getColumnModel();
        model.getColumn(0).setPreferredWidth(150);
        model.getColumn(1).setPreferredWidth(140);
        model.getColumn(2).setPreferredWidth(220);
        model.getColumn(3).setPreferredWidth(150);
        model.getColumn(4).setPreferredWidth(400);
        for (int i = 0; i < model.getColumnCount(); i++) {
            model.getColumn(i).setResizable(false);
        }
    }

    public static void loadIncidents(JTable table, String statusCondition, JLabel loadingLabel, JScrollPane scrollPane) {
        CollectionReference incidents = db.collection("incidents");
        Query query = incidents.whereEqualTo("status", statusCondition)
                .orderBy("date", Query.Direction.ASCENDING);

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
                    String incidentId = document.getId();
                    String incidentDate = getFormattedDate(document.getDate("date"));

                    DocumentReference operatorRef = (DocumentReference) document.get("refOperator");
                    String operator = (operatorRef != null) ? getFullName(operatorRef) : "Sin operario";

                    DocumentReference serviceRef = (DocumentReference) document.get("refService");
                    String service = (serviceRef != null) ? serviceRef.getId() : "Sin servicio";

                    String incidentDescription = document.getString("description");
                    
                    model.addRow(new Object[]{
                        incidentId,
                        incidentDate,
                        operator,
                        service,
                        incidentDescription
                    });
                }
            } else {
                model.addRow(new Object[]{"", "No hay incidencias", "", "", ""});
            }

            SwingUtilities.invokeLater(() -> {
                table.setModel(model); // Actualiza la tabla            
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
}
