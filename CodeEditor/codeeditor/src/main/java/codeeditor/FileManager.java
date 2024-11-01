package codeeditor;

import javax.swing.*;
import javax.swing.tree.*;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class FileManager {
    private final CodeEditor editor;
    private final TabManager tabs;
    private boolean listenersInitialized = false;

    public FileManager(CodeEditor editor, TabManager tab) {
        this.editor = editor;
        this.tabs = tab;
    }

    private void initializeListeners() {
        if (listenersInitialized || editor.getSideMenu() == null) {
            return;
            
        }

        editor.getSideMenu().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    TreePath path = editor.getSideMenu().getPathForLocation(e.getX(), e.getY());
                    if (path != null) {
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                        if (node.getUserObject() instanceof FileNode) {
                            FileNode fileNode = (FileNode) node.getUserObject();
                            File file = fileNode.getFile();
                            String filePath = file.getAbsolutePath();

                            tabs.createNewTab(file.getName());

                            String content = readFileAsString(filePath);
                            int activeIndex = editor.getTabbedPane().getSelectedIndex();

                            editor.getCodeTextPanes().get(activeIndex).setText(content);
                        }
                    }
                }
            }
        });
        
        listenersInitialized = true;
    }

    public String loadFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        int result = fileChooser.showOpenDialog(editor);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            return selectedFile.getAbsolutePath();
        }
        return "not okay";
    }

    public void openFolder() {
        String openFolderPath = loadFile();
        if ("not okay".equals(openFolderPath)) {
            return;
        }
        
        File folder = new File(openFolderPath);

        if (!folder.isDirectory()) {
            JOptionPane.showMessageDialog(null, "Selected path is not a folder.");
            return;
        }
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(new FileNode(folder));
        addFolderContents(root, folder);

        editor.getSideMenu().setModel(new DefaultTreeModel(root));
        editor.getSideMenu().setPreferredSize(new Dimension(200, 0));
        
        // Initialize listeners after side menu is set up
        initializeListeners();
    }

    private void addFolderContents(DefaultMutableTreeNode parentNode, File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(new FileNode(file));
                if (file.isDirectory()) {
                    addFolderContents(childNode, file);
                }
                parentNode.add(childNode);
            }
        }
    }

    public void saveFile(String outputPath, String textData, int activeIndex) {
        if (outputPath == null) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
            int result = fileChooser.showSaveDialog(editor);
            if (result == JFileChooser.APPROVE_OPTION) {
                outputPath = fileChooser.getSelectedFile().getAbsolutePath();
                editor.getFilePaths().set(activeIndex, outputPath);
                // Update tab title
                JPanel tabPanel = (JPanel) editor.getTabbedPane().getTabComponentAt(activeIndex);
                ((JLabel) tabPanel.getComponent(0)).setText(new File(outputPath).getName() + " ");
            }
        }
        
        if (outputPath != null) {
            try {
                Files.write(Paths.get(outputPath), textData.getBytes(StandardCharsets.UTF_8));
                System.out.println("File saved successfully: " + outputPath);
            } catch(IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(editor,
                    "Error saving file: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public String readFileAsString(String path) {
        try {
            return Files.readString(Paths.get(path));
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(editor,
                "Error reading file: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return "Unable to load file";
        }
    }
}