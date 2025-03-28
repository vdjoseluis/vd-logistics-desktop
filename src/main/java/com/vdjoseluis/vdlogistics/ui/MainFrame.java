package com.vdjoseluis.vdlogistics.ui;

import com.vdjoseluis.vdlogistics.firebase.FirebaseConfig;
import com.vdjoseluis.vdlogistics.firebase.FirebaseStorage;
import com.vdjoseluis.vdlogistics.firebase.data.DataCustomer;
import com.vdjoseluis.vdlogistics.firebase.data.DataIncident;
import com.vdjoseluis.vdlogistics.firebase.data.DataLogs;
import com.vdjoseluis.vdlogistics.firebase.data.DataService;
import com.vdjoseluis.vdlogistics.firebase.data.DataUser;
import com.vdjoseluis.vdlogistics.models.Service;
import com.vdjoseluis.vdlogistics.models.User;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Image;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

/**
 *
 * @author José Luis Vásquez Drouet
 */
public class MainFrame extends javax.swing.JFrame {

    BackgroundPanel background = new BackgroundPanel();
    private final String userEmail;

    public MainFrame(String email) {
        this.setContentPane(background);

        initComponents();

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
        DataService.loadServices(pendingCompletionTable, "Pendiente Finalización", loadingLabel, mainScrollPanel);
        DataService.loadServices(newDateTable, "Propuesta nueva fecha", loadingLabel, mainScrollPanel);
        DataUser.loadUsers(usersTable, loadingLabel, mainScrollPanel);
        DataCustomer.loadCustomers(customersTable, loadingLabel, mainScrollPanel);
        DataLogs.loadLogs(logsTable, loadingLabel, mainScrollPanel);
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
        DataCustomer.listenForCustomerNames(cmbServiceCustomer);
    }

    private void enabledDashboardButtons() {
        createUserButton.setEnabled(true);
        updateUserButton.setEnabled(true);
        deleteUserButton.setEnabled(true);
        createServiceButton.setEnabled(true);
        updateServiceButton.setEnabled(true);
        deleteServiceButton.setEnabled(true);
    }

