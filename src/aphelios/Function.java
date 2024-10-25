package aphelios;

import java.util.*;

public class Function {
    List<String> params;
    List<String> body;

    Function(List<String> params, List<String> body) {
        this.params = params;
        this.body = body;
    }

    public Object apply(List<Object> arguments, Interpreter interpreter) {
        if (arguments.size() != params.size()) {
            throw new InterpreterException("Incorrect number of arguments for function");
        }

        Map<String, Object> localVariables = new HashMap<>();
        for (int i = 0; i < params.size(); i++) {
            localVariables.put(params.get(i), arguments.get(i));
        }

        Map<String, Object> oldVariables = new HashMap<>(interpreter.getVariables());
        interpreter.getVariables().putAll(localVariables);

        Object result = null;
        for (String line : body) {
            result = interpreter.executeStatement(line);
            if (result instanceof ReturnValue) {
                result = ((ReturnValue) result).value;
                break;
            }
        }

        interpreter.getVariables().clear();
        interpreter.getVariables().putAll(oldVariables);

        return result;
    }
}