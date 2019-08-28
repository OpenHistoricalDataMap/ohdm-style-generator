package util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

public class FileHelper {

    public static void deleteDirectory(Path path) throws IOException {
        Files.walk(path)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    public static String getFileContentAsString(File file) throws IOException {
        StringBuilder sb = new StringBuilder();
        Files.lines(Paths.get(file.getAbsolutePath()), StandardCharsets.UTF_8).forEach((string) -> {
            sb.append(string);
            sb.append("\n");
        });
        return sb.toString();
    }
}