    private void clearForm(JPanel panel) {
        for (Component component : panel.getComponents()) {
            if (component instanceof JTextField) {
                ((JTextField) component).setText("");
            } else if (component instanceof JComboBox) {
                ((JComboBox<?>) component).setSelectedIndex(0);
            } else if (component instanceof JPasswordField) {
                ((JPasswordField) component).setText("");
            }
        }
        txtUserEmail.setEnabled(true);
        txtUserPassword.setEnabled(true);

        txtServiceDescription.setText("");
        txtServiceComments.setText("");

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        dcServiceDate.setDate(calendar.getTime());
        spServiceHour.setValue(8);
        spServiceMinute.setValue(0);
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

    private void showServiceForm(Service serviceData) {
        txtServiceId.setText(serviceData.getId());
        Date serviceDate = serviceData.getDate();
        if (serviceDate != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(serviceDate);
            dcServiceDate.setDate(serviceDate);
            spServiceHour.setValue(calendar.get(Calendar.HOUR_OF_DAY));
            spServiceMinute.setValue(calendar.get(Calendar.MINUTE));
        }

        txtServiceDescription.setText(serviceData.getDescription());
        cmbServiceOperator.setSelectedItem(serviceData.getOperator());
        cmbServiceType.setSelectedItem(serviceData.getType());
        cmbServiceCustomer.setSelectedItem(serviceData.getCustomer());
        cmbServiceStatus.setSelectedItem(serviceData.getStatus());
        txtServiceComments.setText(serviceData.getComments());
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

        if (description.isEmpty() || date == null) {
            JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.", "VD Logistics", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Service newService = new Service(id, date, operatorName, type, customerName, status, description, comments);
        boolean success = false;

        if ("new".equals(action)) {
            success = DataService.createService(newService, userEmail, dcServiceDate, spServiceHour, spServiceMinute);
        } else if ("update".equals(action)) {
            success = DataService.updateService(newService, userEmail, dcServiceDate, spServiceHour, spServiceMinute);
        }

        if (success) {
            JOptionPane.showMessageDialog(this, "Registrado correctamente!", "VD Logistics", JOptionPane.INFORMATION_MESSAGE);
            clearForm(formServicesPanel);
            navigateCard("services");
        } else {
            JOptionPane.showMessageDialog(this, "Error al registrar servicio.", "VD Logistics", JOptionPane.ERROR_MESSAGE);
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
        addFileButton = new javax.swing.JButton();
        sharedFilesJSPanel = new javax.swing.JScrollPane();
        sharedFileList = new javax.swing.JList<>();
        saveDiscardPanel1 = new javax.swing.JPanel();
        saveService = new javax.swing.JButton();
        discardService = new javax.swing.JButton();
        jMenuBar = new javax.swing.JMenuBar();
        usersMenu = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        servicesMenu = new javax.swing.JMenu();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenuItem5 = new javax.swing.JMenuItem();
        jMenuItem11 = new javax.swing.JMenuItem();
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
        customersTable.setShowGrid(false);
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
        pendingIncidentsTable.setShowGrid(false);
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
        formUsersPanel.add(txtUserId, new org.netbeans.lib.awtextra.AbsoluteConstraints(760, 120, 200, 30));

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
        formServicesPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel20.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 24)); // NOI18N
        jLabel20.setForeground(new java.awt.Color(255, 255, 255));
        jLabel20.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel20.setText("Formulario de Servicios - VD Logistics");
        formServicesPanel.add(jLabel20, new org.netbeans.lib.awtextra.AbsoluteConstraints(590, 40, -1, -1));

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

        javax.swing.GroupLayout opServicesLayout = new javax.swing.GroupLayout(opServices);
        opServices.setLayout(opServicesLayout);
        opServicesLayout.setHorizontalGroup(
            opServicesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, opServicesLayout.createSequentialGroup()
                .addContainerGap(74, Short.MAX_VALUE)
                .addGroup(opServicesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel21, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(createServiceButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(updateServiceButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(deleteServiceButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(80, 80, 80))
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
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(48, 48, 48)
                .addComponent(backServices, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(578, Short.MAX_VALUE))
        );

        formServicesPanel.add(opServices, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, -10, 280, 1100));

        jLabel23.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 16)); // NOI18N
        jLabel23.setForeground(new java.awt.Color(255, 255, 255));
        jLabel23.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel23.setText("Fecha / Hora:");
        formServicesPanel.add(jLabel23, new org.netbeans.lib.awtextra.AbsoluteConstraints(580, 105, -1, -1));

        dcServiceDate.setBackground(new java.awt.Color(255, 255, 255));
        dcServiceDate.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 12)); // NOI18N
        formServicesPanel.add(dcServiceDate, new org.netbeans.lib.awtextra.AbsoluteConstraints(760, 100, 140, 30));

