package codeeditor;

import javax.swing.*;
import javax.swing.text.Element;
import java.awt.*;
import java.awt.geom.Rectangle2D;

public class LineNumberComponent extends JPanel {
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
                Rectangle2D r = textPane.modelToView2D(line.getStartOffset());
                
                int lineNumber = i + 1;
                String number = String.valueOf(lineNumber);
                
                int stringWidth = fontMetrics.stringWidth(number);
                int x = width - stringWidth - MARGIN;
                
                g2d.setColor(Color.GRAY);
                g2d.drawString(number, x, (int)(r.getY() + fontMetrics.getAscent()));
                
            } catch (Exception e) {
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