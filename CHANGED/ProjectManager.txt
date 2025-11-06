import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.sql.*;
import java.util.Date;

public class ProjectManager {
    public enum UserRole { CLIENT, FREELANCER }
    private UserRole currentUserRole;
    private JFrame frame;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    
    // Database integration & Notification Manager
    private final DatabaseManager dbManager;
    private final NotificationManager notificationManager;
    
    private ArrayList<Project> projects;
    private ArrayList<Bid> bids;
    private DefaultTableModel projectTableModel;
    private DefaultTableModel bidTableModel;
    private JTable projectTable;
    private JTable bidTable;
    private int nextProjectId = 1;
    private int nextBidId = 1;
    private JTextField titleField, budgetField, deadlineField, clientNameField;
    private JTextArea descriptionArea;
    private JComboBox<String> categoryCombo, difficultyCombo;
    private JTextField bidAmountField, bidDeadlineField, freelancerNameField;
    private JTextArea bidDescriptionArea;
    
    // File upload components
    private JLabel resumeLabel;
    private JButton attachResumeButton;
    private File selectedResumeFile;
    private JLabel totalProjectsLabel, activeProjectsLabel, completedProjectsLabel;
    private JLabel totalBidsLabel, pendingBidsLabel, successRateLabel;

    private final Color bgColor = new Color(34, 47, 62);
    private final Color sidebarColor = new Color(44, 62, 80);
    private final Color panelColor = new Color(52, 73, 94);
    private final Color textColor = new Color(236, 240, 241);
    private final Color inputBgColor = new Color(99, 110, 114);
    private final Color accentColor = new Color(155, 89, 182);
    private final Color blueAccent = new Color(52, 152, 219);
    private final Color greenAccent = new Color(46, 204, 113);
    private final Color orangeAccent = new Color(230, 126, 34);
    private final Color redAccent = new Color(231, 76, 60);

    // UPDATED CONSTRUCTOR with NotificationManager
    public ProjectManager(DatabaseManager dbManager, NotificationManager notificationManager) {
        this.dbManager = dbManager;
        this.notificationManager = notificationManager;
        projects = new ArrayList<>();
        bids = new ArrayList<>();
    }

    public void showWindow() {
        showWindow(UserRole.FREELANCER);
    }

    public void showWindow(UserRole role) {
        this.currentUserRole = role;
        if (frame != null && frame.isVisible()) {
            frame.dispose();
        }
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) { /* Fallback */ }
        
        projects = new ArrayList<>();
        bids = new ArrayList<>();
        selectedResumeFile = null;
        
