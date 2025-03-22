package com.vdjoseluis.vdlogistics.firebase;

import com.google.cloud.firestore.*;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

public class FirebaseDataCustomer {

    private static final Firestore db = FirebaseConfig.getFirestore();

    private static final String[] COLUMN_NAMES = {"ID", "Nombre", "Teléfono", "Email", "Dirección", "Adicional"};

    private static void setColumnModel(JTable table) {
        TableColumnModel model = table.getColumnModel();
        model.getColumn(0).setPreferredWidth(110);
        model.getColumn(1).setPreferredWidth(260);
        model.getColumn(2).setPreferredWidth(150);
        model.getColumn(3).setPreferredWidth(150);
        model.getColumn(4).setPreferredWidth(260);
        model.getColumn(5).setPreferredWidth(170);
        for (int i = 0; i < model.getColumnCount(); i++) {
            model.getColumn(i).setResizable(false);
        }
    }

    public static void loadCustomers(JTable table, JLabel loadingLabel, JScrollPane scrollPane) {
        CollectionReference customers = db.collection("customers");
        Query query = customers.orderBy("lastName", Query.Direction.ASCENDING);

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
                    String customerId = document.getId();
                    
                    String fullName = document.getString("firstName") + " " + document.getString("lastName");

                    String phone = document.getString("phone");

                    String email = document.getString("email");
                    
                    String address = document.getString("address");
                    
                    String addressAdditional = document.getString("addressAdditional");

                    model.addRow(new Object[]{
                        customerId,
                        fullName,
                        phone,
                        email,
                        address,
                        addressAdditional
                    });
                }
            } else {
                model.addRow(new Object[]{"", "No hay clientes", "", "", "", ""});
            }

            SwingUtilities.invokeLater(() -> {
                table.setModel(model); // Actualiza la tabla            
                setColumnModel(table);
                loadingLabel.setVisible(false);
                table.setVisible(true);
            });
        });
    }    

}
