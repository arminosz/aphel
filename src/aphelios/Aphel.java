package aphelios;

import java.io.IOException;
import java.util.List;

public class Aphel {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java Aphel <filename>");
            return;
        }

        try {
            List<String> lines = FileUtils.readFile(args[0]);
            Interpreter interpreter = new Interpreter();
            interpreter.interpret(lines);
        } catch (InterpreterException e) {
            System.err.println("Interpreter error: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }
}