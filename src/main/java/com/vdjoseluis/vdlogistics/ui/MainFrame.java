package com.vdjoseluis.vdlogistics.ui;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.vdjoseluis.vdlogistics.firebase.FirebaseConfig;
import com.vdjoseluis.vdlogistics.firebase.data.DataCustomer;
import com.vdjoseluis.vdlogistics.firebase.data.DataIncident;
import com.vdjoseluis.vdlogistics.firebase.data.DataLog;
import com.vdjoseluis.vdlogistics.firebase.data.DataService;
import com.vdjoseluis.vdlogistics.firebase.data.DataUser;
import com.vdjoseluis.vdlogistics.firebase.storage.FileService;
import com.vdjoseluis.vdlogistics.firebase.storage.FileUploader;
import com.vdjoseluis.vdlogistics.models.Customer;
import com.vdjoseluis.vdlogistics.models.Incident;
import com.vdjoseluis.vdlogistics.models.Service;
import com.vdjoseluis.vdlogistics.models.User;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.HeadlessException;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author José Luis Vásquez Drouet
 */
public class MainFrame extends javax.swing.JFrame {

    BackgroundPanel background = new BackgroundPanel();
    private final String userEmail;
    private final DefaultListModel<String> tempListModel = new DefaultListModel<>();
    private final List<File> tempFiles = new ArrayList<>();