        initializeGUI();
        loadDataFromDatabase();
        frame.setVisible(true);
    }

    private void initializeGUI() {
        frame = new JFrame("Project Management Dashboard (" + currentUserRole.toString() + ")");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(1280, 800);
        frame.setLocationRelativeTo(null);
        
        JPanel backgroundPanel = new TexturedGradientPanel();
        backgroundPanel.setLayout(new BorderLayout());

        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(sidebarColor);
        sidebar.setPreferredSize(new Dimension(250, 0));
        
        JLabel sidebarHeader = new JLabel("Projects", JLabel.CENTER);
        sidebarHeader.setFont(new Font("Segoe UI", Font.BOLD, 24));
        sidebarHeader.setForeground(textColor);
        sidebarHeader.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebarHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        sidebarHeader.setBorder(BorderFactory.createEmptyBorder(20,0,20,0));
        sidebar.add(sidebarHeader);

        sidebar.add(new NavButton("⚡ Status Dashboard", "STATUS"));
        sidebar.add(new NavButton("📋 Browse Projects", "BROWSE"));
        if (currentUserRole == UserRole.CLIENT) {
            sidebar.add(new NavButton("🏢 Post Project", "POST"));
            sidebar.add(new NavButton("📊 View Bids", "VIEW_BIDS"));
        } else {
            sidebar.add(new NavButton("💼 Place Bid", "BID"));
        }
        
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setOpaque(false);

        contentPanel.add(createProjectStatusPanel(), "STATUS");
        contentPanel.add(createBrowseProjectsPanel(), "BROWSE");
        contentPanel.add(createPostProjectPanel(), "POST");
        contentPanel.add(createBiddingPanel(), "BID");
        contentPanel.add(createViewBidsPanel(), "VIEW_BIDS");
        
        backgroundPanel.add(sidebar, BorderLayout.WEST);
        backgroundPanel.add(contentPanel, BorderLayout.CENTER);
        
        frame.add(backgroundPanel);
        cardLayout.show(contentPanel, "STATUS");
    }
    
    private JPanel createProjectStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 40, 40));
        panel.add(createStyledHeader("Project Status Dashboard", textColor), BorderLayout.NORTH);
        JPanel statsPanel = new JPanel(new GridLayout(2, 3, 25, 25));
        statsPanel.setOpaque(false);
        totalProjectsLabel = createValueLabel("0", blueAccent);
        activeProjectsLabel = createValueLabel("0", greenAccent);
        completedProjectsLabel = createValueLabel("0", accentColor);
        totalBidsLabel = createValueLabel("0", orangeAccent);
        pendingBidsLabel = createValueLabel("0", redAccent);
        successRateLabel = createValueLabel("0%", new Color(26, 188, 156));
        statsPanel.add(createStatCard("Total Projects", totalProjectsLabel, blueAccent, "📁"));
        statsPanel.add(createStatCard("Active Projects", activeProjectsLabel, greenAccent, "⚙️"));
        statsPanel.add(createStatCard("Completed", completedProjectsLabel, accentColor, "✔️"));
        statsPanel.add(createStatCard("Total Bids", totalBidsLabel, orangeAccent, "💬"));
        statsPanel.add(createStatCard("Pending Bids", pendingBidsLabel, redAccent, "⏳"));
        statsPanel.add(createStatCard("Success Rate", successRateLabel, new Color(26, 188, 156), "📈"));
        panel.add(statsPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createPostProjectPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 40, 40));
        panel.add(createStyledHeader("Post a New Project", blueAccent), BorderLayout.NORTH);
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        clientNameField = createStyledTextField(25);
        titleField = createStyledTextField(25);
        categoryCombo = createStyledComboBox(new String[]{"Web Development", "Mobile Development", "Data Science", "UI/UX Design", "Content Writing", "Digital Marketing"});
        budgetField = createStyledTextField(25);
        difficultyCombo = createStyledComboBox(new String[]{"Beginner", "Intermediate", "Expert"});
        deadlineField = createStyledTextField(25);
        descriptionArea = createStyledTextArea(4, 25);
        int y = 0;
        formPanel.add(createStyledLabel("Client Name:"), createGbc(0, y)); formPanel.add(clientNameField, createGbc(1, y++));
        formPanel.add(createStyledLabel("Project Title:"), createGbc(0, y)); formPanel.add(titleField, createGbc(1, y++));
        formPanel.add(createStyledLabel("Category:"), createGbc(0, y)); formPanel.add(categoryCombo, createGbc(1, y++));
        formPanel.add(createStyledLabel("Budget ($):"), createGbc(0, y)); formPanel.add(budgetField, createGbc(1, y++));
        formPanel.add(createStyledLabel("Difficulty:"), createGbc(0, y)); formPanel.add(difficultyCombo, createGbc(1, y++));
        formPanel.add(createStyledLabel("Deadline (days):"), createGbc(0, y)); formPanel.add(deadlineField, createGbc(1, y++));
        formPanel.add(createStyledLabel("Description:"), createGbc(0, y));
        JScrollPane descriptionScrollPane = new JScrollPane(descriptionArea);
        descriptionScrollPane.getViewport().setBackground(inputBgColor);
        descriptionScrollPane.setBorder(BorderFactory.createEmptyBorder());
        formPanel.add(descriptionScrollPane, createGbc(1, y++));
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        buttonPanel.setOpaque(false);
        JButton postButton = createStyledButton("Post Project", blueAccent);
        postButton.addActionListener(e -> postProject());
        JButton clearButton = createStyledButton("Clear Form", new Color(149, 165, 166));
        clearButton.addActionListener(e -> clearPostForm());
        buttonPanel.add(postButton);
        buttonPanel.add(clearButton);
        formPanel.add(buttonPanel, createGbc(1, y));
        panel.add(formPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createBrowseProjectsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10,10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 40, 40));
        panel.add(createStyledHeader("Available Projects", greenAccent), BorderLayout.NORTH);
        String[] columns = {"ID", "Title", "Client", "Category", "Budget", "Difficulty", "Deadline", "Status"};
        projectTableModel = new DefaultTableModel(columns, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        projectTable = new JTable(projectTableModel);
        styleTable(projectTable, greenAccent);
        if (currentUserRole == UserRole.CLIENT) {
            projectTable.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        int row = projectTable.getSelectedRow();
                        if (row != -1) {
                            String id = (String) projectTableModel.getValueAt(row, 0);
                            filterBids(id);
                            cardLayout.show(contentPanel, "VIEW_BIDS");
                        }
                    }
                }
            });
        }
        JScrollPane tableScrollPane = new JScrollPane(projectTable);
        tableScrollPane.getViewport().setBackground(bgColor);
        panel.add(tableScrollPane, BorderLayout.CENTER);
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        bottomPanel.setOpaque(false);
        if (currentUserRole == UserRole.CLIENT) {
            JButton completeButton = createStyledButton("Mark as Complete", accentColor);
            completeButton.addActionListener(e -> completeProject());
            bottomPanel.add(completeButton);
        }
        JButton refreshButton = createStyledButton("🔄 Refresh Projects", greenAccent);
        refreshButton.addActionListener(e -> loadDataFromDatabase());
        bottomPanel.add(refreshButton);
        panel.add(bottomPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createBiddingPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 40, 40));
        panel.add(createStyledHeader("Place Your Bid", orangeAccent), BorderLayout.NORTH);
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        
        freelancerNameField = createStyledTextField(25);
        JTextField projectIdField = createStyledTextField(25);
        bidAmountField = createStyledTextField(25);
        bidDeadlineField = createStyledTextField(25);
        bidDescriptionArea = createStyledTextArea(4, 25);
        resumeLabel = createStyledLabel("No file selected");
        resumeLabel.setForeground(new Color(149, 165, 166));
        resumeLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        
        attachResumeButton = createStyledButton("📎 Attach Resume", new Color(52, 152, 219));
        attachResumeButton.addActionListener(e -> selectResumeFile());
        JButton removeResumeButton = createStyledButton("🗑️ Remove", redAccent);
        removeResumeButton.addActionListener(e -> removeResumeFile());
        
        int y = 0;
        formPanel.add(createStyledLabel("Freelancer Name:"), createGbc(0, y)); 
        formPanel.add(freelancerNameField, createGbc(1, y++));
        formPanel.add(createStyledLabel("Project ID:"), createGbc(0, y)); 
        formPanel.add(projectIdField, createGbc(1, y++));
        
        formPanel.add(createStyledLabel("Bid Amount ($):"), createGbc(0, y)); 
        formPanel.add(bidAmountField, createGbc(1, y++));
        
        formPanel.add(createStyledLabel("Completion Time (days):"), createGbc(0, y));
        formPanel.add(bidDeadlineField, createGbc(1, y++));
        
        formPanel.add(createStyledLabel("Resume/Portfolio:"), createGbc(0, y));
        JPanel resumePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
        resumePanel.setOpaque(false);
        resumePanel.add(attachResumeButton);
        resumePanel.add(removeResumeButton);
        formPanel.add(resumePanel, createGbc(1, y++));
        
        formPanel.add(new JLabel(), createGbc(0, y));
        formPanel.add(resumeLabel, createGbc(1, y++));
        
        formPanel.add(createStyledLabel("Proposal:"), createGbc(0, y));
        JScrollPane bidScrollPane = new JScrollPane(bidDescriptionArea);
        bidScrollPane.getViewport().setBackground(inputBgColor);
        bidScrollPane.setBorder(BorderFactory.createEmptyBorder());
        formPanel.add(bidScrollPane, createGbc(1, y++));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        buttonPanel.setOpaque(false);
        JButton bidButton = createStyledButton("Submit Bid", orangeAccent);
        bidButton.addActionListener(e -> placeBid(projectIdField));
        JButton clearBidButton = createStyledButton("Clear Form", new Color(149, 165, 166));
        clearBidButton.addActionListener(e -> clearBidForm());
        buttonPanel.add(bidButton);
        buttonPanel.add(clearBidButton);
        formPanel.add(buttonPanel, createGbc(1, y));
        
        panel.add(formPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createViewBidsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10,10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 40, 40));
        panel.add(createStyledHeader("Review Project Bids", accentColor), BorderLayout.NORTH);
        String[] bidColumns = {"Bid ID", "Project ID", "Freelancer", "Amount ($)", "Days", "Resume", "Proposal", "Status"};
        bidTableModel = new DefaultTableModel(bidColumns, 0) { @Override public boolean isCellEditable(int r, int c) { return false; }};
        bidTable = new JTable(bidTableModel);
        styleTable(bidTable, accentColor);
        
        bidTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = bidTable.getSelectedRow();
                    if (row != -1) {
                        String bidId = (String) bidTableModel.getValueAt(row, 0);
                        viewResumeForBid(bidId);
                    }
                }
            }
        });
        
        JScrollPane bidTableScrollPane = new JScrollPane(bidTable);
        bidTableScrollPane.getViewport().setBackground(bgColor);
        panel.add(bidTableScrollPane, BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        bottomPanel.setOpaque(false);
        if (currentUserRole == UserRole.CLIENT) {
            JButton acceptBidButton = createStyledButton("✅ Accept Selected Bid", greenAccent);
            acceptBidButton.addActionListener(e -> acceptBid());
            JButton rejectBidButton = createStyledButton("❌ Reject Selected Bid", redAccent);
            rejectBidButton.addActionListener(e -> rejectBid());
            JButton viewResumeButton = createStyledButton("📄 View Resume", blueAccent);
            viewResumeButton.addActionListener(e -> viewSelectedResume());
            bottomPanel.add(acceptBidButton);
            bottomPanel.add(rejectBidButton);
            bottomPanel.add(viewResumeButton);
        }
        JButton showAllButton = createStyledButton("Show All Bids", new Color(149, 165, 166));
        showAllButton.addActionListener(e -> loadDataFromDatabase());
        bottomPanel.add(showAllButton);
        panel.add(bottomPanel, BorderLayout.SOUTH);
        return panel;
    }

    // File handling methods
    private void selectResumeFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Resume/Portfolio");
        
        FileNameExtensionFilter pdfFilter = new FileNameExtensionFilter("PDF Documents (*.pdf)", "pdf");
        FileNameExtensionFilter docFilter = new FileNameExtensionFilter("Word Documents (*.doc, *.docx)", "doc", "docx");
        FileNameExtensionFilter txtFilter = new FileNameExtensionFilter("Text Files (*.txt)", "txt");
        
        fileChooser.addChoosableFileFilter(pdfFilter);
        fileChooser.addChoosableFileFilter(docFilter);
        fileChooser.addChoosableFileFilter(txtFilter);
        fileChooser.setFileFilter(pdfFilter);
        
        int result = fileChooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedResumeFile = fileChooser.getSelectedFile();
            updateResumeLabel();
        }
    }
    
    private void removeResumeFile() {
        selectedResumeFile = null;
        updateResumeLabel();
    }
    
    private void updateResumeLabel() {
        if (selectedResumeFile != null) {
            String fileName = selectedResumeFile.getName();
            long fileSize = selectedResumeFile.length();
            String sizeStr = formatFileSize(fileSize);
            resumeLabel.setText("📎 " + fileName + " (" + sizeStr + ")");
            resumeLabel.setForeground(greenAccent);
        } else {
            resumeLabel.setText("No file selected");
            resumeLabel.setForeground(new Color(149, 165, 166));
        }
    }
    
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        else if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        else return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }
    
    private void viewResumeForBid(String bidId) {
        Bid bid = bids.stream().filter(b -> b.getBidId().equals(bidId)).findFirst().orElse(null);
        if (bid != null && bid.getResumeFile() != null) {
            viewResumeFile(bid.getResumeFile());
        } else {
            JOptionPane.showMessageDialog(frame, "No resume attached to this bid.", "No Resume", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void viewSelectedResume() {
        int row = bidTable.getSelectedRow();
        if (row != -1) {
            String bidId = (String) bidTableModel.getValueAt(row, 0);
            viewResumeForBid(bidId);
        } else {
            JOptionPane.showMessageDialog(frame, "Please select a bid to view its resume.", "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void viewResumeFile(File file) {
        if (file != null && file.exists()) {
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(frame, 
                    "Cannot open file: " + file.getName() + "\nPath: " + file.getAbsolutePath(), 
                    "File Open Error", 
                    JOptionPane.ERROR_MESSAGE);
            } catch (UnsupportedOperationException e) {
                JOptionPane.showMessageDialog(frame, 
                    "File opening not supported on this system.\nFile: " + file.getAbsolutePath(), 
                    "Unsupported Operation", 
                    JOptionPane.WARNING_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(frame, "Resume file not found or corrupted.", "File Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // UPDATED: Action Methods with Notifications
    private void postProject() { 
        if (!validatePostForm()) return;
        String id = "PRJ" + String.format("%03d", nextProjectId++); 
        
        ProjectData projectData = new ProjectData();
        projectData.projectId = id;
        projectData.title = titleField.getText();
        projectData.clientName = clientNameField.getText();
        projectData.category = (String) categoryCombo.getSelectedItem();
        projectData.budget = Double.parseDouble(budgetField.getText());
        projectData.difficulty = (String) difficultyCombo.getSelectedItem();
        projectData.deadlineDays = Integer.parseInt(deadlineField.getText());
        projectData.description = descriptionArea.getText();
        projectData.status = "Open";
        
        boolean saved = dbManager.insertProject(projectData);
        if (saved) {
            Project p = new Project(id, projectData.title, projectData.clientName, 
                                  projectData.category, projectData.budget, 
                                  projectData.difficulty, projectData.deadlineDays, 
                                  projectData.description, projectData.status);
            projects.add(p);
            
            // ✅ TRIGGER NOTIFICATION - PROJECT POSTED
            notificationManager.notifyNewProject(projectData.title, projectData.description);
            
            refreshProjectTable(); 
            clearPostForm(); 
            updateStats(); 
            JOptionPane.showMessageDialog(frame, "Project posted successfully!\nProject ID: " + id, "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(frame, "Failed to save project to database!", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void placeBid(JTextField projectIdField) { 
        if (!validateBidForm(projectIdField)) return;
        String bidId = "BID" + String.format("%03d", nextBidId++);
        
        BidData bidData = new BidData();
        bidData.bidId = bidId;
        bidData.projectId = projectIdField.getText();
        bidData.freelancerName = freelancerNameField.getText();
        bidData.amount = Double.parseDouble(bidAmountField.getText());
        bidData.completionDays = Integer.parseInt(bidDeadlineField.getText());
        bidData.proposal = bidDescriptionArea.getText();
        bidData.status = "Pending";
        
        if (selectedResumeFile != null) {
            bidData.resumeFilePath = selectedResumeFile.getAbsolutePath();
            bidData.resumeFileName = selectedResumeFile.getName();
        }
        
        boolean saved = dbManager.insertBid(bidData);
        if (saved) {
            Bid bid = new Bid(bidId, bidData.projectId, bidData.freelancerName, 
                             bidData.amount, bidData.completionDays, 
                             bidData.proposal, bidData.status, selectedResumeFile);
            bids.add(bid);
            
            // ✅ TRIGGER NOTIFICATION - BID PLACED
            notificationManager.notifyNewMessage(
                bidData.freelancerName, 
                "Bid placed: ₹" + bidData.amount
            );
            
            refreshBidTable(); 
            clearBidForm(); 
            updateStats(); 
            
            String message = "Bid placed successfully!\nBid ID: " + bidId;
            if (selectedResumeFile != null) {
                message += "\nResume attached: " + selectedResumeFile.getName();
            }
            JOptionPane.showMessageDialog(frame, message, "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(frame, "Failed to save bid to database!", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void acceptBid() { 
        int row = bidTable.getSelectedRow();
        if (row == -1) { 
            JOptionPane.showMessageDialog(frame, "Please select a bid to accept.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return; 
        } 
        String bidId = (String) bidTableModel.getValueAt(row, 0);
        String projId = (String) bidTableModel.getValueAt(row, 1); 
        
        boolean bidUpdated = dbManager.updateBidStatus(bidId, "Accepted");
        boolean projectUpdated = dbManager.updateProjectStatus(projId, "In Progress");
        
        if (bidUpdated && projectUpdated) {
            bids.stream().filter(b -> b.getBidId().equals(bidId)).findFirst().ifPresent(b -> b.setStatus("Accepted"));
            projects.stream().filter(p -> p.getProjectId().equals(projId)).findFirst().ifPresent(p -> p.setStatus("In Progress"));
            
            // ✅ TRIGGER NOTIFICATION - BID ACCEPTED
            Bid acceptedBid = bids.stream().filter(b -> b.getBidId().equals(bidId)).findFirst().orElse(null);
            if (acceptedBid != null) {
                notificationManager.notifySystem(
                    "✅ Bid Accepted",
                    "Your bid of ₹" + acceptedBid.getAmount() + " has been accepted for project " + projId
                );
            }
            
            refreshBidTable(); 
            refreshProjectTable(); 
            updateStats(); 
            JOptionPane.showMessageDialog(frame, "Bid accepted!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(frame, "Failed to update database!", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void rejectBid() { 
        int row = bidTable.getSelectedRow();
        if (row == -1) { 
            JOptionPane.showMessageDialog(frame, "Please select a bid to reject.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return; 
        } 
        String bidId = (String) bidTableModel.getValueAt(row, 0);
        
        boolean updated = dbManager.updateBidStatus(bidId, "Rejected");
        if (updated) {
            bids.stream().filter(b -> b.getBidId().equals(bidId)).findFirst().ifPresent(b -> b.setStatus("Rejected"));
            
            // ✅ TRIGGER NOTIFICATION - BID REJECTED
            notificationManager.notifySystem(
                "❌ Bid Rejected",
                "Your bid " + bidId + " has been rejected."
            );
            
            refreshBidTable(); 
            updateStats();
            JOptionPane.showMessageDialog(frame, "Bid rejected.", "Status Updated", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(frame, "Failed to update database!", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void completeProject() { 
        int row = projectTable.getSelectedRow();
        if (row == -1) { 
            JOptionPane.showMessageDialog(frame, "Please select a project to complete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return; 
        } 
        String projId = (String) projectTableModel.getValueAt(row, 0);
        
        boolean updated = dbManager.updateProjectStatus(projId, "Completed");
        if (updated) {
            projects.stream().filter(p -> p.getProjectId().equals(projId)).findFirst().ifPresent(p -> p.setStatus("Completed"));
            
            // ✅ TRIGGER NOTIFICATION - PROJECT COMPLETED
            Project completedProject = projects.stream().filter(p -> p.getProjectId().equals(projId)).findFirst().orElse(null);
            if (completedProject != null) {
                notificationManager.notifySystem(
                    "✅ Project Completed",
                    "Project '" + completedProject.getTitle() + "' has been marked as completed."
                );
            }
            
            refreshProjectTable(); 
            updateStats();
            JOptionPane.showMessageDialog(frame, "Project marked as completed!", "Status Updated", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(frame, "Failed to update database!", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Data loading methods
    private void loadDataFromDatabase() {
        ArrayList<ProjectData> dbProjects = dbManager.getAllProjects();
        projects.clear();
        nextProjectId = 1;
        
        for (ProjectData projectData : dbProjects) {
            Project project = new Project(
                projectData.projectId,
                projectData.title,
                projectData.clientName,
                projectData.category,
                projectData.budget,
                projectData.difficulty,
                projectData.deadlineDays,
                projectData.description,
                projectData.status
            );
            projects.add(project);
            
            try {
                int idNum = Integer.parseInt(projectData.projectId.replace("PRJ", ""));
                if (idNum >= nextProjectId) {
                    nextProjectId = idNum + 1;
                }
            } catch (NumberFormatException e) {
                // Handle non-numeric IDs gracefully
            }
        }
        
        ArrayList<BidData> dbBids = dbManager.getAllBids();
        bids.clear();
        nextBidId = 1;
        
        for (BidData bidData : dbBids) {
            File resumeFile = null;
            if (bidData.resumeFilePath != null && !bidData.resumeFilePath.isEmpty()) {
                resumeFile = new File(bidData.resumeFilePath);
                if (!resumeFile.exists()) {
                    resumeFile = null;
                }
            }
            
            Bid bid = new Bid(
                bidData.bidId,
                bidData.projectId,
                bidData.freelancerName,
                bidData.amount,
                bidData.completionDays,
                bidData.proposal,
                bidData.status,
                resumeFile
            );
            bids.add(bid);
            
            try {
                int idNum = Integer.parseInt(bidData.bidId.replace("BID", ""));
                if (idNum >= nextBidId) {
                    nextBidId = idNum + 1;
                }
            } catch (NumberFormatException e) {
                // Handle non-numeric IDs gracefully
            }
        }
        
        refreshProjectTable();
        refreshBidTable();
        updateStats();
        
        System.out.println("✅ Data loaded from database: " + projects.size() + " projects, " + bids.size() + " bids");
    }

    private void refreshProjectTable() { 
        projectTableModel.setRowCount(0);
        for (Project p : projects) 
            projectTableModel.addRow(new Object[]{p.getProjectId(), p.getTitle(), p.getClientName(), p.getCategory(), "$" + p.getBudget(), p.getDifficulty(), p.getDeadline() + " days", p.getStatus()});
    }

    private void refreshBidTable() { 
        bidTableModel.setRowCount(0);
        for (Bid b : bids) { 
            String prop = b.getProposal().length() > 30 ? b.getProposal().substring(0, 30) + "..." : b.getProposal(); 
            String resumeStatus = (b.getResumeFile() != null) ? "✓ Attached" : "✗ None";
            bidTableModel.addRow(new Object[]{b.getBidId(), b.getProjectId(), b.getFreelancerName(), "$" + b.getAmount(), b.getDays() + " days", resumeStatus, prop, b.getStatus()});
        } 
    }

    private void filterBids(String projectId) { 
        bidTableModel.setRowCount(0);
        for (Bid b : bids) 
            if (b.getProjectId().equals(projectId)) { 
                String prop = b.getProposal().length() > 30 ? b.getProposal().substring(0, 30) + "..." : b.getProposal(); 
                String resumeStatus = (b.getResumeFile() != null) ? "✓ Attached" : "✗ None";
                bidTableModel.addRow(new Object[]{b.getBidId(), b.getProjectId(), b.getFreelancerName(), "$" + b.getAmount(), b.getDays() + " days", resumeStatus, prop, b.getStatus()});
            }
    }

    private boolean validatePostForm() { 
        return !clientNameField.getText().isEmpty() && !titleField.getText().isEmpty();
    }

    private boolean validateBidForm(JTextField f) { 
        return !freelancerNameField.getText().isEmpty() && !f.getText().isEmpty();
    }

    private void clearPostForm() { 
        clientNameField.setText(""); titleField.setText(""); budgetField.setText(""); deadlineField.setText("");
        descriptionArea.setText(""); categoryCombo.setSelectedIndex(0); difficultyCombo.setSelectedIndex(0); 
    }

    private void clearBidForm() { 
        freelancerNameField.setText("");
        bidAmountField.setText(""); 
        bidDeadlineField.setText(""); 
        bidDescriptionArea.setText(""); 
        removeResumeFile();
    }

    private void updateStats() { 
        totalProjectsLabel.setText(String.valueOf(dbManager.getTotalProjects()));
        activeProjectsLabel.setText(String.valueOf(dbManager.getActiveProjects())); 
        completedProjectsLabel.setText(String.valueOf(dbManager.getCompletedProjects())); 
        totalBidsLabel.setText(String.valueOf(dbManager.getTotalBids())); 
        pendingBidsLabel.setText(String.valueOf(dbManager.getPendingBids())); 
        
        int completed = dbManager.getCompletedProjects();
        int total = dbManager.getTotalProjects();
        if (total > 0) successRateLabel.setText(((completed * 100) / total) + "%"); 
        else successRateLabel.setText("0%");
    }

    private GridBagConstraints createGbc(int x, int y) { 
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = x; gbc.gridy = y; 
        gbc.insets = new Insets(8,8,8,8); 
        gbc.anchor = GridBagConstraints.WEST; 
        gbc.fill = GridBagConstraints.HORIZONTAL; 
        return gbc;
    }
    
    private void styleTable(JTable table, Color headerColor) { 
        table.setBackground(bgColor);
        table.setForeground(textColor); 
        table.setGridColor(inputBgColor); 
        table.setRowHeight(30); 
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14)); 
        table.setSelectionBackground(accentColor); 
        table.setSelectionForeground(Color.WHITE); 
        JTableHeader h = table.getTableHeader(); 
        h.setBackground(headerColor); 
        h.setForeground(Color.WHITE);
        h.setFont(new Font("Segoe UI", Font.BOLD, 15)); 
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() { 
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isSelected, boolean hasFocus, int r, int c) { 
                final Component comp = super.getTableCellRendererComponent(t,v,isSelected,hasFocus,r,c); 
                comp.setBackground(r % 2 == 0 ? bgColor : new Color(55, 63, 66)); 
                comp.setForeground(isSelected ? Color.WHITE : textColor); 
                return comp; 
            } 
        });
    }
    
    private JLabel createStyledHeader(String text, Color color) { 
        JLabel l = new JLabel(text, JLabel.CENTER);
        l.setFont(new Font("Segoe UI", Font.BOLD, 28)); 
        l.setForeground(color); 
        l.setBorder(BorderFactory.createEmptyBorder(0,0,20,0)); 
        return l; 
    }
    
    private JLabel createStyledLabel(String text) { 
        JLabel l = new JLabel(text);
        l.setForeground(textColor); 
        l.setFont(new Font("Segoe UI", Font.BOLD, 14)); 
        return l; 
    }
    
    private JTextField createStyledTextField(int columns) { 
        JTextField tf = new JTextField(columns);
        tf.setBackground(inputBgColor); 
        tf.setForeground(textColor); 
        tf.setCaretColor(accentColor); 
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14)); 
        tf.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(80,80,80)), BorderFactory.createEmptyBorder(5,5,5,5))); 
        return tf;
    }
    
    private JComboBox<String> createStyledComboBox(String[] items) { 
        JComboBox<String> cb = new JComboBox<>(items); 
        cb.setBackground(inputBgColor); 
        cb.setForeground(textColor);
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 14)); 
        return cb; 
    }
    
    private JTextArea createStyledTextArea(int rows, int cols) { 
        JTextArea ta = new JTextArea(rows, cols);
        ta.setBackground(inputBgColor); 
        ta.setForeground(textColor); 
        ta.setCaretColor(accentColor); 
        ta.setFont(new Font("Segoe UI", Font.PLAIN, 14)); 
        ta.setLineWrap(true); 
        ta.setWrapStyleWord(true); 
        ta.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(80,80,80)), BorderFactory.createEmptyBorder(5,5,5,5))); 
        return ta;
    }
    
    private JButton createStyledButton(String text, Color color) { 
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14)); 
        b.setBackground(color); 
        b.setForeground(Color.WHITE); 
        b.setFocusPainted(false); 
        b.setCursor(new Cursor(Cursor.HAND_CURSOR)); 
        b.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20)); 
        return b;
    }
    
    private JPanel createStatCard(String title, JLabel valueLabel, Color color, String icon) { 
        JPanel card = new GlassPanel();
        card.setLayout(new BorderLayout()); 
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 5, 0, 0, color), BorderFactory.createEmptyBorder(20, 25, 20, 20))); 
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18)); 
        titleLabel.setForeground(textColor); 
        JLabel iconLabel = new JLabel(icon); 
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40)); 
        iconLabel.setHorizontalAlignment(JLabel.RIGHT);
        JPanel bottomPanel = new JPanel(new BorderLayout()); 
        bottomPanel.setOpaque(false); 
        bottomPanel.add(valueLabel, BorderLayout.WEST); 
        bottomPanel.add(iconLabel, BorderLayout.EAST); 
        card.add(titleLabel, BorderLayout.NORTH); 
        card.add(bottomPanel, BorderLayout.CENTER); 
        return card;
    }
    
    private JLabel createValueLabel(String text, Color color) { 
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 48)); 
        label.setForeground(color); 
        return label; 
    }
    
    private class TexturedGradientPanel extends JPanel {
        private final BufferedImage texture;
        public TexturedGradientPanel() { 
            this.texture = loadImage(); 
        }
        
        private BufferedImage loadImage() { 
            try { 
                return ImageIO.read(getClass().getResource("/background.png"));
            } catch (Exception e) { 
                System.err.println("BG image not found."); 
                return null;
            } 
        }
        
        @Override protected void paintComponent(Graphics g) { 
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create(); 
            g2d.setColor(bgColor); 
            g2d.fillRect(0, 0, getWidth(), getHeight());
            if (texture != null) { 
                for (int y=0; y<getHeight(); y+=texture.getHeight()) 
                    for (int x=0; x<getWidth(); x+=texture.getWidth()) 
                        g2d.drawImage(texture, x, y, this);
            } 
            g2d.dispose(); 
        }
    }
    
    private class GlassPanel extends JPanel {
        public GlassPanel() { 
            setOpaque(false);
        }
        
        @Override protected void paintComponent(Graphics g) { 
            super.paintComponent(g); 
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); 
            g2d.setColor(new Color(44, 62, 80, 150)); 
            g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20); 
            g2d.dispose();
        }
    }
    
    private class NavButton extends JPanel {
        private boolean isHovered = false;
        public NavButton(String text, String cardName) { 
            setLayout(new FlowLayout(FlowLayout.LEFT, 20, 0)); 
            setOpaque(false); 
            setCursor(new Cursor(Cursor.HAND_CURSOR)); 
            JLabel l = new JLabel(text); 
            l.setForeground(textColor);
            l.setFont(new Font("Segoe UI", Font.BOLD, 16)); 
            setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 0)); 
            add(l);
            addMouseListener(new MouseAdapter() { 
                public void mouseClicked(MouseEvent e) { 
                    cardLayout.show(contentPanel, cardName); 
                } 
                public void mouseEntered(MouseEvent e) { 
                    isHovered = true; 
                    repaint(); 
                } 
                public void mouseExited(MouseEvent e) { 
                    isHovered = false; 
                    repaint(); 
                } 
            });
        }
        
        @Override protected void paintComponent(Graphics g) { 
            super.paintComponent(g);
            if (isHovered) { 
                Graphics2D g2d = (Graphics2D) g.create(); 
                g2d.setColor(new Color(52, 73, 94)); 
                g2d.fillRect(0, 0, getWidth(), getHeight()); 
                g2d.dispose();
            } 
        }
    }

    // Data Model Inner Classes
    private static class Project {
        private String projectId, title, clientName, category, difficulty, status, description;
        private double budget; 
        private int deadline;

        public Project(String pId, String t, String cName, String cat, double b, String diff, int dl, String desc, String s) { 
            this.projectId=pId;
            this.title=t; 
            this.clientName=cName; 
            this.category=cat; 
            this.budget=b; 
            this.difficulty=diff; 
            this.deadline=dl; 
            this.description=desc; 
            this.status=s; 
        }
        
        public String getClientName() { return clientName; }
        public String getProjectId() { return projectId; } 
        public String getTitle() { return title; } 
        public String getCategory() { return category; } 
        public double getBudget() { return budget; } 
        public String getDifficulty() { return difficulty; } 
        public int getDeadline() { return deadline; } 
        public String getStatus() { return status; } 
        public String getDescription() { return description; } 
        public void setStatus(String status) { this.status = status; }
    }

    private static class Bid {
        private String bidId, projectId, freelancerName, proposal, status;
        private double amount; 
        private int days;
        private File resumeFile;
        
        public Bid(String bId, String pId, String fName, double a, int d, String p, String s, File resume) { 
            this.bidId=bId;
            this.projectId=pId; 
            this.freelancerName=fName; 
            this.amount=a; 
            this.days=d; 
            this.proposal=p; 
            this.status=s; 
            this.resumeFile=resume; 
        }
        
        public String getFreelancerName() { return freelancerName; }
        public String getBidId() { return bidId; } 
        public String getProjectId() { return projectId; } 
        public double getAmount() { return amount; } 
        public int getDays() { return days; } 
        public String getProposal() { return proposal; } 
        public String getStatus() { return status; } 
        public File getResumeFile() { return resumeFile; }
        public void setStatus(String status) { this.status = status; }
        public void setResumeFile(File resumeFile) { this.resumeFile = resumeFile; }
    }
}