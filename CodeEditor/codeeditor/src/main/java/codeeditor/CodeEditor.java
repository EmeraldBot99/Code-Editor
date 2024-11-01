package codeeditor;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;

public class CodeEditor extends JFrame {
    private JPanel mainPanel;
    private JMenuBar menuBar;
    private ArrayList<JTextPane> codeTextPanes;
    private ArrayList<String> filePaths;
    private JTabbedPane tabbedPane;
    private ArrayList<LineNumberComponent> lineNumbers;
    private int activeIndex;
    private JTree sideMenu;
    private String openFolder;
    private final FileManager fileManager;
    private TabManager tabManager;
    private final SyntaxHighlighter syntaxHighlighter;

    public CodeEditor() {
        setTitle("Code Shizard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1500, 900);
        setLocationRelativeTo(null);


        syntaxHighlighter = new SyntaxHighlighter();
        
        initializeComponents();
        // Initialize TabManager after components are created
        tabManager = new TabManager(this, tabbedPane, codeTextPanes, lineNumbers, filePaths, syntaxHighlighter);
        // Create initial tab after TabManager is initialized
        tabManager.createNewTab("Untitled");

        fileManager = new FileManager(this,tabManager);
        
        createMenuBar();
        layoutComponents();
    }

    private void initializeComponents() {
        mainPanel = new JPanel();
        menuBar = new JMenuBar();
        codeTextPanes = new ArrayList<>();
        filePaths = new ArrayList<>();
        lineNumbers = new ArrayList<>();
        tabbedPane = new JTabbedPane();
        sideMenu = new JTree();
    }

    private void layoutComponents() {
        mainPanel.setLayout(new BorderLayout());
        
        // Add side menu and tabbed pane to the main panel
        JSplitPane splitPane = new JSplitPane();
        splitPane.setLeftComponent(new JScrollPane(sideMenu));
        splitPane.setRightComponent(tabbedPane);
        splitPane.setDividerLocation(200);

        mainPanel.add(splitPane, BorderLayout.CENTER);
        add(mainPanel);
    }

    private void createMenuBar() {
        MenuBarCreator menuBarCreator = new MenuBarCreator(this, fileManager, tabManager);
        menuBar = menuBarCreator.createMenuBar();
        setJMenuBar(menuBar);
    }

    public JTree getSideMenu() {
        return sideMenu;
    }

    public JTabbedPane getTabbedPane() {
        return tabbedPane;
    }

    public ArrayList<JTextPane> getCodeTextPanes() {
        return codeTextPanes;
    }

    public ArrayList<String> getFilePaths() {
        return filePaths;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CodeEditor().setVisible(true));
    }
}