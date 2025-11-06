import javax.swing.*;

import javax.swing.table.DefaultTableModel;

import javax.swing.table.DefaultTableCellRenderer;

import javax.swing.table.JTableHeader;

import java.awt.*;

import java.awt.event.MouseAdapter;

import java.awt.event.MouseEvent;

import java.awt.event.ActionListener;

import java.awt.event.ActionEvent;

import java.util.ArrayList;

import java.util.Date;

import java.text.SimpleDateFormat;

import java.text.DecimalFormat;

import java.sql.SQLException;

import java.sql.Timestamp;

import java.util.List;

import java.util.stream.Collectors;

public class PaymentManager {

// --- DATABASE INTEGRATION & NOTIFICATION MANAGER ---

private final DatabaseManager dbManager;

private final NotificationManager notificationManager;

public enum UserRole { CLIENT, FREELANCER, ADMIN }

private UserRole currentUserRole;

private JFrame frame;

private JPanel contentPanel;

private CardLayout cardLayout;

// --- DATA STRUCTURES (Fields) ---

private final ArrayList milestones = new ArrayList<>();

private final ArrayList escrowAccounts = new ArrayList<>();

private final ArrayList invoices = new ArrayList<>();

private final ArrayList disputes = new ArrayList<>();

// Table models

private DefaultTableModel milestonesTableModel;

private DefaultTableModel escrowTableModel;

private DefaultTableModel invoicesTableModel;

private DefaultTableModel disputesTableModel;

// Tables

private JTable milestonesTable;

private JTable escrowTable;

private JTable invoicesTable;

private JTable disputesTable;

// Form components

private JTextField projectIdField, milestoneAmountField, milestoneDescField;

private JTextField freelancerIdField, clientIdField, disputeReasonField;

private JTextArea milestoneNotesArea, disputeDetailsArea;

private JComboBox paymentMethodCombo;

private JComboBox invoiceStatusCombo, escrowStatusCombo, disputeStatusCombo;

// Dashboard Stat Labels (Kept for logic, but UI creation relies on old helper)

private JLabel milestonesCountLabel, escrowAmountLabel, openDisputesLabel, completedPaymentsLabel, totalInvoicesLabel, successRateLabel, avgResolutionTimeLabel, platformFeeLabel;

// ID generators (used for new local data but primarily used for DB functions now)

private int nextMilestoneId = 1;

private int nextEscrowId = 1;

private int nextInvoiceId = 1;

private int nextDisputeId = 1;

// Colors

private final Color bgColor = new Color(34, 47, 62);

private final Color sidebarColor = new Color(44, 62, 80);

private final Color textColor = new Color(236, 240, 241);

// Light color for text/labels

private final Color inputBgColor = new Color(99, 110, 114);

private final Color blueAccent = new Color(52, 152, 219);

private final Color greenAccent = new Color(46, 204, 113);

private final Color orangeAccent = new Color(230, 126, 34);

private final Color redAccent = new Color(231, 76, 60);

private final Color purpleAccent = new Color(155, 89, 182);

// Date/Currency Formatters

private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

private final DecimalFormat currencyFormat = new DecimalFormat("₹#,##0.00");

// --- CONSTRUCTOR - UPDATED WITH NOTIFICATION MANAGER ---

public PaymentManager(DatabaseManager dbManager, NotificationManager notificationManager) {

this.dbManager = dbManager;

this.notificationManager = notificationManager;

try {

for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {

if ("Nimbus".equals(info.getName())) {

UIManager.setLookAndFeel(info.getClassName());

break;

}

}

} catch (Exception e) {

// Fallback to default

}

}

// --- SHOW WINDOW METHODS ---

public void showWindow() {

showWindow(UserRole.ADMIN);

}

public void showWindow(UserRole role) {

this.currentUserRole = role;

if (frame != null && frame.isVisible()) {

frame.dispose();

}

initializeGUI();

// Initial data load and dashboard update

String prj = projectIdField != null ?

projectIdField.getText() : "";

loadData(prj);

frame.setVisible(true);

}

// --- UI SETUP ---

private void initializeGUI() {

frame = new JFrame("Payment & Escrow Management (" + currentUserRole.toString() + ")");

frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

frame.setSize(1400, 900);

frame.setLocationRelativeTo(null);

JPanel backgroundPanel = new JPanel();

backgroundPanel.setBackground(bgColor);

backgroundPanel.setLayout(new BorderLayout());

JPanel sidebar = createSidebar();

cardLayout = new CardLayout();

contentPanel = new JPanel(cardLayout);

contentPanel.setOpaque(false);

// Initialize Combo Boxes (Keep new combobox initialization)

paymentMethodCombo = createStyledComboBox(new String[]{"Credit Card", "PayPal", "Bank Transfer", "Cryptocurrency"});

// Use default JComboBox for these status fields since we only need the data structure

invoiceStatusCombo = new JComboBox<>(new String[]{"Draft", "Sent", "Paid", "Overdue", "Cancelled"});

escrowStatusCombo = new JComboBox<>(new String[]{"Funded", "Partially Released", "Released", "Refunded", "On Hold"});

disputeStatusCombo = new JComboBox<>(new String[]{"Open", "Under Review", "Resolved", "Escalated", "Closed"});

// Add panels

contentPanel.add(createMilestonesPanel(), "MILESTONES");

contentPanel.add(createEscrowPanel(), "ESCROW");

contentPanel.add(createInvoicesPanel(), "INVOICES");

contentPanel.add(createDisputesPanel(), "DISPUTES");

contentPanel.add(createPaymentDashboard(), "DASHBOARD");

backgroundPanel.add(sidebar, BorderLayout.WEST);

backgroundPanel.add(contentPanel, BorderLayout.CENTER);

frame.add(backgroundPanel);

cardLayout.show(contentPanel, "DASHBOARD");

}

private JPanel createSidebar() {

JPanel sidebar = new JPanel();

sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

sidebar.setBackground(sidebarColor);

sidebar.setPreferredSize(new Dimension(280, 0));

JLabel headerLabel = new JLabel("Payments & Escrow", JLabel.CENTER);

headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));

