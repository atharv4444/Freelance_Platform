import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.IOException;
public class Main extends JFrame {

    // --- UI Color Constants ---
    private static final Color DEEP_BLUE = new Color(25, 42, 86);
private static final Color ROYAL_BLUE = new Color(52, 152, 219);
    private static final Color SUCCESS_GREEN = new Color(46, 204, 113);
private static final Color WARNING_ORANGE = new Color(230, 126, 34);
    private static final Color PURPLE_ACCENT = new Color(155, 89, 182);
// Ensure all dependencies are FINAL and injected with the single DatabaseManager instance
    private final DatabaseManager db = new DatabaseManager();
private final UserManager userManager = new UserManager(db);
    private final WageCalculator wageCalculator = new WageCalculator();
private final ProjectManager projectManager = new ProjectManager(db);
    private final PaymentManager paymentManager = new PaymentManager(db);
public Main() {
        setupMainWindow();
}

    private void setupMainWindow() {
        setTitle("Freelance Fair-Wage Platform");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
// Header Panel with Texture and Dark Gradient
        CustomGradientPanel headerPanel = new CustomGradientPanel(new Color(25, 42, 86, 220), new Color(52, 152, 219, 160), CustomGradientPanel.GradientType.DIAGONAL);
headerPanel.setLayout(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));
        JLabel titleLabel = new JLabel("Freelance Fair-Wage Platform");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
JLabel subtitleLabel = new JLabel("Transparent & Fair Freelancing Solutions");
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

        // Module Buttons
        ModuleButton userMgmtBtn = new ModuleButton("User Management", "Register & Verify Users", ROYAL_BLUE, "👥");
ModuleButton wageCalcBtn = new ModuleButton("Fair Wage Calculator", "Calculate Transparent Rates", SUCCESS_GREEN, "💰");
ModuleButton projectMgmtBtn = new ModuleButton("Project Management", "Post & Manage Projects", WARNING_ORANGE, "📋");
ModuleButton paymentsBtn = new ModuleButton("Payments & Escrow", "Milestone & Secure Payments", PURPLE_ACCENT, "🔒");
// ✅ CORRECTED SUBTITLE

        // Action Listeners
        userMgmtBtn.addActionListener(e -> userManager.showWindow());
wageCalcBtn.addActionListener(e -> wageCalculator.showWindow());
        
        projectMgmtBtn.addActionListener(e -> {
            RoleSelectionDialog dialog = new RoleSelectionDialog(Main.this);
            dialog.setVisible(true);
            ProjectManager.UserRole selectedRole = dialog.getSelectedRole();
            if (selectedRole != null) {
                projectManager.showWindow(selectedRole);
            }
        
});
        
        paymentsBtn.addActionListener(e -> paymentManager.showWindow());  // ✅ CORRECTED ACTION

        mainPanel.add(userMgmtBtn);
        mainPanel.add(wageCalcBtn);
        mainPanel.add(projectMgmtBtn);
        mainPanel.add(paymentsBtn);
add(headerPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
    }

    private void showComingSoon(String module) {
        String message = "<html><div style='text-align:center; padding:10px;'>" +
                "<h2 style='color:#2c3e50; margin-bottom:15px;'>" + module + "</h2>" +
                "<p style='color:#7f8c8d; font-size:14px; margin-bottom:20px;'>" +
                "This module will be implemented in the next phase!</p>" +
      
           "<div style='background-color:#ecf0f1; padding:15px; border-radius:5px;'>" +
                "<p style='color:#2c3e50; font-size:13px; margin:0;'><b>Current Demo Includes:</b></p>" +
                "<p style='color:#27ae60; font-size:13px; margin:5px 0 0 0;'>" +
                "✓ User Management System<br>" +
                "✓ Fair Wage Calculator<br>" +
 
               "✓ Project Management System<br>" +
                "✓ Payment & Escrow System</p>" +
                "</div></div></html>";
JOptionPane.showMessageDialog(this, message, "Coming Soon - " + module, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
}

    // =================================================================================
    // INNER CLASSES - Full definitions are now included
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
        public void setSelectedRole(ProjectManager.UserRole role) { this.selectedRole = role;
}
        public ProjectManager.UserRole getSelectedRole() { return selectedRole;
}
    }

    private static class RoleCard extends JPanel {
        private boolean isHovered = false;
private final Color baseColor = new Color(52, 73, 94);
        private final Color hoverColor = new Color(82, 103, 124);
private final String icon, title, description;
        public RoleCard(String icon, String title, String description, ProjectManager.UserRole role, RoleSelectionDialog parentDialog) {
            this.icon = icon;
this.title = title; this.description = description;
            setOpaque(false); setCursor(new Cursor(Cursor.HAND_CURSOR));
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
this.endColor = endColor; this.type = type;
        }
        private static BufferedImage loadImage() {
            try { return ImageIO.read(CustomGradientPanel.class.getResource("/background.png"));
}
            catch (Exception e) { System.err.println("Background image not found.");
return null; }
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
            } else { g2d.setColor(new Color(34, 47, 62));
g2d.fillRect(0, 0, getWidth(), getHeight()); }
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
GradientPaint gradient = (type == GradientType.DIAGONAL) ? new GradientPaint(0, 0, startColor, getWidth(), getHeight(), endColor) : new GradientPaint(0, 0, startColor, 0, getHeight(), endColor);
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
this.subtitle = sub; this.accentColor = accent; this.emoji = emoji;
            setOpaque(false); setCursor(new Cursor(Cursor.HAND_CURSOR));
addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { isHovered = true; repaint(); }
                public void mouseExited(MouseEvent e) { isHovered = false; repaint(); }
            });
}
        public void addActionListener(ActionListener listener) {
            addMouseListener(new MouseAdapter() { public void mouseClicked(MouseEvent e) { listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null)); } });
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
            g2d.setColor(accentColor); g2d.fillRoundRect(0, 0, 8, h - shadowOffset, arc, arc);
g2d.fillRect(4, 0, 4, h - shadowOffset);
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 36)); g2d.drawString(emoji, 30, 65);
            g2d.setColor(Color.WHITE); g2d.setFont(new Font("Segoe UI", Font.BOLD, 18));
g2d.drawString(title, 30, 105);
            g2d.setColor(new Color(189, 227, 255)); g2d.setFont(new Font("Segoe UI", Font.PLAIN, 13)); g2d.drawString(subtitle, 30, 130);
            g2d.dispose();
}
    }
}