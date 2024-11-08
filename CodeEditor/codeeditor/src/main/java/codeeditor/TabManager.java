package codeeditor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class TabManager {
    private final CodeEditor editor;
    private final JTabbedPane tabbedPane;
    private final ArrayList<JTextPane> codeTextPanes;
    private final ArrayList<LineNumberComponent> lineNumbers;
    private final ArrayList<String> filePaths;
    private final SyntaxHighlighter syntaxHighlighter;
    private final ErrorChecker errorChecker;

    public TabManager(CodeEditor editor, JTabbedPane tabbedPane, ArrayList<JTextPane> codeTextPanes, 
                     ArrayList<LineNumberComponent> lineNumbers, ArrayList<String> filePaths,
                     SyntaxHighlighter syntaxHighlighter) {
        this.editor = editor;
        this.tabbedPane = tabbedPane;
        this.codeTextPanes = codeTextPanes;
        this.lineNumbers = lineNumbers;
        this.filePaths = filePaths;
        this.syntaxHighlighter = syntaxHighlighter;
        this.errorChecker = new ErrorChecker(editor, tabbedPane, codeTextPanes, filePaths);
    }

    public void createNewTab(String title) {
        JTextPane newTextPane = createTextPane();
        LineNumberComponent newLineNumbers = new LineNumberComponent(newTextPane);
        setupDocumentListener(newTextPane, newLineNumbers);

        JScrollPane scrollPane = createScrollPane(newTextPane, newLineNumbers);
        
        codeTextPanes.add(newTextPane);
        lineNumbers.add(newLineNumbers);
        filePaths.add(null);

        addTab(title, scrollPane);
    }

    private JTextPane createTextPane() {
        JTextPane newTextPane = new JTextPane();
        setupFont(newTextPane);
        return newTextPane;
    }

    private void setupFont(JTextPane textPane) {
        File fontFile = new File("CodeEditor\\codeeditor\\src\\Fonts\\Monaco.ttf");
        try {
            Font font = Font.createFont(Font.TRUETYPE_FONT, fontFile);
            Font finalFont = font.deriveFont(Font.PLAIN, 14);
            textPane.setFont(finalFont);
        } catch(FontFormatException | IOException e) {
            textPane.setFont(new Font("Arial", Font.PLAIN, 16));
        }
    }

    private void setupDocumentListener(JTextPane textPane, LineNumberComponent lineNumbers) {
        textPane.getDocument().addDocumentListener(new DocumentListenerImpl(
            lineNumbers,
            () -> syntaxHighlighter.highlightSyntax(codeTextPanes.size() - 1, codeTextPanes),
            errorChecker
        ));
    }

    private JScrollPane createScrollPane(JTextPane textPane, LineNumberComponent lineNumbers) {
        JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.getViewport().addChangeListener(e -> lineNumbers.repaint());
        scrollPane.setRowHeaderView(lineNumbers);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        return scrollPane;
    }

    private void addTab(String title, JScrollPane scrollPane) {
        tabbedPane.addTab(title, scrollPane);
        tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);

        JPanel tabPanel = createTabPanel(title);
        tabbedPane.setTabComponentAt(tabbedPane.getTabCount() - 1, tabPanel);
    }

    private JPanel createTabPanel(String title) {
        JPanel tabPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tabPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel(title + " ");
        JButton closeButton = createCloseButton(tabPanel);
        
        tabPanel.add(titleLabel);
        tabPanel.add(closeButton);
        
        setupTabRenaming(tabPanel);
        
        return tabPanel;
    }

    private JButton createCloseButton(JPanel tabPanel) {
        JButton closeButton = new JButton("×");
        closeButton.setPreferredSize(new Dimension(20, 20));
        closeButton.addActionListener(e -> closeTab(tabbedPane.indexOfTabComponent(tabPanel)));
        return closeButton;
    }

    private void setupTabRenaming(JPanel tabPanel) {
        tabbedPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int tabIndex = tabbedPane.indexAtLocation(e.getX(), e.getY());
                    if (tabIndex >= 0) {
                        renameTab(tabPanel, tabIndex);
                    }
                }
            }
        });
    }

    public void closeTab(int index) {
        if (tabbedPane.getTabCount() > 1) {
            tabbedPane.remove(index);
            codeTextPanes.remove(index);
            lineNumbers.remove(index);
            filePaths.remove(index);
        }
    }

    private void renameTab(JPanel tabPanel, int index) {
        JPanel tempTabPanel = (JPanel)tabbedPane.getTabComponentAt(index);
        JLabel indexedTitleLabel = (JLabel)tempTabPanel.getComponent(0);

        JTextField editor = new JTextField(indexedTitleLabel.getText());
        Rectangle bounds = indexedTitleLabel.getBounds();
        editor.setBounds(bounds);
        indexedTitleLabel.setVisible(false);
        
        tabPanel.add(editor);
        editor.requestFocus();

        editor.addActionListener(e -> updateTabTitle(indexedTitleLabel, editor, tabPanel));
        editor.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                updateTabTitle(indexedTitleLabel, editor, tabPanel);
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
}