headerLabel.setForeground(textColor);

headerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

headerLabel.setBorder(BorderFactory.createEmptyBorder(25, 0, 25, 0));

sidebar.add(headerLabel);

// Restored old NavButton styling and placement for consistency

sidebar.add(new NavButton("💰 Payment Dashboard", "DASHBOARD", blueAccent));

sidebar.add(new NavButton("🎯 Milestone Payments", "MILESTONES", greenAccent));

sidebar.add(new NavButton("🔒 Escrow Accounts", "ESCROW", orangeAccent));

sidebar.add(new NavButton("📄 Invoices & Receipts", "INVOICES", purpleAccent));

sidebar.add(new NavButton("⚖️ Dispute Resolution", "DISPUTES", redAccent));

sidebar.add(Box.createVerticalGlue());

return sidebar;

}

// Panel 1: Payment Dashboard (RESTORED OLD UI LOOK)

private JPanel createPaymentDashboard() {

JPanel panel = new JPanel(new BorderLayout(15, 15));

panel.setOpaque(false);

panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 40, 40));

// --- RESTORED OLD HEADER STYLE ---

panel.add(createStyledHeader("Payment & Escrow Dashboard", blueAccent), BorderLayout.NORTH);

// Stats panel

JPanel statsPanel = new JPanel(new GridLayout(2, 4, 25, 25));

statsPanel.setOpaque(false);

// Setup Stat Labels (Kept for logic update by updateDashboardFor)

milestonesCountLabel = createValueLabel("0", greenAccent);

escrowAmountLabel = createValueLabel("₹0.00", orangeAccent);

completedPaymentsLabel = createValueLabel("0", blueAccent);

openDisputesLabel = createValueLabel("0", redAccent);

totalInvoicesLabel = createValueLabel("0", purpleAccent);

successRateLabel = createValueLabel("100%", new Color(26, 188, 156)); // Match old success rate color

avgResolutionTimeLabel = createValueLabel("N/A", new Color(52, 73, 94)); // Match old time color

platformFeeLabel = createValueLabel("₹0.00", new Color(149, 165, 166)); // Match old fee color

// --- RESTORED OLD STAT CARD CREATION ---

statsPanel.add(createStatCard("Total Milestones", "0", greenAccent, "🎯"));

statsPanel.add(createStatCard("Funds in Escrow", "$0.00", orangeAccent, "🔒"));

statsPanel.add(createStatCard("Completed Payments", "0", blueAccent, "✅"));

statsPanel.add(createStatCard("Active Disputes", "0", redAccent, "⚖️"));

statsPanel.add(createStatCard("Total Invoices", "0", purpleAccent, "📄"));

statsPanel.add(createStatCard("Success Rate", "100%", new Color(26, 188, 156), "📈"));

statsPanel.add(createStatCard("Avg. Resolution Time", "2.5 days", new Color(52, 73, 94), "⏱️"));

statsPanel.add(createStatCard("Platform Fee", "$0.00", new Color(149, 165, 166), "💳"));

panel.add(statsPanel, BorderLayout.CENTER);

// Quick actions

JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));

actionsPanel.setOpaque(false);

JButton createMilestoneBtn = createStyledButton("+ Create Milestone", greenAccent);

createMilestoneBtn.addActionListener(e -> {

cardLayout.show(contentPanel, "MILESTONES");

});

JButton viewEscrowBtn = createStyledButton("View Escrow", orangeAccent);

viewEscrowBtn.addActionListener(e -> {

cardLayout.show(contentPanel, "ESCROW");

});

actionsPanel.add(createMilestoneBtn);

actionsPanel.add(viewEscrowBtn);

panel.add(actionsPanel, BorderLayout.SOUTH);

return panel;

}

// Panel 2: Milestone Payments (RESTORED OLD UI LAYOUT)

private JPanel createMilestonesPanel() {

JPanel panel = new JPanel(new BorderLayout(15, 15));

panel.setOpaque(false);

panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 40, 40));

panel.add(createStyledHeader("Milestone Payment Management", greenAccent), BorderLayout.NORTH);

JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

splitPane.setOpaque(false);

splitPane.setBorder(null);

splitPane.setDividerLocation(800);

// Left: Milestones table

JPanel tablePanel = new JPanel(new BorderLayout());

tablePanel.setOpaque(false);

// --- REVERTED TABLE COLUMNS (Old code) ---

// Removed "Method" and "Paid Date", added "Due Date"

String[] columns = {"ID", "Project", "Description", "Amount", "Status", "Created", "Due Date"};

milestonesTableModel = new DefaultTableModel(columns, 0) {

@Override public boolean isCellEditable(int row, int column) { return false; }

};

milestonesTable = new JTable(milestonesTableModel);

styleTable(milestonesTable, greenAccent);

JScrollPane scrollPane = new JScrollPane(milestonesTable);

scrollPane.getViewport().setBackground(bgColor);

tablePanel.add(scrollPane, BorderLayout.CENTER);

// Table buttons (Restored to bottom of split pane)

JPanel tableButtons = new JPanel(new FlowLayout()); // Use simple FlowLayout for old look

tableButtons.setOpaque(false);

JButton releaseBtn = createStyledButton("💰 Release Payment", greenAccent); // Use old button text

releaseBtn.addActionListener(e -> releaseMilestonePayment());

JButton disputeBtn = createStyledButton("⚖️ Open Dispute", redAccent); // Use old button text

disputeBtn.addActionListener(e -> openDispute());

tableButtons.add(releaseBtn);

tableButtons.add(disputeBtn);

tablePanel.add(tableButtons, BorderLayout.SOUTH);

