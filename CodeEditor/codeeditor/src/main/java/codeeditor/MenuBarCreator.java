package codeeditor;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;

public class MenuBarCreator {
    private final CodeEditor editor;
    private final FileManager fileManager;
    private final TabManager tabManager;

    public MenuBarCreator(CodeEditor editor, FileManager fileManager, TabManager tabManager) {
        this.editor = editor;
        this.fileManager = fileManager;
        this.tabManager = tabManager;
    }

    public JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(createFileMenu());
        menuBar.add(createHelpMenu());
        return menuBar;
    }

    private JMenu createFileMenu() {
        JMenu fileMenu = new JMenu("File");
        
        JMenuItem newItem = new JMenuItem("New");
        newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
        newItem.addActionListener(e -> tabManager.createNewTab("Untitled"));
        
        JMenuItem openItem = new JMenuItem("Open");
        openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        openItem.addActionListener(e -> openFile());
        
        JMenuItem saveItem = new JMenuItem("Save");
        saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        saveItem.addActionListener(e -> {
            int activeIndex = editor.getTabbedPane().getSelectedIndex();
            fileManager.saveFile(
                editor.getFilePaths().get(activeIndex), 
                editor.getCodeTextPanes().get(activeIndex).getText(),
                activeIndex
            );
        });

        JMenuItem openFolderItem = new JMenuItem("Open Folder");
        openFolderItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.CTRL_DOWN_MASK));
        openFolderItem.addActionListener(e -> fileManager.openFolder());

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
        exitItem.addActionListener(e -> System.exit(0));
        
        fileMenu.add(newItem);
        fileMenu.add(openItem);
        fileMenu.addSeparator();
        fileMenu.add(saveItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        fileMenu.addSeparator();
        fileMenu.add(openFolderItem);
        
        return fileMenu;
    }

    private JMenu createHelpMenu() {
        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> 
            JOptionPane.showMessageDialog(editor, 
                "Code Editor\nVersion 0.1", 
                "About", 
                JOptionPane.INFORMATION_MESSAGE));
        helpMenu.add(aboutItem);
        return helpMenu;
    }

    private void openFile() {
        String filePath = fileManager.loadFile();
        if (!filePath.equals("not okay")) {
            tabManager.createNewTab(new File(filePath).getName());
            int activeIndex = editor.getTabbedPane().getSelectedIndex();
            editor.getFilePaths().set(activeIndex, filePath);
            String content = fileManager.readFileAsString(filePath);
            editor.getCodeTextPanes().get(activeIndex).setText(content);
        }
    }
}