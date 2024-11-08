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
    
    // Updated patterns for Python syntax
    private static final Pattern KEYWORDS = Pattern.compile("\\b(def|class|if|elif|else|while|for|in|try|except|finally|with|" +
            "return|break|continue|pass|raise|from|import|as|global|nonlocal|lambda|and|or|not|is|None|True|False)\\b");
    private static final Pattern BUILTINS = Pattern.compile("\\b(print|len|range|str|int|float|list|dict|set|tuple|" +
            "super|object|isinstance|type|input|open|sum|min|max|abs|round)\\b");
    // Variables can start with letter or underscore, followed by letters, numbers, or underscores
    // We use negative lookahead (?!) to exclude keywords and built-ins
    private static final Pattern VARIABLES = Pattern.compile("(?![0-9])\\b(?!(" +
            "def|class|if|elif|else|while|for|in|try|except|finally|with|" +
            "return|break|continue|pass|raise|from|import|as|global|nonlocal|lambda|and|or|not|is|None|True|False|" +
            "print|len|range|str|int|float|list|dict|set|tuple|super|object|isinstance|type|input|open|sum|min|max|abs|round" +
            ")\\b)[_a-zA-Z]\\w*\\b");
    private static final Pattern STRINGS = Pattern.compile("(?:\"\"\"[\\s\\S]*?\"\"\"|'''[\\s\\S]*?'''|" +  // Triple quotes
            "\"[^\"\\\\]*(\\\\.[^\"\\\\]*)*\"|" +  // Double quotes
            "'[^'\\\\]*(\\\\.[^'\\\\]*)*')");     // Single quotes
    private static final Pattern NUMBERS = Pattern.compile("\\b\\d*\\.?\\d+([eE][+-]?\\d+)?\\b");  // Includes floating point
    private static final Pattern COMMENTS = Pattern.compile("#.*?$|'''[\\s\\S]*?'''|\"\"\"[\\s\\S]*?\"\"\"", 
            Pattern.MULTILINE);  // Single line and docstrings
    private static final Pattern DECORATORS = Pattern.compile("@\\w+(?:\\.[\\w.]+)*");
    private static final Pattern SELF = Pattern.compile("\\bself\\b");
    // Pattern for function definitions (to highlight parameters)
    private static final Pattern FUNCTION_PARAMS = Pattern.compile(
            "def\\s+\\w+\\s*\\((.*?)\\)\\s*:",
            Pattern.MULTILINE);
    // Pattern for class attributes (after self.)
    private static final Pattern CLASS_ATTRIBUTES = Pattern.compile(
            "self\\.([a-zA-Z_][a-zA-Z0-9_]*)",
            Pattern.MULTILINE);

    public SyntaxHighlighter() {
        styleContext = new StyleContext();
        defaultStyle = styleContext.getStyle(StyleContext.DEFAULT_STYLE);
        setupStyles();
    }

    private void setupStyles() {
        // Keywords - purple
        Style keywordStyle = styleContext.addStyle("keyword", defaultStyle);
        StyleConstants.setForeground(keywordStyle, new Color(127, 0, 85));
        StyleConstants.setBold(keywordStyle, true);

        // Builtins - blue
        Style builtinStyle = styleContext.addStyle("builtin", defaultStyle);
        StyleConstants.setForeground(builtinStyle, new Color(0, 87, 173));

        // Variables - dark cyan
        Style variableStyle = styleContext.addStyle("variable", defaultStyle);
        StyleConstants.setForeground(variableStyle, new Color(0, 124, 124));

        // Function parameters - teal
        Style paramStyle = styleContext.addStyle("parameter", defaultStyle);
        StyleConstants.setForeground(paramStyle, new Color(0, 148, 148));
        StyleConstants.setItalic(paramStyle, true);

        // Class attributes - violet
        Style attributeStyle = styleContext.addStyle("attribute", defaultStyle);
        StyleConstants.setForeground(attributeStyle, new Color(145, 0, 145));

        // Strings - green
        Style stringStyle = styleContext.addStyle("string", defaultStyle);
        StyleConstants.setForeground(stringStyle, new Color(42, 161, 152));

        // Numbers - orange
        Style numberStyle = styleContext.addStyle("number", defaultStyle);
        StyleConstants.setForeground(numberStyle, new Color(211, 84, 0));

        // Comments - gray
        Style commentStyle = styleContext.addStyle("comment", defaultStyle);
        StyleConstants.setForeground(commentStyle, new Color(128, 128, 128));
        StyleConstants.setItalic(commentStyle, true);

        // Decorators - dark blue
        Style decoratorStyle = styleContext.addStyle("decorator", defaultStyle);
        StyleConstants.setForeground(decoratorStyle, new Color(0, 134, 179));

        // Self - purple italic
        Style selfStyle = styleContext.addStyle("self", defaultStyle);
        StyleConstants.setForeground(selfStyle, new Color(127, 0, 85));
        StyleConstants.setItalic(selfStyle, true);
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
                
                // Order matters: highlight in reverse precedence
                highlightPattern(doc, VARIABLES, "variable");   // Variables last to avoid overriding other patterns
                highlightPattern(doc, KEYWORDS, "keyword");
                highlightPattern(doc, BUILTINS, "builtin");
                highlightPattern(doc, NUMBERS, "number");
                highlightPattern(doc, SELF, "self");
                highlightFunctionParams(doc);                   // Handle function parameters specially
                highlightPattern(doc, CLASS_ATTRIBUTES, "attribute");
                highlightPattern(doc, COMMENTS, "comment");     // Comments first to prevent interference
                highlightPattern(doc, STRINGS, "string");       // Strings before keywords
                highlightPattern(doc, DECORATORS, "decorator"); // Decorators before keywords
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

    private void highlightFunctionParams(StyledDocument doc) {
        try {
            String text = doc.getText(0, doc.getLength());
            java.util.regex.Matcher matcher = FUNCTION_PARAMS.matcher(text);
            
            while(matcher.find() && matcher.group(1) != null) {
                String params = matcher.group(1);
                // Split parameters and highlight each one
                String[] paramList = params.split(",");
                int startPos = matcher.start(1);
                
                for(String param : paramList) {
                    param = param.trim();
                    if(!param.isEmpty() && !param.equals("self")) {
                        // Handle parameters with default values
                        String[] parts = param.split("=");
                        String paramName = parts[0].trim();
                        int paramStart = text.indexOf(paramName, startPos);
                        if(paramStart >= 0) {
                            doc.setCharacterAttributes(paramStart, 
                                paramName.length(),
                                styleContext.getStyle("parameter"), 
                                false);
                            startPos = paramStart + paramName.length();
                        }
                    }
                }
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
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