// Right: Create milestone form (Reverted to old layout for field sizing)

JPanel formPanel = createMilestoneForm();

splitPane.setLeftComponent(tablePanel);

splitPane.setRightComponent(formPanel);

panel.add(splitPane, BorderLayout.CENTER);

return panel;

}

// --- RESTORED OLD MILESTONE FORM LAYOUT (BoxLayout) ---

private JPanel createMilestoneForm() {

JPanel formPanel = new JPanel();

formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS)); // Use BoxLayout

formPanel.setOpaque(false);

formPanel.setBorder(BorderFactory.createTitledBorder(

BorderFactory.createLineBorder(greenAccent, 2),

"Create New Milestone",

0, 0, new Font("Segoe UI", Font.BOLD, 16), textColor));

// Form components

projectIdField = createStyledTextField("Project ID (e.g., PRJ001)");

milestoneDescField = createStyledTextField("Milestone Description");

milestoneAmountField = createStyledTextField("Amount ($)");

styleComboBox(paymentMethodCombo); // Ensure existing combo is styled

milestoneNotesArea = new JTextArea(4, 25);

milestoneNotesArea.setBackground(inputBgColor);

milestoneNotesArea.setForeground(textColor);

milestoneNotesArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));

milestoneNotesArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

JScrollPane notesScroll = new JScrollPane(milestoneNotesArea);

notesScroll.setBorder(BorderFactory.createTitledBorder("Additional Notes"));

// Add components using createFormRow (Old Helper)

formPanel.add(createFormRow("Project ID:", projectIdField));

formPanel.add(Box.createRigidArea(new Dimension(0, 10)));

formPanel.add(createFormRow("Description:", milestoneDescField));

formPanel.add(Box.createRigidArea(new Dimension(0, 10)));

formPanel.add(createFormRow("Amount ($):", milestoneAmountField));

formPanel.add(Box.createRigidArea(new Dimension(0, 10)));

formPanel.add(createFormRow("Payment Method:", paymentMethodCombo));

formPanel.add(Box.createRigidArea(new Dimension(0, 15)));

formPanel.add(notesScroll);

formPanel.add(Box.createRigidArea(new Dimension(0, 15)));

// Buttons

JPanel buttonPanel = new JPanel(new FlowLayout());

buttonPanel.setOpaque(false);

JButton createBtn = createStyledButton("Create Milestone", greenAccent);

createBtn.addActionListener(e -> createMilestone());

JButton clearBtn = createStyledButton("Clear Form", new Color(149, 165, 166));

clearBtn.addActionListener(e -> clearMilestoneForm());

buttonPanel.add(createBtn);

buttonPanel.add(clearBtn);

formPanel.add(buttonPanel);

return formPanel;

}

// Panel 3: Escrow Accounts

private JPanel createEscrowPanel() {

JPanel panel = new JPanel(new BorderLayout(15, 15));

panel.setOpaque(false);

panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 40, 40));

panel.add(createStyledHeader("Escrow Account Management", orangeAccent), BorderLayout.NORTH);

// Escrow table

String[] columns = {"Escrow ID", "Project", "Client", "Freelancer", "Amount", "Status", "Created Date"};

escrowTableModel = new DefaultTableModel(columns, 0) {

@Override public boolean isCellEditable(int row, int column) { return false; }

};

escrowTable = new JTable(escrowTableModel);

styleTable(escrowTable, orangeAccent);

JScrollPane scrollPane = new JScrollPane(escrowTable);

scrollPane.getViewport().setBackground(bgColor);

panel.add(scrollPane, BorderLayout.CENTER);

// Action buttons

JPanel buttonPanel = new JPanel(new FlowLayout());

buttonPanel.setOpaque(false);

JButton releaseBtn = createStyledButton("🔓 Release Funds", greenAccent);

releaseBtn.addActionListener(e -> releaseEscrowFunds());

JButton holdBtn = createStyledButton("⏸️ Hold Funds", redAccent);

holdBtn.addActionListener(e -> holdEscrowFunds());

JButton refreshBtn = createStyledButton("🔄 Refresh", blueAccent);

refreshBtn.addActionListener(e -> refreshTables());

buttonPanel.add(releaseBtn);

buttonPanel.add(holdBtn);

buttonPanel.add(refreshBtn);

panel.add(buttonPanel, BorderLayout.SOUTH);

return panel;

}

// Panel 4: Invoices

private JPanel createInvoicesPanel() {

JPanel panel = new JPanel(new BorderLayout(15, 15));

panel.setOpaque(false);

panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 40, 40));

panel.add(createStyledHeader("Invoice Management", purpleAccent), BorderLayout.NORTH);

// Invoices table

String[] columns = {"Invoice #", "Project", "Client", "Amount", "Status", "Created", "Due Date"};

invoicesTableModel = new DefaultTableModel(columns, 0) {

@Override public boolean isCellEditable(int row, int column) { return false; }

};

invoicesTable = new JTable(invoicesTableModel);

styleTable(invoicesTable, purpleAccent);

JScrollPane scrollPane = new JScrollPane(invoicesTable);

scrollPane.getViewport().setBackground(bgColor);

panel.add(scrollPane, BorderLayout.CENTER);

// Action buttons

JPanel buttonPanel = new JPanel(new FlowLayout());

buttonPanel.setOpaque(false);

JButton generateBtn = createStyledButton("📄 Generate Invoice", purpleAccent);

generateBtn.addActionListener(e -> generateInvoice());

JButton sendBtn = createStyledButton("📧 Send Invoice", blueAccent);

sendBtn.addActionListener(e -> sendInvoice());

JButton printBtn = createStyledButton("🖨️ Print Invoice", new Color(149, 165, 166));

printBtn.addActionListener(e -> printInvoice());

buttonPanel.add(generateBtn);

buttonPanel.add(sendBtn);

