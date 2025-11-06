import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

public class Main extends JFrame {

    // --- UI Color Constants ---
    private static final Color DEEP_BLUE = new Color(25, 42, 86);
    private static final Color ROYAL_BLUE = new Color(52, 152, 219);
    private static final Color SUCCESS_GREEN = new Color(46, 204, 113);
    private static final Color WARNING_ORANGE = new Color(230, 126, 34);
    private static final Color PURPLE_ACCENT = new Color(155, 89, 182);
    private static final Color SLATE_700 = new Color(44, 62, 80);
    private static final Color SLATE_600 = new Color(52, 73, 94);
    private static final Color SLATE_500 = new Color(71, 85, 105);
    private static final Color SLATE_400 = new Color(148, 163, 184);
    private static final Color SLATE_300 = new Color(203, 213, 225);

    // Backend Managers
    private final DatabaseManager db = new DatabaseManager();
    private final ProjectManager projectManager = new ProjectManager(db);
    private final WageCalculator wageCalculator = new WageCalculator();
    private final PaymentManager paymentManager = new PaymentManager(db);
    
    // --- Fake Freelancer Database ---
    private final List<Freelancer> allFreelancers = new ArrayList<>();

    public Main() {
        initializeFreelancerData();
        
        LoginDialog loginDialog = new LoginDialog();
        loginDialog.setVisible(true);
        if(loginDialog.isAuthenticated()){
            String username = loginDialog.getUsername();
            setupMainWindow(username);
        } else {
            System.exit(0);
        }
    }

    private void initializeFreelancerData() {
        allFreelancers.add(new Freelancer("Alice Smith", "Java Backend Developer", 4.9, "alice.smith@email.com", "+123456789"));
        allFreelancers.add(new Freelancer("Bob Johnson", "React UI/UX Designer", 4.8, "bob.johnson@email.com", "+198765432"));
        allFreelancers.add(new Freelancer("Charlie Lee", "Python Data Scientist", 5.0, "charlie.lee@email.com", "+112233445"));
        allFreelancers.add(new Freelancer("Diana Ray", "Mobile App (Flutter)", 4.7, "diana.ray@email.com", "+155566677"));
        allFreelancers.add(new Freelancer("Evan Guo", "DevOps & AWS", 4.9, "evan.guo@email.com", "+144433322"));
        allFreelancers.add(new Freelancer("Fiona Chen", "Java Swing Specialist", 5.0, "fiona.chen@email.com", "+177788899"));
        allFreelancers.add(new Freelancer("George Hill", "Node.js Developer", 4.6, "george.hill@email.com", "+165432198"));
        allFreelancers.add(new Freelancer("Hannah Kim", "Technical Copywriter", 4.8, "hannah.kim@email.com", "+122233344"));
        allFreelancers.add(new Freelancer("Ivan Petrov", "C# .NET Developer", 4.4, "ivan.petrov@email.com", "+199988877"));
        allFreelancers.add(new Freelancer("Julia B.", "Agile Project Manager", 5.0, "julia.b@email.com", "+177766655"));
        allFreelancers.add(new Freelancer("Kevin Malone", "Database Administrator", 4.5, "kevin.malone@email.com", "+11122233"));
        allFreelancers.add(new Freelancer("Laura Wang", "UI/UX Designer (Figma)", 4.9, "laura.wang@email.com", "+133344455"));
    }

    private void setupMainWindow(String username) {
        setTitle("Freelance Fair-Wage Platform");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        
        // Header Panel
        CustomGradientPanel headerPanel = new CustomGradientPanel(
            new Color(25, 42, 86, 220),
            new Color(52, 152, 219, 160),
            CustomGradientPanel.GradientType.DIAGONAL
        );
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
        
        // Main Grid
        CustomGradientPanel mainPanel = new CustomGradientPanel(
            new Color(44, 62, 80, 150),
            new Color(52, 73, 94, 180),
            CustomGradientPanel.GradientType.VERTICAL
        );
        mainPanel.setLayout(new GridLayout(2, 2, 40, 40));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(40, 60, 40, 60));

