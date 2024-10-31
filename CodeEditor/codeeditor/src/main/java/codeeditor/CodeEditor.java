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
import java.util.ArrayList;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class CodeEditor extends JFrame {
    // Declare GUI components
    private JPanel mainPanel;
    private JMenuBar menuBar;
    private ArrayList<JTextPane> codeTextPanes;
    private ArrayList<String> filePaths;
    private JTabbedPane tabbedPane;
    private ArrayList<LineNumberComponent> lineNumbers;
    private int activeIndex;
    private JTree sideMenu;
    private String openFolder;

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

    private void initializeComponents() {
        mainPanel = new JPanel();
        menuBar = new JMenuBar();
        codeTextPanes = new ArrayList<>();
        filePaths = new ArrayList<>();
        lineNumbers = new ArrayList<>();
        tabbedPane = new JTabbedPane();
        sideMenu = new JTree();
        
        // Create initial tab
        createNewTab("Untitled");
    }

    private void createNewTab(String title) {
        // Create new text pane
        JTextPane newTextPane = new JTextPane();
        File fontFile = new File("CodeEditor\\codeeditor\\src\\Fonts\\Monaco.ttf");
        try{
            Font font = Font.createFont(Font.TRUETYPE_FONT, fontFile);
            Font finalFont = font.deriveFont(Font.PLAIN, 14);
            newTextPane.setFont(finalFont);
        }catch(FontFormatException | IOException e){
            newTextPane.setFont(new Font("Arial", Font.PLAIN, 16));
        }

        
        
        // Configure the JTextPane for code editing
        StyledDocument doc = newTextPane.getStyledDocument();
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setFontFamily(attrs, "Monospaced");
        isFontAvailable("Monaco");
        StyleConstants.setFontSize(attrs, 16);
        
        // Create line numbers component
        LineNumberComponent newLineNumbers = new LineNumberComponent(newTextPane);
        
        // Add document listener
        newTextPane.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                newLineNumbers.repaint();
                highlightSyntax(codeTextPanes.size() - 1);
            }
            public void removeUpdate(DocumentEvent e) {
                newLineNumbers.repaint();
                highlightSyntax(codeTextPanes.size() - 1);
            }
            public void changedUpdate(DocumentEvent e) {
                newLineNumbers.repaint();
                highlightSyntax(codeTextPanes.size() - 1);
            }
        });

        // Create scroll pane
        JScrollPane scrollPane = new JScrollPane(newTextPane);
        scrollPane.getViewport().addChangeListener(e -> newLineNumbers.repaint());
        scrollPane.setRowHeaderView(newLineNumbers);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        // Add components to lists
        codeTextPanes.add(newTextPane);
        lineNumbers.add(newLineNumbers);
        filePaths.add(null);

        // Add tab
        tabbedPane.addTab(title, scrollPane);
        tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
        activeIndex = tabbedPane.getSelectedIndex();

        // Add tab close button
        JPanel tabPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tabPanel.setOpaque(false);
        JLabel titleLabel = new JLabel(title + " ");
        JButton closeButton = new JButton("×");
        closeButton.setPreferredSize(new Dimension(20, 20));
        closeButton.addActionListener(e -> closeTab(tabbedPane.indexOfTabComponent(tabPanel)));
        tabPanel.add(titleLabel);
        tabPanel.add(closeButton);
        tabbedPane.setTabComponentAt(tabbedPane.getTabCount() - 1, tabPanel);

        //rename tab
        tabbedPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int tabIndex = tabbedPane.indexAtLocation(e.getX(), e.getY());
                    renameTab(tabPanel,tabIndex);
                }
            }
        });

        // file tree
        
    }

    
    private void isFontAvailable(String fontName) {
        String[] fontFamilies = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        for (String font : fontFamilies) {
            if (font.equalsIgnoreCase(fontName)) {
                System.out.println("fontexists");
            }
        }
        System.out.println("font not available"); // Font is not available
    }


    private void renameTab(JPanel tabPanel,int index){
        JPanel temptabPanel = (JPanel)tabbedPane.getTabComponentAt(index);
        JLabel indexedTitleLabel = (JLabel)temptabPanel.getComponent(0);

        JTextField editor = new JTextField(indexedTitleLabel.getText());
        Rectangle bounds = indexedTitleLabel.getBounds();
        editor.setBounds(bounds);
        indexedTitleLabel.setVisible(false);
        
        tabPanel.add(editor);
        editor.requestFocus();


        editor.addActionListener(e -> {
            updateTabTitle(indexedTitleLabel,editor,tabPanel);
        });

        editor.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                updateTabTitle(indexedTitleLabel,editor,tabPanel);
            }
        });

    }
    
    private void updateTabTitle(JLabel label, JTextField editor, JPanel panel) {
        label.setText(editor.getText().trim() + " ");
        label.setVisible(true);
        panel.remove(editor); 
        panel.revalidate();
        panel.repaint();
    }


    private void closeTab(int index) {
        if (tabbedPane.getTabCount() > 1) {
            tabbedPane.remove(index);
            codeTextPanes.remove(index);
            lineNumbers.remove(index);
            filePaths.remove(index);
        }
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

        Style keywordStyle = styleContext.addStyle("keyword", defaultStyle);
        StyleConstants.setForeground(keywordStyle, new Color(127, 0, 85));

        Style stringStyle = styleContext.addStyle("string", defaultStyle);
        StyleConstants.setForeground(stringStyle, new Color(42, 161, 152));

        Style numberStyle = styleContext.addStyle("number", defaultStyle);
        StyleConstants.setForeground(numberStyle, new Color(0, 128, 0));

        Style commentStyle = styleContext.addStyle("comment", defaultStyle);
        StyleConstants.setForeground(commentStyle, new Color(128, 128, 128));
    }

    private void highlightPattern(StyledDocument doc, Pattern pattern, String styleName) {
        try {
            String text = doc.getText(0, doc.getLength());
            java.util.regex.Matcher matcher = pattern.matcher(text);

            while(matcher.find()) {
                doc.setCharacterAttributes(matcher.start(), 
                    matcher.end() - matcher.start(),
                    styleContext.getStyle(styleName), 
                    false);
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<Timer> highlightTimers = new ArrayList<>();

    private void highlightSyntax(int index) {
        Timer timer = highlightTimers.size() > index ? highlightTimers.get(index) : null;
        
        if (timer != null && timer.isRunning()) {
            timer.restart();
        } else {
            timer = new Timer(100, e -> {
                JTextPane currentPane = codeTextPanes.get(index);
                String text = currentPane.getText();
                StyledDocument doc = currentPane.getStyledDocument();
                doc.setCharacterAttributes(0, text.length(), defaultStyle, true);
                highlightPattern(doc, KEYWORDS, "keyword");
                highlightPattern(doc, STRINGS, "string");
                highlightPattern(doc, NUMBERS, "number");
                highlightPattern(doc, COMMENTS, "comment");
            });
            timer.setRepeats(false);
            
            if (highlightTimers.size() > index) {
                highlightTimers.set(index, timer);
            } else {
                highlightTimers.add(timer);
            }
            
            timer.start();
        }
    }

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
            setPreferredSize(new Dimension(45, Integer.MAX_VALUE));
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            
            Rectangle visibleRect = textPane.getVisibleRect();
            int firstLine = getLineAtPoint(new Point(0, visibleRect.y));
            int lastLine = getLineAtPoint(new Point(0, visibleRect.y + visibleRect.height));
            
            Element root = textPane.getDocument().getDefaultRootElement();
            int totalLines = root.getElementCount();
            
            int width = getWidth();
            int lineHeight = fontMetrics.getHeight();
            
            for (int i = firstLine; i <= Math.min(lastLine + 1, totalLines - 1); i++) {
                Element line = root.getElement(i);
                try {
                    Rectangle r = textPane.modelToView2D(line.getStartOffset()).getBounds();
                    
                    int lineNumber = i + 1;
                    String number = String.valueOf(lineNumber);
                    
                    int stringWidth = fontMetrics.stringWidth(number);
                    int x = width - stringWidth - MARGIN;
                    
                    g2d.setColor(Color.GRAY);
                    g2d.drawString(number, x, r.y + fontMetrics.getAscent());
                    
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }
            
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
        JMenu fileMenu = new JMenu("File");
        
        JMenuItem newItem = new JMenuItem("New");
        newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
        newItem.addActionListener(e -> createNewTab("Untitled"));
        
        JMenuItem openItem = new JMenuItem("Open");
        openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        openItem.addActionListener(e -> openFile());
        
        JMenuItem saveItem = new JMenuItem("Save");
        saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        saveItem.addActionListener(e -> {
            activeIndex = tabbedPane.getSelectedIndex();
            saveFile(filePaths.get(activeIndex), codeTextPanes.get(activeIndex).getText());
        });

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
        exitItem.addActionListener(e -> System.exit(0));
        
        fileMenu.add(newItem);
        fileMenu.add(openItem);
        fileMenu.addSeparator();
        fileMenu.add(saveItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

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
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        add(mainPanel);
    }

    private void openFile() {
        String filePath = loadFile();
        if (!filePath.equals("not okay")) {
            createNewTab(new File(filePath).getName());
            activeIndex = tabbedPane.getSelectedIndex();
            filePaths.set(activeIndex, filePath);
            String content = readFileAsString(filePath);
            codeTextPanes.get(activeIndex).setText(content);
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

    private void saveFile(String outputPath, String textData) {
        if (outputPath == null) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
            int result = fileChooser.showSaveDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                outputPath = fileChooser.getSelectedFile().getAbsolutePath();
                filePaths.set(activeIndex, outputPath);
                // Update tab title
                JPanel tabPanel = (JPanel) tabbedPane.getTabComponentAt(activeIndex);
                ((JLabel) tabPanel.getComponent(0)).setText(new File(outputPath).getName() + " ");
            }
        }
        
        if (outputPath != null) {
            try {
                Files.write(Paths.get(outputPath), textData.getBytes(StandardCharsets.UTF_8));
                System.out.println("File saved successfully: " + outputPath);
            } catch(IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                    "Error saving file: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String readFileAsString(String path) {
        try {
            return Files.readString(Paths.get(path));
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
        SwingUtilities.invokeLater(() -> new CodeEditor().setVisible(true));
    }
}