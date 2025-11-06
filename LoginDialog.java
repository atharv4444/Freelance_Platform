import javax.swing.*;
import javax.swing.text.JTextComponent; // ✅ added import
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.regex.Pattern;

public class LoginDialog extends JDialog {
    // UI Colors
    private static final Color DEEP_BLUE = new Color(25, 42, 86);
    private static final Color ROYAL_BLUE = new Color(52, 152, 219);
    private static final Color SUCCESS_GREEN = new Color(46, 204, 113);
    private static final Color DANGER_RED = new Color(231, 76, 60);
    private static final Color INPUT_BG = new Color(52, 73, 94);
    private static final Color INPUT_TEXT = Color.WHITE;
    private static final Color INPUT_PLACEHOLDER = new Color(149, 165, 166);

    private JTextField usernameField, regUsernameField, regEmailField, regFullNameField;
    private JPasswordField passwordField, regPasswordField;
    private boolean authenticated = false;
    private String loggedInUsername = "";

    private CardLayout cardLayout = new CardLayout();
    private JPanel cardPanel;

    private final String DB_URL = "jdbc:sqlite:freelance_platform.db";

    public LoginDialog() {
        setupLoginDialog();
        ensurePasswordColumn();
    }

    private void ensurePasswordColumn() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet rs = meta.getColumns(null, null, "users", "password");
            if (!rs.next()) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("ALTER TABLE users ADD COLUMN password TEXT");
                    System.out.println("✅ Added 'password' column to users table.");
                }
            }
        } catch (SQLException e) {
            System.err.println("⚠️ Error ensuring password column: " + e.getMessage());
        }
    }

    private void setupLoginDialog() {
        setTitle("Welcome");
        setModal(true);
        setSize(420, 580);
        setLocationRelativeTo(null);
        setUndecorated(true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPanel backgroundPanel = new SimpleGradientPanel();
        backgroundPanel.setLayout(new BorderLayout());
        backgroundPanel.setBorder(BorderFactory.createLineBorder(ROYAL_BLUE, 2));

        JPanel headerPanel = createHeaderPanel();
        backgroundPanel.add(headerPanel, BorderLayout.NORTH);

        cardPanel = new JPanel(cardLayout);
        cardPanel.setOpaque(false);

        JPanel loginPanel = createLoginPanel();
        JPanel registerPanel = createRegisterPanel();

        cardPanel.add(loginPanel, "LOGIN");
        cardPanel.add(registerPanel, "REGISTER");

        backgroundPanel.add(cardPanel, BorderLayout.CENTER);
        cardLayout.show(cardPanel, "LOGIN");

        add(backgroundPanel);
        addDragFunctionality(backgroundPanel);
    }

    private class SimpleGradientPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            GradientPaint gradient = new GradientPaint(0, 0, DEEP_BLUE, 0, getHeight(), new Color(44, 62, 80));
            g2d.setPaint(gradient);
            g2d.fillRect(0, 0, getWidth(), getHeight());
            g2d.dispose();
        }
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(0, 10));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JLabel titleLabel = new JLabel("Freelance Platform");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel("Welcome! Please sign in or create an account.");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(255, 255, 255, 180));

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);
        headerPanel.add(createCloseButton(), BorderLayout.EAST);

        return headerPanel;
    }

    private JButton createCloseButton() {
        JButton closeButton = new JButton("X");
        closeButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        closeButton.setForeground(INPUT_PLACEHOLDER);
        closeButton.setOpaque(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setBorderPainted(false);
        closeButton.setFocusPainted(false);
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        closeButton.addActionListener(e -> dispose());
        closeButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { closeButton.setForeground(DANGER_RED); }
            public void mouseExited(MouseEvent e) { closeButton.setForeground(INPUT_PLACEHOLDER); }
        });
        return closeButton;
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 40, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        usernameField = createStyledTextField();
        new TextPrompt("👤 Username", usernameField);
        panel.add(usernameField, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(15, 0, 0, 0);
        passwordField = createStyledPasswordField();
        new TextPrompt("🔒 Password", passwordField);
        panel.add(passwordField, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(25, 0, 0, 0);
        JButton loginBtn = createStyledButton("🚀 Sign In", SUCCESS_GREEN);
        loginBtn.addActionListener(e -> handleLogin());
        panel.add(loginBtn, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(15, 0, 0, 0);
        panel.add(createToggleLink("Don't have an account? Sign Up", "REGISTER"), gbc);

        return panel;
    }

    private JPanel createRegisterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 40, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 0, 0, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        regFullNameField = createStyledTextField();
        new TextPrompt("👤 Full Name", regFullNameField);
        panel.add(regFullNameField, gbc);

        gbc.gridy++;
        regUsernameField = createStyledTextField();
        new TextPrompt("📛 Username", regUsernameField);
        panel.add(regUsernameField, gbc);

        gbc.gridy++;
        regEmailField = createStyledTextField();
        new TextPrompt("✉️ Email", regEmailField);
        panel.add(regEmailField, gbc);

        gbc.gridy++;
        regPasswordField = createStyledPasswordField();
        new TextPrompt("🔑 Password", regPasswordField);
        panel.add(regPasswordField, gbc);

        gbc.gridy++;
        JButton registerBtn = createStyledButton("📝 Register", SUCCESS_GREEN);
        registerBtn.addActionListener(e -> handleRegister());
        panel.add(registerBtn, gbc);

        gbc.gridy++;
        panel.add(createToggleLink("Already have an account? Sign In", "LOGIN"), gbc);

        return panel;
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField(20);
        field.setBackground(INPUT_BG);
        field.setForeground(INPUT_TEXT);
        field.setCaretColor(INPUT_TEXT);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return field;
    }

    private JPasswordField createStyledPasswordField() {
        JPasswordField field = new JPasswordField(20);
        field.setBackground(INPUT_BG);
        field.setForeground(INPUT_TEXT);
        field.setCaretColor(INPUT_TEXT);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return field;
    }

    private JButton createStyledButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JLabel createToggleLink(String text, String card) {
        JLabel link = new JLabel("<html><u>" + text + "</u></html>");
        link.setForeground(ROYAL_BLUE);
        link.setCursor(new Cursor(Cursor.HAND_CURSOR));
        link.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { cardLayout.show(cardPanel, card); }
        });
        return link;
    }

    private void handleRegister() {
        String name = regFullNameField.getText().trim();
        String username = regUsernameField.getText().trim();
        String email = regEmailField.getText().trim();
        String password = new String(regPasswordField.getPassword()).trim();

        if (name.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields!");
            return;
        }

        if (!Pattern.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$", email)) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address!");
            return;
        }

        String sql = "INSERT INTO users (name, email, type, skill, level, status, password) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, email);
            pstmt.setString(3, "user");
            pstmt.setString(4, "N/A");
            pstmt.setString(5, "N/A");
            pstmt.setString(6, "Active");
            pstmt.setString(7, password);
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Account created successfully! Please log in.");
            cardLayout.show(cardPanel, "LOGIN");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Registration failed! " + e.getMessage());
        }
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter both username and password!");
            return;
        }

        String sql = "SELECT * FROM users WHERE name = ? AND password = ? AND status = 'Active'";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                authenticated = true;
                loggedInUsername = username;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials or inactive user!");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Login failed! " + e.getMessage());
        }
    }

    private void addDragFunctionality(JPanel panel) {
        final Point[] dragPoint = {null};
        panel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { dragPoint[0] = e.getPoint(); }
        });
        panel.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                Point current = e.getLocationOnScreen();
                setLocation(current.x - dragPoint[0].x, current.y - dragPoint[0].y);
            }
        });
    }

    public boolean isAuthenticated() { return authenticated; }
    public String getUsername() { return loggedInUsername; }

    // ✅ Embedded TextPrompt class
    private static class TextPrompt extends JLabel implements FocusListener, javax.swing.event.DocumentListener {
        private final JTextComponent component;
        public TextPrompt(String text, JTextComponent comp) {
            super(text);
            this.component = comp;
            setFont(comp.getFont().deriveFont(Font.ITALIC));
            setForeground(new Color(200, 200, 200, 120));
            comp.setLayout(new BorderLayout());
            comp.add(this);
            comp.addFocusListener(this);
            comp.getDocument().addDocumentListener(this);
            checkVisibility();
        }
        private void checkVisibility() { setVisible(component.getText().isEmpty() && !component.hasFocus()); }
        @Override public void focusGained(FocusEvent e) { checkVisibility(); }
        @Override public void focusLost(FocusEvent e) { checkVisibility(); }
        @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { checkVisibility(); }
        @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { checkVisibility(); }
        @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { checkVisibility(); }
    }
}
