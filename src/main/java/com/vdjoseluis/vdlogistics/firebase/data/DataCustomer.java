package com.vdjoseluis.vdlogistics.firebase.data;

import com.google.cloud.firestore.*;
import com.vdjoseluis.vdlogistics.firebase.FirebaseConfig;
import com.vdjoseluis.vdlogistics.models.Customer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

public class DataCustomer {

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

    public static final Map<String, String> customerMap = new HashMap<>();

    public static void listenForCustomerNames(JComboBox<String> combo) {
        db.collection("customers").addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                System.out.println("Error escuchando cambios: " + e.getMessage());
                return;
            }
            if (snapshots != null) {
                SwingUtilities.invokeLater(() -> {
                    combo.removeAllItems();
                    customerMap.clear();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        String id = doc.getId();
                        String name = doc.getString("firstName") + " " + doc.getString("lastName");
                        combo.addItem(name);
                        customerMap.put(name, id);
                    }
                });
            }
        });
    }

    public static Customer getCustomerById(String customerId) {
        try {
            DocumentSnapshot doc = db.collection("customers").document(customerId).get().get();

            if (doc.exists()) {
                String id = doc.getId();
                String firstName = doc.getString("firstName");
                String lastName = doc.getString("lastName");
                String email = doc.getString("email");
                String phone = doc.getString("phone");
                String address = doc.getString("address");
                String addressAdditional = doc.getString("addressAdditional");

                return new Customer(id, firstName, lastName, email, phone, address, addressAdditional);
            }
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Error al obtener servicio: " + e.getMessage());
        }
        return null;
    }

    public static boolean createCustomer(Customer customer, String userEmail) {
        try {
            DocumentReference docRef = db.collection("customers").document();

            Map<String, Object> data = new HashMap<>();
            data.put("firstName", customer.getFirstName());
            data.put("lastName", customer.getLastName());
            data.put("email", customer.getEmail());
            data.put("phone", customer.getPhone());
            data.put("address", customer.getAddress());
            data.put("addressAdditional", customer.getAdditional());

            docRef.set(data);
            DataLog.registerLog(userEmail, "Registra nuevo cliente", docRef.getId());

            System.out.println("✅ Cliente registrado correctamente ");
            return true;
        } catch (Exception e) {
            System.err.println("❌ Error registrando cliente: " + e.getMessage());
            return false;
        }
    }

    public static boolean updateCustomer(Customer customer, String userEmail) {
        try {
            DocumentReference docRef = db.collection("customers").document(customer.getId());
            
            Map<String, Object> data = new HashMap<>();
            data.put("firstName", customer.getFirstName());
            data.put("lastName", customer.getLastName());
            data.put("email", customer.getEmail());
            data.put("phone", customer.getPhone());
            data.put("address", customer.getAddress());
            data.put("addressAdditional", customer.getAdditional());

            docRef.update(data).get();
            DataLog.registerLog(userEmail, "Actualiza datos clientes", docRef.getId());

            System.out.println("✅ Cliente actualizado correctamente ");
            return true;
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Error creando servicio: " + e.getMessage());
            return false;
        }
    }
    
    public static boolean deleteCustomer(String userEmail, String customerId) {
        try {
            db.collection("customers").document(customerId).delete().get();
            //FileService.deleteServiceFiles(customerId);
            DataLog.registerLog(userEmail, "Elimina cliente", customerId);
            System.out.println("Cliente eliminado correctamente");
            return true;
        } catch (Exception e) {
            System.err.println("Error al eliminar cliente: " + e.getMessage());
            return false;
        }
    }

}