buttonPanel.add(printBtn);

panel.add(buttonPanel, BorderLayout.SOUTH);

return panel;

}

// Panel 5: Disputes

private JPanel createDisputesPanel() {

JPanel panel = new JPanel(new BorderLayout(15, 15));

panel.setOpaque(false);

panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 40, 40));

panel.add(createStyledHeader("Dispute Resolution", redAccent), BorderLayout.NORTH);

// Disputes table (Kept "Milestone" column from new code as it's useful)

String[] columns = {"Dispute ID", "Project", "Milestone", "Raised By", "Reason", "Status", "Created", "Resolution"};

disputesTableModel = new DefaultTableModel(columns, 0) {

@Override public boolean isCellEditable(int row, int column) { return false; }

};

disputesTable = new JTable(disputesTableModel);

styleTable(disputesTable, redAccent);

JScrollPane scrollPane = new JScrollPane(disputesTable);

scrollPane.getViewport().setBackground(bgColor);

panel.add(scrollPane, BorderLayout.CENTER);

// Action buttons

JPanel buttonPanel = new JPanel(new FlowLayout());

buttonPanel.setOpaque(false);

JButton resolveBtn = createStyledButton("✅ Resolve Dispute", greenAccent);

resolveBtn.addActionListener(e -> resolveDispute());

JButton escalateBtn = createStyledButton("⬆️ Escalate", orangeAccent);

escalateBtn.addActionListener(e -> escalateDispute());

JButton mediateBtn = createStyledButton("🤝 Mediate", blueAccent);

mediateBtn.addActionListener(e -> mediateDispute());

buttonPanel.add(resolveBtn);

buttonPanel.add(escalateBtn);

buttonPanel.add(mediateBtn);

panel.add(buttonPanel, BorderLayout.SOUTH);

return panel;

}

// ====================================================================

// DATA LOADING & REFRESH METHODS (LOGIC PRESERVED)

// ====================================================================

private void loadData(String projectId) {

loadMilestonesFor(projectId);

loadEscrowFor(projectId);

loadInvoicesFor(projectId);

loadDisputesFor(projectId);

updateDashboardFor(projectId);

}

private void loadMilestonesFor(String projectId) {

milestonesTableModel.setRowCount(0);

milestones.clear();

List dbMilestones = dbManager.getMilestonesByProject(projectId);

for (Object[] row : dbMilestones) {

String milestoneId = (String) row[0];

String prjId = (String) row[1];

String desc = (String) row[2];

double amount = (Double) row[3];

String status = (String) row[4];

String method = (String) row[5];

String notes = (String) row[6];

String createdDateStr = (String) row[7];

String dueDateStr = (String) row[8];

String completedDateStr = (String) row[9];

PaymentMilestone m = new PaymentMilestone(

milestoneId, prjId, desc, amount, status,

dateFromString(createdDateStr),

dateFromString(completedDateStr),

notes, method

);

milestones.add(m);

milestonesTableModel.addRow(new Object[]{

milestoneId,

prjId,

desc,

currencyFormat.format(amount),

status,

createdDateStr != null ? createdDateStr.split(" ")[0] : "N/A",

dueDateStr != null ? dueDateStr.split(" ")[0] : "Pending"

});

}

}

private void loadEscrowFor(String projectId) {

escrowTableModel.setRowCount(0);

escrowAccounts.clear();

List dbEscrow = dbManager.getEscrowByProject(projectId);

for (Object[] row : dbEscrow) {

String escrowId = (String) row[0];

String prjId = (String) row[1];

String milId = (String) row[2];

Integer clientId = (Integer) row[3];

Integer freelancerId = (Integer) row[4];

double amount = (Double) row[5];

String status = (String) row[6];

String createdDateStr = (String) row[7];

EscrowAccount e = new EscrowAccount(

escrowId, prjId, milId, String.valueOf(clientId), String.valueOf(freelancerId), amount, status, dateFromString(createdDateStr)

);

escrowAccounts.add(e);

escrowTableModel.addRow(new Object[]{

escrowId,

prjId,

"Client_" + clientId,

"Freelancer_" + freelancerId,

currencyFormat.format(amount),

status,

createdDateStr != null ? createdDateStr.split(" ")[0] : "N/A"

});

}

}

private void loadInvoicesFor(String projectId) {

invoicesTableModel.setRowCount(0);

invoices.clear();

List dbInvoices = dbManager.getInvoicesByProject(projectId);

for (Object[] row : dbInvoices) {

String invId = (String) row[0];

String prjId = (String) row[1];

Integer clientId = (Integer) row[2];

Integer freelancerId = (Integer) row[3];

double amount = (Double) row[4];

String status = (String) row[5];

String desc = (String) row[6];

String createdDateStr = (String) row[7];

String dueDateStr = (String) row[8];

Invoice i = new Invoice(

invId, prjId, String.valueOf(clientId), amount, status, dateFromString(createdDateStr), dateFromString(dueDateStr), desc

);

invoices.add(i);

invoicesTableModel.addRow(new Object[]{

invId,

prjId,

"Client_" + clientId,

currencyFormat.format(amount),

status,

createdDateStr != null ? createdDateStr.split(" ")[0] : "N/A",

dueDateStr != null ? dueDateStr.split(" ")[0] : "N/A"

});

}

}

private void loadDisputesFor(String projectId) {

disputesTableModel.setRowCount(0);

disputes.clear();

List dbDisputes = dbManager.getDisputesByProject(projectId);

for (Object[] row : dbDisputes) {

String disId = (String) row[0];

String prjId = (String) row[1];

String milId = (String) row[2];

String raisedBy = (String) row[3];

String reason = (String) row[4];

String status = (String) row[5];

String resolution = (String) row[6];

String createdDateStr = (String) row[7];

DisputeCase d = new DisputeCase(

disId, prjId, milId, raisedBy, reason, status, dateFromString(createdDateStr), resolution

);

disputes.add(d);

disputesTableModel.addRow(new Object[]{

disId,

prjId,

milId,

raisedBy,

reason.length() > 30 ? reason.substring(0, 30) + "..." : reason,

status,

createdDateStr != null ? createdDateStr.split(" ")[0] : "N/A",

resolution != null && !resolution.trim().isEmpty() ? "Resolved" : "Pending"

});

}

}

