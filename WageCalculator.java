import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;

public class WageCalculator {
    private JFrame window;
    private JComboBox<String> skillCombo, experienceCombo, complexityCombo, locationCombo;
    private JTextField hoursField;
    private JLabel finalRateLabel, totalLabel;
    private JTextArea breakdownArea;
    private final DecimalFormat currency = new DecimalFormat("$#,##0.00");

    // --- Modern UI Color Palette (Consistent with other modules) ---
    private final Color bgColor = new Color(45, 52, 54);
    private final Color panelColor = new Color(53, 63, 64);
    private final Color textColor = new Color(223, 230, 233);
    private final Color inputBgColor = new Color(99, 110, 114);
    private final Color accentColor = new Color(52, 152, 219);
    private final Color successColor = new Color(46, 204, 113);
    private final Color warningColor = new Color(230, 126, 34);

    public WageCalculator() {
        // Apply the Nimbus Look and Feel for a modern appearance
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
        setupWindow();
    }

    private void setupWindow() {
        window = new JFrame("Fair Wage Calculator");
        window.setSize(800, 700);
        window.setLocationRelativeTo(null);
        window.setLayout(new BorderLayout());
        window.getContentPane().setBackground(bgColor);

        // --- Main container panel ---
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setOpaque(false);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // --- Top section for inputs and results ---
        JPanel topPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        topPanel.setOpaque(false);
        
        JPanel inputPanel = createInputPanel();
        JPanel resultsPanel = createResultsPanel();
        topPanel.add(inputPanel);
        topPanel.add(resultsPanel);

        // --- Bottom section for breakdown ---
        JScrollPane breakdownPanel = createBreakdownPanel();

        // Use a JSplitPane for a resizable layout
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topPanel, breakdownPanel);
        splitPane.setOpaque(false);
        splitPane.setDividerLocation(350);
        splitPane.setDividerSize(10);
        
        mainPanel.add(splitPane, BorderLayout.CENTER);
        
