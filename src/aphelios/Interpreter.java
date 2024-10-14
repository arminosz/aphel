package aphelios;

import java.util.*;
import aphelios.*;

public class Interpreter {
    private final Map<String, Object> variables = new HashMap<>();
    private final Map<String, Function> functions = new HashMap<>();
    public Map<String, Object> getVariables() {
        return variables;
    }

    public void interpret(List<String> lines) {
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.startsWith("FUNCTION")) {
                i = handleFunctionDefinition(lines, i);
            } else {
                i = executeLine(lines, i);
            }
        }
    }

    private int executeLine(List<String> lines, int currentIndex) {
        String line = lines.get(currentIndex).trim();
        if (line.isEmpty() || line.startsWith("//")) {
            return currentIndex;
        }

        if (line.startsWith("IF(")) {
            return handleIfStatement(lines, currentIndex);
        } else if (line.startsWith("WHILE(")) {
            return handleWhileLoop(lines, currentIndex);
        } else if (line.startsWith("FOR(")) {
            return handleForLoop(lines, currentIndex);
        } else {
            String[] statements = line.split(";");
            for (String statement : statements) {
                statement = statement.trim();
                if (!statement.isEmpty()) {
                    executeStatement(statement);
                }
            }
            return currentIndex;
        }
    }

    Object executeStatement(String statement) {
        if (statement.startsWith("PRINTC(")) {
            handlePrint(statement);
            return null;
        } else if (statement.contains("=")) {
            handleAssignment(statement);
            return null;
        } else if (statement.startsWith("IF(") || statement.startsWith("WHILE(") || statement.startsWith("FOR(")) {
            throw new InterpreterException(statement.split("\\(")[0] + " statements must be handled at the line level.");
        } else if (statement.startsWith("RETURN")) {
            return handleReturn(statement);
        } else if (statement.contains("(") && statement.endsWith(")")) {
            return handleFunctionCall(statement);
        } else {
            throw new InterpreterException("Invalid statement: " + statement);
        }
    }

    private void handlePrint(String statement) {
        String content = statement.substring(7, statement.length() - 1);
        List<String> args = ExpressionEvaluator.splitArguments(content);
        StringBuilder output = new StringBuilder();

        for (String arg : args) {
            arg = arg.trim();
            Object result = ExpressionEvaluator.evaluateExpression(arg, variables, functions);
            if (result instanceof String) {
                output.append(((String) result).replace("\\n", "\n"));
            } else {
                output.append(result != null ? result.toString() : "null");
            }
        }

        System.out.print(output.toString());
    }

    private void handleAssignment(String statement) {
        String[] parts = statement.split("=");
        String variableName = parts[0].trim();
        String expression = parts[1].trim();
        Object value = ExpressionEvaluator.evaluateExpression(expression, variables, functions);
        variables.put(variableName, value);
    }

    private int handleIfStatement(List<String> lines, int currentIndex) {
        String line = lines.get(currentIndex);
        int openParenIndex = line.indexOf('(');
        int closeParenIndex = line.lastIndexOf(')');
        if (openParenIndex == -1 || closeParenIndex == -1 || openParenIndex >= closeParenIndex) {
            throw new InterpreterException("Invalid IF statement syntax: " + line);
        }

        String condition = line.substring(openParenIndex + 1, closeParenIndex).trim();
        Object result = ExpressionEvaluator.evaluateExpression(condition, variables, functions);

        if (result instanceof Boolean && (Boolean) result) {
            if (line.endsWith("{")) {
                int blockEnd = FileUtils.findMatchingBrace(lines, currentIndex + 1);
                currentIndex++;
                while (currentIndex < blockEnd) {
                    currentIndex = executeLine(lines, currentIndex);
                    currentIndex++;
                }
                return blockEnd;
            } else {
                executeStatement(line.substring(closeParenIndex + 1).trim());
                return currentIndex;
            }
        } else {
            if (line.endsWith("{")) {
                return FileUtils.findMatchingBrace(lines, currentIndex + 1);
            } else {
                return currentIndex;
            }
        }
    }

    private int handleWhileLoop(List<String> lines, int startIndex) {
        String conditionLine = lines.get(startIndex);
        int openParenIndex = conditionLine.indexOf('(');
        int closeParenIndex = conditionLine.lastIndexOf(')');
        if (openParenIndex == -1 || closeParenIndex == -1 || openParenIndex >= closeParenIndex) {
            throw new InterpreterException("Invalid WHILE statement syntax: " + conditionLine);
        }

        String condition = conditionLine.substring(openParenIndex + 1, closeParenIndex).trim();
        int blockStart = startIndex + 1;
        int blockEnd = FileUtils.findMatchingBrace(lines, blockStart);

        while (true) {
            Object result = ExpressionEvaluator.evaluateExpression(condition, variables, functions);
            if (!(result instanceof Boolean && (Boolean) result)) {
                break;
            }

            int currentIndex = blockStart;
            while (currentIndex < blockEnd) {
                currentIndex = executeLine(lines, currentIndex);
                currentIndex++;
            }
        }

        return blockEnd;
    }

    private int handleForLoop(List<String> lines, int startIndex) {
        String forLine = lines.get(startIndex);
        String[] parts = forLine.substring(4, forLine.length() - 1).split(";");
        String init = parts[0].trim();
        String condition = parts[1].trim();
        String increment = parts[2].trim();

        executeStatement(init);

        int blockStart = startIndex + 1;
        int blockEnd = FileUtils.findMatchingBrace(lines, blockStart);

        while (true) {
            Object result = ExpressionEvaluator.evaluateExpression(condition, variables, functions);
            if (!(result instanceof Boolean && (Boolean) result)) {
                break;
            }

            int currentIndex = blockStart;
            while (currentIndex < blockEnd) {
                currentIndex = executeLine(lines, currentIndex);
                currentIndex++;
            }

            executeStatement(increment);
        }

        return blockEnd;
    }

    private Object handleReturn(String statement) {
        String expression = statement.substring(6).trim();
        Object result = ExpressionEvaluator.evaluateExpression(expression, variables, functions);
        return new ReturnValue(result);
    }

    private Object handleFunctionCall(String statement) {
        int openParenIndex = statement.indexOf('(');
        String functionName = statement.substring(0, openParenIndex).trim();
        String arguments = statement.substring(openParenIndex + 1, statement.length() - 1);

        List<String> argumentList = ExpressionEvaluator.splitArguments(arguments);
        List<Object> evaluatedArguments = new ArrayList<>();
        for (String argument : argumentList) {
            Object evaluatedArgument = ExpressionEvaluator.evaluateExpression(argument, variables, functions);
            if (!(evaluatedArgument instanceof Number)) {
                try {
                    evaluatedArgument = Double.parseDouble(evaluatedArgument.toString());
                } catch (NumberFormatException e) {
                    // Not a number, leave it as is
                }
            }
            evaluatedArguments.add(evaluatedArgument);
        }

        Function function = functions.get(functionName);
        if (function == null) {
            throw new InterpreterException("Function not defined: " + functionName);
        }
        return function.apply(evaluatedArguments, this);
    }

    private int handleFunctionDefinition(List<String> lines, int startIndex) {
        String functionLine = lines.get(startIndex);
        String functionName = functionLine.substring(9, functionLine.indexOf("(")).trim();

        String paramList = functionLine.substring(functionLine.indexOf("(") + 1, functionLine.indexOf(")")).trim();
        List<String> params = ExpressionEvaluator.splitArguments(paramList);

        List<String> body = new ArrayList<>();
        int endIndex = startIndex + 1;
        while (endIndex < lines.size() && !lines.get(endIndex).trim().equals("}")) {
            body.add(lines.get(endIndex).trim());
            endIndex++;
        }

        functions.put(functionName, new Function(params, body));
        return endIndex;
    }
}