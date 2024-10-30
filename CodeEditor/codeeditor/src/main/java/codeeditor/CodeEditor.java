package codeeditor;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
public class CodeEditor extends JFrame {
    // Declare GUI components
    private JPanel mainPanel;
    private JMenuBar menuBar;
    private JTextPane codeTextPane;
    private JScrollPane scrollPane;
    private LineNumberComponent lineNumbers;

    public CodeEditor() {
        setTitle("Code Shizard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1500, 900);
        setLocationRelativeTo(null);

        initializeComponents();
        createMenuBar();
        layoutComponents();

        setupSyntaxHighlighting();
    }

    //instance variables
    String activeFile = null;

    private void initializeComponents() {
        mainPanel = new JPanel();
        menuBar = new JMenuBar();
        
        // Initialize the text pane with scrolling
        codeTextPane = new JTextPane();
        codeTextPane.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        // Configure the JTextPane for code editing
        StyledDocument doc = codeTextPane.getStyledDocument();
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setFontFamily(attrs, "Monospaced");
        StyleConstants.setFontSize(attrs, 12);
        
        // Create line numbers component
        lineNumbers = new LineNumberComponent(codeTextPane);
        
        // Add document listener to update line numbers when text changes
        codeTextPane.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                lineNumbers.repaint();
                highlighSyntax();
            }
            public void removeUpdate(DocumentEvent e) {
                lineNumbers.repaint();
                highlighSyntax();
            }
            public void changedUpdate(DocumentEvent e) {
                lineNumbers.repaint();
                highlighSyntax();
            }
        });

        scrollPane = new JScrollPane(codeTextPane);
        scrollPane.getViewport().addChangeListener(e -> lineNumbers.repaint());
        scrollPane.setRowHeaderView(lineNumbers);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
    }


    //syntax highlighting
    private StyleContext styleContext;
    private Style defaultStyle;
    private static final Pattern KEYWORDS = Pattern.compile("\\b(public|private|class|void|int|String)\\b");
    private static final Pattern STRINGS = Pattern.compile("\"[^\"\\\\]*(\\\\.[^\"\\\\]*)*\"");
    private static final Pattern NUMBERS = Pattern.compile("\\b\\d+\\b");
    private static final Pattern COMMENTS = Pattern.compile("//.*?$|/\\*[\\s\\S]*?\\*/");
  

    private void setupSyntaxHighlighting() {
        styleContext = new StyleContext();
        defaultStyle = styleContext.getStyle(StyleContext.DEFAULT_STYLE);

        // Create styles for different syntax elements
        Style keywordStyle = styleContext.addStyle("keyword", defaultStyle);
        StyleConstants.setForeground(keywordStyle, new Color(127, 0, 85)); // Purple for keywords

        Style stringStyle = styleContext.addStyle("string", defaultStyle);
        StyleConstants.setForeground(stringStyle, new Color(42, 161, 152)); // Green for strings

        Style numberStyle = styleContext.addStyle("number", defaultStyle);
        StyleConstants.setForeground(numberStyle, new Color(0, 128, 0)); // Dark green for numbers

        Style commentStyle = styleContext.addStyle("comment", defaultStyle);
        StyleConstants.setForeground(commentStyle, new Color(128, 128, 128)); // gray for numbers
    }

    private void highlightPattern(StyledDocument doc, Pattern pattern, String styleName){
        try {
            String text = doc.getText(0, doc.getLength());
            java.util.regex.Matcher matcher = pattern.matcher(text);

            while(matcher.find()){
                doc.setCharacterAttributes(matcher.start(), matcher.end() - matcher.start(),
                    styleContext.getStyle(styleName), false);
            }
        } catch (BadLocationException e){
            e.printStackTrace();
        }
    }

    private Timer highlightTimer;

    private void highlighSyntax(){
        if (highlightTimer != null && highlightTimer.isRunning()) {
            highlightTimer.restart(); // Restart the timer if it is already running
        } else {
            highlightTimer = new Timer(100, e -> {
                String text = codeTextPane.getText();
                StyledDocument doc = codeTextPane.getStyledDocument();
                doc.setCharacterAttributes(0, text.length(), defaultStyle, true);
                highlightPattern(doc, KEYWORDS, "keyword");
                highlightPattern(doc, STRINGS, "string");
                highlightPattern(doc, NUMBERS, "number");
                highlightPattern(doc, COMMENTS, "comment");
            });
            highlightTimer.setRepeats(false); // Only run once
            highlightTimer.start(); // Start the timer
        }
    }


    // Custom component for line numbers
   private class LineNumberComponent extends JPanel {
    private static final int MARGIN = 5;
    private final JTextPane textPane;
    private final FontMetrics fontMetrics;
    
    public LineNumberComponent(JTextPane textPane) {
        this.textPane = textPane;
        setPreferredSize(new Dimension(45, 1));
        setBackground(new Color(240, 240, 240));
        setFont(new Font("Monospaced", Font.PLAIN, 12));
        fontMetrics = getFontMetrics(getFont());
        
        // Make sure the line numbers take up the full height
        setPreferredSize(new Dimension(45, Integer.MAX_VALUE));
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Get visible rect of viewport
        Rectangle visibleRect = textPane.getVisibleRect();
        
        // Get the first and last lines visible in the viewport
        int firstLine = getLineAtPoint(new Point(0, visibleRect.y));
        int lastLine = getLineAtPoint(new Point(0, visibleRect.y + visibleRect.height));
        
        // Get total number of lines
        Element root = textPane.getDocument().getDefaultRootElement();
        int totalLines = root.getElementCount();
        
        // Draw line numbers
        int width = getWidth();
        int lineHeight = fontMetrics.getHeight();
        
        for (int i = firstLine; i <= Math.min(lastLine + 1, totalLines - 1); i++) {
            Element line = root.getElement(i);
            try {
                Rectangle r = textPane.modelToView2D(line.getStartOffset()).getBounds();
                
                int lineNumber = i + 1;
                String number = String.valueOf(lineNumber);
                
                // Right align the line numbers
                int stringWidth = fontMetrics.stringWidth(number);
                int x = width - stringWidth - MARGIN;
                
                // Draw the line number
                g2d.setColor(Color.GRAY);
                g2d.drawString(number, x, r.y + fontMetrics.getAscent());
                
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }
        
        // Draw separator line
        g2d.setColor(new Color(220, 220, 220));
        g2d.drawLine(width - 1, 0, width - 1, getHeight());
    }
    
    private int getLineAtPoint(Point point) {
        int pos = textPane.viewToModel2D(point);
        Element root = textPane.getDocument().getDefaultRootElement();
        return root.getElementIndex(pos);
    }
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
        saveItem.addActionListener(e -> saveFile(activeFile, codeTextPane.getText()));

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
            codeTextPane.setText(content);
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