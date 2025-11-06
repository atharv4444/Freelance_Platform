import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.List;

public class Main extends JFrame {

    // --- UI Color Constants ---
    private static final Color DEEP_BLUE = new Color(25, 42, 86);
    private static final Color ROYAL_BLUE = new Color(52, 152, 219);
    private static final Color SUCCESS_GREEN = new Color(46, 204, 113);
    private static final Color WARNING_ORANGE = new Color(230, 126, 34);
    private static final Color PURPLE_ACCENT = new Color(155, 89, 182);
    
    // Backend Managers - INJECTED DEPENDENCIES WITH NOTIFICATION MANAGER
    private final DatabaseManager db = new DatabaseManager();
    private final NotificationManager notificationManager = new NotificationManager(db);
   
    private final ProjectManager projectManager = new ProjectManager(db, notificationManager);
    private final PaymentManager paymentManager = new PaymentManager(db, notificationManager);
    
    public Main() {
        LoginDialog loginDialog = new LoginDialog();
        loginDialog.setVisible(true);
        if(loginDialog.isAuthenticated()){
            String username = loginDialog.getUsername();
            setupMainWindow(username);
        }else{
            System.exit(0);
        }
    }

    private void setupMainWindow(String username) {
        setTitle("Freelance Fair-Wage Platform");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        
        // Header Panel with Texture and Dark Gradient
        CustomGradientPanel headerPanel = new CustomGradientPanel(new Color(25, 42, 86, 220), new Color(52, 152, 219, 160), CustomGradientPanel.GradientType.DIAGONAL);
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));
        
        JLabel titleLabel = new JLabel("Welcome back, " + username + "!");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        
        JLabel subtitleLabel = new JLabel(getTimeBasedGreeting() + " Ready to manage your freelance projects?");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitleLabel.setForeground(new Color(255, 255, 255, 220));
        subtitleLabel.setHorizontalAlignment(JLabel.CENTER);
        
        JPanel headerContent = new JPanel(new BorderLayout());
        headerContent.setOpaque(false);
        headerContent.add(titleLabel, BorderLayout.CENTER);
        headerContent.add(subtitleLabel, BorderLayout.SOUTH);
        headerPanel.add(headerContent, BorderLayout.CENTER);
        
        // Main Content Panel with Texture and Dark Gradient
        CustomGradientPanel mainPanel = new CustomGradientPanel(new Color(44, 62, 80, 150), new Color(52, 73, 94, 180), CustomGradientPanel.GradientType.VERTICAL);
        mainPanel.setLayout(new GridLayout(2, 2, 40, 40));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(40, 60, 40, 60));

        // Module Buttons - REPLACED User Management with My Notifications
        ModuleButton notificationsBtn = new ModuleButton("My Notifications", "Updates & Platform Activity", ROYAL_BLUE, "🔔");
        ModuleButton wageCalcBtn = new ModuleButton("Fair Wage Calculator", "Calculate Transparent Rates", SUCCESS_GREEN, "💰");
        ModuleButton projectMgmtBtn = new ModuleButton("Project Management", "Post & Manage Projects", WARNING_ORANGE, "📋");
        ModuleButton paymentsBtn = new ModuleButton("Payments & Escrow", "Milestone & Secure Payments", PURPLE_ACCENT, "🔒");

        // Action Listeners
        notificationsBtn.addActionListener(e -> showNotifications());
        
        
        projectMgmtBtn.addActionListener(e -> {
            RoleSelectionDialog dialog = new RoleSelectionDialog(Main.this);
            dialog.setVisible(true);
            ProjectManager.UserRole selectedRole = dialog.getSelectedRole();
            if (selectedRole != null) {
                projectManager.showWindow(selectedRole);
            }
        });
        
        paymentsBtn.addActionListener(e -> paymentManager.showWindow());

        mainPanel.add(notificationsBtn);
        mainPanel.add(wageCalcBtn);
        mainPanel.add(projectMgmtBtn);
        mainPanel.add(paymentsBtn);
        
        add(headerPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
    }

    // Show Notifications & Activity Feed
    private void showNotifications() {
        JDialog notificationDialog = new JDialog(this, "My Notifications & Activity Feed", true);
        notificationDialog.setSize(900, 650);
        notificationDialog.setLocationRelativeTo(this);
        
        // Main panel with gradient background
        CustomGradientPanel backgroundPanel = new CustomGradientPanel(
            new Color(44, 62, 80), 
            new Color(52, 73, 94), 
            CustomGradientPanel.GradientType.VERTICAL
        );
        backgroundPanel.setLayout(new BorderLayout());
        backgroundPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Header with unread count from backend
        JLabel headerLabel = new JLabel("🔔 Notifications & Activity Feed (" + notificationManager.getUnreadCount() + " unread)");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        backgroundPanel.add(headerLabel, BorderLayout.NORTH);
        
        // Notifications list panel
        JPanel notificationsPanel = new JPanel();
        notificationsPanel.setLayout(new BoxLayout(notificationsPanel, BoxLayout.Y_AXIS));
        notificationsPanel.setOpaque(false);
        
        // GET ALL NOTIFICATIONS FROM BACKEND
        List<Notification> notificationsList = notificationManager.getAllNotifications();
        
        // Render notifications from backend
        if (notificationsList.isEmpty()) {
            JLabel emptyLabel = new JLabel("No notifications yet. Check back soon!");
            emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            emptyLabel.setForeground(new Color(148, 163, 184));
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            notificationsPanel.add(Box.createVerticalStrut(50));
            notificationsPanel.add(emptyLabel);
        } else {
            for (Notification notif : notificationsList) {
                addNotificationCard(notificationsPanel, 
                    notif.getTitle(), 
                    notif.getMessage(), 
                    notif.getTimestamp(),
                    getColorForType(notif.getType())
                );
            }
        }
        
        // Scroll pane for notifications
        JScrollPane scrollPane = new JScrollPane(notificationsPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        backgroundPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Footer panel with action buttons
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        footerPanel.setOpaque(false);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        JButton clearAllBtn = new JButton("🗑️ Clear All");
        clearAllBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        clearAllBtn.setBackground(WARNING_ORANGE);
        clearAllBtn.setForeground(Color.WHITE);
        clearAllBtn.setFocusPainted(false);
        clearAllBtn.setBorderPainted(false);
        clearAllBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        clearAllBtn.addActionListener(e -> {
            notificationManager.clearAllNotifications();
            notificationDialog.dispose();
            JOptionPane.showMessageDialog(this, "All notifications cleared!", "Success", JOptionPane.INFORMATION_MESSAGE);
        });
        
        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        closeBtn.setBackground(ROYAL_BLUE);
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFocusPainted(false);
        closeBtn.setBorderPainted(false);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> notificationDialog.dispose());
        
        footerPanel.add(clearAllBtn);
        footerPanel.add(closeBtn);
        
        backgroundPanel.add(footerPanel, BorderLayout.SOUTH);
        
        notificationDialog.add(backgroundPanel);
        notificationDialog.setVisible(true);
    }
    
    // Helper method to add notification cards to the panel
    private void addNotificationCard(JPanel parent, String title, String message, String time, Color accentColor) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Card background
                g2d.setColor(new Color(71, 85, 105, 180));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                
                // Accent bar on left
                g2d.setColor(accentColor);
                g2d.fillRoundRect(0, 0, 5, getHeight(), 12, 12);
                
                g2d.dispose();
            }
        };
        
        card.setLayout(new BorderLayout(15, 5));
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        
        // Content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        
        // Title
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Message
        JLabel messageLabel = new JLabel("<html><body style='width: 600px'>" + message + "</body></html>");
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        messageLabel.setForeground(new Color(203, 213, 225));
        messageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Time
        JLabel timeLabel = new JLabel(time);
        timeLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        timeLabel.setForeground(new Color(148, 163, 184));
        timeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(5));
        contentPanel.add(messageLabel);
        contentPanel.add(Box.createVerticalStrut(5));
        contentPanel.add(timeLabel);
        
        card.add(contentPanel, BorderLayout.CENTER);
        
        parent.add(card);
        parent.add(Box.createVerticalStrut(10));
    }

    // Get color based on notification type
    private Color getColorForType(String type) {
        switch (type) {
            case "PROJECT": return SUCCESS_GREEN;
            case "MESSAGE": return ROYAL_BLUE;
            case "PAYMENT": return SUCCESS_GREEN;
            case "REVIEW": return WARNING_ORANGE;
            case "MILESTONE": return PURPLE_ACCENT;
            case "SYSTEM": return new Color(100, 116, 139);
            default: return ROYAL_BLUE;
        }
    }

    private String getTimeBasedGreeting() {
        int hour = java.time.LocalTime.now().getHour();
        if (hour < 12) return "Good morning! ☀️";
        else if (hour < 17) return "Good afternoon! 🌤️";
        else return "Good evening! 🌙";
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }

    // =================================================================================
    // INNER CLASSES
    // =================================================================================

    private static class RoleSelectionDialog extends JDialog {
        private ProjectManager.UserRole selectedRole = null;
        
        public RoleSelectionDialog(JFrame parent) {
            super(parent, "Select Your Role", true);
            setSize(600, 350);
            setLocationRelativeTo(parent);
            setUndecorated(true);
            getRootPane().setBorder(BorderFactory.createLineBorder(ROYAL_BLUE, 2));
            
            JPanel backgroundPanel = new JPanel(new BorderLayout());
            backgroundPanel.setBackground(new Color(44, 62, 80));
            backgroundPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            
            JLabel titleLabel = new JLabel("Continue as a...", JLabel.CENTER);
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
            titleLabel.setForeground(Color.WHITE);
            titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 30, 10));
            backgroundPanel.add(titleLabel, BorderLayout.NORTH);
            
            JPanel cardPanel = new JPanel(new GridLayout(1, 2, 20, 20));
            cardPanel.setOpaque(false);
            
            RoleCard clientCard = new RoleCard("🏢", "Client", "Post projects and manage bids.", ProjectManager.UserRole.CLIENT, this);
            RoleCard freelancerCard = new RoleCard("💻", "Freelancer", "Browse projects and place bids.", ProjectManager.UserRole.FREELANCER, this);
            
            cardPanel.add(clientCard);
            cardPanel.add(freelancerCard);
            backgroundPanel.add(cardPanel, BorderLayout.CENTER);
            add(backgroundPanel);
        }
        
        public void setSelectedRole(ProjectManager.UserRole role) { 
            this.selectedRole = role;
        }
        
        public ProjectManager.UserRole getSelectedRole() { 
            return selectedRole;
        }
    }

    private static class RoleCard extends JPanel {
        private boolean isHovered = false;
        private final Color baseColor = new Color(52, 73, 94);
        private final Color hoverColor = new Color(82, 103, 124);
        private final String icon, title, description;
        
        public RoleCard(String icon, String title, String description, ProjectManager.UserRole role, RoleSelectionDialog parentDialog) {
            this.icon = icon;
            this.title = title; 
            this.description = description;
            setOpaque(false); 
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { isHovered = true; repaint(); }
                public void mouseExited(MouseEvent e) { isHovered = false; repaint(); }
                public void mouseClicked(MouseEvent e) { parentDialog.setSelectedRole(role); parentDialog.dispose(); }
            });
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(isHovered ? hoverColor : baseColor);
            g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            
            if(isHovered){
                g2d.setColor(ROYAL_BLUE);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 15, 15);
            }
            
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
            FontMetrics iconMetrics = g2d.getFontMetrics();
            g2d.drawString(this.icon, getWidth()/2 - iconMetrics.stringWidth(this.icon)/2, 70);
            
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 22));
            FontMetrics titleMetrics = g2d.getFontMetrics();
            g2d.drawString(this.title, getWidth()/2 - titleMetrics.stringWidth(this.title)/2, 120);
            
            g2d.setColor(new Color(200, 200, 200));
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            FontMetrics descMetrics = g2d.getFontMetrics();
            g2d.drawString(this.description, getWidth()/2 - descMetrics.stringWidth(this.description)/2, 150);
            
            g2d.dispose();
        }
    }
    
    private static class CustomGradientPanel extends JPanel {
        public enum GradientType { VERTICAL, DIAGONAL }
        private final Color startColor;
        private final Color endColor;
        private final GradientType type;
        private static final BufferedImage texture = loadImage();
        
        public CustomGradientPanel(Color startColor, Color endColor, GradientType type) {
            this.startColor = startColor;
            this.endColor = endColor; 
            this.type = type;
        }
        
        private static BufferedImage loadImage() {
            try { 
                return ImageIO.read(CustomGradientPanel.class.getResource("/background.png"));
            } catch (Exception e) { 
                System.err.println("Background image not found.");
                return null; 
            }
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            
            if (texture != null) {
                for (int y = 0; y < getHeight(); y += texture.getHeight()) {
                    for (int x = 0; x < getWidth(); x += texture.getWidth()) {
                        g2d.drawImage(texture, x, y, this);
                    }
                }
            } else { 
                g2d.setColor(new Color(34, 47, 62));
                g2d.fillRect(0, 0, getWidth(), getHeight()); 
            }
            
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            GradientPaint gradient = (type == GradientType.DIAGONAL) ? 
                new GradientPaint(0, 0, startColor, getWidth(), getHeight(), endColor) : 
                new GradientPaint(0, 0, startColor, 0, getHeight(), endColor);
            g2d.setPaint(gradient);
            g2d.fillRect(0, 0, getWidth(), getHeight());
            g2d.dispose();
        }
    }

    private static class ModuleButton extends JPanel {
        private final String title, subtitle, emoji;
        private final Color accentColor;
        private boolean isHovered = false;
        private final Color baseColor = new Color(44, 62, 80, 200);
        private final Color hoverColor = new Color(52, 152, 219, 220);
        private final Color shadowColor = new Color(0, 0, 0, 70);
        private final int shadowOffset = 5, arc = 20;
        
        public ModuleButton(String title, String sub, Color accent, String emoji) {
            this.title = title;
            this.subtitle = sub; 
            this.accentColor = accent; 
            this.emoji = emoji;
            setOpaque(false); 
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { isHovered = true; repaint(); }
                public void mouseExited(MouseEvent e) { isHovered = false; repaint(); }
            });
        }
        
        public void addActionListener(ActionListener listener) {
            addMouseListener(new MouseAdapter() { 
                public void mouseClicked(MouseEvent e) { 
                    listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null)); 
                } 
            });
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int w = getWidth(), h = getHeight();
            g2d.setColor(shadowColor);
            g2d.fillRoundRect(shadowOffset, shadowOffset, w - shadowOffset, h - shadowOffset, arc, arc);
            g2d.setColor(isHovered ? hoverColor : baseColor);
            g2d.fillRoundRect(0, 0, w - shadowOffset, h - shadowOffset, arc, arc);
            g2d.setColor(accentColor); 
            g2d.fillRoundRect(0, 0, 8, h - shadowOffset, arc, arc);
            g2d.fillRect(4, 0, 4, h - shadowOffset);
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 36)); 
            g2d.drawString(emoji, 30, 65);
            g2d.setColor(Color.WHITE); 
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 18));
            g2d.drawString(title, 30, 105);
            g2d.setColor(new Color(189, 227, 255)); 
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 13)); 
            g2d.drawString(subtitle, 30, 130);
            g2d.dispose();
        }
    }
}