private void updateDashboardFor(String projectId) {

long totalMilestones = dbManager.getMilestoneCountByProject(projectId);

double fundsInEscrow = dbManager.getEscrowTotalByProject(projectId);

long activeDisputes = dbManager.getOpenDisputeCountByProject(projectId);

long completedMilestones = milestones.stream().filter(m -> m.getStatus().equals("Released")).count();

long totalInvoices = invoices.size();

milestonesCountLabel.setText(String.valueOf(totalMilestones));

escrowAmountLabel.setText(currencyFormat.format(fundsInEscrow));

completedPaymentsLabel.setText(String.valueOf(completedMilestones));

openDisputesLabel.setText(String.valueOf(activeDisputes));

totalInvoicesLabel.setText(String.valueOf(totalInvoices));

successRateLabel.setText(totalMilestones > 0 ? String.format("%.0f%%", (double)completedMilestones / totalMilestones * 100) : "100%");

avgResolutionTimeLabel.setText("N/A");

platformFeeLabel.setText(currencyFormat.format(0.0));

}

private void refreshTables() {

String prj = projectIdField != null ?

projectIdField.getText() : "";

loadData(prj);

}

private Date dateFromString(String dateStr) {

if (dateStr == null || dateStr.isEmpty()) return null;

try {

return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateStr);

} catch (Exception e) {

try {

return dateFormat.parse(dateStr);

} catch (Exception ex) {

return null;

}

}

}

// ====================================================================

// ACTION METHODS - NOW WITH NOTIFICATIONS!

// ====================================================================

private void createMilestone() {

if (validateMilestoneForm()) {

String milestoneId = "MIL" + String.format("%03d", nextMilestoneId++);

double amount = Double.parseDouble(milestoneAmountField.getText());

String projectId = projectIdField.getText();

String paymentMethod = (String) paymentMethodCombo.getSelectedItem();

String notes = milestoneNotesArea.getText();

int rows = dbManager.insertMilestone(milestoneId, projectId, milestoneDescField.getText(),

amount, paymentMethod, notes);

if (rows > 0) {

createEscrowAccount(milestoneId, projectId, 1, 2, amount);

// ✅ TRIGGER NOTIFICATION - MILESTONE CREATED

notificationManager.notifySystem(

"🎯 New Milestone Created",

"Milestone '" + milestoneDescField.getText() + "' created for project " + projectId + " with amount ₹" + amount

);

refreshTables();

clearMilestoneForm();

JOptionPane.showMessageDialog(frame,

"Milestone created successfully!\nMilestone ID: " + milestoneId +

"\nFunds held in escrow pending completion.",

"Milestone Created", JOptionPane.INFORMATION_MESSAGE);

} else {

JOptionPane.showMessageDialog(frame, "Failed to create milestone in database.", "Database Error", JOptionPane.ERROR_MESSAGE);

}

}

}

private void createEscrowAccount(String milestoneId, String projectId, Integer clientId, Integer freelancerId, double amount) {

String escrowId = "ESC" + String.format("%03d", nextEscrowId++);

dbManager.insertEscrow(escrowId, projectId, milestoneId, clientId, freelancerId, amount);

}

private void releaseMilestonePayment() {

int selectedRow = milestonesTable.getSelectedRow();

if (selectedRow != -1) {

String milestoneId = (String) milestonesTableModel.getValueAt(selectedRow, 0);

String projectId = (String) milestonesTableModel.getValueAt(selectedRow, 1);

int milestoneUpdated = dbManager.updateMilestoneStatus(milestoneId, "Released");

dbManager.updateEscrowStatusByMilestone(milestoneId, "Released");

generateInvoiceForMilestone(milestoneId);

// ✅ TRIGGER NOTIFICATION - PAYMENT RELEASED

notificationManager.notifyPaymentReleased(

Double.parseDouble(milestonesTableModel.getValueAt(selectedRow, 3).toString().replace("₹", "").replace(",", "")),

projectId

);

refreshTables();

JOptionPane.showMessageDialog(frame, "Payment released successfully!\nInvoice generated and funds transferred to freelancer.", "Payment Released", JOptionPane.INFORMATION_MESSAGE);

} else {

JOptionPane.showMessageDialog(frame, "Please select a milestone to release.", "No Selection", JOptionPane.WARNING_MESSAGE);

}

}

private void generateInvoiceForMilestone(String milestoneId) {

PaymentMilestone milestone = milestones.stream()

.filter(m -> m.getMilestoneId().equals(milestoneId))

.findFirst().orElse(null);

if (milestone != null) {

String invoiceId = "INV" + String.format("%03d", nextInvoiceId++);

String dueDateIso = dateFormat.format(new Date(new Date().getTime() + (7 * 24 * 60 * 60 * 1000)));

dbManager.insertInvoice(invoiceId, milestone.getProjectId(), 1, 2,

milestone.getAmount(), milestone.getDescription(), dueDateIso);

}

}

