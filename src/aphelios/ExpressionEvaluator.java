package aphelios;

import java.util.*;
import aphelios.*;

public class ExpressionEvaluator {
    public static Object evaluateExpression(String expression, Map<String, Object> variables, Map<String, Function> functions) {
        if (expression == null || expression.isEmpty()) {
            return null;
        }

        expression = expression.trim();

        if (expression.startsWith("\"") && expression.endsWith("\"")) {
            return expression.substring(1, expression.length() - 1);
        }

        if (expression.equals("true")) {
            return Boolean.TRUE;
        } else if (expression.equals("false")) {
            return Boolean.FALSE;
        }

        try {
            return Integer.parseInt(expression);
        } catch (NumberFormatException e) {
            // Not an integer, continue
        }

        try {
            return Double.parseDouble(expression);
        } catch (NumberFormatException e) {
            // Not a double, continue
        }

        if (variables.containsKey(expression)) {
            return variables.get(expression);
        }

        if (expression.contains("+") || expression.contains("-") ||
            expression.contains("*") || expression.contains("/") ||
            expression.contains("==") || expression.contains("!=") ||
            expression.contains("<") || expression.contains(">") ||
            expression.contains("<=") || expression.contains(">=")) {
            return evaluateArithmeticExpression(expression, variables, functions);
        }

        if (expression.contains("&&") || expression.contains("||")) {
            return evaluateLogicalExpression(expression, variables, functions);
        }

        if (expression.contains("(") && expression.endsWith(")")) {
            return null;
        }

        throw new InterpreterException("Invalid expression: " + expression);
    }

    public static Object evaluateArithmeticExpression(String expression, Map<String, Object> variables, Map<String, Function> functions) {
        List<String> postfix = infixToPostfix(tokenizeExpression(expression));
        return evaluatePostfix(postfix, variables, functions);
    }

