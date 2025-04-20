package com.vdjoseluis.vdlogistics.firebase.data;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.vdjoseluis.vdlogistics.firebase.FirebaseConfig;
import com.vdjoseluis.vdlogistics.models.Incident;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
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

    public static String getFormattedDate(Date date) {
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

    public static Incident getIncidentById(String incidentId) {
        try {
            DocumentSnapshot doc = db.collection("incidents").document(incidentId).get().get();

            if (doc.exists()) {
                String id = doc.getId();
                Date date = doc.getTimestamp("date").toDate();

                DocumentReference operatorRef = (DocumentReference) doc.get("refOperator");
                String operator = (operatorRef != null) ? getFullName(operatorRef) : "Sin operario";

                String description = doc.getString("description");

                DocumentReference serviceRef = (DocumentReference) doc.get("refService");
                String service = (serviceRef != null) ? serviceRef.getId() : "Sin servicio";

                String status = doc.getString("status");

                return new Incident(id, date, operator, description, service, status);
            }
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Error al obtener servicio: " + e.getMessage());
        }
        return null;
    }

    public static boolean createIncident(Incident incident, String userEmail) {
        try {
            DocumentReference docRef = db.collection("incidents").document();

            String operatorId = DataUser.operatorMap.get(incident.getOperator());

            Map<String, Object> data = new HashMap<>();
            data.put("date", Timestamp.now());
            data.put("description", incident.getDescription());
            data.put("refOperator", db.collection("users").document(operatorId));
            data.put("refService", db.collection("services").document(incident.getService()));
            data.put("status", incident.getStatus());

            docRef.set(data);
            DataLog.registerLog(userEmail, "Crea incidencia", docRef.getId());

            System.out.println("✅ Incidencia registrada correctamente ");
            return true;
        } catch (Exception e) {
            System.err.println("❌ Error creando incidencia: " + e.getMessage());
            return false;
        }
    }

    public static boolean updateIncident(Incident incident, String userEmail) {
        try {
            DocumentReference docRef = db.collection("incidents").document(incident.getId());

            String operatorId = DataUser.operatorMap.get(incident.getOperator());

            Map<String, Object> data = new HashMap<>();
            data.put("description", incident.getDescription());
            data.put("refOperator", db.collection("users").document(operatorId));
            data.put("refService", db.collection("services").document(incident.getService()));
             if (incident.getStatus()!= null)  data.put("status", incident.getStatus());

            docRef.update(data).get();
            DataLog.registerLog(userEmail, "Actualiza incidencia", docRef.getId());

            System.out.println("✅ Incidencia actualizada correctamente ");
            return true;
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Error creando incidencia: " + e.getMessage());
            return false;
        }
    }
    
    public static boolean updateIncidentStatusById(String id, String userEmail) {
        try {
            DocumentReference docRef = db.collection("incidents").document(id);

            Map<String, Object> data = new HashMap<>();            
            data.put("status", "Tramitada");

            docRef.update(data).get();
            DataLog.registerLog(userEmail, "Tramita incidencia", docRef.getId());

            System.out.println("✅ Incidencia actualizada correctamente ");
            return true;
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Error creando incidencia: " + e.getMessage());
            return false;
        }
    }

    public static boolean deleteIncident(String userEmail, String incidentId) {
        try {
            db.collection("incidents").document(incidentId).delete().get();
            DataLog.registerLog(userEmail, "Elimina incidencia", incidentId);
            System.out.println("Incidencia eliminada correctamente");
            return true;
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error al eliminar incidencia: " + e.getMessage());
            return false;
        }
    }

}
