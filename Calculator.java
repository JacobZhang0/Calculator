import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class Calculator {
    private static JTextField displayField;

    public static void main(String[] args) {
        JFrame mainFrame = new JFrame();
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setTitle("Scientific Calculator - by Jacob");
        mainFrame.setSize(300, 400);
        mainFrame.setResizable(false);
        mainFrame.getContentPane().setBackground(Color.WHITE);
        mainFrame.setLocationRelativeTo(null);

        ImageIcon AppIcon = new ImageIcon("CalculatorIcon.png");
        mainFrame.setIconImage(AppIcon.getImage());

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        displayField = new JTextField();
        displayField.setHorizontalAlignment(SwingConstants.RIGHT);
        displayField.setEditable(true);
        displayField.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        displayField.setPreferredSize(new Dimension(displayField.getPreferredSize().width, 30));
        displayField.addKeyListener(new EnterKeyListener());
        panel.add(displayField, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(6, 4, 5, 5));

        String[] buttonLabels = {
                "7", "8", "9", "/",
                "4", "5", "6", "*",
                "1", "2", "3", "-",
                "0", ".", "=", "+",
                "sin", "cos", "tan", "sqrt",
                "log", "ln", "x^2", "C"
        };

        for (String label : buttonLabels) {
            JButton button = new JButton(label);
            button.addActionListener(new ButtonClickListener());
            buttonPanel.add(button);
        }

        panel.add(buttonPanel, BorderLayout.CENTER);
        mainFrame.add(panel);
        mainFrame.setVisible(true);
    }

    static class ButtonClickListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            JButton source = (JButton) e.getSource();
            String buttonText = source.getText();

            if (buttonText.equals("=")) {
                calculateExpression();
            } else if (buttonText.equals("C")) {
                displayField.setText("");
            } else {
                String currentText = displayField.getText();
                displayField.setText(currentText + buttonText);
            }
        }

        private void calculateExpression() {
            String expression = displayField.getText();
            try {
                double result = CalculatorEngine.evaluate(expression);
                displayField.setText(String.valueOf(result));
            } catch (IllegalArgumentException ex) {
                displayField.setText("Error");
            }
        }
    }

    static class EnterKeyListener extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                calculateExpression();
            }
        }

        private void calculateExpression() {
            String expression = displayField.getText();
            try {
                double result = CalculatorEngine.evaluate(expression);
                displayField.setText(String.valueOf(result));
            } catch (IllegalArgumentException ex) {
                displayField.setText("Error");
            }
        }
    }
}

class CalculatorEngine {
    public static double evaluate(String expression) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < expression.length()) ? expression.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parseExpression() {
                nextChar();
                double x = parseTerm();
                while (true) {
                    if (eat('+')) x += parseTerm();
                    else if (eat('-')) x -= parseTerm();
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                while (true) {
                    if (eat('*')) x *= parseFactor();
                    else if (eat('/')) x /= parseFactor();
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return parseFactor();
                if (eat('-')) return -parseFactor();

                double x;
                int startPos = this.pos;
                if (eat('(')) {
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') {
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(expression.substring(startPos, this.pos));
                } else if (ch >= 'a' && ch <= 'z') {
                    while (ch >= 'a' && ch <= 'z') nextChar();
                    String func = expression.substring(startPos, this.pos);
                    x = evaluateFunction(func, parseFactor());
                } else {
                    throw new IllegalArgumentException("Invalid expression");
                }

                return x;
            }

            double evaluateFunction(String func, double value) {
                // Handle the supported functions
                if (func.equals("sin")) return Math.sin(value);
                if (func.equals("cos")) return Math.cos(value);
                if (func.equals("tan")) return Math.tan(value);
                if (func.equals("sqrt")) return Math.sqrt(value);
                if (func.equals("log")) return Math.log10(value);
                if (func.equals("ln")) return Math.log(value);
                if (func.equals("x^2")) return Math.pow(value, 2);
                throw new IllegalArgumentException("Unknown function: " + func);
            }
        }.parseExpression();
    }
}
