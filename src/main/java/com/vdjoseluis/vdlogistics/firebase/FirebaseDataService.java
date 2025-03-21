package com.vdjoseluis.vdlogistics.firebase;

import com.google.cloud.firestore.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

public class FirebaseDataService {

    private static final Firestore db = FirebaseConfig.getFirestore();

    private static final String[] COLUMN_NAMES = {"ID", "Fecha", "Operario", "Tipo de Servicio", "Cliente", "Población"};

    private static void setColumnModel(JTable table) {
        TableColumnModel model = table.getColumnModel();
        model.getColumn(0).setPreferredWidth(80);
        model.getColumn(1).setPreferredWidth(150);
        model.getColumn(2).setPreferredWidth(250);
        model.getColumn(3).setPreferredWidth(130);
        model.getColumn(4).setPreferredWidth(250);
        model.getColumn(5).setPreferredWidth(240);
        for (int i = 0; i < model.getColumnCount(); i++) {
            model.getColumn(i).setResizable(false);
        }
    }

    public static void loadServices(JTable table, String statusCondition, JLabel loadingLabel, JScrollPane scrollPane) {
        CollectionReference services = db.collection("services");
        Query query = services.whereEqualTo("status", statusCondition)
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
                    return false; // Bloqueo de edición
                }
            };

            if (snapshots != null && !snapshots.isEmpty()) {
                for (QueryDocumentSnapshot document : snapshots) {
                    String serviceId = document.getId();
                    String serviceDate = getFormattedDate(document.getDate("date"));

                    DocumentReference operatorRef = (DocumentReference) document.get("refOperator");
                    String operator = (operatorRef != null) ? getFullName(operatorRef) : "Sin operario";

                    DocumentReference customerRef = (DocumentReference) document.get("refCustomer");
                    String customerName = (customerRef != null) ? getFullName(customerRef) : "Sin cliente";

                    String city = (customerRef != null) ? getCityFromCustomer(customerRef) : "Sin ciudad";

                    model.addRow(new Object[]{
                        serviceId,
                        serviceDate,
                        operator,
                        document.getString("type"),
                        customerName,
                        city
                    });
                }
            } else {
                model.addRow(new Object[]{"", "No hay servicios", "", "", ""});
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

    private static String getCityFromCustomer(DocumentReference ref) {
        try {
            DocumentSnapshot doc = ref.get().get();
            String address = doc.getString("address");
            if (address != null && address.contains(",")) {
                String[] parts = address.split(",");
                return parts[parts.length - 1].trim();  // Última parte después de la última coma
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return "Sin ciudad";
    }
}
