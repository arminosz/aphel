package aphelios;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import aphelios.*;

public class FileUtils {
    public static List<String> readFile(String filename) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line.trim());
            }
        }
        return lines;
    }

    public static int findMatchingBrace(List<String> lines, int start) {
        int braceCount = 1;
        for (int i = start; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.equals("{")) {
                braceCount++;
            } else if (line.equals("}")) {
                braceCount--;
                if (braceCount == 0) {
                    return i;
                }
            }
        }
        throw new InterpreterException("Unmatched brace");
    }
}