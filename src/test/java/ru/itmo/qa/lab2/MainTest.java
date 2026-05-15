package ru.itmo.qa.lab2;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

public class MainTest {
  @TempDir
  Path tempDir;

  @BeforeEach
  void setUp() {
    Path plotsDir = tempDir.resolve("plots");
    try {
      Files.createDirectories(plotsDir);
    } catch (IOException e) {
      throw new RuntimeException("Failed to create test directory", e);
    }

    Main.setOutputDir(plotsDir.toString());
  }

  @Test
  @DisplayName("Main should generate CSV files for all functions")
  void shouldGenerateFunctionDataFiles() throws IOException {
    Main.main(new String[] {});

    String[] expectedFiles = {
        "Sine.csv", "Cosine.csv", "Secant.csv", "Cosecant.csv",
        "Tangent.csv", "Cotangent.csv", "NaturalLogarithm.csv",
        "BaseNLogarithm_2.csv", "BaseNLogarithm_3.csv", "BaseNLogarithm_10.csv",
        "BaseNLogarithm_12.csv", "EquationSystem.csv"
    };

    for (String filename : expectedFiles) {
      Path filePath = tempDir.resolve("plots" + File.separator + filename);
      assertTrue(Files.exists(filePath), "Missing file: " + filename);
      assertTrue(Files.size(filePath) > 0, "Empty file: " + filename);
    }
  }

  @Test
  @DisplayName("CSV files should contain valid data")
  void testCsvContent() throws IOException {
    Main.main(new String[] {});

    Path sineFile = tempDir.resolve("plots" + File.separator + "Sine.csv");
    String content = Files.readString(sineFile);

    assertTrue(content.startsWith("x,y"));
  }

  @Test
  @DisplayName("Should handle IOException in main method")
  void testMainWithIOException() {
    PrintStream originalErr = System.err;

    try {
      ByteArrayOutputStream errContent = new ByteArrayOutputStream();
      System.setErr(new PrintStream(errContent));

      var main = new Main();
      main.setOutputDir("/invalid/path/");
      main.main(new String[] {});

      assertTrue(errContent.toString().contains("Ошибка при работе с файлами"));
    } finally {
      System.setErr(originalErr);
    }
  }
}
