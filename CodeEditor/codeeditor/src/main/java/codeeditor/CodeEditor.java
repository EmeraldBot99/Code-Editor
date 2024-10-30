package codeeditor;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class CodeEditor extends JFrame {
    // Declare GUI components
    private JPanel mainPanel;
    private JMenuBar menuBar;
    private JTextPane codeTextPane;  // Changed from JTextArea to JTextPane
    private JScrollPane scrollPane;

    public CodeEditor() {
        setTitle("Code Shizard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1500, 900);
        setLocationRelativeTo(null);

        initializeComponents();
        createMenuBar();
        layoutComponents();
    }

    //instance variables
    String activeFile = null;

    private void initializeComponents() {
        mainPanel = new JPanel();
        menuBar = new JMenuBar();
        
        // Initialize the text pane with scrolling
        codeTextPane = new JTextPane();  // Changed to JTextPane
        codeTextPane.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        // Configure the JTextPane for code editing
        StyledDocument doc = codeTextPane.getStyledDocument();
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setFontFamily(attrs, "Monospaced");
        StyleConstants.setFontSize(attrs, 12);
        ((AbstractDocument) doc).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) 
                throws BadLocationException {
                super.insertString(fb, offset, string, attr);
            }
            
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) 
                throws BadLocationException {
                super.replace(fb, offset, length, text, attrs);
            }
        });
        
        scrollPane = new JScrollPane(codeTextPane);  // Updated to use codeTextPane
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
    }

    private void createMenuBar() {
        // Create File menu
        JMenu fileMenu = new JMenu("File");
        
        // Open file menu item
        JMenuItem openItem = new JMenuItem("Open");
        openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        openItem.addActionListener(e -> openFile());
        
        //save menu item
        JMenuItem saveItem = new JMenuItem("save");
        saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        saveItem.addActionListener(e -> saveFile(activeFile, codeTextPane.getText()));  // Updated to use codeTextPane

        // Exit menu item
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
        exitItem.addActionListener(e -> System.exit(0));
        
        fileMenu.add(openItem);
        fileMenu.addSeparator();
        fileMenu.add(saveItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        // Create Help menu
        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> 
            JOptionPane.showMessageDialog(this, 
                "Code Editor\nVersion 0.1", 
                "About", 
                JOptionPane.INFORMATION_MESSAGE));
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);
    }

    private void layoutComponents() {
        // Use BorderLayout for the main panel
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        add(mainPanel);
    }

    private void openFile() {
        String filePath = loadFile();
        if (activeFile == null){
            activeFile = filePath;
        }
        
        if (!filePath.equals("not okay")) {
            String content = readFileAsString(filePath);
            codeTextPane.setText(content);  // Updated to use codeTextPane
            setTitle("Code Editor - " + new File(filePath).getName());
        }
    }

    private String loadFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            return selectedFile.getAbsolutePath();
        }
        return "not okay";
    }

    private void saveFile(String outputPath, String textData){
        try {
            Files.write(Paths.get(outputPath), textData.getBytes(StandardCharsets.UTF_8));
            System.out.println("file saved successfully");
            System.out.println(outputPath);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private String readFileAsString(String path) {
        try {
            String content = Files.readString(Paths.get(path));
            return content;
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error reading file: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return "Unable to load file";
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new CodeEditor().setVisible(true);
            }
        });
    }
}