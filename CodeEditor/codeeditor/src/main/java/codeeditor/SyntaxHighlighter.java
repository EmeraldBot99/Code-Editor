// File: SyntaxHighlighter.java
package codeeditor;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class SyntaxHighlighter {
    private final StyleContext styleContext;
    private final Style defaultStyle;
    private final ArrayList<Timer> highlightTimers = new ArrayList<>();
    
    private static final Pattern KEYWORDS = Pattern.compile("\\b(public|private|class|void|int|String)\\b");
    private static final Pattern STRINGS = Pattern.compile("\"[^\"\\\\]*(\\\\.[^\"\\\\]*)*\"");
    private static final Pattern NUMBERS = Pattern.compile("\\b\\d+\\b");
    private static final Pattern COMMENTS = Pattern.compile("//.*?$|/\\*[\\s\\S]*?\\*/");

    public SyntaxHighlighter() {
        styleContext = new StyleContext();
        defaultStyle = styleContext.getStyle(StyleContext.DEFAULT_STYLE);
        setupStyles();
    }

    private void setupStyles() {
        Style keywordStyle = styleContext.addStyle("keyword", defaultStyle);
        StyleConstants.setForeground(keywordStyle, new Color(127, 0, 85));

        Style stringStyle = styleContext.addStyle("string", defaultStyle);
        StyleConstants.setForeground(stringStyle, new Color(42, 161, 152));

        Style numberStyle = styleContext.addStyle("number", defaultStyle);
        StyleConstants.setForeground(numberStyle, new Color(0, 128, 0));

        Style commentStyle = styleContext.addStyle("comment", defaultStyle);
        StyleConstants.setForeground(commentStyle, new Color(128, 128, 128));
    }

    public void highlightSyntax(int index, ArrayList<JTextPane> codeTextPanes) {
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
}