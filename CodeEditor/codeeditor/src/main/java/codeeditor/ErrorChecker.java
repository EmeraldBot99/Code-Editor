package codeeditor;
import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;

public class ErrorChecker {
    private final CodeEditor editor;
    private final JTabbedPane tabbedPane;
    private final ArrayList<JTextPane> codeTextPanes;
    private final ArrayList<String> filePaths;

    public ErrorChecker(CodeEditor editor, JTabbedPane tabbedPane,
                       ArrayList<JTextPane> codeTextPanes, ArrayList<String> filePaths) {
        if (tabbedPane == null || codeTextPanes == null || filePaths == null) {
            throw new IllegalArgumentException("Parameters cannot be null");
        }
        
        this.editor = editor;
        this.tabbedPane = tabbedPane;
        this.codeTextPanes = codeTextPanes;
        this.filePaths = filePaths;
    }

    public CompletableFuture<String> findPythonErrorsAsync() {
        return CompletableFuture.supplyAsync(() -> {
            StringBuilder result = new StringBuilder();
            
            // Get current index from EDT
            int[] currentIndex = new int[1];
            SwingUtilities.invokeLater(() -> {
                currentIndex[0] = tabbedPane.getSelectedIndex();
            });
            
            try {
                // Wait a bit for the EDT to process
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "Operation interrupted";
            }

            if (currentIndex[0] >= 0 && currentIndex[0] < filePaths.size() 
                && currentIndex[0] < codeTextPanes.size()) {
                
                String filePath = filePaths.get(currentIndex[0]);
                
                if (filePath != null && filePath.endsWith(".py")) {
                    JTextPane currentPane = codeTextPanes.get(currentIndex[0]);
                    
                    if (currentPane == null) {
                        return "Error: Text pane is null";
                    }

                    // Get code from EDT
                    String[] code = new String[1];
                    SwingUtilities.invokeLater(() -> {
                        code[0] = currentPane.getText();
                    });
                    
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return "Operation interrupted";
                    }

                    // Create temporary file
                    File tempFile = null;
                    try {
                        tempFile = File.createTempFile("python_check_", ".py");
                        try (FileWriter writer = new FileWriter(tempFile)) {
                            writer.write(code[0]);
                        }

                        // Run Python with syntax checking
                        ProcessBuilder processBuilder = new ProcessBuilder(
                            "python",
                            "-c",
                            "import py_compile; py_compile.compile('" + 
                            tempFile.getAbsolutePath().replace("\\", "\\\\") + 
                            "', doraise=True)"
                        );
                        
                        processBuilder.redirectErrorStream(true);
                        Process process = processBuilder.start();
                        
                        // Read output asynchronously
                        CompletableFuture<String> outputFuture = readOutputAsync(process);
                        CompletableFuture<String> errorFuture = readErrorAsync(process);

                        // Wait for process completion
                        int exitCode = process.waitFor();
                        
                        // Get results from futures
                        String output = outputFuture.get();
                        String errorOutput = errorFuture.get();

                        if (exitCode == 0 && errorOutput.isEmpty()) {
                            result.append("No syntax errors found.");
                        } else {
                            result.append("Syntax errors found:\n");
                            if (!output.isEmpty()) {
                                result.append("Output:\n").append(output).append("\n");
                            }
                            if (!errorOutput.isEmpty()) {
                                result.append("Errors:\n").append(errorOutput).append("\n");
                            }
                        }

                    } catch (Exception e) {
                        result.append("Error: ").append(e.getMessage());
                        e.printStackTrace();
                    } finally {
                        if (tempFile != null && tempFile.exists()) {
                            tempFile.delete();
                        }
                    }
                } else {
                    result.append("Error: Invalid file path or not a Python file");
                }
            } else {
                result.append("Error: Invalid tab index or empty collections");
            }
            
            return result.toString();
        });
    }

    private CompletableFuture<String> readOutputAsync(Process process) {
        return CompletableFuture.supplyAsync(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                StringBuilder output = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
                return output.toString();
            } catch (IOException e) {
                return "Error reading output: " + e.getMessage();
            }
        });
    }

    private CompletableFuture<String> readErrorAsync(Process process) {
        return CompletableFuture.supplyAsync(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream()))) {
                StringBuilder output = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
                return output.toString();
            } catch (IOException e) {
                return "Error reading error stream: " + e.getMessage();
            }
        });
    }
}