private void openDispute() {

int selectedRow = milestonesTable.getSelectedRow();

if (selectedRow != -1) {

String milestoneId = (String) milestonesTableModel.getValueAt(selectedRow, 0);

String projectId = (String) milestonesTableModel.getValueAt(selectedRow, 1);

String reason = JOptionPane.showInputDialog(frame, "Enter dispute reason:", "Open Dispute", JOptionPane.QUESTION_MESSAGE);

if (reason != null && !reason.trim().isEmpty()) {

String disputeId = "DSP" + String.format("%03d", nextDisputeId++);

dbManager.insertDispute(disputeId, projectId, milestoneId, "Client", reason);

dbManager.updateMilestoneStatus(milestoneId, "Disputed");

dbManager.updateEscrowStatusByMilestone(milestoneId, "On Hold");

// ✅ TRIGGER NOTIFICATION - DISPUTE OPENED

notificationManager.notifySystem(

"⚖️ Dispute Opened",

"Dispute has been opened for milestone " + milestoneId + ". Reason: " + reason

);

refreshTables();

JOptionPane.showMessageDialog(frame,

"Dispute opened successfully!\nDispute ID: " + disputeId + "\nPayment has been held pending resolution.",

"Dispute Opened", JOptionPane.INFORMATION_MESSAGE);

}

} else {

JOptionPane.showMessageDialog(frame, "Please select a milestone to dispute.", "No Selection", JOptionPane.WARNING_MESSAGE);

}

}

private void releaseEscrowFunds() {

int selectedRow = escrowTable.getSelectedRow();

if (selectedRow != -1) {

String escrowId = (String) escrowTableModel.getValueAt(selectedRow, 0);

dbManager.updateEscrowStatus(escrowId, "Released");

// ✅ TRIGGER NOTIFICATION - ESCROW RELEASED

notificationManager.notifyPaymentReleased(

Double.parseDouble(escrowTableModel.getValueAt(selectedRow, 4).toString().replace("₹", "").replace(",", "")),

(String) escrowTableModel.getValueAt(selectedRow, 1)

);

refreshTables();

JOptionPane.showMessageDialog(frame, "Escrow funds released successfully!", "Funds Released", JOptionPane.INFORMATION_MESSAGE);

} else {

JOptionPane.showMessageDialog(frame, "Please select an escrow account.", "No Selection", JOptionPane.WARNING_MESSAGE);

}

}

private void holdEscrowFunds() {

int selectedRow = escrowTable.getSelectedRow();

if (selectedRow != -1) {

String escrowId = (String) escrowTableModel.getValueAt(selectedRow, 0);

dbManager.updateEscrowStatus(escrowId, "On Hold");

refreshTables();

JOptionPane.showMessageDialog(frame, "Escrow funds placed on hold.", "Funds Held", JOptionPane.WARNING_MESSAGE);

} else {

JOptionPane.showMessageDialog(frame, "Please select an escrow account.", "No Selection", JOptionPane.WARNING_MESSAGE);

}

}

private void generateInvoice() {

String projectId = JOptionPane.showInputDialog(frame, "Enter Project ID:", "Generate Invoice", JOptionPane.QUESTION_MESSAGE);

if (projectId != null && !projectId.trim().isEmpty()) {

String invoiceId = "INV" + String.format("%03d", nextInvoiceId++);

String dueDateIso = dateFormat.format(new Date(new Date().getTime() + (7 * 24 * 60 * 60 * 1000)));

dbManager.insertInvoice(invoiceId, projectId, null, null, 50.0, "Platform Fee for Project " + projectId, dueDateIso);

refreshTables();

JOptionPane.showMessageDialog(frame, "Platform Fee invoice generated successfully!\nInvoice ID: " + invoiceId, "Invoice Generated", JOptionPane.INFORMATION_MESSAGE);

}

}

private void sendInvoice() {

int selectedRow = invoicesTable.getSelectedRow();

if (selectedRow != -1) {

String invoiceId = (String) invoicesTableModel.getValueAt(selectedRow, 0);

dbManager.updateInvoiceStatus(invoiceId, "Sent");

refreshTables();

JOptionPane.showMessageDialog(frame, "Invoice " + invoiceId + " sent to client via email.", "Invoice Sent", JOptionPane.INFORMATION_MESSAGE);

} else {

JOptionPane.showMessageDialog(frame, "Please select an invoice to send.", "No Selection", JOptionPane.WARNING_MESSAGE);

}

}

private void printInvoice() {

int selectedRow = invoicesTable.getSelectedRow();

if (selectedRow != -1) {

String invoiceId = (String) invoicesTableModel.getValueAt(selectedRow, 0);

JOptionPane.showMessageDialog(frame, "Printing invoice " + invoiceId + "...", "Print Invoice", JOptionPane.INFORMATION_MESSAGE);

} else {

JOptionPane.showMessageDialog(frame, "Please select an invoice to print.", "No Selection", JOptionPane.WARNING_MESSAGE);

}

}

private void resolveDispute() {

int selectedRow = disputesTable.getSelectedRow();

if (selectedRow != -1) {

String disputeId = (String) disputesTableModel.getValueAt(selectedRow, 0);

String resolution = JOptionPane.showInputDialog(frame, "Enter resolution details:", "Resolve Dispute", JOptionPane.QUESTION_MESSAGE);

if (resolution != null && !resolution.trim().isEmpty()) {

dbManager.updateDispute(disputeId, "Resolved", resolution);

// ✅ TRIGGER NOTIFICATION - DISPUTE RESOLVED

notificationManager.notifySystem(

"✅ Dispute Resolved",

"Dispute " + disputeId + " has been resolved. Resolution: " + resolution

);

refreshTables();

JOptionPane.showMessageDialog(frame, "Dispute resolved successfully!", "Dispute Resolved", JOptionPane.INFORMATION_MESSAGE);

}

} else {

JOptionPane.showMessageDialog(frame, "Please select a dispute to resolve.", "No Selection", JOptionPane.WARNING_MESSAGE);

}

}

