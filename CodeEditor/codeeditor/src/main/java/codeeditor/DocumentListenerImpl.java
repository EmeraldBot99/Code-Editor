package codeeditor;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class DocumentListenerImpl implements DocumentListener {
    private final LineNumberComponent lineNumbers;
    private final Runnable highlightAction;

    public DocumentListenerImpl(LineNumberComponent lineNumbers, Runnable highlightAction) {
        this.lineNumbers = lineNumbers;
        this.highlightAction = highlightAction;
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        lineNumbers.repaint();
        highlightAction.run();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        lineNumbers.repaint();
        highlightAction.run();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        lineNumbers.repaint();
        highlightAction.run();
    }
}