    public static List<String> tokenizeExpression(String expression) {
        List<String> tokens = new ArrayList<>();
        StringBuilder currentToken = new StringBuilder();
        boolean insideQuotes = false;

        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            if (c == '"') {
                insideQuotes = !insideQuotes;
                currentToken.append(c);
            } else if (!insideQuotes && (c == '(' || c == ')' || c == '+' || c == '-' || c == '*' || c == '/' ||
                       c == '=' || c == '!' || c == '<' || c == '>' || c == ',')) {
                if (currentToken.length() > 0) {
                    tokens.add(currentToken.toString());
                    currentToken = new StringBuilder();
                }
                if (c == '=' && i > 0 && (expression.charAt(i - 1) == '=' || 
                                          expression.charAt(i - 1) == '!' || 
                                          expression.charAt(i - 1) == '<' || 
                                          expression.charAt(i - 1) == '>')) {
                    tokens.set(tokens.size() - 1, tokens.get(tokens.size() - 1) + "=");
                } else {
                    tokens.add(String.valueOf(c));
                }
            } else if (!insideQuotes && (c == '&' || c == '|')) {
                if (currentToken.length() > 0) {
                    tokens.add(currentToken.toString());
                    currentToken = new StringBuilder();
                }
                if (i + 1 < expression.length() && expression.charAt(i + 1) == c) {
                    tokens.add(c == '&' ? "&&" : "||");
                    i++; // Skip the next character
                } else {
                    tokens.add(String.valueOf(c));
                }
            } else if (!Character.isWhitespace(c) || insideQuotes) {
                currentToken.append(c);
            } else if (currentToken.length() > 0) {
                tokens.add(currentToken.toString());
                currentToken = new StringBuilder();
            }
        }
        if (currentToken.length() > 0) {
            tokens.add(currentToken.toString());
        }
        return tokens;
    }

    public static List<String> infixToPostfix(List<String> infix) {
        List<String> postfix = new ArrayList<>();
        Stack<String> operators = new Stack<>();

        for (int i = 0; i < infix.size(); i++) {
            String token = infix.get(i);
            if (token.matches("[a-zA-Z_][a-zA-Z0-9_]*") || 
                token.matches("-?\\d+(\\.\\d*)?") || 
                (token.startsWith("\"") && token.endsWith("\""))) {
                postfix.add(token);
                // If the previous token was '!', add it after the operand
                if (!operators.isEmpty() && operators.peek().equals("!")) {
                    postfix.add(operators.pop());
                }
            } else if (token.equals("(")) {
                operators.push(token);
            } else if (token.equals(")")) {
                while (!operators.isEmpty() && !operators.peek().equals("(")) {
                    postfix.add(operators.pop());
                }
                if (!operators.isEmpty() && operators.peek().equals("(")) {
                    operators.pop(); // Remove the "("
                }
            } else if (isOperator(token)) {
                if (token.equals("!")) {
                    operators.push(token);
                } else {
                    while (!operators.isEmpty() && precedence(operators.peek()) >= precedence(token)) {
                        postfix.add(operators.pop());
                    }
                    operators.push(token);
                }
            }
        }

        while (!operators.isEmpty()) {
            if (operators.peek().equals("(") || operators.peek().equals(")")) {
                throw new InterpreterException("Mismatched parentheses");
            }
            postfix.add(operators.pop());
        }

        return postfix;
    }

    public static Object evaluatePostfix(List<String> postfix, Map<String, Object> variables, Map<String, Function> functions) {
        Stack<Object> values = new Stack<>();

        for (String token : postfix) {
            if (token.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
                Object variableValue = variables.get(token);
                if (variableValue == null) {
                    throw new InterpreterException("Variable '" + token + "' is not defined.");
                }
                values.push(variableValue); 
            } else if (token.matches("-?\\d+(\\.\\d*)?")) {
                values.push(Double.parseDouble(token));
            } else if (token.startsWith("\"") && token.endsWith("\"")) {
                values.push(token.substring(1, token.length() - 1));
            } else if (isOperator(token)) {
                if (token.equals("!")) {
                    if (values.isEmpty()) {
                        throw new InterpreterException("Not enough operands for operator " + token);
                    }
                    Object a = values.pop();
                    values.push(performUnaryOperation(token, a));
                } else if (token.equals("&&") || token.equals("||")) {
                    if (values.size() < 2) {
                        throw new InterpreterException("Not enough operands for operator " + token);
                    }
                    Object b = values.pop();
                    Object a = values.pop();
                    values.push(performLogicalOperation(token, a, b));
                } else {
                    if (values.size() < 2) {
                        throw new InterpreterException("Not enough operands for operator " + token);
                    }
                    Object b = values.pop();
                    Object a = values.pop();
                    if (a == null || b == null) { throw new InterpreterException("Cannot perform operation on null operands.");
                    }
                    values.push(performOperation(token, a, b));
                }
            }
        }

        if (values.isEmpty()) {
            throw new InterpreterException("Expression evaluation resulted in no value");
        }

        if (values.size() > 1) {
            throw new InterpreterException("Too many values in the stack");
        }

        return values.pop();
    }

    private static Object performUnaryOperation(String operator, Object a) {
        if (operator.equals("!")) {
            if (a instanceof Boolean) {
                return !(Boolean) a;
            }
            throw new InterpreterException("Cannot perform logical NOT on non-boolean value");
        }
        throw new InterpreterException("Unknown unary operator: " + operator);
    }

    private static Object performLogicalOperation(String operator, Object a, Object b) {
        if (a instanceof Boolean && b instanceof Boolean) {
            boolean boolA = (Boolean) a;
            boolean boolB = (Boolean) b;
            switch (operator) {
                case "&&":
                    return boolA && boolB;
                case "||":
                    return boolA || boolB;
                default:
                    throw new InterpreterException("Unknown logical operator: " + operator);
            }
        }
        throw new InterpreterException("Cannot perform logical operation on non-boolean values");
    }

    private static boolean isOperator(String token) {
        return token.equals("+") || token.equals("-") || token.equals("*") || token.equals("/") ||
               token.equals("==") || token.equals("!=") || token.equals("<") || token.equals(">") ||
               token.equals("<=") || token.equals(">=") || token.equals("&&") || token.equals("||") ||
               token.equals("!") || token.equals(",");
    }

    private static int precedence(String operator) {
        switch (operator) {
            case ",": return 0;
            case "||": return 1;
            case "&&": return 2;
            case "==": case "!=": return 3;
            case "<": case ">": case "<=": case ">=": return 4;
            case "+": case "-": return 5;
            case "*": case "/": return 6;
            default: return -1;
        }
    }

    private static Object performOperation(String operator, Object a, Object b) {
        switch (operator) {
            case "+": return add(a, b);
            case "-": return subtract(a, b);
            case "*": return multiply(a, b);
            case "/": return divide(a, b);
            case "==": return Objects.equals(a, b);
            case "!=": return !Objects.equals(a, b);
            case "<": return compare(a, b) < 0;
            case ">": return compare(a, b) > 0;
            case "<=": return compare(a, b) <= 0;
            case ">=": return compare(a, b) >= 0;
            case "&&": return logicalAnd(a, b);
            case "||": return logicalOr(a, b);
            case ",": return concatenate(a, b);
            default: throw new InterpreterException("Unknown operator: " + operator);
        }
    }
    private static Boolean logicalAnd(Object a, Object b) {
        if (a instanceof Boolean && b instanceof Boolean) {
            return (Boolean) a && (Boolean) b;
        }
        throw new InterpreterException("Cannot perform logical AND on " + a + " and " + b);
    }

    private static Boolean logicalOr(Object a, Object b) {
        if (a instanceof Boolean && b instanceof Boolean) {
            return (Boolean) a || (Boolean) b;
        }
        throw new InterpreterException("Cannot perform logical OR on " + a + " and " + b);
    }

    private static Object add(Object a, Object b) {
        if (a instanceof Number && b instanceof Number) {
            return ((Number)a).doubleValue() + ((Number)b).doubleValue();
        } else {
            return String.valueOf(a) + String.valueOf(b);
        }
    }

    private static Object subtract(Object a, Object b) {
        if (a instanceof Number && b instanceof Number) {
            return ((Number) a).doubleValue() - ((Number) b).doubleValue();
        }
        throw new InterpreterException("Cannot subtract " + a + " and " + b);
    }

    private static Object multiply(Object a, Object b) {
        if (a instanceof Number && b instanceof Number) {
            return ((Number) a).doubleValue() * ((Number) b).doubleValue();
        }
        throw new InterpreterException("Cannot multiply " + a + " and " + b);
    }

    private static Object divide(Object a, Object b) {
        if (a instanceof Number && b instanceof Number) {
            if (((Number) b).doubleValue() == 0) {
                throw new InterpreterException("Division by zero.");
            }
            double result = ((Number) a).doubleValue() / ((Number) b).doubleValue();
            if (result == Math.floor(result)) {
                return (int) result;
            } else {
                return result;
            }
        }
        throw new InterpreterException("Cannot divide " + a + " and " + b);
    }

    private static int compare(Object a, Object b) {
        if (a instanceof Number && b instanceof Number) {
            return Double.compare(((Number) a).doubleValue(), ((Number) b).doubleValue());
        } else if (a instanceof String && b instanceof String) {
            return ((String) a).compareTo((String) b);
        }
        throw new InterpreterException("Cannot compare " + a + " and " + b);
    }

    private static String concatenate(Object a, Object b) {
        return String.valueOf(a) + String.valueOf(b);
    }

    public static Object evaluateLogicalExpression(String expression, Map<String, Object> variables, Map<String, Function> functions) {
        List<String> postfix = infixToPostfix(tokenizeExpression(expression));
        return evaluatePostfix(postfix, variables, functions);
    }

    public static List<String> splitArguments(String arguments) {
        List<String> argumentList = new ArrayList<>();
        StringBuilder currentArgument = new StringBuilder();
        boolean inQuotes = false;
        int parenCount = 0;

        for (char c : arguments.toCharArray()) {
            if (c == '"' && parenCount == 0) {
                inQuotes = !inQuotes;
                currentArgument.append(c);
            } else if (c == '(' && !inQuotes) {
                parenCount++;
                currentArgument.append(c);
            } else if (c == ')' && !inQuotes) {
                parenCount--;
                currentArgument.append(c);
            } else if (c == ',' && !inQuotes && parenCount == 0) {
                argumentList.add(currentArgument.toString().trim());
                currentArgument = new StringBuilder();
            } else {
                currentArgument.append(c);
            }
        }

        if (currentArgument.length() > 0) {
            argumentList.add(currentArgument.toString().trim());
        }

        return argumentList;
    }
}