        jLabel31.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 24)); // NOI18N
        jLabel31.setForeground(new java.awt.Color(255, 255, 255));
        jLabel31.setText("/");
        formServicesPanel.add(jLabel31, new org.netbeans.lib.awtextra.AbsoluteConstraints(910, 100, -1, -1));

        spServiceHour.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 12)); // NOI18N
        spServiceHour.setModel(new SpinnerNumberModel(8, 8, 20, 1));
        spServiceHour.setEditor(new JSpinner.NumberEditor(spServiceHour, "00"));
        formServicesPanel.add(spServiceHour, new org.netbeans.lib.awtextra.AbsoluteConstraints(930, 100, 50, 30));

        jLabel30.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 24)); // NOI18N
        jLabel30.setForeground(new java.awt.Color(255, 255, 255));
        jLabel30.setText(":");
        formServicesPanel.add(jLabel30, new org.netbeans.lib.awtextra.AbsoluteConstraints(980, 100, -1, -1));

        spServiceMinute.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 12)); // NOI18N
        spServiceMinute.setModel(new SpinnerNumberModel(0, 0, 59, 1));
        spServiceMinute.setEditor(new JSpinner.NumberEditor(spServiceMinute, "00"));
        formServicesPanel.add(spServiceMinute, new org.netbeans.lib.awtextra.AbsoluteConstraints(990, 100, 50, 30));

        jLabel27.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 16)); // NOI18N
        jLabel27.setForeground(new java.awt.Color(255, 255, 255));
        jLabel27.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel27.setText("Descripción:");
        formServicesPanel.add(jLabel27, new org.netbeans.lib.awtextra.AbsoluteConstraints(590, 165, -1, -1));

        txtServiceDescription.setColumns(20);
        txtServiceDescription.setRows(5);
        serviceDescriptionJSPanel.setViewportView(txtServiceDescription);

        formServicesPanel.add(serviceDescriptionJSPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(760, 160, 280, -1));

        jLabel24.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 16)); // NOI18N
        jLabel24.setForeground(new java.awt.Color(255, 255, 255));
        jLabel24.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel24.setText("Operario:");
        formServicesPanel.add(jLabel24, new org.netbeans.lib.awtextra.AbsoluteConstraints(610, 275, -1, -1));

        cmbServiceOperator.setBackground(new java.awt.Color(255, 255, 255));
        cmbServiceOperator.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 12)); // NOI18N
        formServicesPanel.add(cmbServiceOperator, new org.netbeans.lib.awtextra.AbsoluteConstraints(760, 270, 280, 30));

        jLabel25.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 16)); // NOI18N
        jLabel25.setForeground(new java.awt.Color(255, 255, 255));
        jLabel25.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel25.setText("Tipo de servicio:");
        jLabel25.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        formServicesPanel.add(jLabel25, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 335, -1, -1));

        cmbServiceType.setBackground(new java.awt.Color(255, 255, 255));
        cmbServiceType.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 12)); // NOI18N
        cmbServiceType.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Medición", "Transporte", "Montaje" }));
        formServicesPanel.add(cmbServiceType, new org.netbeans.lib.awtextra.AbsoluteConstraints(760, 330, 120, 30));

        jLabel28.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 16)); // NOI18N
        jLabel28.setForeground(new java.awt.Color(255, 255, 255));
        jLabel28.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel28.setText("Cliente:");
        jLabel28.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        formServicesPanel.add(jLabel28, new org.netbeans.lib.awtextra.AbsoluteConstraints(630, 395, -1, -1));

        cmbServiceCustomer.setBackground(new java.awt.Color(255, 255, 255));
        cmbServiceCustomer.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 12)); // NOI18N
        formServicesPanel.add(cmbServiceCustomer, new org.netbeans.lib.awtextra.AbsoluteConstraints(760, 390, 280, 30));

        jLabel26.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 16)); // NOI18N
        jLabel26.setForeground(new java.awt.Color(255, 255, 255));
        jLabel26.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel26.setText("Estado:");
        jLabel26.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        formServicesPanel.add(jLabel26, new org.netbeans.lib.awtextra.AbsoluteConstraints(630, 455, -1, -1));

        cmbServiceStatus.setBackground(new java.awt.Color(255, 255, 255));
        cmbServiceStatus.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 12)); // NOI18N
        cmbServiceStatus.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Pendiente", "Confirmado", "Incidencia", "Finalizado", "Pendiente Finalización", "Propuesta nueva fecha", " " }));
        formServicesPanel.add(cmbServiceStatus, new org.netbeans.lib.awtextra.AbsoluteConstraints(760, 450, 180, 30));

        jLabel32.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 16)); // NOI18N
        jLabel32.setForeground(new java.awt.Color(255, 255, 255));
        jLabel32.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel32.setText("Comentarios:");
        jLabel32.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        formServicesPanel.add(jLabel32, new org.netbeans.lib.awtextra.AbsoluteConstraints(590, 515, -1, -1));

        txtServiceComments.setColumns(20);
        txtServiceComments.setRows(5);
        txtServiceComments.setPreferredSize(new java.awt.Dimension(160, 60));
        serviceCommentsJSPanel.setViewportView(txtServiceComments);

        formServicesPanel.add(serviceCommentsJSPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(760, 510, 280, 70));

        jLabel29.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 16)); // NOI18N
        jLabel29.setForeground(new java.awt.Color(255, 255, 255));
        jLabel29.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel29.setText("Archivos adjuntos:");
        jLabel29.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        formServicesPanel.add(jLabel29, new org.netbeans.lib.awtextra.AbsoluteConstraints(550, 610, -1, -1));

        addFileButton.setBackground(new java.awt.Color(3, 121, 157));
        addFileButton.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 24)); // NOI18N
        addFileButton.setForeground(new java.awt.Color(255, 255, 255));
        addFileButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/54719.png"))); // NOI18N
        addFileButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        formServicesPanel.add(addFileButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(670, 640, 30, 30));

        sharedFilesJSPanel.setPreferredSize(new java.awt.Dimension(280, 60));

        sharedFileList.setPreferredSize(new java.awt.Dimension(30, 60));
        sharedFilesJSPanel.setViewportView(sharedFileList);

        formServicesPanel.add(sharedFilesJSPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(760, 610, 280, 60));

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

        formServicesPanel.add(saveDiscardPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(570, 700, 500, 110));

        mainContent.add(formServicesPanel, "formServices");

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

        jMenuBar.add(servicesMenu);

        incidentsMenu.setText("Incidencias");

        jMenuItem6.setText("Crear Nueva");
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
                navigateCard("formServices");
                updateServiceButton.setEnabled(false);
                showServiceForm(serviceData);
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
                showServiceForm(serviceData);
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
                showServiceForm(serviceData);
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
                showServiceForm(serviceData);
            } else {
                JOptionPane.showMessageDialog(this, "Error al cargar datos del usuario", "VD Logistics", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_newDateTableMouseClicked

    /**
     * @param args the command line arguments
     */
//    public static void main(String args[]) {
//        /* Set the Nimbus look and feel */
//        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
//        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
//         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
//         */
//        try {
//            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
//                if ("Nimbus".equals(info.getName())) {
//                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
//                    break;
//                }
//            }
//        } catch (ClassNotFoundException ex) {
//            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (InstantiationException ex) {
//            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (IllegalAccessException ex) {
//            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
//            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        }
//        //</editor-fold>
//
//        /* Create and display the form */
//        java.awt.EventQueue.invokeLater(new Runnable() {
//            public void run() {
//                new MainFrame("vdjoseluis@outlook.com").setVisible(true);
//            }
//        });
//    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addFileButton;
    private javax.swing.JButton backServices;
    private javax.swing.JButton backUsers;
    private javax.swing.JComboBox<String> cmbServiceCustomer;
    private javax.swing.JComboBox<String> cmbServiceOperator;
    private javax.swing.JComboBox<String> cmbServiceStatus;
    private javax.swing.JComboBox<String> cmbServiceType;
    private javax.swing.JComboBox<String> comboUserType;
    private javax.swing.JScrollPane confirmedScrollPanel;
    private javax.swing.JTable confirmedTable;
    private javax.swing.JButton createServiceButton;
    private javax.swing.JButton createUserButton;
    private javax.swing.JMenu currentUserMenu;
    private javax.swing.JMenu customersMenu;
    private javax.swing.JPanel customersPanel;
    private javax.swing.JScrollPane customersScrollPanel;
    private javax.swing.JTable customersTable;
    private com.toedter.calendar.JDateChooser dcServiceDate;
    private javax.swing.JButton deleteServiceButton;
    private javax.swing.JButton deleteUserButton;
    private javax.swing.JButton discardService;
    private javax.swing.JButton discardUser;
    private javax.swing.JPanel formServicesPanel;
    private javax.swing.JPanel formUsersPanel;
    private javax.swing.JMenu incidentsMenu;
    private javax.swing.JPanel incidentsPanel;
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
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JMenuBar jMenuBar;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem10;
    private javax.swing.JMenuItem jMenuItem11;
    private javax.swing.JMenuItem jMenuItem12;
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
    private javax.swing.JLabel loadingLabel;
    private javax.swing.JPanel logsPanel;
    private javax.swing.JScrollPane logsScrollPanel;
    private javax.swing.JTable logsTable;
    private javax.swing.JPanel mainContent;
    private javax.swing.JScrollPane mainScrollPanel;
    private javax.swing.JScrollPane newDateScrollPanel;
    private javax.swing.JTable newDateTable;
    private javax.swing.JPanel opServices;
    private javax.swing.JPanel opUsers;
    private javax.swing.JPanel otherServicesPanel;
    private javax.swing.JScrollPane pendingCompletionScrollPanel;
    private javax.swing.JTable pendingCompletionTable;
    private javax.swing.JScrollPane pendingIncidentsScrollPanel;
    private javax.swing.JTable pendingIncidentsTable;
    private javax.swing.JScrollPane pendingScrollPanel;
    private javax.swing.JTable pendingTable;
    private javax.swing.JScrollPane processedIncidentsScrollPanel;
    private javax.swing.JTable processedIncidentsTable;
    private javax.swing.JPanel saveDiscardPanel;
    private javax.swing.JPanel saveDiscardPanel1;
    private javax.swing.JButton saveService;
    private javax.swing.JButton saveUser;
    private javax.swing.JScrollPane serviceCommentsJSPanel;
    private javax.swing.JScrollPane serviceDescriptionJSPanel;
    private javax.swing.JMenu servicesMenu;
    private javax.swing.JPanel servicesPanel;
    private javax.swing.JList<String> sharedFileList;
    private javax.swing.JScrollPane sharedFilesJSPanel;
    private javax.swing.JSpinner spServiceHour;
    private javax.swing.JSpinner spServiceMinute;
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