private void escalateDispute() {

int selectedRow = disputesTable.getSelectedRow();

if (selectedRow != -1) {

String disputeId = (String) disputesTableModel.getValueAt(selectedRow, 0);

dbManager.updateDispute(disputeId, "Escalated", null);

// ✅ TRIGGER NOTIFICATION - DISPUTE ESCALATED

notificationManager.notifySystem(

"⬆️ Dispute Escalated",

"Dispute " + disputeId + " has been escalated to senior mediation team."

);

refreshTables();

JOptionPane.showMessageDialog(frame, "Dispute escalated to senior mediation team.", "Dispute Escalated", JOptionPane.WARNING_MESSAGE);

} else {

JOptionPane.showMessageDialog(frame, "Please select a dispute to escalate.", "No Selection", JOptionPane.WARNING_MESSAGE);

}

}

private void mediateDispute() {

int selectedRow = disputesTable.getSelectedRow();

if (selectedRow != -1) {

String disputeId = (String) disputesTableModel.getValueAt(selectedRow, 0);

dbManager.updateDispute(disputeId, "Under Review", null);

refreshTables();

JOptionPane.showMessageDialog(frame, "Mediation process started. Both parties will be contacted.", "Mediation Started", JOptionPane.INFORMATION_MESSAGE);

} else {

JOptionPane.showMessageDialog(frame, "Please select a dispute to mediate.", "No Selection", JOptionPane.WARNING_MESSAGE);

}

}

// ====================================================================

// VALIDATION & UTILITY METHODS

// ====================================================================

private boolean validateMilestoneForm() {

if (projectIdField.getText().trim().isEmpty() ||

milestoneDescField.getText().trim().isEmpty()) {

JOptionPane.showMessageDialog(frame, "Project ID and Description cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);

return false;

}

try {

double amount = Double.parseDouble(milestoneAmountField.getText());

if (amount <= 0) {

JOptionPane.showMessageDialog(frame, "Amount must be greater than zero.", "Validation Error", JOptionPane.ERROR_MESSAGE);

return false;

}

} catch (NumberFormatException e) {

JOptionPane.showMessageDialog(frame, "Invalid amount format.", "Validation Error", JOptionPane.ERROR_MESSAGE);

return false;

}

return true;

}

private void clearMilestoneForm() {

projectIdField.setText("");

milestoneDescField.setText("");

milestoneAmountField.setText("");

milestoneNotesArea.setText("");

paymentMethodCombo.setSelectedIndex(0);

}

// --- UI Helper Methods ---

private JLabel createStyledHeader(String text, Color color) {

JLabel header = new JLabel(text, JLabel.CENTER);

header.setFont(new Font("Segoe UI", Font.BOLD, 28));

header.setForeground(color);

header.setBorder(BorderFactory.createEmptyBorder(0, 0, 25, 0));

return header;

}

private JPanel createStatCard(String title, String value, Color color, String icon) {

JPanel card = new JPanel(new BorderLayout(10, 5));

card.setBackground(new Color(52, 73, 94));

card.setBorder(BorderFactory.createCompoundBorder(

BorderFactory.createLineBorder(color, 3),

BorderFactory.createEmptyBorder(20, 25, 20, 25)

));

JPanel topPanel = new JPanel(new BorderLayout());

topPanel.setOpaque(false);

JLabel iconLabel = new JLabel(icon);

iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));

iconLabel.setHorizontalAlignment(JLabel.RIGHT);

JLabel valueLabel = new JLabel(value);

valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));

valueLabel.setForeground(color);

topPanel.add(valueLabel, BorderLayout.WEST);

topPanel.add(iconLabel, BorderLayout.EAST);

JLabel titleLabel = new JLabel(title);

titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));

titleLabel.setForeground(textColor);

card.add(topPanel, BorderLayout.CENTER);

card.add(titleLabel, BorderLayout.SOUTH);

return card;

}

private JLabel createValueLabel(String text, Color color) {

JLabel label = new JLabel(text);

label.setFont(new Font("Segoe UI", Font.BOLD, 36));

label.setForeground(color);

return label;

}

private void styleTable(JTable table, Color accent) {

table.setBackground(bgColor);

table.setForeground(textColor);

table.setSelectionBackground(accent.darker());

table.setSelectionForeground(Color.WHITE);

table.setGridColor(new Color(80, 80, 80));

table.setRowHeight(35);

table.setFont(new Font("Segoe UI", Font.PLAIN, 13));

JTableHeader header = table.getTableHeader();

header.setBackground(accent);

header.setForeground(Color.WHITE);

header.setFont(new Font("Segoe UI", Font.BOLD, 14));

header.setPreferredSize(new Dimension(0, 40));

header.setReorderingAllowed(false);

table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {

@Override

public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

if (!isSelected) {

comp.setBackground(row % 2 == 0 ? bgColor : new Color(40, 55, 71));

comp.setForeground(textColor);

}

return comp;

}

});

}

private JTextField createStyledTextField(String placeholder) {

JTextField field = new JTextField(20);

field.setBackground(inputBgColor);

field.setForeground(textColor);

field.setFont(new Font("Segoe UI", Font.PLAIN, 14));

field.setBorder(BorderFactory.createCompoundBorder(

BorderFactory.createLineBorder(new Color(80, 80, 80)),

BorderFactory.createEmptyBorder(8, 8, 8, 8)

));

field.setText(placeholder);

field.setForeground(Color.GRAY);

field.addFocusListener(new java.awt.event.FocusAdapter() {

public void focusGained(java.awt.event.FocusEvent evt) {

if (field.getText().equals(placeholder)) {

field.setText("");

field.setForeground(textColor);

}

}

public void focusLost(java.awt.event.FocusEvent evt) {

if (field.getText().isEmpty()) {

field.setText(placeholder);

field.setForeground(Color.GRAY);

}

}

});

return field;

}

private JComboBox createStyledComboBox(String[] items) {

JComboBox cb = new JComboBox<>(items);

styleComboBox(cb);

return cb;

}

private void styleComboBox(JComboBox cb) {

cb.setBackground(inputBgColor);

cb.setForeground(textColor);

cb.setFont(new Font("Segoe UI", Font.PLAIN, 14));

}

