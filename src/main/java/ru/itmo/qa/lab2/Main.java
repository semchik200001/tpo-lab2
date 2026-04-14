package ru.itmo.qa.lab2;

import ru.itmo.qa.lab2.function.AbstractFunction;
import ru.itmo.qa.lab2.log.BaseNLogarithm;
import ru.itmo.qa.lab2.log.NaturalLogarithm;
import ru.itmo.qa.lab2.trig.*;
import ru.itmo.qa.lab2.util.CSVGraphWriter;
import ru.itmo.qa.lab2.util.FunctionGraph;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;

import static java.math.BigDecimal.ONE;
import static java.math.RoundingMode.HALF_EVEN;

/**
 * Вариант 46879.
 *
 * Базовые функции:
 *  - sin(x) — ряд Тейлора
 *  - ln(x)  — ряд для arctanh ((z-1)/(z+1))
 *
 * Зависимости:
 * | Класс            | Зависимости      | Формула              |
 * |------------------|------------------|----------------------|
 * | Sine             | —                | Ряд Тейлора          |
 * | Cosine           | Sine             | sin(π/2 - x)         |
 * | Cosecant         | Sine             | 1/sin(x)             |
 * | Secant           | Cosine           | 1/cos(x)             |
 * | Tangent          | Sine + Cosine    | sin(x)/cos(x)        |
 * | Cotangent        | Sine + Cosine    | cos(x)/sin(x)        |
 * | NaturalLogarithm | —                | Ряд                  |
 * | BaseNLogarithm   | NaturalLogarithm | ln(x)/ln(base)       |
 *
 * Система функций варианта 46879:
 *  x ≤ 0 : (((cos(x) + cos(x)) * sec(x)^3) + cos(x))
 *  x > 0 : ((((log_2(x) * log_5(x) / log_2(x)) * log_2(x))^2) + (log_2(x) / log_10(x)))
 */
public class Main {

  private static String outputDir = System.getProperty("user.dir") + File.separator + "plots" + File.separator;

  private static final BigDecimal PRECISION = new BigDecimal("0.0000001");
  private static final BigDecimal POSITIVE_END = new BigDecimal(10).setScale(7, HALF_EVEN);
  private static final BigDecimal NEGATIVE_END = POSITIVE_END.negate();
  private static final BigDecimal STEP = new BigDecimal("0.01");

  public static void main(String[] args) {
    try {
      generateFunctionData();
      displayAllPlots();
    } catch (IOException e) {
      System.err.println("Ошибка при работе с файлами: " + e.getMessage());
      e.printStackTrace();
    }
  }

  public static void setOutputDir(String path) {
    outputDir = path.endsWith(File.separator) ? path : path + File.separator;
  }

  private static void generateFunctionData() throws IOException {
    new CSVGraphWriter(new Sine(), outputDir).write(NEGATIVE_END, POSITIVE_END, STEP, PRECISION);
    new CSVGraphWriter(new Cosine(), outputDir).write(NEGATIVE_END, POSITIVE_END, STEP, PRECISION);
    new CSVGraphWriter(new Secant(), outputDir).write(NEGATIVE_END, POSITIVE_END, STEP, PRECISION);
    new CSVGraphWriter(new Cosecant(), outputDir).write(NEGATIVE_END, POSITIVE_END, STEP, PRECISION);
    new CSVGraphWriter(new Tangent(), outputDir).write(NEGATIVE_END, POSITIVE_END, STEP, PRECISION);
    new CSVGraphWriter(new Cotangent(), outputDir).write(NEGATIVE_END, POSITIVE_END, STEP, PRECISION);
    new CSVGraphWriter(new NaturalLogarithm(), outputDir).write(NEGATIVE_END, POSITIVE_END, STEP, PRECISION);
    new CSVGraphWriter(new BaseNLogarithm(2), outputDir).write(NEGATIVE_END, POSITIVE_END, STEP, PRECISION);
    new CSVGraphWriter(new BaseNLogarithm(3), outputDir).write(NEGATIVE_END, POSITIVE_END, STEP, PRECISION);
    new CSVGraphWriter(new BaseNLogarithm(10), outputDir).write(NEGATIVE_END, POSITIVE_END, STEP, PRECISION);
    new CSVGraphWriter(new EquationSystem(), outputDir).write(NEGATIVE_END, POSITIVE_END, STEP, PRECISION);
  }

  private static void displayAllPlots() {
    Map<AbstractFunction, String> functionMap = new HashMap<>() {
      {
        put(new Sine(), "sin(x)");
        put(new Cosine(), "cos(x)");
        put(new Secant(), "sec(x)");
        put(new Cosecant(), "csc(x)");
        put(new Tangent(), "tan(x)");
        put(new Cotangent(), "cot(x)");
        put(new NaturalLogarithm(), "ln(x)");
        put(new BaseNLogarithm(2), "log2(x)");
        put(new BaseNLogarithm(3), "log3(x)");
        put(new BaseNLogarithm(10), "log10(x)");
        put(new EquationSystem(), "f(x)");
      }
    };

    List<String> trimFunctions = List.of("tan(x)", "cot(x)", "sec(x)", "csc(x)", "f(x)");

    for (Map.Entry<AbstractFunction, String> entry : functionMap.entrySet()) {
      CSVGraphWriter writer = new CSVGraphWriter(entry.getKey(), outputDir);
      writer.write(NEGATIVE_END, POSITIVE_END, STEP, PRECISION);

      FunctionGraph graph = new FunctionGraph(
          "График функции: " + entry.getValue(),
          entry.getValue(),
          writer.getFilePath(),
          trimFunctions.contains(entry.getValue())
      );

      graph.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
      graph.pack();
      graph.setLocationRelativeTo(null);
      graph.setVisible(true);
    }
  }
}