        ModuleButton freelancersBtn = new ModuleButton("Top Freelancers", "Browse high-rated professionals", ROYAL_BLUE, "⭐");
        ModuleButton wageCalcBtn = new ModuleButton("Fair Wage Calculator", "Calculate Transparent Rates", SUCCESS_GREEN, "💰");
        ModuleButton projectMgmtBtn = new ModuleButton("Project Management", "Post & Manage Projects", WARNING_ORANGE, "📋");
        ModuleButton paymentsBtn = new ModuleButton("Payments & Escrow", "Milestone & Secure Payments", PURPLE_ACCENT, "🔒");

        freelancersBtn.addActionListener(e -> showFreelancerSearch());
        wageCalcBtn.addActionListener(e -> wageCalculator.showWindow());
        
        projectMgmtBtn.addActionListener(e -> {
            RoleSelectionDialog dialog = new RoleSelectionDialog(Main.this);
            dialog.setVisible(true);
            ProjectManager.UserRole selectedRole = dialog.getSelectedRole();
            if (selectedRole != null) {
                projectManager.showWindow(selectedRole);
            }
        });
        
        paymentsBtn.addActionListener(e -> paymentManager.showWindow());
        
        mainPanel.add(freelancersBtn);
        mainPanel.add(wageCalcBtn);
        mainPanel.add(projectMgmtBtn);
        mainPanel.add(paymentsBtn);
        
        add(headerPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
    }
        // =============================================================
    // 🧠  TOP FREELANCERS SEARCH & DISPLAY
    // =============================================================
    private void showFreelancerSearch() {
        JDialog searchDialog = new JDialog(this, "Find Top Freelancers", true);
        searchDialog.setSize(1300, 850);
        searchDialog.setMinimumSize(new Dimension(1200, 750));
        searchDialog.setResizable(true);

        searchDialog.setLocationRelativeTo(this);

        CustomGradientPanel backgroundPanel = new CustomGradientPanel(
            SLATE_700, SLATE_600, CustomGradientPanel.GradientType.VERTICAL
        );
        backgroundPanel.setLayout(new BorderLayout());
        backgroundPanel.setBorder(BorderFactory.createEmptyBorder(40, 60, 40, 60));

        // --- Top Panel: Search + Sort ---
        JPanel topPanel = new JPanel(new GridBagLayout());
        topPanel.setOpaque(false);
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 5, 0, 5);

        PlaceholderTextField searchField = new PlaceholderTextField("Search by name or skill (e.g., 'Java', 'Alice')");
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setPreferredSize(new Dimension(500, 40));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(SLATE_400),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        topPanel.add(searchField, gbc);

        JLabel sortLabel = new JLabel("Sort by:");
        sortLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sortLabel.setForeground(Color.WHITE);
        gbc.gridx = 1; gbc.weightx = 0;
        topPanel.add(sortLabel, gbc);
        
        JComboBox<String> sortComboBox = new JComboBox<>(new String[]{
            "Default", "Rating (High to Low)", "Name (A-Z)"
        });
        sortComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sortComboBox.setPreferredSize(new Dimension(180, 35));
        gbc.gridx = 2;
        topPanel.add(sortComboBox, gbc);

        backgroundPanel.add(topPanel, BorderLayout.NORTH);

