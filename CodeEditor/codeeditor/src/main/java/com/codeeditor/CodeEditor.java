package com.codeeditor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class CodeEditor extends JFrame {
    // Declare GUI components
    private JPanel mainPanel;
    private JButton button1;
    private JTextField textField;
    private JLabel label;
    private JMenuBar menuBar;

    public CodeEditor() {
        // Set up the frame
        setTitle("Code Editor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);  // Center the window

        // Initialize components
        initializeComponents();
        
        // Set up the menu bar
        createMenuBar();
        
        // Add components to the frame
        layoutComponents();
        
        // Add event listeners
        addEventListeners();
    }

    private void initializeComponents() {
        mainPanel = new JPanel();
        button1 = new JButton("Click Me");
        textField = new JTextField(20);
        label = new JLabel("Enter text:");
        menuBar = new JMenuBar();
    }

    private void createMenuBar() {
        // Create File menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);

        // Create Help menu
        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> 
            JOptionPane.showMessageDialog(this, 
                "Code Editor\nVersion 1.0", 
                "About", 
                JOptionPane.INFORMATION_MESSAGE));
        helpMenu.add(aboutItem);

        // Add menus to menu bar
        menuBar.add(fileMenu);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);
    }

    private void layoutComponents() {
        // Set layout manager
        mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        // Add label
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        mainPanel.add(label, gbc);

        // Add text field
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(textField, gbc);

        // Add button
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(button1, gbc);

        // Add panel to frame
        add(mainPanel);
    }

    private void addEventListeners() {
        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = textField.getText();
                if (!text.isEmpty()) {
                    JOptionPane.showMessageDialog(CodeEditor.this,
                        "You entered: " + text,
                        "Message",
                        JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
    }

    public static void main(String[] args) {
        // Run GUI in event dispatch thread
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new CodeEditor().setVisible(true);
            }
        });
    }
}