private JButton createStyledButton(String text, Color color) {

JButton button = new JButton(text);

button.setFont(new Font("Segoe UI", Font.BOLD, 14));

button.setBackground(color);

button.setForeground(Color.WHITE);

button.setFocusPainted(false);

button.setCursor(new Cursor(Cursor.HAND_CURSOR));

button.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));

return button;

}

private JPanel createFormRow(String labelText, JComponent component) {

JPanel row = new JPanel(new BorderLayout(10, 0));

row.setOpaque(false);

JLabel label = new JLabel(labelText);

label.setFont(new Font("Segoe UI", Font.BOLD, 14));

label.setForeground(textColor);

label.setPreferredSize(new Dimension(120, 30));

row.add(label, BorderLayout.WEST);

row.add(component, BorderLayout.CENTER);

return row;

}

// --- NavButton (JPanel) ---

private class NavButton extends JPanel {

private boolean isHovered = false;

private Color accentColor;

public NavButton(String text, String cardName, Color accent) {

this.accentColor = accent;

setLayout(new BorderLayout());

setOpaque(false);

setCursor(new Cursor(Cursor.HAND_CURSOR));

setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));

setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

JLabel label = new JLabel(text);

label.setForeground(textColor);

label.setFont(new Font("Segoe UI", Font.BOLD, 16));

add(label, BorderLayout.WEST);

addMouseListener(new MouseAdapter() {

@Override

public void mouseClicked(MouseEvent e) {

cardLayout.show(contentPanel, cardName);

}

@Override

public void mouseEntered(MouseEvent e) {

isHovered = true;

repaint();

}

@Override

public void mouseExited(MouseEvent e) {

isHovered = false;

repaint();

}

});

}

@Override

protected void paintComponent(Graphics g) {

super.paintComponent(g);

if (isHovered) {

Graphics2D g2d = (Graphics2D) g;

g2d.setColor(accentColor.darker());

g2d.fillRect(0, 0, 5, getHeight());

g2d.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 30));

g2d.fillRect(5, 0, getWidth() - 5, getHeight());

}

}

}

// ====================================================================

// INTERNAL DATA CLASSES (PRESERVED)

// ====================================================================

private static class PaymentMilestone {

private String milestoneId, projectId, description, status, paymentMethod, notes;

private double amount;

private Date createdDate, paidDate;

public PaymentMilestone(String milestoneId, String projectId, String description, double amount,

String status, Date createdDate, Date paidDate, String notes, String paymentMethod) {

this.milestoneId = milestoneId;

this.projectId = projectId;

this.description = description;

this.amount = amount;

this.status = status;

this.createdDate = createdDate;

this.paidDate = paidDate;

this.notes = notes;

this.paymentMethod = paymentMethod;

}

public String getMilestoneId() { return milestoneId; }

public String getProjectId() { return projectId; }

public String getDescription() { return description; }

public double getAmount() { return amount; }

public String getStatus() { return status; }

public Date getCreatedDate() { return createdDate; }

public Date getPaidDate() { return paidDate; }

public String getPaymentMethod() { return paymentMethod; }

public void setStatus(String status) { this.status = status; }

public void setPaidDate(Date paidDate) { this.paidDate = paidDate; }

}

private static class EscrowAccount {

private String accountId, projectId, milestoneId, clientId, freelancerId, status;

private double amountHeld;

private Date createdDate;

public EscrowAccount(String accountId, String projectId, String milestoneId, String clientId, String freelancerId, double amountHeld, String status, Date createdDate) {

this.accountId = accountId;

this.projectId = projectId;

this.milestoneId = milestoneId;

this.clientId = clientId;

this.freelancerId = freelancerId;

this.amountHeld = amountHeld;

this.status = status;

this.createdDate = createdDate;

}

public String getAccountId() { return accountId; }

public String getProjectId() { return projectId; }

public String getMilestoneId() { return milestoneId; }

public double getAmountHeld() { return amountHeld; }

public String getStatus() { return status; }

public Date getCreatedDate() { return createdDate; }

public void setStatus(String status) { this.status = status; }

}

private static class Invoice {

private String invoiceId, projectId, clientId, status, description;

private double amount;

private Date dueDate, createdDate;

public Invoice(String invoiceId, String projectId, String clientId, double amount, String status, Date createdDate, Date dueDate, String description) {

this.invoiceId = invoiceId;

this.projectId = projectId;

this.clientId = clientId;

this.amount = amount;

this.status = status;

this.createdDate = createdDate;

this.dueDate = dueDate;

this.description = description;

}

public String getInvoiceId() { return invoiceId; }

public String getProjectId() { return projectId; }

public double getAmount() { return amount; }

public String getStatus() { return status; }

public Date getCreatedDate() { return createdDate; }

public Date getDueDate() { return dueDate; }

public String getDescription() { return description; }

public void setStatus(String status) { this.status = status; }

}

private static class DisputeCase {

private String disputeId, projectId, milestoneId, raisedBy, reason, status, resolution;

private Date createdDate;

public DisputeCase(String disputeId, String projectId, String milestoneId, String raisedBy, String reason, String status, Date createdDate, String resolution) {

this.disputeId = disputeId;

this.projectId = projectId;

this.milestoneId = milestoneId;

this.raisedBy = raisedBy;

this.reason = reason;

this.status = status;

this.createdDate = createdDate;

this.resolution = resolution;

}

public String getDisputeId() { return disputeId; }

public String getProjectId() { return projectId; }

public String getMilestoneId() { return milestoneId; }

public String getRaisedBy() { return raisedBy; }

public String getReason() { return reason; }

public String getStatus() { return status; }

public Date getCreatedDate() { return createdDate; }

public String getResolution() { return resolution; }

public void setStatus(String status) { this.status = status; }

public void setResolution(String resolution) { this.resolution = resolution; }

}

}