    public MainFrame(String email) {
        this.setContentPane(background);

        initComponents();

        incidentIdToService.setVisible(false);

        userEmail = email;
        User currentUser = DataUser.getCurrentUser(email);
        if (currentUser != null) {
            String userLabel = currentUser.getFirstName().charAt(0) + ". " + currentUser.getLastName();
            currentUserMenu.setText("Usuario: " + userLabel);
        }

        loadingLabel.setVisible(true);
        this.setLocationRelativeTo(null);
        jMenuBar.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 5));
        loadingLabel.setBounds(mainScrollPanel.getX() + 50, mainScrollPanel.getY() + 50, 100, 100);
        fillComboBoxes();

        DataService.loadServices(confirmedTable, "Confirmado", loadingLabel, mainScrollPanel);
        DataService.loadServices(pendingTable, "Pendiente", loadingLabel, mainScrollPanel);
        DataService.loadServices(completedTable, "Finalizado", loadingLabel, mainScrollPanel);
        DataService.loadServices(pendingCompletionTable, "Pendiente Finalización", loadingLabel, mainScrollPanel);
        DataService.loadServices(newDateTable, "Propuesta nueva fecha", loadingLabel, mainScrollPanel);
        DataUser.loadUsers(usersTable, loadingLabel, mainScrollPanel);
        DataCustomer.loadCustomers(customersTable, loadingLabel, mainScrollPanel);
        DataLog.loadLogs(logsTable, loadingLabel, mainScrollPanel);
        DataIncident.loadIncidents(pendingIncidentsTable, "Pendiente", loadingLabel, mainScrollPanel);
        DataIncident.loadIncidents(processedIncidentsTable, "Tramitada", loadingLabel, mainScrollPanel);
    }

    private void updateScroll() {
        mainContent.revalidate();
        mainContent.repaint();
    }

    private void navigateCard(String cardName) {
        CardLayout cl = (CardLayout) mainContent.getLayout();
        cl.show(mainContent, cardName);
        updateScroll();
    }

    private void fillComboBoxes() {
        DataUser.listenForOperatorNames(cmbServiceOperator);
        DataUser.listenForOperatorNames(cmbIncidentOperator);
        DataCustomer.listenForCustomerNames(cmbServiceCustomer);
    }

    private void enabledDashboardButtons() {
        createUserButton.setEnabled(true);
        updateUserButton.setEnabled(true);
        deleteUserButton.setEnabled(true);
        createServiceButton.setEnabled(true);
        updateServiceButton.setEnabled(true);
        deleteServiceButton.setEnabled(true);
        newIncidentFromServiceButton.setEnabled(true);
        createIncidentButton.setEnabled(true);
        updateIncidentButton.setEnabled(true);
        deleteIncidentButton.setEnabled(true);
        createCustomerButton.setEnabled(true);
        updateCustomerButton.setEnabled(true);
        deleteCustomerButton.setEnabled(true);
    }

    private void clearForm(JPanel panel) {
        for (Component component : panel.getComponents()) {
            if (component instanceof JTextField) {
                ((JTextField) component).setText("");
            } else if (component instanceof JComboBox) {
                ((JComboBox<?>) component).setSelectedIndex(0);
                ((JComboBox<?>) component).setEnabled(true);
            } else if (component instanceof JPasswordField) {
                ((JPasswordField) component).setText("");
            }
        }
        txtUserEmail.setEnabled(true);
        txtUserPassword.setEnabled(true);

        txtServiceDescription.setText("");
        txtServiceDescription.setEditable(true);
        txtServiceComments.setText("");
        txtIncidentDescription.setText("");

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        dcServiceDate.setDate(calendar.getTime());
        spServiceHour.setValue(8);
        spServiceMinute.setValue(0);

        tempFiles.clear();
        tempListModel.clear();
    }

    private void createUser() {
        String email = txtUserEmail.getText();
        String password = new String(txtUserPassword.getPassword());
        String firstName = txtUserFirstName.getText();
        String lastName = txtUserLastName.getText();
        String phone = txtUserPhone.getText();
        String address = txtUserAddress.getText();
        String type = (String) comboUserType.getSelectedItem();

        if (email.isEmpty() || password.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.", "VD Logistics", JOptionPane.WARNING_MESSAGE);
            return;
        }

        User newUser = new User(email, firstName, lastName, phone, type, address);
        if (DataUser.createUser(newUser, password)) {
            clearForm(formUsersPanel);
            enabledDashboardButtons();
            JOptionPane.showMessageDialog(this, "Usuario creado correctamente", "VD Logistics", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Error al crear usuario.", "VD Logistics", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showUserForm(Map<String, String> userData) {
        txtUserId.setText(userData.get("id"));
        txtUserEmail.setText(userData.get("email"));
        txtUserEmail.setEnabled(false);
        txtUserPassword.setEnabled(false);
        txtUserFirstName.setText(userData.get("firstName"));
        txtUserLastName.setText(userData.get("lastName"));
        txtUserPhone.setText(userData.get("phone"));
        txtUserAddress.setText(userData.get("address"));
        comboUserType.setSelectedItem(userData.get("type"));
    }

    private void showServiceForm(Service serviceData, boolean incident) {
        newIncidentFromServiceButton.setEnabled(false);
        txtServiceId.setText(serviceData.getId());
        FileService fileOpener = new FileService(sharedFileList, operatorFileList, serviceData.getId());
        Date serviceDate = serviceData.getDate();
        if (serviceDate != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(serviceDate);
            dcServiceDate.setDate(serviceDate);
            spServiceHour.setValue(calendar.get(Calendar.HOUR_OF_DAY));
            spServiceMinute.setValue(calendar.get(Calendar.MINUTE));
        }

        txtServiceDescription.setText(serviceData.getDescription());
        txtServiceDescription.setEditable(!incident);
        cmbServiceOperator.setSelectedItem(serviceData.getOperator());
        cmbServiceType.setSelectedItem(serviceData.getType());
        cmbServiceCustomer.setSelectedItem(serviceData.getCustomer());
        cmbServiceCustomer.setEnabled(!incident);
        cmbServiceStatus.setSelectedItem(serviceData.getStatus());
        txtServiceComments.setText(serviceData.getComments());

        newIncidentFromServiceButton.setEnabled(!incident);
    }

    private void uploadFromTempFiles(String serviceId) {
        for (File selectedFile : tempFiles) {
            String fileName = selectedFile.getName();

            String storagePath = "services/" + serviceId + "/resources/" + fileName;

            try {
                FileUploader.uploadFile(selectedFile, storagePath);
                System.out.println("Archivo subido exitosamente a: " + storagePath);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error al subir el archivo " + fileName + ": " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
        tempFiles.clear();
        tempListModel.clear();
    }

    private void saveService(String action) {
        String id = ("update".equals(action)) ? txtServiceId.getText() : null;
        Date date = dcServiceDate.getDate();
        String description = txtServiceDescription.getText();
        String operatorName = (String) cmbServiceOperator.getSelectedItem();
        String type = (String) cmbServiceType.getSelectedItem();
        String customerName = (String) cmbServiceCustomer.getSelectedItem();
        String status = ("update".equals(action)) ? (String) cmbServiceStatus.getSelectedItem() : "Pendiente";
        String comments = txtServiceComments.getText();

        String incidentId = incidentIdToService.getText();

        if (description.isEmpty() || date == null) {
            JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.", "VD Logistics", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Service newService = new Service(id, date, operatorName, type, customerName, status, description, comments);
        boolean success = false;
        boolean successIncident = false;

        if ("new".equals(action)) {
            id = DataService.createService(newService, userEmail, dcServiceDate, spServiceHour, spServiceMinute);
            if (txtServiceDescription.getText().startsWith("Incidencia")) {
                successIncident = DataIncident.updateIncidentStatusById(incidentId, userEmail);
            }
        } else if ("update".equals(action)) {
            id = DataService.updateService(newService, userEmail, dcServiceDate, spServiceHour, spServiceMinute);
        }
        success = id != null;

        if (success && successIncident) {
            uploadFromTempFiles(id);
            JOptionPane.showMessageDialog(this, "Incidencia tramitada correctamente!", "VD Logistics", JOptionPane.INFORMATION_MESSAGE);
            clearForm(formServicesPanel);
            enabledDashboardButtons();
            navigateCard("services");
        } else if (success) {
            uploadFromTempFiles(id);
            JOptionPane.showMessageDialog(this, "Registrado correctamente!", "VD Logistics", JOptionPane.INFORMATION_MESSAGE);
            clearForm(formServicesPanel);
            enabledDashboardButtons();
            navigateCard("services");
        } else {
            JOptionPane.showMessageDialog(this, "Error al registrar servicio.", "VD Logistics", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showIncidentForm(Incident incidentData, boolean fromService) {
        if (!fromService) {
            txtIncidentId.setText(incidentData.getId());
            Date incidentDate = incidentData.getDate();
            txtIncidentDate.setText(DataIncident.getFormattedDate(incidentDate));
        }

        cmbIncidentOperator.setSelectedItem(incidentData.getOperator());
        cmbIncidentOperator.setEnabled(!fromService);
        txtIncidentDescription.setText(incidentData.getDescription());
        txtIncidentService.setText(incidentData.getService());
        processIncidentButton.setEnabled(false);
        processIncidentButton.setEnabled(!fromService && incidentData.getStatus().equals("Pendiente"));
    }

    private void saveIncident(String action) {
        String id = ("update".equals(action)) ? txtIncidentId.getText() : null;
        Date date = null;
        String operatorName = (String) cmbIncidentOperator.getSelectedItem();
        String description = txtIncidentDescription.getText();
        String serviceId = txtIncidentService.getText();
        String status = (action.equals("new")) ? "Pendiente" : null;

        if (description.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.", "VD Logistics", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Incident newIncident = new Incident(id, date, operatorName, description, serviceId, status);
        boolean success = false;

        if ("new".equals(action)) {
            success = DataIncident.createIncident(newIncident, userEmail);
        } else if ("update".equals(action)) {
            success = DataIncident.updateIncident(newIncident, userEmail);
        }

        if (success) {
            JOptionPane.showMessageDialog(this, "Registrado correctamente!", "VD Logistics", JOptionPane.INFORMATION_MESSAGE);
            clearForm(formIncidentsPanel);
            enabledDashboardButtons();
            navigateCard("incidents");
        } else {
            JOptionPane.showMessageDialog(this, "Error al registrar incidencia.", "VD Logistics", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showCustomerForm(Customer customerData) {
        txtCustomerId.setText(customerData.getId());
        txtCustomerFirstName.setText(customerData.getFirstName());
        txtCustomerLastName.setText(customerData.getLastName());
        txtCustomerEmail.setText(customerData.getEmail());
        txtCustomerPhone.setText(customerData.getPhone());
        txtCustomerAddress.setText(customerData.getAddress());
        txtCustomerAdditional.setText(customerData.getAdditional());

//        processIncidentButton.setEnabled(false);
//        processIncidentButton.setEnabled(!fromService && incidentData.getStatus().equals("Pendiente"));
    }

    private void saveCustomer(String action) {
        String id = ("update".equals(action)) ? txtCustomerId.getText() : null;
        String firstName = txtCustomerFirstName.getText();
        String lastName = txtCustomerLastName.getText();
        String email = txtCustomerEmail.getText();
        String phone = txtCustomerPhone.getText();
        String address = txtCustomerAddress.getText();
        String additional = txtCustomerAdditional.getText();

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.", "VD Logistics", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Customer newCustomer = new Customer(id, firstName, lastName, email, phone, address, additional);
        boolean success = false;

        if ("new".equals(action)) {
            success = DataCustomer.createCustomer(newCustomer, userEmail);
        } else if ("update".equals(action)) {
            success = DataCustomer.updateCustomer(newCustomer, userEmail);
        }

        if (success) {
            JOptionPane.showMessageDialog(this, "Registrado correctamente!", "VD Logistics", JOptionPane.INFORMATION_MESSAGE);
            clearForm(formCustomersPanel);
            enabledDashboardButtons();
            navigateCard("customers");
        } else {
            JOptionPane.showMessageDialog(this, "Error al registrar cliente.", "VD Logistics", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        loadingLabel = new javax.swing.JLabel();
        jFileChooser = new javax.swing.JFileChooser();
        mainScrollPanel = new javax.swing.JScrollPane();
        mainContent = new javax.swing.JPanel();
        servicesPanel = new BackgroundPanel();
        jLabel1 = new javax.swing.JLabel();
        confirmedScrollPanel = new javax.swing.JScrollPane();
        confirmedTable = new javax.swing.JTable();
        jLabel2 = new javax.swing.JLabel();
        pendingScrollPanel = new javax.swing.JScrollPane();
        pendingTable = new javax.swing.JTable();
        otherServicesPanel = new BackgroundPanel();
        jLabel3 = new javax.swing.JLabel();
        pendingCompletionScrollPanel = new javax.swing.JScrollPane();
        pendingCompletionTable = new javax.swing.JTable();
        jLabel4 = new javax.swing.JLabel();
        newDateScrollPanel = new javax.swing.JScrollPane();
        newDateTable = new javax.swing.JTable();
        completedServicesPanel = new BackgroundPanel();
        completedScrollPanel = new javax.swing.JScrollPane();
        completedTable = new javax.swing.JTable();
        jLabel34 = new javax.swing.JLabel();
        usersPanel = new BackgroundPanel();
        usersScrollPanel = new javax.swing.JScrollPane();
        usersTable = new javax.swing.JTable();
        jLabel6 = new javax.swing.JLabel();
        customersPanel = new BackgroundPanel();
        customersScrollPanel = new javax.swing.JScrollPane();
        customersTable = new javax.swing.JTable();
        jLabel7 = new javax.swing.JLabel();
        logsPanel = new BackgroundPanel();
        logsScrollPanel = new javax.swing.JScrollPane();
        logsTable = new javax.swing.JTable();
        jLabel8 = new javax.swing.JLabel();
        incidentsPanel = new BackgroundPanel();
        jLabel18 = new javax.swing.JLabel();
        pendingIncidentsScrollPanel = new javax.swing.JScrollPane();
        pendingIncidentsTable = new javax.swing.JTable();
        jLabel19 = new javax.swing.JLabel();
        processedIncidentsScrollPanel = new javax.swing.JScrollPane();
        processedIncidentsTable = new javax.swing.JTable();
        formUsersPanel = new BackgroundPanel();
        jLabel9 = new javax.swing.JLabel();
        opUsers = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        createUserButton = new javax.swing.JButton();
        updateUserButton = new javax.swing.JButton();
        deleteUserButton = new javax.swing.JButton();
        backUsers = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel10 = new javax.swing.JLabel();
        txtUserId = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        txtUserFirstName = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        txtUserLastName = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        txtUserPhone = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        txtUserEmail = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        txtUserPassword = new javax.swing.JPasswordField();
        jLabel16 = new javax.swing.JLabel();
        txtUserAddress = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        comboUserType = new javax.swing.JComboBox<>();
        saveDiscardPanel = new javax.swing.JPanel();
        saveUser = new javax.swing.JButton();
        discardUser = new javax.swing.JButton();
        formServicesPanel = new BackgroundPanel();
        jLabel20 = new javax.swing.JLabel();
        opServices = new javax.swing.JPanel();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        txtServiceId = new javax.swing.JTextField();
        createServiceButton = new javax.swing.JButton();
        updateServiceButton = new javax.swing.JButton();
        deleteServiceButton = new javax.swing.JButton();
        backServices = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JSeparator();
        newIncidentFromServiceButton = new javax.swing.JButton();
        incidentIdToService = new javax.swing.JTextField();
        jLabel23 = new javax.swing.JLabel();
        dcServiceDate = new com.toedter.calendar.JDateChooser();
        jLabel31 = new javax.swing.JLabel();
        spServiceHour = new javax.swing.JSpinner();
        jLabel30 = new javax.swing.JLabel();
        spServiceMinute = new javax.swing.JSpinner();
        jLabel27 = new javax.swing.JLabel();
        serviceDescriptionJSPanel = new javax.swing.JScrollPane();
        txtServiceDescription = new javax.swing.JTextArea();
        jLabel24 = new javax.swing.JLabel();
        cmbServiceOperator = new javax.swing.JComboBox<>();
        jLabel25 = new javax.swing.JLabel();
        cmbServiceType = new javax.swing.JComboBox<>();
        jLabel28 = new javax.swing.JLabel();
        cmbServiceCustomer = new javax.swing.JComboBox<>();
        jLabel26 = new javax.swing.JLabel();
        cmbServiceStatus = new javax.swing.JComboBox<>();
        jLabel32 = new javax.swing.JLabel();
        serviceCommentsJSPanel = new javax.swing.JScrollPane();
        txtServiceComments = new javax.swing.JTextArea();
        jLabel29 = new javax.swing.JLabel();
        sharedFilesJSPanel = new javax.swing.JScrollPane();
        sharedFileList = new javax.swing.JList<>();
        deleteFileButton = new javax.swing.JButton();
        addFileButton = new javax.swing.JButton();
        jLabel33 = new javax.swing.JLabel();
        operatorFilesJSPanel = new javax.swing.JScrollPane();
        operatorFileList = new javax.swing.JList<>();
        saveDiscardPanel1 = new javax.swing.JPanel();
        saveService = new javax.swing.JButton();
        discardService = new javax.swing.JButton();
        formIncidentsPanel = new BackgroundPanel();
        jLabel35 = new javax.swing.JLabel();
        opIncidents = new javax.swing.JPanel();
        jLabel36 = new javax.swing.JLabel();
        createIncidentButton = new javax.swing.JButton();
        updateIncidentButton = new javax.swing.JButton();
        deleteIncidentButton = new javax.swing.JButton();
        backIncidents = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JSeparator();
        jLabel37 = new javax.swing.JLabel();
        txtIncidentId = new javax.swing.JTextField();
        jLabel45 = new javax.swing.JLabel();
        txtIncidentDate = new javax.swing.JTextField();
        jLabel39 = new javax.swing.JLabel();
        cmbIncidentOperator = new javax.swing.JComboBox<>();
        jLabel42 = new javax.swing.JLabel();
        incidentDescriptionJSPanel = new javax.swing.JScrollPane();
        txtIncidentDescription = new javax.swing.JTextArea();
        jLabel40 = new javax.swing.JLabel();
        txtIncidentService = new javax.swing.JTextField();
        processIncidentButton = new javax.swing.JButton();
        saveDiscardPanel2 = new javax.swing.JPanel();
        saveIncident = new javax.swing.JButton();
        discardIncident = new javax.swing.JButton();
        formCustomersPanel = new BackgroundPanel();
        jLabel38 = new javax.swing.JLabel();
        opCustomers = new javax.swing.JPanel();
        jLabel41 = new javax.swing.JLabel();
        createCustomerButton = new javax.swing.JButton();
        updateCustomerButton = new javax.swing.JButton();
        deleteCustomerButton = new javax.swing.JButton();
        backCustomers = new javax.swing.JButton();
        jSeparator4 = new javax.swing.JSeparator();
        jLabel43 = new javax.swing.JLabel();
        txtCustomerId = new javax.swing.JTextField();
        jLabel46 = new javax.swing.JLabel();
        txtCustomerFirstName = new javax.swing.JTextField();
        jLabel44 = new javax.swing.JLabel();
        txtCustomerLastName = new javax.swing.JTextField();
        jLabel49 = new javax.swing.JLabel();
        txtCustomerEmail = new javax.swing.JTextField();
        jLabel47 = new javax.swing.JLabel();
        txtCustomerPhone = new javax.swing.JTextField();
        jLabel50 = new javax.swing.JLabel();
        txtCustomerAddress = new javax.swing.JTextField();
        jLabel48 = new javax.swing.JLabel();
        txtCustomerAdditional = new javax.swing.JTextField();
        servicesByCustomer = new javax.swing.JButton();
        saveDiscardPanel3 = new javax.swing.JPanel();
        saveCustomer = new javax.swing.JButton();
        discardCustomer = new javax.swing.JButton();
        searchServicesPanel = new BackgroundPanel();
        jLabel52 = new javax.swing.JLabel();
        jLabel51 = new javax.swing.JLabel();
        txtSearchByPhone = new javax.swing.JTextField();
        searchServiceButton = new javax.swing.JButton();
        searchScrollPanel = new javax.swing.JScrollPane();
        searchTable = new javax.swing.JTable();
        jMenuBar = new javax.swing.JMenuBar();
        usersMenu = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        servicesMenu = new javax.swing.JMenu();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenuItem5 = new javax.swing.JMenuItem();
        jMenuItem11 = new javax.swing.JMenuItem();
        jMenuItem13 = new javax.swing.JMenuItem();
        jMenuItem14 = new javax.swing.JMenuItem();
        incidentsMenu = new javax.swing.JMenu();
        jMenuItem6 = new javax.swing.JMenuItem();
        jMenuItem7 = new javax.swing.JMenuItem();
        customersMenu = new javax.swing.JMenu();
        jMenuItem8 = new javax.swing.JMenuItem();
        jMenuItem9 = new javax.swing.JMenuItem();
        currentUserMenu = new javax.swing.JMenu();
        jMenuItem10 = new javax.swing.JMenuItem();
        jMenuItem12 = new javax.swing.JMenuItem();

        loadingLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        loadingLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/loaderSpinner.gif"))); // NOI18N

        jFileChooser.setMultiSelectionEnabled(true);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("VD Logistics");
        setSize(new java.awt.Dimension(1366, 768));
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });

        mainScrollPanel.setPreferredSize(new java.awt.Dimension(1369, 903));

        mainContent.setPreferredSize(new java.awt.Dimension(1366, 750));
        mainContent.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                mainContentComponentResized(evt);
            }
        });
        mainContent.setLayout(new java.awt.CardLayout());

        servicesPanel.setBackground(new java.awt.Color(0, 153, 204));
        servicesPanel.setPreferredSize(new java.awt.Dimension(1366, 750));
        servicesPanel.setLayout(new java.awt.GridBagLayout());

        jLabel1.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Pendientes de Confirmar Cita");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(40, 509, 0, 0);
        servicesPanel.add(jLabel1, gridBagConstraints);

        confirmedScrollPanel.setAutoscrolls(true);
        confirmedScrollPanel.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 12)); // NOI18N
        confirmedScrollPanel.setViewportView(confirmedTable);

        confirmedTable.setAutoCreateRowSorter(true);
        confirmedTable.setBackground(java.awt.SystemColor.control);
        confirmedTable.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        confirmedTable.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        confirmedTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        confirmedTable.setFillsViewportHeight(true);
        confirmedTable.setFocusable(false);
        confirmedTable.setRowHeight(40);
        confirmedTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        confirmedTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        confirmedTable.setShowGrid(true);
        confirmedTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                confirmedTableMouseClicked(evt);
            }
        });
        confirmedScrollPanel.setViewportView(confirmedTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 1078;
        gridBagConstraints.ipady = 224;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 133, 0, 133);
        servicesPanel.add(confirmedScrollPanel, gridBagConstraints);

        jLabel2.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 24)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Servicios Confirmados");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(30, 549, 0, 0);
        servicesPanel.add(jLabel2, gridBagConstraints);

        pendingScrollPanel.setAutoscrolls(true);
        pendingScrollPanel.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 12)); // NOI18N
        pendingScrollPanel.setViewportView(pendingTable);

        pendingTable.setAutoCreateRowSorter(true);
        pendingTable.setBackground(java.awt.SystemColor.control);
        pendingTable.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        pendingTable.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        pendingTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        pendingTable.setFillsViewportHeight(true);
        pendingTable.setFocusable(false);
        pendingTable.setRowHeight(40);
        pendingTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        pendingTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        pendingTable.setShowGrid(true);
        pendingTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                pendingTableMouseClicked(evt);
            }
        });
        pendingScrollPanel.setViewportView(pendingTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 1078;
        gridBagConstraints.ipady = 224;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 133, 154, 133);
        servicesPanel.add(pendingScrollPanel, gridBagConstraints);

        mainContent.add(servicesPanel, "services");

        otherServicesPanel.setBackground(new java.awt.Color(0, 153, 204));
        otherServicesPanel.setPreferredSize(new java.awt.Dimension(1366, 750));
        otherServicesPanel.setLayout(new java.awt.GridBagLayout());

        jLabel3.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 24)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("Propuesta Nueva Cita");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(40, 509, 0, 0);
        otherServicesPanel.add(jLabel3, gridBagConstraints);

        pendingCompletionScrollPanel.setAutoscrolls(true);
        pendingCompletionScrollPanel.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 12)); // NOI18N
        pendingCompletionScrollPanel.setViewportView(pendingCompletionTable);

        pendingCompletionTable.setAutoCreateRowSorter(true);
        pendingCompletionTable.setBackground(java.awt.SystemColor.control);
        pendingCompletionTable.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        pendingCompletionTable.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        pendingCompletionTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        pendingCompletionTable.setFillsViewportHeight(true);
        pendingCompletionTable.setFocusable(false);
        pendingCompletionTable.setRowHeight(40);
        pendingCompletionTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        pendingCompletionTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        pendingCompletionTable.setShowGrid(true);
        pendingCompletionTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                pendingCompletionTableMouseClicked(evt);
            }
        });
        pendingCompletionScrollPanel.setViewportView(pendingCompletionTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 1078;
        gridBagConstraints.ipady = 224;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 133, 0, 133);
        otherServicesPanel.add(pendingCompletionScrollPanel, gridBagConstraints);

        jLabel4.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 24)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText("Pendientes de Finalización");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(30, 549, 0, 0);
        otherServicesPanel.add(jLabel4, gridBagConstraints);

        newDateScrollPanel.setFont(new java.awt.Font("Lucida Console", 0, 12)); // NOI18N
        newDateScrollPanel.setViewportView(newDateTable);

        newDateTable.setAutoCreateRowSorter(true);
        newDateTable.setBackground(java.awt.SystemColor.control);
        newDateTable.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        newDateTable.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        newDateTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        newDateTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        newDateTable.setFillsViewportHeight(true);
        newDateTable.setFocusable(false);
        newDateTable.setRowHeight(40);
        newDateTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        newDateTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        newDateTable.setShowGrid(true);
        newDateTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                newDateTableMouseClicked(evt);
            }
        });
        newDateScrollPanel.setViewportView(newDateTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 1078;
        gridBagConstraints.ipady = 224;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 133, 154, 133);
        otherServicesPanel.add(newDateScrollPanel, gridBagConstraints);

        mainContent.add(otherServicesPanel, "otherServices");

        completedServicesPanel.setBackground(new java.awt.Color(0, 153, 204));
        completedServicesPanel.setPreferredSize(new java.awt.Dimension(1366, 750));
        completedServicesPanel.setLayout(new java.awt.GridBagLayout());

        completedScrollPanel.setAutoscrolls(true);
        completedScrollPanel.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 12)); // NOI18N
        completedScrollPanel.setViewportView(completedTable);

        completedTable.setAutoCreateRowSorter(true);
        completedTable.setBackground(java.awt.SystemColor.control);
        completedTable.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        completedTable.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        completedTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        completedTable.setFillsViewportHeight(true);
        completedTable.setFocusable(false);
        completedTable.setRowHeight(40);
        completedTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        completedTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        completedTable.setShowGrid(true);
        completedTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                completedTableMouseClicked(evt);
            }
        });
        completedScrollPanel.setViewportView(completedTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 1078;
        gridBagConstraints.ipady = 594;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 133, 114, 133);
        completedServicesPanel.add(completedScrollPanel, gridBagConstraints);

        jLabel34.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 24)); // NOI18N
        jLabel34.setForeground(new java.awt.Color(255, 255, 255));
        jLabel34.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel34.setText("Servicios Finalizados");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        gridBagConstraints.insets = new java.awt.Insets(30, 550, 0, 550);
        completedServicesPanel.add(jLabel34, gridBagConstraints);

        mainContent.add(completedServicesPanel, "completedServices");

        usersPanel.setBackground(new java.awt.Color(0, 153, 204));
        usersPanel.setPreferredSize(new java.awt.Dimension(1366, 750));
        usersPanel.setLayout(new java.awt.GridBagLayout());

        usersScrollPanel.setAutoscrolls(true);
        usersScrollPanel.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 12)); // NOI18N
        usersScrollPanel.setViewportView(usersTable);

        usersTable.setAutoCreateRowSorter(true);
        usersTable.setBackground(java.awt.SystemColor.control);
        usersTable.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        usersTable.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        usersTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        usersTable.setFillsViewportHeight(true);
        usersTable.setFocusable(false);
        usersTable.setRowHeight(40);
        usersTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        usersTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        usersTable.setShowGrid(true);
        usersTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                usersTableMouseClicked(evt);
            }
        });
        usersScrollPanel.setViewportView(usersTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 1078;
        gridBagConstraints.ipady = 594;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 133, 114, 133);
        usersPanel.add(usersScrollPanel, gridBagConstraints);

        jLabel6.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 24)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel6.setText("Usuarios - VD Logistics");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(30, 549, 0, 0);
        usersPanel.add(jLabel6, gridBagConstraints);

        mainContent.add(usersPanel, "users");

        customersPanel.setBackground(new java.awt.Color(0, 153, 204));
        customersPanel.setMinimumSize(new java.awt.Dimension(1366, 750));
        customersPanel.setPreferredSize(new java.awt.Dimension(1366, 750));
        customersPanel.setLayout(new java.awt.GridBagLayout());

        customersScrollPanel.setAutoscrolls(true);
        customersScrollPanel.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 12)); // NOI18N
        customersScrollPanel.setViewportView(customersTable);

        customersTable.setAutoCreateRowSorter(true);
        customersTable.setBackground(java.awt.SystemColor.control);
        customersTable.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        customersTable.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        customersTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        customersTable.setFillsViewportHeight(true);
        customersTable.setFocusable(false);
        customersTable.setRowHeight(40);
        customersTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        customersTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        customersTable.setShowGrid(true);
        customersTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                customersTableMouseClicked(evt);
            }
        });
        customersScrollPanel.setViewportView(customersTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 1078;
        gridBagConstraints.ipady = 594;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 133, 114, 133);
        customersPanel.add(customersScrollPanel, gridBagConstraints);

        jLabel7.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 24)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(255, 255, 255));
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel7.setText("Clientes - VD Logistics");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(30, 549, 0, 0);
        customersPanel.add(jLabel7, gridBagConstraints);

        mainContent.add(customersPanel, "customers");

        logsPanel.setBackground(new java.awt.Color(0, 153, 204));
        logsPanel.setPreferredSize(new java.awt.Dimension(1366, 750));
        logsPanel.setLayout(new java.awt.GridBagLayout());

        logsScrollPanel.setAutoscrolls(true);
        logsScrollPanel.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 12)); // NOI18N
        logsScrollPanel.setViewportView(logsTable);

        logsTable.setAutoCreateRowSorter(true);
        logsTable.setBackground(java.awt.SystemColor.control);
        logsTable.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        logsTable.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        logsTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        logsTable.setFillsViewportHeight(true);
        logsTable.setFocusable(false);
        logsTable.setRowHeight(40);
        logsTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        logsTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        logsTable.setShowGrid(true);
        logsScrollPanel.setViewportView(logsTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 1078;
        gridBagConstraints.ipady = 624;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 133, 84, 133);
        logsPanel.add(logsScrollPanel, gridBagConstraints);

        jLabel8.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 24)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(255, 255, 255));
        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel8.setText("Usuarios - VD Logistics");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(30, 549, 0, 0);
        logsPanel.add(jLabel8, gridBagConstraints);

        mainContent.add(logsPanel, "logs");

        incidentsPanel.setBackground(new java.awt.Color(0, 153, 204));
        incidentsPanel.setPreferredSize(new java.awt.Dimension(1366, 750));
        incidentsPanel.setLayout(new java.awt.GridBagLayout());

        jLabel18.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 24)); // NOI18N
        jLabel18.setForeground(new java.awt.Color(255, 255, 255));
        jLabel18.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel18.setText("Incidencias Tramitadas");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(40, 509, 0, 0);
        incidentsPanel.add(jLabel18, gridBagConstraints);

        pendingIncidentsScrollPanel.setAutoscrolls(true);
        pendingIncidentsScrollPanel.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 12)); // NOI18N
        pendingIncidentsScrollPanel.setViewportView(pendingIncidentsTable);

        pendingIncidentsTable.setAutoCreateRowSorter(true);
        pendingIncidentsTable.setBackground(java.awt.SystemColor.control);
        pendingIncidentsTable.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        pendingIncidentsTable.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        pendingIncidentsTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        pendingIncidentsTable.setFillsViewportHeight(true);
        pendingIncidentsTable.setFocusable(false);
        pendingIncidentsTable.setRowHeight(40);
        pendingIncidentsTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        pendingIncidentsTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        pendingIncidentsTable.setShowGrid(true);
        pendingIncidentsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                pendingIncidentsTableMouseClicked(evt);
            }
        });
        pendingIncidentsScrollPanel.setViewportView(pendingIncidentsTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 1078;
        gridBagConstraints.ipady = 224;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 133, 0, 133);
        incidentsPanel.add(pendingIncidentsScrollPanel, gridBagConstraints);

        jLabel19.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 24)); // NOI18N
        jLabel19.setForeground(new java.awt.Color(255, 255, 255));
        jLabel19.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel19.setText("Incidencias Pendientes");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(30, 549, 0, 0);
        incidentsPanel.add(jLabel19, gridBagConstraints);

        processedIncidentsScrollPanel.setFont(new java.awt.Font("Lucida Console", 0, 12)); // NOI18N
        processedIncidentsScrollPanel.setViewportView(processedIncidentsTable);

        processedIncidentsTable.setAutoCreateRowSorter(true);
        processedIncidentsTable.setBackground(java.awt.SystemColor.control);
        processedIncidentsTable.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        processedIncidentsTable.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        processedIncidentsTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        processedIncidentsTable.setFillsViewportHeight(true);
        processedIncidentsTable.setFocusable(false);
        processedIncidentsTable.setRowHeight(40);
        processedIncidentsTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        processedIncidentsTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        processedIncidentsTable.setShowGrid(true);
        processedIncidentsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                processedIncidentsTableMouseClicked(evt);
            }
        });
        processedIncidentsScrollPanel.setViewportView(processedIncidentsTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 1078;
        gridBagConstraints.ipady = 224;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 133, 154, 133);
        incidentsPanel.add(processedIncidentsScrollPanel, gridBagConstraints);

        mainContent.add(incidentsPanel, "incidents");

        formUsersPanel.setBackground(new java.awt.Color(0, 153, 204));
        formUsersPanel.setPreferredSize(new java.awt.Dimension(1366, 750));
        formUsersPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel9.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 24)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(255, 255, 255));
        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel9.setText("Formulario de Usuarios - VD Logistics");
        formUsersPanel.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(590, 40, -1, -1));

        opUsers.setBackground(new java.awt.Color(0, 153, 153));
        opUsers.setAlignmentX(0.0F);
        opUsers.setAlignmentY(0.0F);
        opUsers.setPreferredSize(new java.awt.Dimension(240, 1100));

        jLabel5.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 20)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setText("Operaciones");

        createUserButton.setBackground(new java.awt.Color(0, 68, 85));
        createUserButton.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 12)); // NOI18N
        createUserButton.setForeground(new java.awt.Color(255, 255, 255));
        createUserButton.setText("Añadir Nuevo");
        createUserButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        createUserButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createUserButtonActionPerformed(evt);
            }
        });

        updateUserButton.setBackground(new java.awt.Color(0, 68, 85));
        updateUserButton.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 12)); // NOI18N
        updateUserButton.setForeground(new java.awt.Color(255, 255, 255));
        updateUserButton.setText("Editar datos");
        updateUserButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        updateUserButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateUserButtonActionPerformed(evt);
            }
        });

        deleteUserButton.setBackground(new java.awt.Color(0, 68, 85));
        deleteUserButton.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 12)); // NOI18N
        deleteUserButton.setForeground(new java.awt.Color(255, 255, 255));
        deleteUserButton.setText("Eliminar");
        deleteUserButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        deleteUserButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteUserButtonActionPerformed(evt);
            }
        });

        backUsers.setBackground(new java.awt.Color(3, 121, 157));
        backUsers.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 12)); // NOI18N
        backUsers.setForeground(new java.awt.Color(255, 255, 255));
        backUsers.setText("Volver");
        backUsers.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        backUsers.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backUsersActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout opUsersLayout = new javax.swing.GroupLayout(opUsers);
        opUsers.setLayout(opUsersLayout);
        opUsersLayout.setHorizontalGroup(
            opUsersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, opUsersLayout.createSequentialGroup()
                .addContainerGap(74, Short.MAX_VALUE)
                .addGroup(opUsersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(createUserButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(updateUserButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(deleteUserButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(80, 80, 80))
            .addComponent(jSeparator1)
            .addGroup(opUsersLayout.createSequentialGroup()
                .addGap(76, 76, 76)
                .addComponent(backUsers, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        opUsersLayout.setVerticalGroup(
            opUsersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(opUsersLayout.createSequentialGroup()
                .addGap(68, 68, 68)
                .addComponent(jLabel5)
                .addGap(60, 60, 60)
                .addComponent(createUserButton, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(51, 51, 51)
                .addComponent(updateUserButton, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(48, 48, 48)
                .addComponent(deleteUserButton, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(48, 48, 48)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(48, 48, 48)
                .addComponent(backUsers, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(607, Short.MAX_VALUE))
        );

        formUsersPanel.add(opUsers, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, -10, 280, 1100));

        jLabel10.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 16)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(255, 255, 255));
        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel10.setText("ID:");
        formUsersPanel.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 125, -1, -1));

        txtUserId.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 12)); // NOI18N
        txtUserId.setForeground(new java.awt.Color(0, 0, 0));
        txtUserId.setDisabledTextColor(new java.awt.Color(153, 153, 153));
        txtUserId.setEnabled(false);
        formUsersPanel.add(txtUserId, new org.netbeans.lib.awtextra.AbsoluteConstraints(760, 120, 230, 30));

        jLabel11.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 16)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(255, 255, 255));
        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel11.setText("Nombre:");
        formUsersPanel.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(620, 185, -1, -1));

        txtUserFirstName.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 12)); // NOI18N
        txtUserFirstName.setForeground(new java.awt.Color(0, 0, 0));
        formUsersPanel.add(txtUserFirstName, new org.netbeans.lib.awtextra.AbsoluteConstraints(760, 180, 230, 30));

        jLabel12.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 16)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(255, 255, 255));
        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel12.setText("Apellidos:");
        formUsersPanel.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(610, 245, -1, -1));

        txtUserLastName.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 12)); // NOI18N
        txtUserLastName.setForeground(new java.awt.Color(0, 0, 0));
        formUsersPanel.add(txtUserLastName, new org.netbeans.lib.awtextra.AbsoluteConstraints(760, 240, 230, 30));

        jLabel13.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 16)); // NOI18N
        jLabel13.setForeground(new java.awt.Color(255, 255, 255));
        jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel13.setText("Teléfono:");
        formUsersPanel.add(jLabel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(620, 305, -1, -1));

        txtUserPhone.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 12)); // NOI18N
        txtUserPhone.setForeground(new java.awt.Color(0, 0, 0));
        formUsersPanel.add(txtUserPhone, new org.netbeans.lib.awtextra.AbsoluteConstraints(760, 300, 120, 30));

        jLabel14.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 16)); // NOI18N
        jLabel14.setForeground(new java.awt.Color(255, 255, 255));
        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel14.setText("Email:");
        formUsersPanel.add(jLabel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 365, -1, -1));

        txtUserEmail.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 12)); // NOI18N
        txtUserEmail.setForeground(new java.awt.Color(0, 0, 0));
        formUsersPanel.add(txtUserEmail, new org.netbeans.lib.awtextra.AbsoluteConstraints(760, 360, 230, 30));

        jLabel17.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 16)); // NOI18N
        jLabel17.setForeground(new java.awt.Color(255, 255, 255));
        jLabel17.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel17.setText("Contraseña:");
        formUsersPanel.add(jLabel17, new org.netbeans.lib.awtextra.AbsoluteConstraints(590, 425, -1, -1));

        txtUserPassword.setFont(new java.awt.Font("Monospaced", 0, 14)); // NOI18N
        formUsersPanel.add(txtUserPassword, new org.netbeans.lib.awtextra.AbsoluteConstraints(760, 420, 230, 30));

        jLabel16.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 16)); // NOI18N
        jLabel16.setForeground(new java.awt.Color(255, 255, 255));
        jLabel16.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel16.setText("Dirección:");
        formUsersPanel.add(jLabel16, new org.netbeans.lib.awtextra.AbsoluteConstraints(610, 485, -1, -1));

        txtUserAddress.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 12)); // NOI18N
        txtUserAddress.setForeground(new java.awt.Color(0, 0, 0));
        formUsersPanel.add(txtUserAddress, new org.netbeans.lib.awtextra.AbsoluteConstraints(760, 480, 310, 30));

        jLabel15.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 16)); // NOI18N
        jLabel15.setForeground(new java.awt.Color(255, 255, 255));
        jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel15.setText("Tipo / Función:");
        formUsersPanel.add(jLabel15, new org.netbeans.lib.awtextra.AbsoluteConstraints(580, 545, -1, -1));

        comboUserType.setBackground(new java.awt.Color(255, 255, 255));
        comboUserType.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 12)); // NOI18N
        comboUserType.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Administrativo", "Operario" }));
        formUsersPanel.add(comboUserType, new org.netbeans.lib.awtextra.AbsoluteConstraints(760, 540, 230, 30));

        saveDiscardPanel.setBackground(new java.awt.Color(0, 153, 153));
        saveDiscardPanel.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        saveUser.setBackground(new java.awt.Color(3, 121, 157));
        saveUser.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 12)); // NOI18N
        saveUser.setForeground(new java.awt.Color(255, 255, 255));
        saveUser.setText("Guardar");
        saveUser.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        saveUser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveUserActionPerformed(evt);
            }
        });

        discardUser.setBackground(new java.awt.Color(0, 68, 85));
        discardUser.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 12)); // NOI18N
        discardUser.setForeground(new java.awt.Color(255, 255, 255));
        discardUser.setText("Cancelar");
        discardUser.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        discardUser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                discardUserActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout saveDiscardPanelLayout = new javax.swing.GroupLayout(saveDiscardPanel);
        saveDiscardPanel.setLayout(saveDiscardPanelLayout);
        saveDiscardPanelLayout.setHorizontalGroup(
            saveDiscardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(saveDiscardPanelLayout.createSequentialGroup()
                .addGap(60, 60, 60)
                .addComponent(saveUser, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 122, Short.MAX_VALUE)
                .addComponent(discardUser, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(60, 60, 60))
        );
        saveDiscardPanelLayout.setVerticalGroup(
            saveDiscardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(saveDiscardPanelLayout.createSequentialGroup()
                .addGap(38, 38, 38)
                .addGroup(saveDiscardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(saveUser, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(discardUser, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(32, Short.MAX_VALUE))
        );

        formUsersPanel.add(saveDiscardPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(570, 610, 500, 110));

        mainContent.add(formUsersPanel, "formUsers");

        formServicesPanel.setBackground(new java.awt.Color(0, 153, 204));
        formServicesPanel.setAutoscrolls(true);
        formServicesPanel.setPreferredSize(new java.awt.Dimension(1366, 750));
        formServicesPanel.setLayout(new java.awt.GridBagLayout());

        jLabel20.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 24)); // NOI18N
        jLabel20.setForeground(new java.awt.Color(255, 255, 255));
        jLabel20.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel20.setText("Formulario de Servicios - VD Logistics");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 14;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(40, 310, 0, 0);
        formServicesPanel.add(jLabel20, gridBagConstraints);

        opServices.setBackground(new java.awt.Color(0, 153, 153));
        opServices.setAlignmentX(0.0F);
        opServices.setAlignmentY(0.0F);
        opServices.setPreferredSize(new java.awt.Dimension(240, 1100));

        jLabel21.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 20)); // NOI18N
        jLabel21.setForeground(new java.awt.Color(255, 255, 255));
        jLabel21.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel21.setText("Operaciones");

        jLabel22.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 16)); // NOI18N
        jLabel22.setForeground(new java.awt.Color(255, 255, 255));
        jLabel22.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel22.setText("ID:");

        txtServiceId.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 12)); // NOI18N
        txtServiceId.setForeground(new java.awt.Color(0, 0, 0));
        txtServiceId.setDisabledTextColor(new java.awt.Color(153, 153, 153));
        txtServiceId.setEnabled(false);

        createServiceButton.setBackground(new java.awt.Color(0, 68, 85));
        createServiceButton.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 12)); // NOI18N
        createServiceButton.setForeground(new java.awt.Color(255, 255, 255));
        createServiceButton.setText("Nuevo servicio");
        createServiceButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        createServiceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createServiceButtonActionPerformed(evt);
            }
        });

        updateServiceButton.setBackground(new java.awt.Color(0, 68, 85));
        updateServiceButton.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 12)); // NOI18N
        updateServiceButton.setForeground(new java.awt.Color(255, 255, 255));
        updateServiceButton.setText("Editar datos");
        updateServiceButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        updateServiceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateServiceButtonActionPerformed(evt);
            }
        });

        deleteServiceButton.setBackground(new java.awt.Color(0, 68, 85));
        deleteServiceButton.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 12)); // NOI18N
        deleteServiceButton.setForeground(new java.awt.Color(255, 255, 255));
        deleteServiceButton.setText("Eliminar");
        deleteServiceButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        deleteServiceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteServiceButtonActionPerformed(evt);
            }
        });

        backServices.setBackground(new java.awt.Color(3, 121, 157));
        backServices.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 12)); // NOI18N
        backServices.setForeground(new java.awt.Color(255, 255, 255));
        backServices.setText("Volver");
        backServices.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        backServices.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backServicesActionPerformed(evt);
            }
        });

        newIncidentFromServiceButton.setBackground(new java.awt.Color(0, 68, 85));
        newIncidentFromServiceButton.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 12)); // NOI18N
        newIncidentFromServiceButton.setForeground(new java.awt.Color(255, 255, 255));
        newIncidentFromServiceButton.setText("Abrir incidencia");
        newIncidentFromServiceButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        newIncidentFromServiceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newIncidentFromServiceButtonActionPerformed(evt);
            }
        });

        incidentIdToService.setText("IncidentId");
        incidentIdToService.setFocusable(false);

        javax.swing.GroupLayout opServicesLayout = new javax.swing.GroupLayout(opServices);
        opServices.setLayout(opServicesLayout);
        opServicesLayout.setHorizontalGroup(
            opServicesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator2)
            .addGroup(opServicesLayout.createSequentialGroup()
                .addGroup(opServicesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(opServicesLayout.createSequentialGroup()
                        .addGap(76, 76, 76)
                        .addComponent(backServices, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(opServicesLayout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addComponent(jLabel22)
                        .addGap(26, 26, 26)
                        .addComponent(txtServiceId, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(32, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, opServicesLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(opServicesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, opServicesLayout.createSequentialGroup()
                        .addGroup(opServicesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel21, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(createServiceButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(updateServiceButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(deleteServiceButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(newIncidentFromServiceButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(80, 80, 80))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, opServicesLayout.createSequentialGroup()
                        .addComponent(incidentIdToService, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(110, 110, 110))))
        );
        opServicesLayout.setVerticalGroup(
            opServicesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(opServicesLayout.createSequentialGroup()
                .addGap(68, 68, 68)
                .addComponent(jLabel21)
                .addGap(30, 30, 30)
                .addGroup(opServicesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel22)
                    .addComponent(txtServiceId, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(44, 44, 44)
                .addComponent(createServiceButton, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(44, 44, 44)
                .addComponent(updateServiceButton, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(44, 44, 44)
                .addComponent(deleteServiceButton, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(44, 44, 44)
                .addComponent(newIncidentFromServiceButton, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(48, 48, 48)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(48, 48, 48)
                .addComponent(backServices, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addComponent(incidentIdToService, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(177, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 23;
        gridBagConstraints.ipadx = 20;
        gridBagConstraints.ipady = 566;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        formServicesPanel.add(opServices, gridBagConstraints);

        jLabel23.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 16)); // NOI18N
        jLabel23.setForeground(new java.awt.Color(255, 255, 255));
        jLabel23.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel23.setText("Fecha / Hora:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(36, 300, 0, 0);
        formServicesPanel.add(jLabel23, gridBagConstraints);

        dcServiceDate.setBackground(new java.awt.Color(255, 255, 255));
        dcServiceDate.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 12)); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 10;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.ipadx = 113;
        gridBagConstraints.ipady = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(31, 60, 0, 0);
        formServicesPanel.add(dcServiceDate, gridBagConstraints);

        jLabel31.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 24)); // NOI18N
        jLabel31.setForeground(new java.awt.Color(255, 255, 255));
        jLabel31.setText("/");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 11;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.ipadx = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(31, 30, 0, 0);
        formServicesPanel.add(jLabel31, gridBagConstraints);

        spServiceHour.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 12)); // NOI18N
        spServiceHour.setModel(new SpinnerNumberModel(8, 8, 20, 1));
        spServiceHour.setEditor(new JSpinner.NumberEditor(spServiceHour, "00"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 12;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.ipadx = 22;
        gridBagConstraints.ipady = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(31, 30, 0, 0);
        formServicesPanel.add(spServiceHour, gridBagConstraints);

        jLabel30.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 24)); // NOI18N
        jLabel30.setForeground(new java.awt.Color(255, 255, 255));
        jLabel30.setText(":");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 13;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(31, 10, 0, 0);
        formServicesPanel.add(jLabel30, gridBagConstraints);

        spServiceMinute.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 12)); // NOI18N
        spServiceMinute.setModel(new SpinnerNumberModel(0, 0, 59, 1));
        spServiceMinute.setEditor(new JSpinner.NumberEditor(spServiceMinute, "00"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 23;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 24;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.ipadx = 22;
        gridBagConstraints.ipady = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(31, 9, 0, 0);
        formServicesPanel.add(spServiceMinute, gridBagConstraints);

        jLabel27.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 16)); // NOI18N
        jLabel27.setForeground(new java.awt.Color(255, 255, 255));
        jLabel27.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel27.setText("Descripción:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(25, 310, 0, 0);
        formServicesPanel.add(jLabel27, gridBagConstraints);

        txtServiceDescription.setColumns(20);
        txtServiceDescription.setLineWrap(true);
        txtServiceDescription.setRows(5);
        serviceDescriptionJSPanel.setViewportView(txtServiceDescription);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 10;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 38;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.ipadx = 398;
        gridBagConstraints.ipady = 56;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(20, 60, 0, 186);
        formServicesPanel.add(serviceDescriptionJSPanel, gridBagConstraints);

        jLabel24.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 16)); // NOI18N
        jLabel24.setForeground(new java.awt.Color(255, 255, 255));
        jLabel24.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel24.setText("Operario:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(25, 330, 0, 0);
        formServicesPanel.add(jLabel24, gridBagConstraints);

        cmbServiceOperator.setBackground(new java.awt.Color(255, 255, 255));
        cmbServiceOperator.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 12)); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 10;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 30;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.ipadx = 20;
        gridBagConstraints.ipady = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(20, 60, 0, 0);
        formServicesPanel.add(cmbServiceOperator, gridBagConstraints);

        jLabel25.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 16)); // NOI18N
        jLabel25.setForeground(new java.awt.Color(255, 255, 255));
        jLabel25.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel25.setText("Tipo de servicio:");
        jLabel25.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(25, 280, 0, 0);
        formServicesPanel.add(jLabel25, gridBagConstraints);

        cmbServiceType.setBackground(new java.awt.Color(255, 255, 255));
        cmbServiceType.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 12)); // NOI18N
        cmbServiceType.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Medición", "Transporte", "Montaje" }));
        cmbServiceType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbServiceTypeActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 10;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.ipadx = 20;
        gridBagConstraints.ipady = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(20, 60, 0, 0);
        formServicesPanel.add(cmbServiceType, gridBagConstraints);

        jLabel28.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 16)); // NOI18N
        jLabel28.setForeground(new java.awt.Color(255, 255, 255));
        jLabel28.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel28.setText("Cliente:");
        jLabel28.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(25, 350, 0, 0);
        formServicesPanel.add(jLabel28, gridBagConstraints);

        cmbServiceCustomer.setBackground(new java.awt.Color(255, 255, 255));
        cmbServiceCustomer.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 12)); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 10;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 30;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.ipadx = 20;
        gridBagConstraints.ipady = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(20, 60, 0, 0);
        formServicesPanel.add(cmbServiceCustomer, gridBagConstraints);

        jLabel26.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 16)); // NOI18N
        jLabel26.setForeground(new java.awt.Color(255, 255, 255));
        jLabel26.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel26.setText("Estado:");
        jLabel26.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(25, 350, 0, 0);
        formServicesPanel.add(jLabel26, gridBagConstraints);

        cmbServiceStatus.setBackground(new java.awt.Color(255, 255, 255));
        cmbServiceStatus.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 12)); // NOI18N
        cmbServiceStatus.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Pendiente", "Confirmado", "Incidencia", "Finalizado", "Pendiente Finalización", "Propuesta nueva fecha", " " }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 10;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.ipadx = 20;
        gridBagConstraints.ipady = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(20, 60, 0, 0);
        formServicesPanel.add(cmbServiceStatus, gridBagConstraints);

        jLabel32.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 16)); // NOI18N
        jLabel32.setForeground(new java.awt.Color(255, 255, 255));
        jLabel32.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel32.setText("Comentarios:");
        jLabel32.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(25, 310, 0, 0);
        formServicesPanel.add(jLabel32, gridBagConstraints);

        txtServiceComments.setColumns(20);
        txtServiceComments.setLineWrap(true);
        txtServiceComments.setRows(4);
        serviceCommentsJSPanel.setViewportView(txtServiceComments);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 10;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.gridwidth = 38;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.ipadx = 398;
        gridBagConstraints.ipady = 56;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(20, 60, 0, 186);
        formServicesPanel.add(serviceCommentsJSPanel, gridBagConstraints);

        jLabel29.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 16)); // NOI18N
        jLabel29.setForeground(new java.awt.Color(255, 255, 255));
        jLabel29.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel29.setText("Archivos adjuntos:");
        jLabel29.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(25, 270, 0, 0);
        formServicesPanel.add(jLabel29, gridBagConstraints);

        sharedFilesJSPanel.setPreferredSize(new java.awt.Dimension(280, 60));

        sharedFileList.setPreferredSize(new java.awt.Dimension(30, 60));
        sharedFileList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                sharedFileListMouseClicked(evt);
            }
        });
        sharedFilesJSPanel.setViewportView(sharedFileList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 10;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.gridwidth = 38;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.ipadx = 398;
        gridBagConstraints.ipady = 58;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(20, 60, 0, 186);
        formServicesPanel.add(sharedFilesJSPanel, gridBagConstraints);

        deleteFileButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/delete-filled.png"))); // NOI18N
        deleteFileButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        deleteFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteFileButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 17;
        gridBagConstraints.ipadx = 4;
        gridBagConstraints.ipady = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 330, 0, 0);
        formServicesPanel.add(deleteFileButton, gridBagConstraints);

        addFileButton.setBackground(new java.awt.Color(3, 121, 157));
        addFileButton.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 24)); // NOI18N
        addFileButton.setForeground(new java.awt.Color(255, 255, 255));
        addFileButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/54719.png"))); // NOI18N
        addFileButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        addFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addFileButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 17;
        gridBagConstraints.gridwidth = 8;
        gridBagConstraints.ipadx = 4;
        gridBagConstraints.ipady = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 30, 0, 0);
        formServicesPanel.add(addFileButton, gridBagConstraints);

        jLabel33.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 16)); // NOI18N
        jLabel33.setForeground(new java.awt.Color(255, 255, 255));
        jLabel33.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel33.setText("Archivos adjuntos:");
        jLabel33.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 19;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(25, 270, 0, 0);
        formServicesPanel.add(jLabel33, gridBagConstraints);

        operatorFilesJSPanel.setPreferredSize(new java.awt.Dimension(280, 60));

        operatorFileList.setPreferredSize(new java.awt.Dimension(30, 60));
        operatorFileList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                operatorFileListMouseClicked(evt);
            }
        });
        operatorFilesJSPanel.setViewportView(operatorFileList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 10;
        gridBagConstraints.gridy = 19;
        gridBagConstraints.gridwidth = 38;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.ipadx = 398;
        gridBagConstraints.ipady = 58;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(20, 60, 0, 186);
        formServicesPanel.add(operatorFilesJSPanel, gridBagConstraints);

        saveDiscardPanel1.setBackground(new java.awt.Color(0, 153, 153));
        saveDiscardPanel1.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        saveService.setBackground(new java.awt.Color(3, 121, 157));
        saveService.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 12)); // NOI18N
        saveService.setForeground(new java.awt.Color(255, 255, 255));
        saveService.setText("Guardar");
        saveService.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        saveService.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveServiceActionPerformed(evt);
            }
        });

        discardService.setBackground(new java.awt.Color(0, 68, 85));
        discardService.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 12)); // NOI18N
        discardService.setForeground(new java.awt.Color(255, 255, 255));
        discardService.setText("Cancelar");
        discardService.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        discardService.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                discardServiceActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout saveDiscardPanel1Layout = new javax.swing.GroupLayout(saveDiscardPanel1);
        saveDiscardPanel1.setLayout(saveDiscardPanel1Layout);
        saveDiscardPanel1Layout.setHorizontalGroup(
            saveDiscardPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(saveDiscardPanel1Layout.createSequentialGroup()
                .addGap(60, 60, 60)
                .addComponent(saveService, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 122, Short.MAX_VALUE)
                .addComponent(discardService, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(60, 60, 60))
        );
        saveDiscardPanel1Layout.setVerticalGroup(
            saveDiscardPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(saveDiscardPanel1Layout.createSequentialGroup()
                .addGap(38, 38, 38)
                .addGroup(saveDiscardPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(saveService, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(discardService, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(32, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 21;
        gridBagConstraints.gridwidth = 23;
        gridBagConstraints.ipadx = 116;
        gridBagConstraints.ipady = 20;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(20, 290, 20, 0);
        formServicesPanel.add(saveDiscardPanel1, gridBagConstraints);

        mainContent.add(formServicesPanel, "formServices");

        formIncidentsPanel.setBackground(new java.awt.Color(0, 153, 204));
        formIncidentsPanel.setPreferredSize(new java.awt.Dimension(1366, 750));
        formIncidentsPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel35.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 24)); // NOI18N
        jLabel35.setForeground(new java.awt.Color(255, 255, 255));
        jLabel35.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel35.setText("Formulario de Incidencias - VD Logistics");
        formIncidentsPanel.add(jLabel35, new org.netbeans.lib.awtextra.AbsoluteConstraints(590, 40, -1, -1));

        opIncidents.setBackground(new java.awt.Color(0, 153, 153));
        opIncidents.setAlignmentX(0.0F);
        opIncidents.setAlignmentY(0.0F);
        opIncidents.setPreferredSize(new java.awt.Dimension(240, 1100));

        jLabel36.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 20)); // NOI18N
        jLabel36.setForeground(new java.awt.Color(255, 255, 255));
        jLabel36.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel36.setText("Operaciones");

        createIncidentButton.setBackground(new java.awt.Color(0, 68, 85));
        createIncidentButton.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 12)); // NOI18N
        createIncidentButton.setForeground(new java.awt.Color(255, 255, 255));
        createIncidentButton.setText("Crear Nueva");
        createIncidentButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        createIncidentButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createIncidentButtonActionPerformed(evt);
            }
        });

        updateIncidentButton.setBackground(new java.awt.Color(0, 68, 85));
        updateIncidentButton.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 12)); // NOI18N
        updateIncidentButton.setForeground(new java.awt.Color(255, 255, 255));
        updateIncidentButton.setText("Editar datos");
        updateIncidentButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        updateIncidentButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateIncidentButtonActionPerformed(evt);
            }
        });

        deleteIncidentButton.setBackground(new java.awt.Color(0, 68, 85));
        deleteIncidentButton.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 12)); // NOI18N
        deleteIncidentButton.setForeground(new java.awt.Color(255, 255, 255));
        deleteIncidentButton.setText("Eliminar");
        deleteIncidentButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        deleteIncidentButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteIncidentButtonActionPerformed(evt);
            }
        });

        backIncidents.setBackground(new java.awt.Color(3, 121, 157));
        backIncidents.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 12)); // NOI18N
        backIncidents.setForeground(new java.awt.Color(255, 255, 255));
        backIncidents.setText("Volver");
        backIncidents.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        backIncidents.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backIncidentsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout opIncidentsLayout = new javax.swing.GroupLayout(opIncidents);
        opIncidents.setLayout(opIncidentsLayout);
        opIncidentsLayout.setHorizontalGroup(
            opIncidentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, opIncidentsLayout.createSequentialGroup()
                .addContainerGap(74, Short.MAX_VALUE)
                .addGroup(opIncidentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel36, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(createIncidentButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(updateIncidentButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(deleteIncidentButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(80, 80, 80))
            .addComponent(jSeparator3)
            .addGroup(opIncidentsLayout.createSequentialGroup()
                .addGap(76, 76, 76)
                .addComponent(backIncidents, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        opIncidentsLayout.setVerticalGroup(
            opIncidentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(opIncidentsLayout.createSequentialGroup()
                .addGap(68, 68, 68)
                .addComponent(jLabel36)
                .addGap(60, 60, 60)
                .addComponent(createIncidentButton, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(51, 51, 51)
                .addComponent(updateIncidentButton, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(48, 48, 48)
                .addComponent(deleteIncidentButton, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(48, 48, 48)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(48, 48, 48)
                .addComponent(backIncidents, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(607, Short.MAX_VALUE))
        );

        formIncidentsPanel.add(opIncidents, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, -10, 280, 1100));

        jLabel37.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 16)); // NOI18N
        jLabel37.setForeground(new java.awt.Color(255, 255, 255));
        jLabel37.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel37.setText("ID:");
        formIncidentsPanel.add(jLabel37, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 125, -1, -1));

        txtIncidentId.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 12)); // NOI18N
        txtIncidentId.setForeground(new java.awt.Color(0, 0, 0));
        txtIncidentId.setDisabledTextColor(new java.awt.Color(153, 153, 153));
        txtIncidentId.setEnabled(false);
        txtIncidentId.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtIncidentIdActionPerformed(evt);
            }
        });
        formIncidentsPanel.add(txtIncidentId, new org.netbeans.lib.awtextra.AbsoluteConstraints(760, 120, 280, 30));

        jLabel45.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 16)); // NOI18N
        jLabel45.setForeground(new java.awt.Color(255, 255, 255));
        jLabel45.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel45.setText("Fecha / Hora:");
        formIncidentsPanel.add(jLabel45, new org.netbeans.lib.awtextra.AbsoluteConstraints(580, 205, -1, -1));

        txtIncidentDate.setEditable(false);
        formIncidentsPanel.add(txtIncidentDate, new org.netbeans.lib.awtextra.AbsoluteConstraints(760, 200, 280, 30));

        jLabel39.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 16)); // NOI18N
        jLabel39.setForeground(new java.awt.Color(255, 255, 255));
        jLabel39.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel39.setText("Operario:");
        formIncidentsPanel.add(jLabel39, new org.netbeans.lib.awtextra.AbsoluteConstraints(610, 285, -1, -1));

        cmbIncidentOperator.setBackground(new java.awt.Color(255, 255, 255));
        cmbIncidentOperator.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 12)); // NOI18N
        formIncidentsPanel.add(cmbIncidentOperator, new org.netbeans.lib.awtextra.AbsoluteConstraints(760, 280, 320, 30));

        jLabel42.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 16)); // NOI18N
        jLabel42.setForeground(new java.awt.Color(255, 255, 255));
        jLabel42.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel42.setText("Descripción:");
        formIncidentsPanel.add(jLabel42, new org.netbeans.lib.awtextra.AbsoluteConstraints(590, 365, -1, -1));

        txtIncidentDescription.setColumns(20);
        txtIncidentDescription.setRows(5);
        incidentDescriptionJSPanel.setViewportView(txtIncidentDescription);

        formIncidentsPanel.add(incidentDescriptionJSPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(760, 360, 320, 90));

        jLabel40.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 16)); // NOI18N
        jLabel40.setForeground(new java.awt.Color(255, 255, 255));
        jLabel40.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel40.setText("Servicio:");
        formIncidentsPanel.add(jLabel40, new org.netbeans.lib.awtextra.AbsoluteConstraints(620, 500, -1, -1));

        txtIncidentService.setEditable(false);
        txtIncidentService.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 12)); // NOI18N
        txtIncidentService.setForeground(new java.awt.Color(0, 0, 0));
        formIncidentsPanel.add(txtIncidentService, new org.netbeans.lib.awtextra.AbsoluteConstraints(760, 500, 240, 30));

        processIncidentButton.setBackground(new java.awt.Color(0, 153, 153));
        processIncidentButton.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 12)); // NOI18N
        processIncidentButton.setForeground(new java.awt.Color(255, 255, 255));
        processIncidentButton.setText("Tramitar Incidencia");
        processIncidentButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        processIncidentButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                processIncidentButtonActionPerformed(evt);
            }
        });
        formIncidentsPanel.add(processIncidentButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(750, 590, 160, 30));

        saveDiscardPanel2.setBackground(new java.awt.Color(0, 153, 153));
        saveDiscardPanel2.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        saveIncident.setBackground(new java.awt.Color(3, 121, 157));
        saveIncident.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 12)); // NOI18N
        saveIncident.setForeground(new java.awt.Color(255, 255, 255));
        saveIncident.setText("Guardar");
        saveIncident.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        saveIncident.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveIncidentActionPerformed(evt);
            }
        });

        discardIncident.setBackground(new java.awt.Color(0, 68, 85));
        discardIncident.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 12)); // NOI18N
        discardIncident.setForeground(new java.awt.Color(255, 255, 255));
        discardIncident.setText("Cancelar");
        discardIncident.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        discardIncident.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                discardIncidentActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout saveDiscardPanel2Layout = new javax.swing.GroupLayout(saveDiscardPanel2);
        saveDiscardPanel2.setLayout(saveDiscardPanel2Layout);
        saveDiscardPanel2Layout.setHorizontalGroup(
            saveDiscardPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(saveDiscardPanel2Layout.createSequentialGroup()
                .addGap(60, 60, 60)
                .addComponent(saveIncident, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 122, Short.MAX_VALUE)
                .addComponent(discardIncident, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(60, 60, 60))
        );
        saveDiscardPanel2Layout.setVerticalGroup(
            saveDiscardPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(saveDiscardPanel2Layout.createSequentialGroup()
                .addGap(38, 38, 38)
                .addGroup(saveDiscardPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(saveIncident, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(discardIncident, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(32, Short.MAX_VALUE))
        );

        formIncidentsPanel.add(saveDiscardPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(570, 680, 500, 110));

        mainContent.add(formIncidentsPanel, "formIncidents");

        formCustomersPanel.setBackground(new java.awt.Color(0, 153, 204));
        formCustomersPanel.setPreferredSize(new java.awt.Dimension(1366, 750));
        formCustomersPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel38.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 24)); // NOI18N
        jLabel38.setForeground(new java.awt.Color(255, 255, 255));
        jLabel38.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel38.setText("Formulario de Clientes - VD Logistics");
        formCustomersPanel.add(jLabel38, new org.netbeans.lib.awtextra.AbsoluteConstraints(590, 40, -1, -1));

        opCustomers.setBackground(new java.awt.Color(0, 153, 153));
        opCustomers.setAlignmentX(0.0F);
        opCustomers.setAlignmentY(0.0F);
        opCustomers.setPreferredSize(new java.awt.Dimension(240, 1100));

        jLabel41.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 20)); // NOI18N
        jLabel41.setForeground(new java.awt.Color(255, 255, 255));
        jLabel41.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel41.setText("Operaciones");

        createCustomerButton.setBackground(new java.awt.Color(0, 68, 85));
        createCustomerButton.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 12)); // NOI18N
        createCustomerButton.setForeground(new java.awt.Color(255, 255, 255));
        createCustomerButton.setText("Añadir Nuevo");
        createCustomerButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        createCustomerButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createCustomerButtonActionPerformed(evt);
            }
        });

        updateCustomerButton.setBackground(new java.awt.Color(0, 68, 85));
        updateCustomerButton.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 12)); // NOI18N
        updateCustomerButton.setForeground(new java.awt.Color(255, 255, 255));
        updateCustomerButton.setText("Editar datos");
        updateCustomerButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        updateCustomerButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateCustomerButtonActionPerformed(evt);
            }
        });

        deleteCustomerButton.setBackground(new java.awt.Color(0, 68, 85));
        deleteCustomerButton.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 12)); // NOI18N
        deleteCustomerButton.setForeground(new java.awt.Color(255, 255, 255));
        deleteCustomerButton.setText("Eliminar");
        deleteCustomerButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        deleteCustomerButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteCustomerButtonActionPerformed(evt);
            }
        });

        backCustomers.setBackground(new java.awt.Color(3, 121, 157));
        backCustomers.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 12)); // NOI18N
        backCustomers.setForeground(new java.awt.Color(255, 255, 255));
        backCustomers.setText("Volver");
        backCustomers.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        backCustomers.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backCustomersActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout opCustomersLayout = new javax.swing.GroupLayout(opCustomers);
        opCustomers.setLayout(opCustomersLayout);
        opCustomersLayout.setHorizontalGroup(
            opCustomersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, opCustomersLayout.createSequentialGroup()
                .addContainerGap(74, Short.MAX_VALUE)
                .addGroup(opCustomersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel41, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(createCustomerButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(updateCustomerButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(deleteCustomerButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(80, 80, 80))
            .addComponent(jSeparator4)
            .addGroup(opCustomersLayout.createSequentialGroup()
                .addGap(76, 76, 76)
                .addComponent(backCustomers, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        opCustomersLayout.setVerticalGroup(
            opCustomersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(opCustomersLayout.createSequentialGroup()
                .addGap(68, 68, 68)
                .addComponent(jLabel41)
                .addGap(60, 60, 60)
                .addComponent(createCustomerButton, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(51, 51, 51)
                .addComponent(updateCustomerButton, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(48, 48, 48)
                .addComponent(deleteCustomerButton, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(48, 48, 48)
                .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(48, 48, 48)
                .addComponent(backCustomers, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(607, Short.MAX_VALUE))
        );

        formCustomersPanel.add(opCustomers, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, -10, 280, 1100));

        jLabel43.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 16)); // NOI18N
        jLabel43.setForeground(new java.awt.Color(255, 255, 255));
        jLabel43.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel43.setText("ID:");
        formCustomersPanel.add(jLabel43, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 125, -1, -1));

        txtCustomerId.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 12)); // NOI18N
        txtCustomerId.setForeground(new java.awt.Color(0, 0, 0));
        txtCustomerId.setDisabledTextColor(new java.awt.Color(153, 153, 153));
        txtCustomerId.setEnabled(false);
        txtCustomerId.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtCustomerIdActionPerformed(evt);
            }
        });
        formCustomersPanel.add(txtCustomerId, new org.netbeans.lib.awtextra.AbsoluteConstraints(760, 120, 280, 30));

        jLabel46.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 16)); // NOI18N
        jLabel46.setForeground(new java.awt.Color(255, 255, 255));
        jLabel46.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel46.setText("Nombre:");
        formCustomersPanel.add(jLabel46, new org.netbeans.lib.awtextra.AbsoluteConstraints(620, 195, -1, -1));
        formCustomersPanel.add(txtCustomerFirstName, new org.netbeans.lib.awtextra.AbsoluteConstraints(760, 190, 280, 30));

        jLabel44.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 16)); // NOI18N
        jLabel44.setForeground(new java.awt.Color(255, 255, 255));
        jLabel44.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel44.setText("Apellidos:");
        formCustomersPanel.add(jLabel44, new org.netbeans.lib.awtextra.AbsoluteConstraints(610, 265, -1, -1));
        formCustomersPanel.add(txtCustomerLastName, new org.netbeans.lib.awtextra.AbsoluteConstraints(760, 260, 280, 30));

        jLabel49.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 16)); // NOI18N
        jLabel49.setForeground(new java.awt.Color(255, 255, 255));
        jLabel49.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel49.setText("Email:");
        formCustomersPanel.add(jLabel49, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 335, -1, -1));
        formCustomersPanel.add(txtCustomerEmail, new org.netbeans.lib.awtextra.AbsoluteConstraints(760, 330, 280, 30));

        jLabel47.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 16)); // NOI18N
        jLabel47.setForeground(new java.awt.Color(255, 255, 255));
        jLabel47.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel47.setText("Teléfono:");
        formCustomersPanel.add(jLabel47, new org.netbeans.lib.awtextra.AbsoluteConstraints(610, 405, -1, -1));
        formCustomersPanel.add(txtCustomerPhone, new org.netbeans.lib.awtextra.AbsoluteConstraints(760, 400, 140, 30));

        jLabel50.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 16)); // NOI18N
        jLabel50.setForeground(new java.awt.Color(255, 255, 255));
        jLabel50.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel50.setText("Dirección:");
        formCustomersPanel.add(jLabel50, new org.netbeans.lib.awtextra.AbsoluteConstraints(600, 475, -1, -1));
        formCustomersPanel.add(txtCustomerAddress, new org.netbeans.lib.awtextra.AbsoluteConstraints(760, 470, 320, 30));

        jLabel48.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 16)); // NOI18N
        jLabel48.setForeground(new java.awt.Color(255, 255, 255));
        jLabel48.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel48.setText("Info Adicional:");
        formCustomersPanel.add(jLabel48, new org.netbeans.lib.awtextra.AbsoluteConstraints(570, 545, -1, -1));
        formCustomersPanel.add(txtCustomerAdditional, new org.netbeans.lib.awtextra.AbsoluteConstraints(760, 540, 280, 30));

        servicesByCustomer.setBackground(new java.awt.Color(0, 153, 153));
        servicesByCustomer.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 12)); // NOI18N
        servicesByCustomer.setForeground(new java.awt.Color(255, 255, 255));
        servicesByCustomer.setText("Ver Servicios");
        servicesByCustomer.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        servicesByCustomer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                servicesByCustomerActionPerformed(evt);
            }
        });
        formCustomersPanel.add(servicesByCustomer, new org.netbeans.lib.awtextra.AbsoluteConstraints(740, 630, 160, 30));

        saveDiscardPanel3.setBackground(new java.awt.Color(0, 153, 153));
        saveDiscardPanel3.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        saveCustomer.setBackground(new java.awt.Color(3, 121, 157));
        saveCustomer.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 12)); // NOI18N
        saveCustomer.setForeground(new java.awt.Color(255, 255, 255));
        saveCustomer.setText("Guardar");
        saveCustomer.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        saveCustomer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveCustomerActionPerformed(evt);
            }
        });

        discardCustomer.setBackground(new java.awt.Color(0, 68, 85));
        discardCustomer.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 12)); // NOI18N
        discardCustomer.setForeground(new java.awt.Color(255, 255, 255));
        discardCustomer.setText("Cancelar");
        discardCustomer.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        discardCustomer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                discardCustomerActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout saveDiscardPanel3Layout = new javax.swing.GroupLayout(saveDiscardPanel3);
        saveDiscardPanel3.setLayout(saveDiscardPanel3Layout);
        saveDiscardPanel3Layout.setHorizontalGroup(
            saveDiscardPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(saveDiscardPanel3Layout.createSequentialGroup()
                .addGap(60, 60, 60)
                .addComponent(saveCustomer, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 122, Short.MAX_VALUE)
                .addComponent(discardCustomer, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(60, 60, 60))
        );
        saveDiscardPanel3Layout.setVerticalGroup(
            saveDiscardPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(saveDiscardPanel3Layout.createSequentialGroup()
                .addGap(38, 38, 38)
                .addGroup(saveDiscardPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(saveCustomer, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(discardCustomer, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(32, Short.MAX_VALUE))
        );

        formCustomersPanel.add(saveDiscardPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(570, 680, 500, 110));

        mainContent.add(formCustomersPanel, "formCustomers");

        searchServicesPanel.setBackground(new java.awt.Color(0, 153, 204));
        searchServicesPanel.setPreferredSize(new java.awt.Dimension(1366, 750));
        searchServicesPanel.setLayout(new java.awt.GridBagLayout());

        jLabel52.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 24)); // NOI18N
        jLabel52.setForeground(new java.awt.Color(255, 255, 255));
        jLabel52.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel52.setText("Listado de Servicios por Cliente");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(40, 500, 0, 0);
        searchServicesPanel.add(jLabel52, gridBagConstraints);

        jLabel51.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 16)); // NOI18N
        jLabel51.setForeground(new java.awt.Color(255, 255, 255));
        jLabel51.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel51.setText("Número de teléfono:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(46, 480, 0, 0);
        searchServicesPanel.add(jLabel51, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.ipadx = 135;
        gridBagConstraints.ipady = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(41, 19, 0, 0);
        searchServicesPanel.add(txtSearchByPhone, gridBagConstraints);

        searchServiceButton.setBackground(new java.awt.Color(0, 153, 153));
        searchServiceButton.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 12)); // NOI18N
        searchServiceButton.setForeground(new java.awt.Color(255, 255, 255));
        searchServiceButton.setText("Buscar");
        searchServiceButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        searchServiceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchServiceButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.ipadx = 42;
        gridBagConstraints.ipady = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(41, 20, 0, 0);
        searchServicesPanel.add(searchServiceButton, gridBagConstraints);

        searchScrollPanel.setAutoscrolls(true);
        searchScrollPanel.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 12)); // NOI18N
        searchScrollPanel.setViewportView(searchTable);

        searchTable.setAutoCreateRowSorter(true);
        searchTable.setBackground(java.awt.SystemColor.control);
        searchTable.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        searchTable.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        searchTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Id Servicio", "Fecha", "Tipo de servicio", "Estado", "Descripción"
            }
        ));
        searchTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        searchTable.setFillsViewportHeight(true);
        searchTable.setFocusable(false);
        searchTable.setRowHeight(40);
        searchTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        searchTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        searchTable.setShowGrid(true);
        searchTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                searchTableMouseClicked(evt);
            }
        });
        searchScrollPanel.setViewportView(searchTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 1078;
        gridBagConstraints.ipady = 464;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(60, 133, 60, 133);
        searchServicesPanel.add(searchScrollPanel, gridBagConstraints);

        mainContent.add(searchServicesPanel, "searchServices");

        mainScrollPanel.setViewportView(mainContent);

        usersMenu.setText("Usuarios");

        jMenuItem1.setText("Añadir Nuevo");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        usersMenu.add(jMenuItem1);

        jMenuItem2.setText("Listado de Usuarios");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        usersMenu.add(jMenuItem2);

        jMenuItem3.setText("Registro de Actividad");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        usersMenu.add(jMenuItem3);

        jMenuBar.add(usersMenu);

        servicesMenu.setText("Servicios");

        jMenuItem4.setText("Crear Nuevo");
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem4ActionPerformed(evt);
            }
        });
        servicesMenu.add(jMenuItem4);

        jMenuItem5.setText("Confirmados / Por Confirmar Cita");
        jMenuItem5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem5ActionPerformed(evt);
            }
        });
        servicesMenu.add(jMenuItem5);

        jMenuItem11.setText("Pendientes Finalizar / Propuesta Nueva Cita");
        jMenuItem11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem11ActionPerformed(evt);
            }
        });
        servicesMenu.add(jMenuItem11);

        jMenuItem13.setText("Servicios Finalizados");
        jMenuItem13.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem13ActionPerformed(evt);
            }
        });
        servicesMenu.add(jMenuItem13);

        jMenuItem14.setText("Buscar servicios");
        jMenuItem14.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem14ActionPerformed(evt);
            }
        });
        servicesMenu.add(jMenuItem14);

        jMenuBar.add(servicesMenu);

        incidentsMenu.setText("Incidencias");

        jMenuItem6.setText("Crear Nueva");
        jMenuItem6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem6ActionPerformed(evt);
            }
        });
        incidentsMenu.add(jMenuItem6);

        jMenuItem7.setText("Listado de Incidencias");
        jMenuItem7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem7ActionPerformed(evt);
            }
        });
        incidentsMenu.add(jMenuItem7);

        jMenuBar.add(incidentsMenu);

        customersMenu.setText("Clientes");

        jMenuItem8.setText("Alta Nuevo Cliente");
        jMenuItem8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem8ActionPerformed(evt);
            }
        });
        customersMenu.add(jMenuItem8);

        jMenuItem9.setText("Listado de Clientes");
        jMenuItem9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem9ActionPerformed(evt);
            }
        });
        customersMenu.add(jMenuItem9);

        jMenuBar.add(customersMenu);

        currentUserMenu.setBackground(new java.awt.Color(0, 68, 85));
        currentUserMenu.setForeground(new java.awt.Color(255, 255, 255));
        currentUserMenu.setText("Usuario: VD Logistics");
        currentUserMenu.setFont(new java.awt.Font("Dialog", 1, 11)); // NOI18N
        currentUserMenu.setOpaque(true);

        jMenuItem10.setText("Cerrar Sesión");
        jMenuItem10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem10ActionPerformed(evt);
            }
        });
        currentUserMenu.add(jMenuItem10);

        jMenuItem12.setText("Salir");
        jMenuItem12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem12ActionPerformed(evt);
            }
        });
        currentUserMenu.add(jMenuItem12);

        jMenuBar.add(currentUserMenu);

        setJMenuBar(jMenuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainScrollPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 1366, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainScrollPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 856, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jMenuItem10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem10ActionPerformed
        this.dispose();
        new LoginFrame().setVisible(true);
    }//GEN-LAST:event_jMenuItem10ActionPerformed

    private void mainContentComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_mainContentComponentResized
        // TODO add your handling code here:
    }//GEN-LAST:event_mainContentComponentResized

    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
        boolean maximized = (getExtendedState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH;

        if (maximized) {
            mainScrollPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
            mainScrollPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        } else {
            mainScrollPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            mainScrollPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        }
    }//GEN-LAST:event_formComponentResized

    private void jMenuItem5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem5ActionPerformed
        navigateCard("services");
    }//GEN-LAST:event_jMenuItem5ActionPerformed

    private void jMenuItem11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem11ActionPerformed
        navigateCard("otherServices");
    }//GEN-LAST:event_jMenuItem11ActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        navigateCard("users");
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void jMenuItem9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem9ActionPerformed
        navigateCard("customers");
    }//GEN-LAST:event_jMenuItem9ActionPerformed

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
        navigateCard("logs");
    }//GEN-LAST:event_jMenuItem3ActionPerformed

    private void createUserButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createUserButtonActionPerformed
        clearForm(formUsersPanel);
        enabledDashboardButtons();
        createUserButton.setEnabled(false);
        txtUserFirstName.requestFocus();
    }//GEN-LAST:event_createUserButtonActionPerformed

    private void updateUserButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateUserButtonActionPerformed
        navigateCard("users");
        clearForm(formUsersPanel);
        enabledDashboardButtons();
    }//GEN-LAST:event_updateUserButtonActionPerformed

    private void deleteUserButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteUserButtonActionPerformed
        if (!txtUserId.getText().isEmpty()) {
            int confirm = JOptionPane.showConfirmDialog(this, "¿ Estás seguro de eliminar este usuario ?", "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
            String userId = txtUserId.getText().trim();
            if (confirm == JOptionPane.YES_OPTION) {
                boolean success = DataUser.deleteUser(userId, this);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Usuario ha sido eliminado", "VD Logistics", JOptionPane.INFORMATION_MESSAGE);
                    navigateCard("users");
                    clearForm(formUsersPanel);
                    enabledDashboardButtons();
                }
            }
        } else {
            navigateCard("users");
            clearForm(formUsersPanel);
            enabledDashboardButtons();
        }
    }//GEN-LAST:event_deleteUserButtonActionPerformed

    private void backUsersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backUsersActionPerformed
        navigateCard("users");
        clearForm(formUsersPanel);
        enabledDashboardButtons();
    }//GEN-LAST:event_backUsersActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        navigateCard("formUsers");
        clearForm(formUsersPanel);
        enabledDashboardButtons();
        createUserButton.setEnabled(false);
        txtUserFirstName.requestFocus();
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void saveUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveUserActionPerformed
        if (txtUserId.getText().isEmpty()) {
            createUser();
        } else {
            String userId = txtUserId.getText();
            String firstName = txtUserFirstName.getText();
            String lastName = txtUserLastName.getText();
            String phone = txtUserPhone.getText();
            String address = txtUserAddress.getText();
            String type = comboUserType.getSelectedItem().toString();

            boolean success = DataUser.updateUser(userId, firstName, lastName, phone, address, type);
            if (success) {
                JOptionPane.showMessageDialog(this, "Usuario actualizado correctamente", "VD Logistics", JOptionPane.INFORMATION_MESSAGE);
                navigateCard("users");
                clearForm(formUsersPanel);
                enabledDashboardButtons();
            } else {
                JOptionPane.showMessageDialog(this, "Error al actualizar usuario", "VD Logistics", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_saveUserActionPerformed

    private void discardUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_discardUserActionPerformed
        navigateCard("users");
        clearForm(formUsersPanel);
        enabledDashboardButtons();
    }//GEN-LAST:event_discardUserActionPerformed

    private void usersTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_usersTableMouseClicked
        int selectedRow = usersTable.getSelectedRow();
        if (selectedRow != -1) {
            String userId = usersTable.getValueAt(selectedRow, 0).toString();
            Map<String, String> userData = DataUser.getDataUser(userId);
            if (!userData.isEmpty()) {
                navigateCard("formUsers");
                updateUserButton.setEnabled(false);
                txtUserFirstName.requestFocus();
                showUserForm(userData);
            } else {
                JOptionPane.showMessageDialog(this, "Error al cargar datos del usuario", "VD Logistics", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_usersTableMouseClicked

    private void jMenuItem12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem12ActionPerformed
        System.exit(0);
    }//GEN-LAST:event_jMenuItem12ActionPerformed

    private void jMenuItem7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem7ActionPerformed
        navigateCard("incidents");
    }//GEN-LAST:event_jMenuItem7ActionPerformed

    private void createServiceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createServiceButtonActionPerformed
        clearForm(formServicesPanel);
        txtServiceId.setText("");
        enabledDashboardButtons();
        createServiceButton.setEnabled(false);
        newIncidentFromServiceButton.setEnabled(false);
        dcServiceDate.requestFocus();
    }//GEN-LAST:event_createServiceButtonActionPerformed

    private void updateServiceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateServiceButtonActionPerformed
        navigateCard("services");
        clearForm(formServicesPanel);
        txtServiceId.setText("");
        enabledDashboardButtons();
    }//GEN-LAST:event_updateServiceButtonActionPerformed

    private void deleteServiceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteServiceButtonActionPerformed
        if (!txtServiceId.getText().isEmpty()) {
            int confirm = JOptionPane.showConfirmDialog(this, "¿ Estás seguro de eliminar este servicio ?", "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
            String serviceId = txtServiceId.getText().trim();
            if (confirm == JOptionPane.YES_OPTION) {
                DataService.deleteService(this.userEmail, serviceId);
                navigateCard("services");
                clearForm(formUsersPanel);
                enabledDashboardButtons();
            }
        } else {
            navigateCard("services");
            clearForm(formUsersPanel);
            enabledDashboardButtons();
        }
    }//GEN-LAST:event_deleteServiceButtonActionPerformed

    private void backServicesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backServicesActionPerformed
        if (cmbServiceStatus.getSelectedItem() == "Propuesta nueva fecha" || cmbServiceStatus.getSelectedItem() == "Pendiente Finalización") {
            navigateCard("otherServices");
        } else {
            navigateCard("services");
        }
        clearForm(formServicesPanel);
        enabledDashboardButtons();
    }//GEN-LAST:event_backServicesActionPerformed

    private void saveServiceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveServiceActionPerformed
        if (txtServiceId.getText().isEmpty()) {
            saveService("new");
        } else {
            saveService("update");
            navigateCard("services");
            clearForm(formServicesPanel);
            enabledDashboardButtons();
        }
    }//GEN-LAST:event_saveServiceActionPerformed

    private void discardServiceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_discardServiceActionPerformed
        if (cmbServiceStatus.getSelectedItem() == "Propuesta nueva fecha" || cmbServiceStatus.getSelectedItem() == "Pendiente Finalización") {
            navigateCard("otherServices");
        } else {
            navigateCard("services");
        }
        clearForm(formServicesPanel);
        enabledDashboardButtons();
    }//GEN-LAST:event_discardServiceActionPerformed

    private void confirmedTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_confirmedTableMouseClicked
        int selectedRow = confirmedTable.getSelectedRow();
        if (selectedRow != -1 && confirmedTable.getValueAt(selectedRow, 1) != "No hay servicios") {
            String serviceId = confirmedTable.getValueAt(selectedRow, 0).toString();
            Service serviceData = DataService.getServiceById(serviceId);
            if (serviceData != null) {
                FileService fileOpener = new FileService(sharedFileList, operatorFileList, serviceId);
                navigateCard("formServices");
                updateServiceButton.setEnabled(false);
                showServiceForm(serviceData, false);
            } else {
                JOptionPane.showMessageDialog(this, "Error al cargar datos del usuario", "VD Logistics", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_confirmedTableMouseClicked

    private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem4ActionPerformed
        navigateCard("formServices");
        clearForm(formServicesPanel);
        txtServiceId.setText("");
        enabledDashboardButtons();
        createServiceButton.setEnabled(false);
    }//GEN-LAST:event_jMenuItem4ActionPerformed

    private void pendingTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pendingTableMouseClicked
        int selectedRow = pendingTable.getSelectedRow();
        if (selectedRow != -1 && pendingTable.getValueAt(selectedRow, 1) != "No hay servicios") {
            String serviceId = pendingTable.getValueAt(selectedRow, 0).toString();
            Service serviceData = DataService.getServiceById(serviceId);
            if (serviceData != null) {
                navigateCard("formServices");
                updateServiceButton.setEnabled(false);
                showServiceForm(serviceData, false);
            } else {
                JOptionPane.showMessageDialog(this, "Error al cargar datos del usuario", "VD Logistics", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_pendingTableMouseClicked

    private void pendingCompletionTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pendingCompletionTableMouseClicked
        int selectedRow = pendingCompletionTable.getSelectedRow();
        if (selectedRow != -1 && pendingCompletionTable.getValueAt(selectedRow, 1) != "No hay servicios") {
            String serviceId = pendingCompletionTable.getValueAt(selectedRow, 0).toString();
            Service serviceData = DataService.getServiceById(serviceId);
            if (serviceData != null) {
                navigateCard("formServices");
                updateServiceButton.setEnabled(false);
                showServiceForm(serviceData, false);
            } else {
                JOptionPane.showMessageDialog(this, "Error al cargar datos del usuario", "VD Logistics", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_pendingCompletionTableMouseClicked

    private void newDateTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_newDateTableMouseClicked
        int selectedRow = newDateTable.getSelectedRow();
        if (selectedRow != -1 && newDateTable.getValueAt(selectedRow, 1) != "No hay servicios") {
            String serviceId = newDateTable.getValueAt(selectedRow, 0).toString();
            Service serviceData = DataService.getServiceById(serviceId);
            if (serviceData != null) {
                navigateCard("formServices");
                updateServiceButton.setEnabled(false);
                showServiceForm(serviceData, false);
            } else {
                JOptionPane.showMessageDialog(this, "Error al cargar datos del usuario", "VD Logistics", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_newDateTableMouseClicked

    private void sharedFileListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_sharedFileListMouseClicked
        if (evt.getClickCount() == 2) {
            String folderPath = "services/" + txtServiceId.getText() + "/resources/";
            FileService.openSelectedFile(sharedFileList, folderPath);
        }
    }//GEN-LAST:event_sharedFileListMouseClicked

    private void addFileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addFileButtonActionPerformed
        int returnVal = jFileChooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File[] selectedFiles = jFileChooser.getSelectedFiles();

            for (File selectedFile : selectedFiles) {
                String fileName = selectedFile.getName();

                // Agregar a la lista temporal
                tempFiles.add(selectedFile);
                tempListModel.addElement(fileName);
            }

            // Actualizar JList con los archivos temporales
            sharedFileList.setModel(tempListModel);
        }
    }//GEN-LAST:event_addFileButtonActionPerformed

    private void operatorFileListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_operatorFileListMouseClicked
        if (evt.getClickCount() == 2) {
            String folderPath = "services/" + txtServiceId.getText() + "/byOperator/";
            FileService.openSelectedFile(operatorFileList, folderPath);
        }
    }//GEN-LAST:event_operatorFileListMouseClicked

    private void deleteFileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteFileButtonActionPerformed
        String selectedFile = sharedFileList.getSelectedValue();

        if (selectedFile == null || selectedFile.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No has seleccionado ningún archivo.", "VD Logistics", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(null, "¿Seguro que quieres eliminar este archivo?",
                "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            deleteFileFromStorage(selectedFile, txtServiceId.getText());
        }
    }//GEN-LAST:event_deleteFileButtonActionPerformed

    private void completedTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_completedTableMouseClicked
        int selectedRow = completedTable.getSelectedRow();
        if (selectedRow != -1 && completedTable.getValueAt(selectedRow, 1) != "No hay servicios") {
            String serviceId = completedTable.getValueAt(selectedRow, 0).toString();
            Service serviceData = DataService.getServiceById(serviceId);
            if (serviceData != null) {
                navigateCard("formServices");
                updateServiceButton.setEnabled(false);
                showServiceForm(serviceData, false);
            } else {
                JOptionPane.showMessageDialog(this, "Error al cargar datos del usuario", "VD Logistics", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_completedTableMouseClicked

    private void jMenuItem13ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem13ActionPerformed
        navigateCard("completedServices");
    }//GEN-LAST:event_jMenuItem13ActionPerformed

    private void createIncidentButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createIncidentButtonActionPerformed
        clearForm(formIncidentsPanel);
        JOptionPane.showMessageDialog(this, "Debes seleccionar un servicio", "VD Logistics", JOptionPane.INFORMATION_MESSAGE);
        enabledDashboardButtons();
        navigateCard("services");
    }//GEN-LAST:event_createIncidentButtonActionPerformed

    private void updateIncidentButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateIncidentButtonActionPerformed
        navigateCard("incidents");
        clearForm(formIncidentsPanel);
        enabledDashboardButtons();
    }//GEN-LAST:event_updateIncidentButtonActionPerformed

    private void deleteIncidentButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteIncidentButtonActionPerformed
        if (!txtIncidentId.getText().isEmpty()) {
            int confirm = JOptionPane.showConfirmDialog(this, "¿ Estás seguro de eliminar esta incidencia ?", "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
            String incidentId = txtIncidentId.getText().trim();
            if (confirm == JOptionPane.YES_OPTION) {
                DataIncident.deleteIncident(this.userEmail, incidentId);
                navigateCard("incidents");
                clearForm(formIncidentsPanel);
                enabledDashboardButtons();
            }
        } else {
            navigateCard("incidents");
            clearForm(formIncidentsPanel);
            enabledDashboardButtons();
        }
    }//GEN-LAST:event_deleteIncidentButtonActionPerformed

    private void backIncidentsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backIncidentsActionPerformed
        navigateCard("incidents");
        clearForm(formIncidentsPanel);
        enabledDashboardButtons();
    }//GEN-LAST:event_backIncidentsActionPerformed

    private void saveIncidentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveIncidentActionPerformed
        if (txtIncidentId.getText().isEmpty()) {
            saveIncident("new");
        } else {
            saveIncident("update");
            navigateCard("incidents");
            clearForm(formIncidentsPanel);
            enabledDashboardButtons();
        }
    }//GEN-LAST:event_saveIncidentActionPerformed

    private void discardIncidentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_discardIncidentActionPerformed
        navigateCard("incidents");
        clearForm(formIncidentsPanel);
        enabledDashboardButtons();
    }//GEN-LAST:event_discardIncidentActionPerformed

    private void pendingIncidentsTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pendingIncidentsTableMouseClicked
        int selectedRow = pendingIncidentsTable.getSelectedRow();
        if (selectedRow != -1 && pendingIncidentsTable.getValueAt(selectedRow, 1) != "No hay incidencias") {
            String incidentId = pendingIncidentsTable.getValueAt(selectedRow, 0).toString();
            Incident incidentData = DataIncident.getIncidentById(incidentId);
            if (incidentData != null) {
                navigateCard("formIncidents");
                updateIncidentButton.setEnabled(false);
                showIncidentForm(incidentData, false);
            } else {
                JOptionPane.showMessageDialog(this, "Error al cargar los datos", "VD Logistics", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_pendingIncidentsTableMouseClicked

    private void processedIncidentsTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_processedIncidentsTableMouseClicked
        int selectedRow = processedIncidentsTable.getSelectedRow();
        if (selectedRow != -1 && processedIncidentsTable.getValueAt(selectedRow, 1) != "No hay incidencias") {
            String incidentId = processedIncidentsTable.getValueAt(selectedRow, 0).toString();
            Incident incidentData = DataIncident.getIncidentById(incidentId);
            if (incidentData != null) {
                navigateCard("formIncidents");
                updateIncidentButton.setEnabled(false);
                showIncidentForm(incidentData, false);
            } else {
                JOptionPane.showMessageDialog(this, "Error al cargar los datos", "VD Logistics", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_processedIncidentsTableMouseClicked

    private void jMenuItem6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem6ActionPerformed
        clearForm(formIncidentsPanel);
        JOptionPane.showMessageDialog(this, "Debes seleccionar un servicio", "VD Logistics", JOptionPane.INFORMATION_MESSAGE);
        enabledDashboardButtons();
        navigateCard("services");
    }//GEN-LAST:event_jMenuItem6ActionPerformed

    private void txtIncidentIdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtIncidentIdActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtIncidentIdActionPerformed

    private void processIncidentButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_processIncidentButtonActionPerformed
        Service incidentService = DataService.getServiceById(txtIncidentService.getText());
        String incidentOperator = incidentService.getOperator();
        String incidentTypeService = incidentService.getType();
        String incidentCustomer = incidentService.getCustomer();
        String incidentStatusService = "Pendiente";
        String incidentDescription = "Incidencia No. " + txtIncidentId.getText() + ": " + txtIncidentDescription.getText();
        String incidentComments = "";
        Service serviceData = new Service(null, null, incidentOperator, incidentTypeService, incidentCustomer, incidentStatusService, incidentDescription, incidentComments);
        incidentIdToService.setText(txtIncidentId.getText());
        navigateCard("formServices");
        updateServiceButton.setEnabled(false);
        showServiceForm(serviceData, true);
    }//GEN-LAST:event_processIncidentButtonActionPerformed

    private void newIncidentFromServiceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newIncidentFromServiceButtonActionPerformed
        String serviceId = txtServiceId.getText();
        String incidentOperator = (String) cmbServiceOperator.getSelectedItem();
        String incidentStatus = "Pendiente";
        String incidentDescription = "";
        Incident incidentData = new Incident(null, null, incidentOperator, incidentDescription, serviceId, incidentStatus);

        navigateCard("formIncidents");
        updateIncidentButton.setEnabled(false);
        showIncidentForm(incidentData, true);
    }//GEN-LAST:event_newIncidentFromServiceButtonActionPerformed

    private void cmbServiceTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbServiceTypeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbServiceTypeActionPerformed

    private void createCustomerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createCustomerButtonActionPerformed
        clearForm(formCustomersPanel);
        enabledDashboardButtons();
        createCustomerButton.setEnabled(false);
        //newIncidentFromServiceButton.setEnabled(false);
        txtCustomerFirstName.requestFocus();
    }//GEN-LAST:event_createCustomerButtonActionPerformed

    private void updateCustomerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateCustomerButtonActionPerformed
        navigateCard("customers");
        clearForm(formCustomersPanel);
        enabledDashboardButtons();
    }//GEN-LAST:event_updateCustomerButtonActionPerformed

    private void deleteCustomerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteCustomerButtonActionPerformed
        if (!txtCustomerId.getText().isEmpty()) {
            int confirm = JOptionPane.showConfirmDialog(this, "¿ Estás seguro de eliminar este cliente ?", "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
            String customerId = txtCustomerId.getText().trim();
            if (confirm == JOptionPane.YES_OPTION) {
                DataCustomer.deleteCustomer(this.userEmail, customerId);
                navigateCard("customers");
                clearForm(formCustomersPanel);
                enabledDashboardButtons();
            }
        } else {
            navigateCard("customers");
            clearForm(formCustomersPanel);
            enabledDashboardButtons();
        }
    }//GEN-LAST:event_deleteCustomerButtonActionPerformed

    private void backCustomersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backCustomersActionPerformed
        navigateCard("customers");
        clearForm(formCustomersPanel);
        enabledDashboardButtons();
    }//GEN-LAST:event_backCustomersActionPerformed

    private void txtCustomerIdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCustomerIdActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtCustomerIdActionPerformed

    private void servicesByCustomerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_servicesByCustomerActionPerformed
        if (!txtCustomerId.getText().isEmpty()) {
            List<String> customerIds = DataCustomer.getCustomerByPhone(txtCustomerPhone.getText());
            navigateCard("searchServices");
            clearForm(formCustomersPanel);
            enabledDashboardButtons();
            DataService.loadServicesByCustomerIds(searchTable, customerIds, loadingLabel, mainScrollPanel);
        } else {
            JOptionPane.showMessageDialog(null, "No encontramos ningún cliente registrado", "VD Logistics", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_servicesByCustomerActionPerformed

    private void saveCustomerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveCustomerActionPerformed
        if (txtCustomerId.getText().isEmpty()) {
            saveCustomer("new");
        } else {
            saveCustomer("update");
            navigateCard("customers");
            clearForm(formCustomersPanel);
            enabledDashboardButtons();
        }
    }//GEN-LAST:event_saveCustomerActionPerformed

    private void discardCustomerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_discardCustomerActionPerformed
        navigateCard("customers");
        clearForm(formCustomersPanel);
        enabledDashboardButtons();
    }//GEN-LAST:event_discardCustomerActionPerformed

    private void customersTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_customersTableMouseClicked
        int selectedRow = customersTable.getSelectedRow();
        if (selectedRow != -1 && customersTable.getValueAt(selectedRow, 1) != "No hay clientes") {
            String customerId = customersTable.getValueAt(selectedRow, 0).toString();
            Customer customerData = DataCustomer.getCustomerById(customerId);
            if (customerData != null) {
                navigateCard("formCustomers");
                updateCustomerButton.setEnabled(false);
                showCustomerForm(customerData);
            } else {
                JOptionPane.showMessageDialog(this, "Error al cargar los datos", "VD Logistics", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_customersTableMouseClicked

    private void jMenuItem8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem8ActionPerformed
        navigateCard("formCustomers");
        clearForm(formCustomersPanel);
        enabledDashboardButtons();
        createCustomerButton.setEnabled(false);
        //newIncidentFromServiceButton.setEnabled(false);
        txtCustomerFirstName.requestFocus();
    }//GEN-LAST:event_jMenuItem8ActionPerformed

    private void searchTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_searchTableMouseClicked
        int selectedRow = searchTable.getSelectedRow();
        if (selectedRow != -1 && searchTable.getValueAt(selectedRow, 1) != "No hay servicios") {
            String serviceId = searchTable.getValueAt(selectedRow, 0).toString();
            Service serviceData = DataService.getServiceById(serviceId);
            if (serviceData != null) {
                FileService fileOpener = new FileService(sharedFileList, operatorFileList, serviceId);
                navigateCard("formServices");
                updateServiceButton.setEnabled(false);
                showServiceForm(serviceData, false);

            } else {
                JOptionPane.showMessageDialog(this, "Error al cargar datos del usuario", "VD Logistics", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_searchTableMouseClicked

    private void searchServiceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchServiceButtonActionPerformed
        List<String> customerIds = DataCustomer.getCustomerByPhone(txtSearchByPhone.getText());

        if (customerIds == null || customerIds.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No encontramos ningún cliente", "VD Logistics", JOptionPane.WARNING_MESSAGE);
        } else {
            DataService.loadServicesByCustomerIds(searchTable, customerIds, loadingLabel, mainScrollPanel);
        }
    }//GEN-LAST:event_searchServiceButtonActionPerformed

    private void jMenuItem14ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem14ActionPerformed
        navigateCard("searchServices");
        clearForm(searchServicesPanel);
        DefaultTableModel model = (DefaultTableModel) searchTable.getModel();
        model.setRowCount(0);
        txtSearchByPhone.requestFocus();
    }//GEN-LAST:event_jMenuItem14ActionPerformed

    private void deleteFileFromStorage(String fileName, String serviceId) {
        try {
            String bucketName = "vd-logistics.firebasestorage.app";
            String filePathInBucket = "services/" + serviceId + "/resources/" + fileName;

            Storage storage = FirebaseConfig.getStorage();
            Blob blob = storage.get(bucketName).get(filePathInBucket);

            if (blob == null) {
                JOptionPane.showMessageDialog(null, "El archivo no existe en Firebase Storage.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            blob.delete();
            JOptionPane.showMessageDialog(null, "Archivo eliminado correctamente.", "VD Logistics", JOptionPane.INFORMATION_MESSAGE);

            new FileService(sharedFileList, operatorFileList, serviceId);

        } catch (HeadlessException e) {
            JOptionPane.showMessageDialog(null, "Error al eliminar archivo: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addFileButton;
    private javax.swing.JButton backCustomers;
    private javax.swing.JButton backIncidents;
    private javax.swing.JButton backServices;
    private javax.swing.JButton backUsers;
    private javax.swing.JComboBox<String> cmbIncidentOperator;
    private javax.swing.JComboBox<String> cmbServiceCustomer;
    private javax.swing.JComboBox<String> cmbServiceOperator;
    private javax.swing.JComboBox<String> cmbServiceStatus;
    private javax.swing.JComboBox<String> cmbServiceType;
    private javax.swing.JComboBox<String> comboUserType;
    private javax.swing.JScrollPane completedScrollPanel;
    private javax.swing.JPanel completedServicesPanel;
    private javax.swing.JTable completedTable;
    private javax.swing.JScrollPane confirmedScrollPanel;
    private javax.swing.JTable confirmedTable;
    private javax.swing.JButton createCustomerButton;
    private javax.swing.JButton createIncidentButton;
    private javax.swing.JButton createServiceButton;
    private javax.swing.JButton createUserButton;
    private javax.swing.JMenu currentUserMenu;
    private javax.swing.JMenu customersMenu;
    private javax.swing.JPanel customersPanel;
    private javax.swing.JScrollPane customersScrollPanel;
    private javax.swing.JTable customersTable;
    private com.toedter.calendar.JDateChooser dcServiceDate;
    private javax.swing.JButton deleteCustomerButton;
    private javax.swing.JButton deleteFileButton;
    private javax.swing.JButton deleteIncidentButton;
    private javax.swing.JButton deleteServiceButton;
    private javax.swing.JButton deleteUserButton;
    private javax.swing.JButton discardCustomer;
    private javax.swing.JButton discardIncident;
    private javax.swing.JButton discardService;
    private javax.swing.JButton discardUser;
    private javax.swing.JPanel formCustomersPanel;
    private javax.swing.JPanel formIncidentsPanel;
    private javax.swing.JPanel formServicesPanel;
    private javax.swing.JPanel formUsersPanel;
    private javax.swing.JScrollPane incidentDescriptionJSPanel;
    private javax.swing.JTextField incidentIdToService;
    private javax.swing.JMenu incidentsMenu;
    private javax.swing.JPanel incidentsPanel;
    private javax.swing.JFileChooser jFileChooser;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel42;
    private javax.swing.JLabel jLabel43;
    private javax.swing.JLabel jLabel44;
    private javax.swing.JLabel jLabel45;
    private javax.swing.JLabel jLabel46;
    private javax.swing.JLabel jLabel47;
    private javax.swing.JLabel jLabel48;
    private javax.swing.JLabel jLabel49;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel50;
    private javax.swing.JLabel jLabel51;
    private javax.swing.JLabel jLabel52;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JMenuBar jMenuBar;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem10;
    private javax.swing.JMenuItem jMenuItem11;
    private javax.swing.JMenuItem jMenuItem12;
    private javax.swing.JMenuItem jMenuItem13;
    private javax.swing.JMenuItem jMenuItem14;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JMenuItem jMenuItem6;
    private javax.swing.JMenuItem jMenuItem7;
    private javax.swing.JMenuItem jMenuItem8;
    private javax.swing.JMenuItem jMenuItem9;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JLabel loadingLabel;
    private javax.swing.JPanel logsPanel;
    private javax.swing.JScrollPane logsScrollPanel;
    private javax.swing.JTable logsTable;
    private javax.swing.JPanel mainContent;
    private javax.swing.JScrollPane mainScrollPanel;
    private javax.swing.JScrollPane newDateScrollPanel;
    private javax.swing.JTable newDateTable;
    private javax.swing.JButton newIncidentFromServiceButton;
    private javax.swing.JPanel opCustomers;
    private javax.swing.JPanel opIncidents;
    private javax.swing.JPanel opServices;
    private javax.swing.JPanel opUsers;
    private javax.swing.JList<String> operatorFileList;
    private javax.swing.JScrollPane operatorFilesJSPanel;
    private javax.swing.JPanel otherServicesPanel;
    private javax.swing.JScrollPane pendingCompletionScrollPanel;
    private javax.swing.JTable pendingCompletionTable;
    private javax.swing.JScrollPane pendingIncidentsScrollPanel;
    private javax.swing.JTable pendingIncidentsTable;
    private javax.swing.JScrollPane pendingScrollPanel;
    private javax.swing.JTable pendingTable;
    private javax.swing.JButton processIncidentButton;
    private javax.swing.JScrollPane processedIncidentsScrollPanel;
    private javax.swing.JTable processedIncidentsTable;
    private javax.swing.JButton saveCustomer;
    private javax.swing.JPanel saveDiscardPanel;
    private javax.swing.JPanel saveDiscardPanel1;
    private javax.swing.JPanel saveDiscardPanel2;
    private javax.swing.JPanel saveDiscardPanel3;
    private javax.swing.JButton saveIncident;
    private javax.swing.JButton saveService;
    private javax.swing.JButton saveUser;
    private javax.swing.JScrollPane searchScrollPanel;
    private javax.swing.JButton searchServiceButton;
    private javax.swing.JPanel searchServicesPanel;
    private javax.swing.JTable searchTable;
    private javax.swing.JScrollPane serviceCommentsJSPanel;
    private javax.swing.JScrollPane serviceDescriptionJSPanel;
    private javax.swing.JButton servicesByCustomer;
    private javax.swing.JMenu servicesMenu;
    private javax.swing.JPanel servicesPanel;
    private javax.swing.JList<String> sharedFileList;
    private javax.swing.JScrollPane sharedFilesJSPanel;
    private javax.swing.JSpinner spServiceHour;
    private javax.swing.JSpinner spServiceMinute;
    private javax.swing.JTextField txtCustomerAdditional;
    private javax.swing.JTextField txtCustomerAddress;
    private javax.swing.JTextField txtCustomerEmail;
    private javax.swing.JTextField txtCustomerFirstName;
    private javax.swing.JTextField txtCustomerId;
    private javax.swing.JTextField txtCustomerLastName;
    private javax.swing.JTextField txtCustomerPhone;
    private javax.swing.JTextField txtIncidentDate;
    private javax.swing.JTextArea txtIncidentDescription;
    private javax.swing.JTextField txtIncidentId;
    private javax.swing.JTextField txtIncidentService;
    private javax.swing.JTextField txtSearchByPhone;
    private javax.swing.JTextArea txtServiceComments;
    private javax.swing.JTextArea txtServiceDescription;
    private javax.swing.JTextField txtServiceId;
    private javax.swing.JTextField txtUserAddress;
    private javax.swing.JTextField txtUserEmail;
    private javax.swing.JTextField txtUserFirstName;
    private javax.swing.JTextField txtUserId;
    private javax.swing.JTextField txtUserLastName;
    private javax.swing.JPasswordField txtUserPassword;
    private javax.swing.JTextField txtUserPhone;
    private javax.swing.JButton updateCustomerButton;
    private javax.swing.JButton updateIncidentButton;
    private javax.swing.JButton updateServiceButton;
    private javax.swing.JButton updateUserButton;
    private javax.swing.JMenu usersMenu;
    private javax.swing.JPanel usersPanel;
    private javax.swing.JScrollPane usersScrollPanel;
    private javax.swing.JTable usersTable;
    // End of variables declaration//GEN-END:variables

    class BackgroundPanel extends JPanel {

        private Image image;

        @Override
        public void paint(Graphics g) {
            image = new ImageIcon(getClass().getResource("/images/fullBackground.png")).getImage();
            g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
            setOpaque(false);
            super.paint(g);
        }
    }
}