        // --- Button Panel ---
        JPanel buttonPanel = createButtonPanel();
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        window.add(mainPanel);
        calculateWage(); // Initial calculation
    }
    
    private JPanel createInputPanel() {
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setOpaque(false);
        inputPanel.setBorder(createStyledTitledBorder("Project Parameters"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Components
        skillCombo = createStyledComboBox(new String[]{"Web Development", "Mobile App Development", "Data Science", "AI/Machine Learning", "UI/UX Design", "Content Writing"});
        experienceCombo = createStyledComboBox(new String[]{"Entry Level (0-2 years)", "Junior (2-4 years)", "Mid-Level (4-7 years)", "Senior (7-10 years)", "Expert (10+ years)"});
        complexityCombo = createStyledComboBox(new String[]{"Simple", "Moderate", "Complex", "Highly Complex", "Expert Level"});
        locationCombo = createStyledComboBox(new String[]{"Tier 3 City", "Tier 2 City", "Tier 1 City", "Metro City", "International"});
        hoursField = createStyledTextField("40", 10);
        
        // Set defaults
        experienceCombo.setSelectedIndex(1);
        complexityCombo.setSelectedIndex(1);
        locationCombo.setSelectedIndex(2);

        // Add action listeners to all input components
        java.awt.event.ActionListener listener = e -> calculateWage();
        skillCombo.addActionListener(listener);
        experienceCombo.addActionListener(listener);
        complexityCombo.addActionListener(listener);
        locationCombo.addActionListener(listener);
        hoursField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                calculateWage();
            }
        });
        
        // Layout
        gbc.gridx = 0; gbc.gridy = 0; inputPanel.add(createStyledLabel("Skill:"), gbc);
        gbc.gridx = 1; inputPanel.add(skillCombo, gbc);
        gbc.gridx = 0; gbc.gridy = 1; inputPanel.add(createStyledLabel("Experience:"), gbc);
        gbc.gridx = 1; inputPanel.add(experienceCombo, gbc);
        gbc.gridx = 0; gbc.gridy = 2; inputPanel.add(createStyledLabel("Complexity:"), gbc);
        gbc.gridx = 1; inputPanel.add(complexityCombo, gbc);
        gbc.gridx = 0; gbc.gridy = 3; inputPanel.add(createStyledLabel("Location:"), gbc);
        gbc.gridx = 1; inputPanel.add(locationCombo, gbc);
        gbc.gridx = 0; gbc.gridy = 4; inputPanel.add(createStyledLabel("Est. Hours:"), gbc);
        gbc.gridx = 1; inputPanel.add(hoursField, gbc);

        return inputPanel;
    }

    private JPanel createResultsPanel() {
        JPanel resultsPanel = new JPanel();
        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
        resultsPanel.setOpaque(false);
        resultsPanel.setBorder(createStyledTitledBorder("Fair Wage Estimate"));

        // Panel for the Fair Rate
        JPanel rateCard = new JPanel(new BorderLayout());
        rateCard.setBackground(panelColor);
        rateCard.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        finalRateLabel = new JLabel(currency.format(0), JLabel.CENTER);
        finalRateLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        finalRateLabel.setForeground(accentColor);
        JLabel rateSubLabel = createStyledLabel("Fair Hourly Rate");
        rateSubLabel.setHorizontalAlignment(JLabel.CENTER);
        rateCard.add(finalRateLabel, BorderLayout.CENTER);
        rateCard.add(rateSubLabel, BorderLayout.SOUTH);

        // Panel for the Total Project Cost
        JPanel totalCard = new JPanel(new BorderLayout());
        totalCard.setBackground(panelColor);
        totalCard.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        totalLabel = new JLabel(currency.format(0), JLabel.CENTER);
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 42));
        totalLabel.setForeground(successColor);
        JLabel totalSubLabel = createStyledLabel("Total Project Cost");
        totalSubLabel.setHorizontalAlignment(JLabel.CENTER);
        totalCard.add(totalLabel, BorderLayout.CENTER);
        totalCard.add(totalSubLabel, BorderLayout.SOUTH);
        
        resultsPanel.add(rateCard);
        resultsPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        resultsPanel.add(totalCard);

        return resultsPanel;
    }
    
    private JScrollPane createBreakdownPanel() {
        breakdownArea = new JTextArea(10, 40);
        breakdownArea.setEditable(false);
        breakdownArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        breakdownArea.setBackground(panelColor);
        breakdownArea.setForeground(textColor);
        breakdownArea.setBorder(BorderFactory.createCompoundBorder(
                createStyledTitledBorder("Calculation Breakdown"),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        return new JScrollPane(breakdownArea);
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JButton quoteBtn = createStyledButton("📄 Generate Quote", accentColor);
        JButton resetBtn = createStyledButton("🔄 Reset", warningColor);

        quoteBtn.addActionListener(e -> generateQuote());
        resetBtn.addActionListener(e -> resetFields());

        buttonPanel.add(quoteBtn);
        buttonPanel.add(resetBtn);
        return buttonPanel;
    }

    private void calculateWage() {
        try {
            double baseRate = getSkillRate((String) skillCombo.getSelectedItem());
            double expMultiplier = getExperienceMultiplier((String) experienceCombo.getSelectedItem());
            double compMultiplier = getComplexityMultiplier((String) complexityCombo.getSelectedItem());
            double locMultiplier = getLocationMultiplier((String) locationCombo.getSelectedItem());
            double finalRate = baseRate * expMultiplier * compMultiplier * locMultiplier;

            double hours = 0;
            try {
                hours = Double.parseDouble(hoursField.getText().trim());
            } catch (NumberFormatException ignored) {
                 hours = 0; // Default to 0 if input is invalid
            }
            double total = finalRate * hours;

            // Update result labels
            finalRateLabel.setText(currency.format(finalRate));
            totalLabel.setText(currency.format(total));

            // Update breakdown text area
            String f = "%-25s: %s%n";
            StringBuilder breakdown = new StringBuilder();
            breakdown.append("CALCULATION DETAILS\n");
            breakdown.append("=========================================\n");
            breakdown.append(String.format(f, "Base Skill Rate", currency.format(baseRate) + "/hr"));
            breakdown.append("-----------------------------------------\n");
            breakdown.append(String.format(f, "Experience Multiplier", String.format("x %.2f", expMultiplier)));
            breakdown.append(String.format(f, "Complexity Multiplier", String.format("x %.2f", compMultiplier)));
            breakdown.append(String.format(f, "Location Multiplier", String.format("x %.2f", locMultiplier)));
            breakdown.append("-----------------------------------------\n");
            breakdown.append(String.format(f, "FINAL HOURLY RATE", currency.format(finalRate) + "/hr"));
            breakdown.append(String.format(f, "x Estimated Hours", String.format("%.1f", hours)));
            breakdown.append("=========================================\n");
            breakdown.append(String.format(f, "TOTAL PROJECT COST", currency.format(total)));
            
            breakdownArea.setText(breakdown.toString());

        } catch (Exception e) {
            breakdownArea.setText("Error in calculation: \n" + e.getMessage());
        }
    }
    
    private double getSkillRate(String skill) {
        switch (skill) {
            case "Web Development": return 25.0;
            case "Mobile App Development": return 30.0;
            case "Data Science": return 35.0;
            case "AI/Machine Learning": return 40.0;
            case "UI/UX Design": return 28.0;
            case "Content Writing": return 20.0;
            default: return 25.0;
        }
    }

    private double getExperienceMultiplier(String experience) {
        switch (experience) {
            case "Entry Level (0-2 years)": return 0.8;
            case "Junior (2-4 years)": return 1.0;
            case "Mid-Level (4-7 years)": return 1.3;
            case "Senior (7-10 years)": return 1.6;
            case "Expert (10+ years)": return 2.0;
            default: return 1.0;
        }
    }

    private double getComplexityMultiplier(String complexity) {
        switch (complexity) {
            case "Simple": return 0.9;
            case "Moderate": return 1.0;
            case "Complex": return 1.3;
            case "Highly Complex": return 1.6;
            case "Expert Level": return 2.0;
            default: return 1.0;
        }
    }

    private double getLocationMultiplier(String location) {
        switch (location) {
            case "Tier 3 City": return 0.7;
            case "Tier 2 City": return 0.85;
            case "Tier 1 City": return 1.0;
            case "Metro City": return 1.2;
            case "International": return 1.5;
            default: return 1.0;
        }
    }
    
    private void generateQuote() {
        // Enhanced with HTML for better formatting in the dialog
        String htmlQuote = "<html><body style='width: 350px; font-family: sans-serif;'>"
            + "<h2>Freelance Project Quote</h2>"
            + "<hr>"
            + "<p><b>Skill:</b> " + skillCombo.getSelectedItem() + "</p>"
            + "<p><b>Experience:</b> " + experienceCombo.getSelectedItem() + "</p>"
            + "<p><b>Complexity:</b> " + complexityCombo.getSelectedItem() + "</p>"
            + "<p><b>Location Tier:</b> " + locationCombo.getSelectedItem() + "</p>"
            + "<p><b>Estimated Hours:</b> " + hoursField.getText() + "</p>"
            + "<hr>"
            + "<p><b>Calculated Fair Rate:</b><br><font size='+1' color='#3498db'>" + currency.format(Double.parseDouble(finalRateLabel.getText().replaceAll("[^\\d.]", ""))) + "/hr</font></p>"
            + "<p><b>Estimated Total Cost:</b><br><font size='+2' color='#2ecc71'>" + totalLabel.getText() + "</font></p>"
            + "</body></html>";
            
        JOptionPane.showMessageDialog(window, new JLabel(htmlQuote), "Project Quote", JOptionPane.INFORMATION_MESSAGE);
    }

    private void resetFields() {
        skillCombo.setSelectedIndex(0);
        experienceCombo.setSelectedIndex(1);
        complexityCombo.setSelectedIndex(1);
        locationCombo.setSelectedIndex(2);
        hoursField.setText("40");
        calculateWage();
    }
    
    public void showWindow() {
        window.setVisible(true);
    }
    
    // --- Reused component styling helper methods from UserManager ---
    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(textColor);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        return label;
    }
    
    private JTextField createStyledTextField(String text, int columns) {
        JTextField textField = new JTextField(text, columns);
        textField.setBackground(inputBgColor);
        textField.setForeground(textColor);
        textField.setCaretColor(accentColor);
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(80, 80, 80)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        return textField;
    }

    private JComboBox<String> createStyledComboBox(String[] items) {
        JComboBox<String> comboBox = new JComboBox<>(items);
        comboBox.setBackground(inputBgColor);
        comboBox.setForeground(textColor);
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        return comboBox;
    }
    
    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        return button;
    }

    private javax.swing.border.TitledBorder createStyledTitledBorder(String title) {
        return BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(accentColor, 1),
            " " + title + " ",
            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
            javax.swing.border.TitledBorder.DEFAULT_POSITION,
            new Font("Segoe UI", Font.BOLD, 16),
            textColor
        );
    }
}