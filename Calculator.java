import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Calculator {
    private JFrame frame;
    private JTextField entry;

    public Calculator() {
        frame = new JFrame("Calculator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        entry = new JTextField(20);
        frame.add(entry, BorderLayout.NORTH);

        JPanel panel = new JPanel(new GridLayout(5, 4));
        String[] buttons = {"7", "8", "9", "/",
                "4", "5", "6", "*",
                "1", "2", "3", "-",
                "0", ".", "=", "+",
                "Clear"};

        for (String button : buttons) {
            JButton btn = new JButton(button);
            btn.addActionListener(new ButtonListener(button));
            panel.add(btn);
        }

        frame.add(panel, BorderLayout.CENTER);

        frame.pack();
        frame.setVisible(true);
    }

    private class ButtonListener implements ActionListener {
        private String button;

        public ButtonListener(String button) {
            this.button = button;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (button.equals("=")) {
                try {
                    double result = eval(entry.getText());
                    entry.setText(String.valueOf(result));
                } catch (Exception ex) {
                    entry.setText("Error");
                }
            } else if (button.equals("Clear")) {
                entry.setText("");
            } else {
                entry.setText(entry.getText() + button);
            }
        }

        private double eval(String expression) {
            return new Object() {
                int pos = -1, ch;

                void nextChar() {
                    ch = (++pos < expression.length())? expression.charAt(pos) : -1;
                }

                boolean eat(int charToEat) {
                    while (ch == ' ')nextChar();
                    if (ch == charToEat) {
                        nextChar();
                        return true;
                    }
                    return false;
                }

                double parse() {
                    nextChar();
                    double v = parseExpression();
                    if (ch!= -1) throw new RuntimeException("Unexpected: " + (char) ch);
                    return v;
                }

                // Grammar:
                // expression = term | expression `+` term | expression `-` term
                // term = factor | term `*` factor | term `/` factor
                // factor = `+` factor | `-` factor | `(` expression `)`
                //        | number | functionName factor | factor `^` factor

                double parseExpression() {
                    double v = parseTerm();
                    while (true) {
                        if (eat('+')) {
                            v += parseTerm(); // addition
                        } else if (eat('-')) {
                            v -= parseTerm(); // subtraction
                        } else {
                            break;
                        }
                    }
                    return v;
                }

                double parseTerm() {
                    double v = parseFactor();
                    while (true) {
                        if (eat('*')) {
                            v *= parseFactor(); // multiplication
                        } else if (eat('/')) {
                            v /= parseFactor(); // division
                        } else {
                            break;
                        }
                    }
                    return v;
                }

                double parseFactor() {
                    if (eat('+')) return parseFactor(); // unary plus
                    if (eat('-')) return -parseFactor(); // unary minus

                    double v;
                    int startPos = this.pos;
                    if (eat('(')) { // parentheses
                        v = parseExpression();
                        eat(')');
                    } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
                        while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                        v = Double.parseDouble(expression.substring(startPos, this.pos));
                    } else if (ch >= 'a' && ch <= 'z') { // functions
                        while (ch >= 'a' && ch <= 'z') nextChar();
                        String func = expression.substring(startPos, this.pos);
                        v = parseFactor();
                        if (func.equals("sqrt")) v = Math.sqrt(v);
                        else if (func.equals("sin")) v = Math.sin(Math.toRadians(v));
                        else if (func.equals("cos")) v = Math.cos(Math.toRadians(v));
                        else if (func.equals("tan")) v = Math.tan(Math.toRadians(v));
                        else throw new RuntimeException("Unknown function: " + func);
                    } else {
                        throw new RuntimeException("Unexpected: " + (char) ch);
                    }

                    return v;
                }
            }.parse();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Calculator();
            }
        });
    }
}