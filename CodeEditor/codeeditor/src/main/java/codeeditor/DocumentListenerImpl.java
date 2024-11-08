package codeeditor;

import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class DocumentListenerImpl implements DocumentListener {
    private final LineNumberComponent lineNumbers;
    private final Runnable highlightAction;
    private final ErrorChecker errorChecker;

    public DocumentListenerImpl(LineNumberComponent lineNumbers, Runnable highlightAction, ErrorChecker errorChecker) {
        this.lineNumbers = lineNumbers;
        this.highlightAction = highlightAction;
        this.errorChecker = errorChecker;
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        lineNumbers.repaint();
        highlightAction.run();
        errorChecker.findPythonErrorsAsync()
        .thenAccept(result -> {
            SwingUtilities.invokeLater(() -> {
                System.out.println(result);
            });
        })
        .exceptionally(throwable -> {
            SwingUtilities.invokeLater(() -> {
                System.err.println("Error checking syntax: " + throwable.getMessage());
            });
            return null;
        });
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        lineNumbers.repaint();
        highlightAction.run();
        errorChecker.findPythonErrorsAsync()
        .thenAccept(result -> {
            SwingUtilities.invokeLater(() -> {
                System.out.println(result);
            });
        })
        .exceptionally(throwable -> {
            SwingUtilities.invokeLater(() -> {
                System.err.println("Error checking syntax: " + throwable.getMessage());
            });
            return null;
        });
    }
      

    @Override
    public void changedUpdate(DocumentEvent e) {
        lineNumbers.repaint();
        highlightAction.run();
        //  errorChecker.findPythonErrors();
    }
}