        // --- Results Scroll Panel ---
        JPanel resultsPanel = new JPanel();
        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
        resultsPanel.setOpaque(false);
        resultsPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        JScrollPane scrollPane = new JScrollPane(resultsPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        backgroundPanel.add(scrollPane, BorderLayout.CENTER);

        // --- Footer ---
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        footerPanel.setOpaque(false);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        closeBtn.setBackground(WARNING_ORANGE);
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFocusPainted(false);
        closeBtn.setBorderPainted(false);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.setPreferredSize(new Dimension(100, 40));
        closeBtn.addActionListener(e -> searchDialog.dispose());
        footerPanel.add(closeBtn);
        backgroundPanel.add(footerPanel, BorderLayout.SOUTH);

        // --- Search & Sort Updates ---
        Runnable updateAction = () -> {
            String searchTerm = searchField.getText();
            String sortOrder = (String) sortComboBox.getSelectedItem();
            updateFreelancerList(resultsPanel, searchTerm, sortOrder);
        };

        DocumentListener searchListener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateAction.run(); }
            public void removeUpdate(DocumentEvent e) { updateAction.run(); }
            public void changedUpdate(DocumentEvent e) { updateAction.run(); }
        };
        searchField.getDocument().addDocumentListener(searchListener);
        sortComboBox.addActionListener(e -> updateAction.run());

        updateAction.run();

        searchDialog.add(backgroundPanel);
        searchDialog.setVisible(true);
    }

    private void updateFreelancerList(JPanel parentPanel, String searchTerm, String sortOrder) {
        parentPanel.removeAll();
        String lowerSearch = searchTerm.toLowerCase();

        List<Freelancer> filteredList = allFreelancers.stream()
            .filter(f -> f.name.toLowerCase().contains(lowerSearch) ||
                         f.skill.toLowerCase().contains(lowerSearch))
            .collect(Collectors.toList());

        switch (sortOrder) {
            case "Rating (High to Low)" -> filteredList.sort(Comparator.comparing(Freelancer::getRating).reversed());
            case "Name (A-Z)" -> filteredList.sort(Comparator.comparing(Freelancer::getName));
        }

        if (filteredList.isEmpty()) {
            JLabel emptyLabel = new JLabel("No freelancers found matching your search.");
            emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            emptyLabel.setForeground(SLATE_400);
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            parentPanel.add(Box.createVerticalStrut(50));
            parentPanel.add(emptyLabel);
        } else {
            for (Freelancer f : filteredList) {
                addFreelancerCard(parentPanel, f);
            }
        }

        parentPanel.revalidate();
        parentPanel.repaint();
    }

    private void addFreelancerCard(JPanel parent, Freelancer freelancer) {
        parent.add(new FreelancerCard(freelancer));
        parent.add(Box.createVerticalStrut(12));
    }

    // =============================================================
    // 🧑‍💻 FREELANCER DATA MODEL & CARD UI
    // =============================================================
    private static class Freelancer {
        String name;
        String skill;
        double rating;
        String email;
        String phone;

        public Freelancer(String name, String skill, double rating, String email, String phone) {
            this.name = name;
            this.skill = skill;
            this.rating = rating;
            this.email = email;
            this.phone = phone;
        }
        
        public String getInitials() {
            String[] names = name.split(" ");
            if (names.length >= 2) return "" + names[0].charAt(0) + names[1].charAt(0);
            else if (names.length == 1 && names[0].length() > 0) return "" + names[0].charAt(0);
            else return "?";
        }
        
        public double getRating() { return rating; }
        public String getName() { return name; }
    }

    private static class FreelancerCard extends JPanel {
        private final Freelancer freelancer;
        private boolean isHovered = false;

        public FreelancerCard(Freelancer freelancer) {
            this.freelancer = freelancer;
            setLayout(new BorderLayout(15, 5));
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 20));
            setMinimumSize(new Dimension(0, 110));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
            setPreferredSize(new Dimension(Integer.MAX_VALUE, 110));

            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { isHovered = true; repaint(); }
                public void mouseExited(MouseEvent e) { isHovered = false; repaint(); }
                public void mouseClicked(MouseEvent e) {
                    Window owner = SwingUtilities.getWindowAncestor(FreelancerCard.this);
                    FreelancerContactDialog dialog = new FreelancerContactDialog(owner, freelancer);
                    dialog.setVisible(true);
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g); // ✅ Clears background first

            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Full clear to remove ghost lines
            g2d.setComposite(AlphaComposite.Clear);
            g2d.fillRect(0, 0, getWidth(), getHeight());
            g2d.setComposite(AlphaComposite.SrcOver);

            // Background color (changes slightly on hover)
            Color bgColor = isHovered ? new Color(81, 95, 115, 220) : SLATE_500;
            g2d.setColor(bgColor);
            g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

            // Accent line on the left
            g2d.setColor(ROYAL_BLUE);
            g2d.fillRoundRect(0, 0, 5, getHeight(), 12, 12);

            // Hover border outline
            if (isHovered) {
                g2d.setColor(ROYAL_BLUE);
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            }

            // Avatar circle
            int avatarSize = 60;
            int avatarX = 25;
            int avatarY = (getHeight() - avatarSize) / 2;
            g2d.setColor(PURPLE_ACCENT);
            g2d.fillOval(avatarX, avatarY, avatarSize, avatarSize);

            // Avatar initials
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 18));
            String initials = freelancer.getInitials();
            FontMetrics fm = g2d.getFontMetrics();
            int textX = avatarX + (avatarSize - fm.stringWidth(initials)) / 2;
            int textY = avatarY + (avatarSize - fm.getHeight()) / 2 + fm.getAscent();
            g2d.drawString(initials, textX, textY);

            // Name and Skill text
            int textXOffset = avatarX + avatarSize + 25;
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 18));
            g2d.drawString(freelancer.name, textXOffset, 40);

            g2d.setColor(SLATE_300);
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            g2d.drawString(freelancer.skill, textXOffset, 65);

            // Rating
            String rating = String.format("⭐ %.1f", freelancer.rating);
            g2d.setColor(SUCCESS_GREEN);
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 16));
            FontMetrics fmRating = g2d.getFontMetrics();
            int ratingX = getWidth() - fmRating.stringWidth(rating) - 25;
            int ratingY = (getHeight() - fmRating.getHeight()) / 2 + fmRating.getAscent();
            g2d.drawString(rating, ratingX, ratingY);

            g2d.dispose();
        }
    }
        // =============================================================
    // 💬 FREELANCER CONTACT DIALOG
    // =============================================================
    private static class FreelancerContactDialog extends JDialog {
        public FreelancerContactDialog(Window owner, Freelancer freelancer) {
            super(owner, "Contact Details", ModalityType.APPLICATION_MODAL);
            setUndecorated(true);
            setLocationRelativeTo(owner);
            getRootPane().setBorder(BorderFactory.createLineBorder(ROYAL_BLUE, 2));

            CustomGradientPanel backgroundPanel =
                    new CustomGradientPanel(SLATE_700, SLATE_600, CustomGradientPanel.GradientType.VERTICAL);
            backgroundPanel.setLayout(new BorderLayout());

            JPanel content = new JPanel();
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.setOpaque(false);
            content.setBorder(BorderFactory.createEmptyBorder(30, 30, 10, 30));

            // Avatar Circle
            JPanel avatarPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    int avatarSize = 80;
                    g2d.setColor(PURPLE_ACCENT);
                    g2d.fillOval(0, 0, avatarSize, avatarSize);

                    g2d.setColor(Color.WHITE);
                    g2d.setFont(new Font("Segoe UI", Font.BOLD, 28));
                    String initials = freelancer.getInitials();
                    FontMetrics fm = g2d.getFontMetrics();
                    int textX = (avatarSize - fm.stringWidth(initials)) / 2;
                    int textY = (avatarSize - fm.getHeight()) / 2 + fm.getAscent();
                    g2d.drawString(initials, textX, textY);
                    g2d.dispose();
                }
            };
            avatarPanel.setPreferredSize(new Dimension(80, 80));
            avatarPanel.setOpaque(false);
            avatarPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel nameLabel = new JLabel(freelancer.name);
            nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
            nameLabel.setForeground(Color.WHITE);
            nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel skillLabel = new JLabel(freelancer.skill);
            skillLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            skillLabel.setForeground(SLATE_300);
            skillLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            content.add(avatarPanel);
            content.add(Box.createVerticalStrut(15));
            content.add(nameLabel);
            content.add(Box.createVerticalStrut(5));
            content.add(skillLabel);
            content.add(Box.createVerticalStrut(20));

            JSeparator separator = new JSeparator();
            separator.setBackground(SLATE_500);
            separator.setForeground(SLATE_500);
            content.add(separator);
            content.add(Box.createVerticalStrut(20));

            content.add(createInfoRow("Email:", freelancer.email));
            content.add(Box.createVerticalStrut(15));
            content.add(createInfoRow("Phone:", freelancer.phone));

            backgroundPanel.add(content, BorderLayout.CENTER);

            JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
            footer.setOpaque(false);
            footer.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));

            JButton closeBtn = new JButton("Close");
            closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            closeBtn.setBackground(ROYAL_BLUE);
            closeBtn.setForeground(Color.WHITE);
            closeBtn.setFocusPainted(false);
            closeBtn.setBorderPainted(false);
            closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            closeBtn.addActionListener(e -> dispose());

            footer.add(closeBtn);
            backgroundPanel.add(footer, BorderLayout.SOUTH);

            add(backgroundPanel);
            pack();
        }

        private JPanel createInfoRow(String title, String value) {
            JPanel row = new JPanel(new BorderLayout(10, 0));
            row.setOpaque(false);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            titleLabel.setForeground(Color.WHITE);

            JLabel valueLabel = new JLabel(value);
            valueLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            valueLabel.setForeground(SLATE_300);

            row.add(titleLabel, BorderLayout.WEST);
            row.add(valueLabel, BorderLayout.CENTER);
            return row;
        }
    }

    // =============================================================
    // ✏️ UI HELPERS
    // =============================================================
    private static class PlaceholderTextField extends JTextField {
        private final String placeholder;
        public PlaceholderTextField(String placeholder) { this.placeholder = placeholder; }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (placeholder == null || placeholder.isEmpty() || !getText().isEmpty()) return;
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getDisabledTextColor());
            g2.setFont(getFont().deriveFont(Font.ITALIC));
            Insets in = getInsets();
            int y = (getHeight() - g2.getFontMetrics().getHeight()) / 2 + g2.getFontMetrics().getAscent();
            g2.drawString(placeholder, in.left, y);
        }
    }

    private static class RoleSelectionDialog extends JDialog {
        private ProjectManager.UserRole selectedRole;
        public RoleSelectionDialog(JFrame parent) {
            super(parent, "Select Your Role", true);
            setSize(600, 350);
            setLocationRelativeTo(parent);
            setUndecorated(true);
            getRootPane().setBorder(BorderFactory.createLineBorder(ROYAL_BLUE, 2));

            JPanel background = new JPanel(new BorderLayout());
            background.setBackground(SLATE_700);
            background.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            JLabel title = new JLabel("Continue as a...", JLabel.CENTER);
            title.setFont(new Font("Segoe UI", Font.BOLD, 24));
            title.setForeground(Color.WHITE);
            title.setBorder(BorderFactory.createEmptyBorder(10, 10, 30, 10));
            background.add(title, BorderLayout.NORTH);

            JPanel cardPanel = new JPanel(new GridLayout(1, 2, 20, 20));
            cardPanel.setOpaque(false);
            cardPanel.add(new RoleCard("🏢", "Client", "Post projects and manage bids.",
                    ProjectManager.UserRole.CLIENT, this));
            cardPanel.add(new RoleCard("💻", "Freelancer", "Browse projects and place bids.",
                    ProjectManager.UserRole.FREELANCER, this));

            background.add(cardPanel, BorderLayout.CENTER);
            add(background);
        }
        public ProjectManager.UserRole getSelectedRole() { return selectedRole; }
        public void setSelectedRole(ProjectManager.UserRole role) { selectedRole = role; }
    }

    private static class RoleCard extends JPanel {
        private boolean hovered = false;
        private final String icon, title, description;
        private final Color baseColor = new Color(52, 73, 94);
        private final Color hoverColor = new Color(82, 103, 124);

        public RoleCard(String icon, String title, String desc,
                        ProjectManager.UserRole role, RoleSelectionDialog parent) {
            this.icon = icon; this.title = title; this.description = desc;
            setOpaque(false); setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                public void mouseExited(MouseEvent e) { hovered = false; repaint(); }
                public void mouseClicked(MouseEvent e) { parent.setSelectedRole(role); parent.dispose(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(hovered ? hoverColor : baseColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            if (hovered) {
                g2.setColor(ROYAL_BLUE);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 15, 15);
            }
            g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
            g2.setColor(Color.WHITE);
            int centerX = getWidth() / 2;
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(icon, centerX - fm.stringWidth(icon) / 2, 70);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 22));
            fm = g2.getFontMetrics();
            g2.drawString(title, centerX - fm.stringWidth(title) / 2, 120);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            g2.setColor(new Color(200, 200, 200));
            fm = g2.getFontMetrics();
            g2.drawString(description, centerX - fm.stringWidth(description) / 2, 150);
            g2.dispose();
        }
    }

    private static class CustomGradientPanel extends JPanel {
        public enum GradientType { VERTICAL, DIAGONAL }
        private final Color startColor, endColor;
        private final GradientType type;
        private static final BufferedImage texture = loadImage();

        public CustomGradientPanel(Color start, Color end, GradientType type) {
            this.startColor = start; this.endColor = end; this.type = type;
        }
        private static BufferedImage loadImage() {
            try { return ImageIO.read(CustomGradientPanel.class.getResource("/background.png")); }
            catch (Exception e) { return null; }
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            if (texture != null) {
                for (int y = 0; y < getHeight(); y += texture.getHeight())
                    for (int x = 0; x < getWidth(); x += texture.getWidth())
                        g2.drawImage(texture, x, y, this);
            } else {
                g2.setColor(new Color(34, 47, 62));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
            GradientPaint gp = (type == GradientType.DIAGONAL)
                    ? new GradientPaint(0, 0, startColor, getWidth(), getHeight(), endColor)
                    : new GradientPaint(0, 0, startColor, 0, getHeight(), endColor);
            g2.setPaint(gp);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        }
    }

    private static class ModuleButton extends JPanel {
        private final String title, subtitle, emoji;
        private final Color accent;
        private boolean hovered = false;
        public ModuleButton(String title, String subtitle, Color accent, String emoji) {
            this.title = title; this.subtitle = subtitle; this.accent = accent; this.emoji = emoji;
            setOpaque(false); setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                public void mouseExited(MouseEvent e) { hovered = false; repaint(); }
            });
        }
        public void addActionListener(ActionListener l) {
            addMouseListener(new MouseAdapter() { public void mouseClicked(MouseEvent e) { l.actionPerformed(null); }});
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            g2.setColor(new Color(0,0,0,70));
            g2.fillRoundRect(5,5,w-5,h-5,20,20);
            g2.setColor(hovered ? new Color(52,152,219,220) : new Color(44,62,80,200));
            g2.fillRoundRect(0,0,w-5,h-5,20,20);
            g2.setColor(accent);
            g2.fillRoundRect(0,0,8,h-5,20,20);
            g2.setFont(new Font("Segoe UI Emoji", Font.BOLD, 36));
            g2.drawString(emoji,30,65);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
            g2.drawString(title,30,105);
            g2.setColor(new Color(189,227,255));
            g2.setFont(new Font("Segoe UI", Font.PLAIN,13));
            g2.drawString(subtitle,30,130);
            g2.dispose();
        }
    }

    // =============================================================
    // 🌤️ UTILITIES
    // =============================================================
    private String getTimeBasedGreeting() {
        int hour = java.time.LocalTime.now().getHour();
        if (hour < 12) return "Good morning! ☀️";
        else if (hour < 17) return "Good afternoon! 🌤️";
        else return "Good evening! 🌙";
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }
}
