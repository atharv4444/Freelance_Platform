import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.DecimalFormat;

public class PaymentManager {
    public enum UserRole { CLIENT, FREELANCER, ADMIN }
    private UserRole currentUserRole;
    
    private JFrame frame;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    
    // Data structures
    private ArrayList<PaymentMilestone> milestones;
    private ArrayList<EscrowAccount> escrowAccounts;
    private ArrayList<Invoice> invoices;
    private ArrayList<DisputeCase> disputes;
    
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
    private JComboBox<String> milestoneStatusCombo, paymentMethodCombo;
    
    // ID generators
    private int nextMilestoneId = 1;
    private int nextEscrowId = 1;
    private int nextInvoiceId = 1;
    private int nextDisputeId = 1;
    
    // Colors
    private final Color bgColor = new Color(34, 47, 62);
    private final Color sidebarColor = new Color(44, 62, 80);
    private final Color textColor = new Color(236, 240, 241);
    private final Color inputBgColor = new Color(99, 110, 114);
    private final Color blueAccent = new Color(52, 152, 219);
    private final Color greenAccent = new Color(46, 204, 113);
    private final Color orangeAccent = new Color(230, 126, 34);
    private final Color redAccent = new Color(231, 76, 60);
    private final Color purpleAccent = new Color(155, 89, 182);
    
    public PaymentManager() {
        milestones = new ArrayList<>();
        escrowAccounts = new ArrayList<>();
        invoices = new ArrayList<>();
        disputes = new ArrayList<>();
    }
    
    public void showWindow() {
        showWindow(UserRole.CLIENT); // Default to client view
    }
    
    public void showWindow(UserRole role) {
        this.currentUserRole = role;
        if (frame != null && frame.isVisible()) {
            frame.dispose();
        }
        initializeGUI();
        loadSampleData();
        frame.setVisible(true);
    }
    
    private void initializeGUI() {
        frame = new JFrame("Payment & Escrow Management (" + currentUserRole.toString() + ")");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(1400, 900);
        frame.setLocationRelativeTo(null);
        
        // Background panel
        JPanel backgroundPanel = new JPanel();
        backgroundPanel.setBackground(bgColor);
        backgroundPanel.setLayout(new BorderLayout());
        
        // Sidebar
        JPanel sidebar = createSidebar();
        
        // Content panel
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setOpaque(false);
        
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
        
        // Header
        JLabel headerLabel = new JLabel("Payments & Escrow", JLabel.CENTER);
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        headerLabel.setForeground(textColor);
        headerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerLabel.setBorder(BorderFactory.createEmptyBorder(25, 0, 25, 0));
        sidebar.add(headerLabel);
        
        // Navigation buttons
        sidebar.add(new NavButton("💰 Payment Dashboard", "DASHBOARD", blueAccent));
        sidebar.add(new NavButton("🎯 Milestone Payments", "MILESTONES", greenAccent));
        sidebar.add(new NavButton("🔒 Escrow Accounts", "ESCROW", orangeAccent));
        sidebar.add(new NavButton("📄 Invoices & Receipts", "INVOICES", purpleAccent));
        sidebar.add(new NavButton("⚖️ Dispute Resolution", "DISPUTES", redAccent));
        
        sidebar.add(Box.createVerticalGlue());
        return sidebar;
    }
    
