import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

public class UserManager {
    private JFrame window;
    private DefaultTableModel tableModel;
    private JTable userTable;
    private JTextField nameField, emailField, skillField;
    private JComboBox<String> typeCombo, levelCombo;
    
    // --- DATABASE INTEGRATION ---
    private DatabaseManager dbManager;

    // --- Modern UI Color Palette ---
    private final Color bgColor = new Color(45, 52, 54);
    private final Color panelColor = new Color(53, 63, 64, 200);
    private final Color textColor = new Color(223, 230, 233);
    private final Color inputBgColor = new Color(99, 110, 114);
    private final Color accentColor = new Color(52, 152, 219);
    private final Color successColor = new Color(46, 204, 113);
    private final Color warningColor = new Color(230, 126, 34);
    private final Color dangerColor = new Color(231, 76, 60);

    public UserManager() {
        dbManager = new DatabaseManager();
        
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) { /* Fallback */ }
        
        setupWindow();
        refreshUserTable();
    }

    public UserManager(DatabaseManager db) {
        this.dbManager = db;
        try { for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels())
            if ("Nimbus".equals(info.getName())) { UIManager.setLookAndFeel(info.getClassName()); break; }
        } catch (Exception ignored) {}
        setupWindow();
        refreshUserTable();
    }

    private void setupWindow() {
        window = new JFrame("User Management System");
        window.setSize(950, 700);
        window.setLocationRelativeTo(null);
        window.setLayout(new BorderLayout(10, 10));
        window.getContentPane().setBackground(bgColor);

        JPanel inputPanel = createInputPanel();
        JScrollPane scrollPane = createTablePanel();
        JPanel buttonPanel = createButtonPanel();

        window.add(inputPanel, BorderLayout.NORTH);
        window.add(scrollPane, BorderLayout.CENTER);
        window.add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void refreshUserTable() {
        tableModel.setRowCount(0);
        List<String[]> users = dbManager.getAllUsers();
        for (String[] user : users) {
            tableModel.addRow(user);
        }
    }

    private void addUser() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String skill = skillField.getText().trim();

        if (name.isEmpty() || email.isEmpty() || (skillField.isEnabled() && skill.isEmpty())) {
            JOptionPane.showMessageDialog(window, "Please fill all required fields!", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            JOptionPane.showMessageDialog(window, "Please enter a valid email address!", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String[] userData = new String[] {
            null, // ID is handled by the database
            name, email,
            (String) typeCombo.getSelectedItem(),
            skillField.isEnabled() ? skill : "N/A",
            levelCombo.isEnabled() ? (String) levelCombo.getSelectedItem() : "N/A",
            "Pending"
        };
        
        if (dbManager.addUser(userData)) {
            refreshUserTable();
            clearFields();
            JOptionPane.showMessageDialog(window, "User registered successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(window, "Failed to add user. Email may already exist.", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void verifyUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) { JOptionPane.showMessageDialog(window, "Please select a user to verify!", "No Selection", JOptionPane.WARNING_MESSAGE); return; }
        
        int userId = Integer.parseInt((String) tableModel.getValueAt(selectedRow, 0));
        String userName = (String) tableModel.getValueAt(selectedRow, 1);
        
        dbManager.updateUserStatus(userId);
        refreshUserTable();
        
        JOptionPane.showMessageDialog(window, "User '" + userName + "' has been verified!", "Verification Complete", JOptionPane.INFORMATION_MESSAGE);
    }

    private void removeUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) { JOptionPane.showMessageDialog(window, "Please select a user to remove!", "No Selection", JOptionPane.WARNING_MESSAGE); return; }
        
        int userId = Integer.parseInt((String) tableModel.getValueAt(selectedRow, 0));
        String userName = (String) tableModel.getValueAt(selectedRow, 1);

        int choice = JOptionPane.showConfirmDialog(window, "Are you sure you want to permanently remove user '" + userName + "'?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            dbManager.deleteUser(userId);
            refreshUserTable();
            JOptionPane.showMessageDialog(window, "User '" + userName + "' has been removed.", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void clearFields() {
        nameField.setText(""); emailField.setText(""); skillField.setText("");
        typeCombo.setSelectedIndex(0); levelCombo.setSelectedIndex(0);
    }
    
    public void showWindow() {
        window.setVisible(true);
    }

    // --- UNCHANGED UI METHODS ---
    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(accentColor, 1), " Register New User ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new Font("Segoe UI", Font.BOLD, 14), textColor), BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        nameField = createStyledTextField(20); emailField = createStyledTextField(20); skillField = createStyledTextField(20);
        typeCombo = createStyledComboBox(new String[]{"Freelancer", "Client"});
        levelCombo = createStyledComboBox(new String[]{"Beginner", "Intermediate", "Advanced", "Expert"});
        typeCombo.addActionListener(e -> { boolean isFreelancer = "Freelancer".equals(typeCombo.getSelectedItem()); skillField.setEnabled(isFreelancer); levelCombo.setEnabled(isFreelancer); if (!isFreelancer) { skillField.setText("N/A"); } else { skillField.setText(""); } });
        gbc.gridx = 0; gbc.gridy = 0; panel.add(createStyledLabel("Name:"), gbc);
        gbc.gridx = 1; panel.add(nameField, gbc);
        gbc.gridx = 2; panel.add(createStyledLabel("Email:"), gbc);
        gbc.gridx = 3; panel.add(emailField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; panel.add(createStyledLabel("Type:"), gbc);
        gbc.gridx = 1; panel.add(typeCombo, gbc);
        gbc.gridx = 2; panel.add(createStyledLabel("Skill:"), gbc);
        gbc.gridx = 3; panel.add(skillField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; panel.add(createStyledLabel("Level:"), gbc);
        gbc.gridx = 1; panel.add(levelCombo, gbc);
        return panel;
    }
    private JScrollPane createTablePanel() {
        String[] columns = {"ID", "Name", "Email", "Type", "Skill", "Level", "Status"};
        tableModel = new DefaultTableModel(columns, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        userTable = new JTable(tableModel);
        styleTable(userTable);
        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.getViewport().setBackground(bgColor);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        return scrollPane;
    }
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        panel.setOpaque(false);
        JButton addBtn = createStyledButton("✓  Add User", successColor);
        JButton verifyBtn = createStyledButton("✨ Verify Selected", accentColor);
        JButton removeBtn = createStyledButton("🗑️ Remove Selected", dangerColor);
        JButton clearBtn = createStyledButton("✗ Clear Fields", warningColor);
        addBtn.addActionListener(e -> addUser());
        verifyBtn.addActionListener(e -> verifyUser());
        removeBtn.addActionListener(e -> removeUser());
        clearBtn.addActionListener(e -> clearFields());
        panel.add(addBtn); panel.add(verifyBtn); panel.add(removeBtn); panel.add(clearBtn);
        return panel;
    }
    private void styleTable(JTable table) {
        table.setBackground(bgColor); table.setForeground(textColor); table.setGridColor(inputBgColor);
        table.setRowHeight(30); table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setSelectionBackground(accentColor); table.setSelectionForeground(Color.WHITE);
        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(25, 42, 86)); header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 15));
        header.setBorder(BorderFactory.createLineBorder(accentColor));
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                final Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                c.setBackground(row % 2 == 0 ? bgColor : new Color(55, 63, 66));
                if (col == 6) {
                    String status = (String) value;
                    if ("Verified".equals(status)) { c.setForeground(successColor); setFont(getFont().deriveFont(Font.BOLD));
                    } else if ("Pending".equals(status)) { c.setForeground(warningColor); setFont(getFont().deriveFont(Font.ITALIC)); }
                } else { c.setForeground(isSelected ? Color.WHITE : textColor); setFont(getFont().deriveFont(Font.PLAIN)); }
                setHorizontalAlignment(JLabel.CENTER);
                return c;
            }
        });
    }
    private JLabel createStyledLabel(String text) { JLabel l = new JLabel(text); l.setForeground(textColor); l.setFont(new Font("Segoe UI", Font.BOLD, 14)); return l; }
    private JTextField createStyledTextField(int columns) { JTextField tf = new JTextField(columns); tf.setBackground(inputBgColor); tf.setForeground(textColor); tf.setCaretColor(accentColor); tf.setFont(new Font("Segoe UI", Font.PLAIN, 14)); tf.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(80, 80, 80)), BorderFactory.createEmptyBorder(5, 5, 5, 5))); return tf; }
    private JComboBox<String> createStyledComboBox(String[] items) { JComboBox<String> cb = new JComboBox<>(items); cb.setBackground(inputBgColor); cb.setForeground(textColor); cb.setFont(new Font("Segoe UI", Font.PLAIN, 14)); return cb; }
    private JButton createStyledButton(String text, Color color) { JButton b = new JButton(text); b.setFont(new Font("Segoe UI", Font.BOLD, 14)); b.setBackground(color); b.setForeground(Color.WHITE); b.setFocusPainted(false); b.setCursor(new Cursor(Cursor.HAND_CURSOR)); b.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20)); return b; }
}