package aphelios;

import java.io.IOException;
import java.util.List;

public class Aphel {
    public static boolean DEBUG = false;

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java Aphel [-d] <filename>");
            return;
        }

        String filename = args[args.length - 1];
        if (args.length > 1 && args[0].equals("-d")) {
            DEBUG = true;
        }

        try {
            List<String> lines = FileUtils.readFile(filename);
            Interpreter interpreter = new Interpreter();
            interpreter.interpret(lines);
        } catch (InterpreterException e) {
            System.err.println("Interpreter error: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }

    public static void debug(String message) {
        if (DEBUG) {
            System.out.println("[DEBUG] " + message);
        }
    }
}