    // Panel 1: Payment Dashboard
    private JPanel createPaymentDashboard() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 40, 40));
        
        panel.add(createStyledHeader("Payment & Escrow Dashboard", blueAccent), BorderLayout.NORTH);
        
        // Stats panel
        JPanel statsPanel = new JPanel(new GridLayout(2, 4, 25, 25));
        statsPanel.setOpaque(false);
        
        // Create stat cards
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
    
    // Panel 2: Milestone Payments
    private JPanel createMilestonesPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 40, 40));
        
        panel.add(createStyledHeader("Milestone Payment Management", greenAccent), BorderLayout.NORTH);
        
        // Split panel
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setOpaque(false);
        splitPane.setBorder(null);
        splitPane.setDividerLocation(800);
        
        // Left: Milestones table
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setOpaque(false);
        
        String[] columns = {"ID", "Project", "Description", "Amount", "Status", "Created", "Due Date"};
        milestonesTableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        
        milestonesTable = new JTable(milestonesTableModel);
        styleTable(milestonesTable, greenAccent);
        
        JScrollPane scrollPane = new JScrollPane(milestonesTable);
        scrollPane.getViewport().setBackground(bgColor);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        // Table buttons
        JPanel tableButtons = new JPanel(new FlowLayout());
        tableButtons.setOpaque(false);
        
        JButton releaseBtn = createStyledButton("💰 Release Payment", greenAccent);
        releaseBtn.addActionListener(e -> releaseMilestonePayment());
        
        JButton disputeBtn = createStyledButton("⚖️ Open Dispute", redAccent);
        disputeBtn.addActionListener(e -> openDispute());
        
        tableButtons.add(releaseBtn);
        tableButtons.add(disputeBtn);
        tablePanel.add(tableButtons, BorderLayout.SOUTH);
        
        // Right: Create milestone form
        JPanel formPanel = createMilestoneForm();
        
        splitPane.setLeftComponent(tablePanel);
        splitPane.setRightComponent(formPanel);
        panel.add(splitPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createMilestoneForm() {
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setOpaque(false);
        formPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(greenAccent, 2), 
            "Create New Milestone", 
            0, 0, new Font("Segoe UI", Font.BOLD, 16), textColor));
        
        // Form components
        projectIdField = createStyledTextField("Project ID (e.g., PRJ001)");
        milestoneDescField = createStyledTextField("Milestone Description");
        milestoneAmountField = createStyledTextField("Amount ($)");
        
        paymentMethodCombo = new JComboBox<>(new String[]{"Credit Card", "PayPal", "Bank Transfer", "Cryptocurrency"});
        styleComboBox(paymentMethodCombo);
        
        milestoneNotesArea = new JTextArea(4, 25);
        milestoneNotesArea.setBackground(inputBgColor);
        milestoneNotesArea.setForeground(textColor);
        milestoneNotesArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        milestoneNotesArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JScrollPane notesScroll = new JScrollPane(milestoneNotesArea);
        notesScroll.setBorder(BorderFactory.createTitledBorder("Additional Notes"));
        
        // Add components
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
        refreshBtn.addActionListener(e -> refreshEscrowTable());
        
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
        
        // Disputes table
        String[] columns = {"Dispute ID", "Project", "Raised By", "Reason", "Status", "Created", "Resolution"};
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
    
    // Action Methods
    private void createMilestone() {
        if (validateMilestoneForm()) {
            String milestoneId = "MIL" + String.format("%03d", nextMilestoneId++);
            double amount = Double.parseDouble(milestoneAmountField.getText());
            
            PaymentMilestone milestone = new PaymentMilestone(
                milestoneId,
                projectIdField.getText(),
                milestoneDescField.getText(),
                amount,
                "Pending",
                new Date(),
                null,
                milestoneNotesArea.getText(),
                (String) paymentMethodCombo.getSelectedItem()
            );
            
            milestones.add(milestone);
            
            // Create corresponding escrow account
            createEscrowAccount(milestoneId, projectIdField.getText(), amount);
            
            refreshMilestonesTable();
            clearMilestoneForm();
            updateDashboard();
            
            JOptionPane.showMessageDialog(frame,
                "Milestone created successfully!\nMilestone ID: " + milestoneId + 
                "\nFunds held in escrow pending completion.",
                "Milestone Created", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void createEscrowAccount(String milestoneId, String projectId, double amount) {
        String escrowId = "ESC" + String.format("%03d", nextEscrowId++);
        
        EscrowAccount escrow = new EscrowAccount(
            escrowId,
            projectId,
            milestoneId,
            "Client_" + projectId,
            "Freelancer_" + projectId,
            amount,
            "Holding",
            new Date()
        );
        
        escrowAccounts.add(escrow);
        refreshEscrowTable();
    }
    
    private void releaseMilestonePayment() {
        int selectedRow = milestonesTable.getSelectedRow();
        if (selectedRow != -1) {
            String milestoneId = (String) milestonesTableModel.getValueAt(selectedRow, 0);
            
            // Find milestone and update status
            for (PaymentMilestone milestone : milestones) {
                if (milestone.getMilestoneId().equals(milestoneId)) {
                    milestone.setStatus("Released");
                    milestone.setCompletedDate(new Date());
                    break;
                }
            }
            
            // Update corresponding escrow
            for (EscrowAccount escrow : escrowAccounts) {
                if (escrow.getMilestoneId().equals(milestoneId)) {
                    escrow.setStatus("Released");
                    break;
                }
            }
            
            // Generate invoice
            generateInvoiceForMilestone(milestoneId);
            
            refreshMilestonesTable();
            refreshEscrowTable();
            updateDashboard();
            
            JOptionPane.showMessageDialog(frame,
                "Payment released successfully!\nInvoice generated and funds transferred to freelancer.",
                "Payment Released", JOptionPane.INFORMATION_MESSAGE);
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
            
            Invoice invoice = new Invoice(
                invoiceId,
                milestone.getProjectId(),
                "Client_" + milestone.getProjectId(),
                milestone.getAmount(),
                "Paid",
                new Date(),
                new Date(),
                milestone.getDescription()
            );
            
            invoices.add(invoice);
            refreshInvoicesTable();
        }
    }
    
    private void openDispute() {
        int selectedRow = milestonesTable.getSelectedRow();
        if (selectedRow != -1) {
            String milestoneId = (String) milestonesTableModel.getValueAt(selectedRow, 0);
            String projectId = (String) milestonesTableModel.getValueAt(selectedRow, 1);
            
            String reason = JOptionPane.showInputDialog(frame, 
                "Enter dispute reason:", 
                "Open Dispute", 
                JOptionPane.QUESTION_MESSAGE);
                
            if (reason != null && !reason.trim().isEmpty()) {
                String disputeId = "DSP" + String.format("%03d", nextDisputeId++);
                
                DisputeCase dispute = new DisputeCase(
                    disputeId,
                    projectId,
                    milestoneId,
                    "Client", // Assuming client raises dispute
                    reason,
                    "Open",
                    new Date(),
                    null
                );
                
                disputes.add(dispute);
                
                // Hold the milestone
                for (PaymentMilestone milestone : milestones) {
                    if (milestone.getMilestoneId().equals(milestoneId)) {
                        milestone.setStatus("Disputed");
                        break;
                    }
                }
                
                refreshMilestonesTable();
                refreshDisputesTable();
                updateDashboard();
                
                JOptionPane.showMessageDialog(frame,
                    "Dispute opened successfully!\nDispute ID: " + disputeId + 
                    "\nPayment has been held pending resolution.",
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
            
            for (EscrowAccount escrow : escrowAccounts) {
                if (escrow.getEscrowId().equals(escrowId)) {
                    escrow.setStatus("Released");
                    break;
                }
            }
            
            refreshEscrowTable();
            updateDashboard();
            
            JOptionPane.showMessageDialog(frame, "Escrow funds released successfully!", "Funds Released", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(frame, "Please select an escrow account.", "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void holdEscrowFunds() {
        int selectedRow = escrowTable.getSelectedRow();
        if (selectedRow != -1) {
            String escrowId = (String) escrowTableModel.getValueAt(selectedRow, 0);
            
            for (EscrowAccount escrow : escrowAccounts) {
                if (escrow.getEscrowId().equals(escrowId)) {
                    escrow.setStatus("On Hold");
                    break;
                }
            }
            
            refreshEscrowTable();
            updateDashboard();
            
            JOptionPane.showMessageDialog(frame, "Escrow funds placed on hold.", "Funds Held", JOptionPane.WARNING_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(frame, "Please select an escrow account.", "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void generateInvoice() {
        String projectId = JOptionPane.showInputDialog(frame, "Enter Project ID:", "Generate Invoice", JOptionPane.QUESTION_MESSAGE);
        if (projectId != null && !projectId.trim().isEmpty()) {
            String invoiceId = "INV" + String.format("%03d", nextInvoiceId++);
            
            Invoice invoice = new Invoice(
                invoiceId,
                projectId,
                "Client_" + projectId,
                0.0, // Amount will be calculated
                "Generated",
                new Date(),
                new Date(),
                "Project completion invoice"
            );
            
            invoices.add(invoice);
            refreshInvoicesTable();
            updateDashboard();
            
            JOptionPane.showMessageDialog(frame, "Invoice generated successfully!\nInvoice ID: " + invoiceId, "Invoice Generated", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void sendInvoice() {
        int selectedRow = invoicesTable.getSelectedRow();
        if (selectedRow != -1) {
            String invoiceId = (String) invoicesTableModel.getValueAt(selectedRow, 0);
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
            
            String resolution = JOptionPane.showInputDialog(frame, 
                "Enter resolution details:", 
                "Resolve Dispute", 
                JOptionPane.QUESTION_MESSAGE);
                
            if (resolution != null && !resolution.trim().isEmpty()) {
                for (DisputeCase dispute : disputes) {
                    if (dispute.getDisputeId().equals(disputeId)) {
                        dispute.setStatus("Resolved");
                        dispute.setResolution(resolution);
                        break;
                    }
                }
                
                refreshDisputesTable();
                updateDashboard();
                
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
            
            for (DisputeCase dispute : disputes) {
                if (dispute.getDisputeId().equals(disputeId)) {
                    dispute.setStatus("Escalated");
                    break;
                }
            }
            
            refreshDisputesTable();
            JOptionPane.showMessageDialog(frame, "Dispute escalated to senior mediation team.", "Dispute Escalated", JOptionPane.WARNING_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(frame, "Please select a dispute to escalate.", "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void mediateDispute() {
        int selectedRow = disputesTable.getSelectedRow();
        if (selectedRow != -1) {
            String disputeId = (String) disputesTableModel.getValueAt(selectedRow, 0);
            
            for (DisputeCase dispute : disputes) {
                if (dispute.getDisputeId().equals(disputeId)) {
                    dispute.setStatus("In Mediation");
                    break;
                }
            }
            
            refreshDisputesTable();
            JOptionPane.showMessageDialog(frame, "Mediation process started. Both parties will be contacted.", "Mediation Started", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(frame, "Please select a dispute for mediation.", "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    // Table refresh methods
    private void refreshMilestonesTable() {
        milestonesTableModel.setRowCount(0);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        
        for (PaymentMilestone milestone : milestones) {
            milestonesTableModel.addRow(new Object[]{
                milestone.getMilestoneId(),
                milestone.getProjectId(),
                milestone.getDescription(),
                "$" + new DecimalFormat("#,##0.00").format(milestone.getAmount()),
                milestone.getStatus(),
                dateFormat.format(milestone.getCreatedDate()),
                milestone.getCompletedDate() != null ? dateFormat.format(milestone.getCompletedDate()) : "Pending"
            });
        }
    }
    
    private void refreshEscrowTable() {
        escrowTableModel.setRowCount(0);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        
        for (EscrowAccount escrow : escrowAccounts) {
            escrowTableModel.addRow(new Object[]{
                escrow.getEscrowId(),
                escrow.getProjectId(),
                escrow.getClientId(),
                escrow.getFreelancerId(),
                "$" + new DecimalFormat("#,##0.00").format(escrow.getAmount()),
                escrow.getStatus(),
                dateFormat.format(escrow.getCreatedDate())
            });
        }
    }
    
    private void refreshInvoicesTable() {
        invoicesTableModel.setRowCount(0);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        
        for (Invoice invoice : invoices) {
            invoicesTableModel.addRow(new Object[]{
                invoice.getInvoiceId(),
                invoice.getProjectId(),
                invoice.getClientId(),
                "$" + new DecimalFormat("#,##0.00").format(invoice.getAmount()),
                invoice.getStatus(),
                dateFormat.format(invoice.getCreatedDate()),
                dateFormat.format(invoice.getDueDate())
            });
        }
    }
    
    private void refreshDisputesTable() {
        disputesTableModel.setRowCount(0);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        
        for (DisputeCase dispute : disputes) {
            disputesTableModel.addRow(new Object[]{
                dispute.getDisputeId(),
                dispute.getProjectId(),
                dispute.getRaisedBy(),
                dispute.getReason().length() > 30 ? dispute.getReason().substring(0, 30) + "..." : dispute.getReason(),
                dispute.getStatus(),
                dateFormat.format(dispute.getCreatedDate()),
                dispute.getResolution() != null ? "Resolved" : "Pending"
            });
        }
    }
    
    private void updateDashboard() {
        // Update dashboard stats (you would call this method to refresh stats)
        // This can be expanded to update the actual stat cards
    }
    
    // Validation methods
    private boolean validateMilestoneForm() {
        if (projectIdField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please enter a Project ID.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (milestoneDescField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please enter a milestone description.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        try {
            double amount = Double.parseDouble(milestoneAmountField.getText());
            if (amount <= 0) {
                JOptionPane.showMessageDialog(frame, "Amount must be greater than 0.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "Please enter a valid amount.", "Validation Error", JOptionPane.ERROR_MESSAGE);
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
    
    // Sample data
    private void loadSampleData() {
        // Sample milestones
        milestones.add(new PaymentMilestone("MIL001", "PRJ001", "Website Development - Phase 1", 2500.0, "Pending", new Date(), null, "Initial development phase", "Credit Card"));
        milestones.add(new PaymentMilestone("MIL002", "PRJ002", "UI Design Completion", 1200.0, "Released", new Date(), new Date(), "Final UI designs delivered", "PayPal"));
        
        // Sample escrow accounts
        escrowAccounts.add(new EscrowAccount("ESC001", "PRJ001", "MIL001", "Client_PRJ001", "Freelancer_PRJ001", 2500.0, "Holding", new Date()));
        escrowAccounts.add(new EscrowAccount("ESC002", "PRJ002", "MIL002", "Client_PRJ002", "Freelancer_PRJ002", 1200.0, "Released", new Date()));
        
        // Sample invoices
        invoices.add(new Invoice("INV001", "PRJ002", "Client_PRJ002", 1200.0, "Paid", new Date(), new Date(), "UI Design project completion"));
        
        // Sample disputes
        disputes.add(new DisputeCase("DSP001", "PRJ003", "MIL003", "Client", "Work not delivered on time", "Open", new Date(), null));
        
        nextMilestoneId = milestones.size() + 1;
        nextEscrowId = escrowAccounts.size() + 1;
        nextInvoiceId = invoices.size() + 1;
        nextDisputeId = disputes.size() + 1;
        
        refreshMilestonesTable();
        refreshEscrowTable();
        refreshInvoicesTable();
        refreshDisputesTable();
    }
    
    // Helper methods for UI components
    private JLabel createStyledHeader(String text, Color color) {
        JLabel header = new JLabel(text, JLabel.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 28));
        header.setForeground(color);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 25, 0));
        return header;
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
        
        // Add placeholder text
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
    
    private void styleComboBox(JComboBox<String> comboBox) {
        comboBox.setBackground(inputBgColor);
        comboBox.setForeground(textColor);
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
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
    
    private void styleTable(JTable table, Color headerColor) {
        table.setBackground(bgColor);
        table.setForeground(textColor);
        table.setGridColor(new Color(80, 80, 80));
        table.setRowHeight(35);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setSelectionBackground(headerColor.darker());
        table.setSelectionForeground(Color.WHITE);
        
        JTableHeader header = table.getTableHeader();
        header.setBackground(headerColor);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setPreferredSize(new Dimension(0, 40));
        
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
    
    private JPanel createStatCard(String title, String value, Color color, String icon) {
        JPanel card = new JPanel(new BorderLayout(10, 5));
        card.setBackground(new Color(52, 73, 94));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 3),
            BorderFactory.createEmptyBorder(20, 25, 20, 25)
        ));
        
        // Icon and value
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
        
        // Title
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(textColor);
        
        card.add(topPanel, BorderLayout.CENTER);
        card.add(titleLabel, BorderLayout.SOUTH);
        
        return card;
    }
    
    // Navigation button class
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
    
    // Data model classes
    private static class PaymentMilestone {
        private String milestoneId, projectId, description, status, notes, paymentMethod;
        private double amount;
        private Date createdDate, completedDate;
        
        public PaymentMilestone(String milestoneId, String projectId, String description, double amount, String status, Date createdDate, Date completedDate, String notes, String paymentMethod) {
            this.milestoneId = milestoneId;
            this.projectId = projectId;
            this.description = description;
            this.amount = amount;
            this.status = status;
            this.createdDate = createdDate;
            this.completedDate = completedDate;
            this.notes = notes;
            this.paymentMethod = paymentMethod;
        }
        
        // Getters and setters
        public String getMilestoneId() { return milestoneId; }
        public String getProjectId() { return projectId; }
        public String getDescription() { return description; }
        public double getAmount() { return amount; }
        public String getStatus() { return status; }
        public Date getCreatedDate() { return createdDate; }
        public Date getCompletedDate() { return completedDate; }
        public String getNotes() { return notes; }
        public String getPaymentMethod() { return paymentMethod; }
        
        public void setStatus(String status) { this.status = status; }
        public void setCompletedDate(Date completedDate) { this.completedDate = completedDate; }
    }
    
    private static class EscrowAccount {
        private String escrowId, projectId, milestoneId, clientId, freelancerId, status;
        private double amount;
        private Date createdDate;
        
        public EscrowAccount(String escrowId, String projectId, String milestoneId, String clientId, String freelancerId, double amount, String status, Date createdDate) {
            this.escrowId = escrowId;
            this.projectId = projectId;
            this.milestoneId = milestoneId;
            this.clientId = clientId;
            this.freelancerId = freelancerId;
            this.amount = amount;
            this.status = status;
            this.createdDate = createdDate;
        }
        
        // Getters and setters
        public String getEscrowId() { return escrowId; }
        public String getProjectId() { return projectId; }
        public String getMilestoneId() { return milestoneId; }
        public String getClientId() { return clientId; }
        public String getFreelancerId() { return freelancerId; }
        public double getAmount() { return amount; }
        public String getStatus() { return status; }
        public Date getCreatedDate() { return createdDate; }
        
        public void setStatus(String status) { this.status = status; }
    }
    
    private static class Invoice {
        private String invoiceId, projectId, clientId, status, description;
        private double amount;
        private Date createdDate, dueDate;
        
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
        
        // Getters
        public String getInvoiceId() { return invoiceId; }
        public String getProjectId() { return projectId; }
        public String getClientId() { return clientId; }
        public double getAmount() { return amount; }
        public String getStatus() { return status; }
        public Date getCreatedDate() { return createdDate; }
        public Date getDueDate() { return dueDate; }
        public String getDescription() { return description; }
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
        
        // Getters and setters
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
