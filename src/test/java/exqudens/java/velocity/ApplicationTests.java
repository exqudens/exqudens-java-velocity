package exqudens.java.velocity;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ApplicationTests {

    @Test
    public void test1(@TempDir Path tempDir) throws Throwable {
        var templateContent = String.join("\n",
                "#set($greetings = 'Hello')",
                "$greetings $name!",
                ""
        );
        var templateFile = tempDir.resolve("template.html");
        var outputFile = tempDir.resolve("output.html");

        Files.writeString(templateFile, templateContent);

        var args = List.of(
                "-t", templateFile.toFile().getAbsolutePath(),
                "-c", "name=\"John Dou\"",
                "-o", outputFile.toFile().getAbsolutePath()
        );
        var app = new Application(args);

        app.run();

        var expected = String.join("\n",
                "Hello \"John Dou\"!",
                ""
        );
        var actual = Files.readString(outputFile);

        Assertions.assertEquals(expected, actual);
    }

}
