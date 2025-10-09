// File: Main.java
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;


public class Main extends JFrame {


    // --- UI Color Constants (Restored for Gradient UI) ---
    private static final Color DEEP_BLUE = new Color(25, 42, 86);
    private static final Color ROYAL_BLUE = new Color(52, 152, 219);
    private static final Color LIGHT_BLUE = new Color(133, 193, 233);
    private static final Color SKY_BLUE = new Color(236, 240, 241);
    private static final Color SUCCESS_GREEN = new Color(46, 204, 113);
    private static final Color WARNING_ORANGE = new Color(230, 126, 34);
    private static final Color PURPLE_ACCENT = new Color(155, 89, 182);


    private UserManager userManager;
    private WageCalculator wageCalculator;


    public Main() {
        userManager = new UserManager();
        wageCalculator = new WageCalculator();
        setupMainWindow();
    }


    private void setupMainWindow() {
        setTitle("Freelance Fair-Wage Platform");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
        setResizable(false);


        // --- Header Panel ---
        CustomGradientPanel headerPanel = new CustomGradientPanel(DEEP_BLUE, ROYAL_BLUE, CustomGradientPanel.GradientType.DIAGONAL);
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


        // --- Main Content Panel ---
        CustomGradientPanel mainPanel = new CustomGradientPanel(LIGHT_BLUE, SKY_BLUE, CustomGradientPanel.GradientType.VERTICAL);
        mainPanel.setLayout(new GridLayout(2, 2, 40, 40));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(40, 60, 40, 60));


        // --- Module Buttons ---
        ModuleButton userMgmtBtn = new ModuleButton("User Management", "Register & Verify Users", ROYAL_BLUE, "👥");
        ModuleButton wageCalcBtn = new ModuleButton("Fair Wage Calculator", "Calculate Transparent Rates", SUCCESS_GREEN, "💰");
        ModuleButton projectMgmtBtn = new ModuleButton("Project Management", "Coming Soon", WARNING_ORANGE, "📋");
        ModuleButton paymentsBtn = new ModuleButton("Payments & Escrow", "Coming Soon", PURPLE_ACCENT, "🔒");


        // Action Listeners
        userMgmtBtn.addActionListener(e -> userManager.showWindow());
        wageCalcBtn.addActionListener(e -> wageCalculator.showWindow());
        projectMgmtBtn.addActionListener(e -> showComingSoon("Project Management"));
        paymentsBtn.addActionListener(e -> showComingSoon("Payments & Escrow"));


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
                "✓ Fair Wage Calculator</p>" +
                "</div></div></html>";


        JOptionPane.showMessageDialog(this, message, "Coming Soon - " + module, JOptionPane.INFORMATION_MESSAGE);
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }


    // =================================================================================
    // INNER CLASSES
    // =================================================================================


    /**
     * Corrected version that only draws gradients and does NOT load an image.
     */
    private static class CustomGradientPanel extends JPanel {
        public enum GradientType {
            VERTICAL, DIAGONAL
        }


        private final Color startColor;
        private final Color endColor;
        private final GradientType type;


        public CustomGradientPanel(Color startColor, Color endColor, GradientType type) {
            this.startColor = startColor;
            this.endColor = endColor;
            this.type = type;
            setOpaque(false);
        }


        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);


            GradientPaint gradient;
            if (type == GradientType.DIAGONAL) {
                gradient = new GradientPaint(0, 0, startColor, getWidth(), getHeight(), endColor);
            } else {
                gradient = new GradientPaint(0, 0, startColor, 0, getHeight(), endColor);
            }


            g2d.setPaint(gradient);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    }


    /**
     * Custom button with colors corrected for the gradient background.
     */
    private static class ModuleButton extends JPanel {
        private final String title, subtitle, emoji;
        private final Color accentColor;
        private boolean isHovered = false;
        private final Color baseColor = new Color(25, 42, 86); // Solid base color
        private final Color hoverColor = new Color(41, 62, 112); // Solid hover color
        private final Color shadowColor = new Color(0, 0, 0, 50);
        private final int shadowOffset = 5, arc = 20;


        public ModuleButton(String title, String sub, Color accent, String emoji) {
            this.title = title; this.subtitle = sub; this.accentColor = accent; this.emoji = emoji;
            setOpaque(false); setCursor(new Cursor(Cursor.HAND_CURSOR));
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
