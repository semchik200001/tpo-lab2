package ru.itmo.qa.lab2.function.module;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import ru.itmo.qa.lab2.EquationSystem;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.stream.Stream;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EquationSystemTest {

  private static final BigDecimal DEFAULT_PRECISION = new BigDecimal("0.0000001");

  private EquationSystem system;

  @BeforeEach
  void init() {
    system = new EquationSystem();
  }

  @Test
  void shouldNotAcceptNullArgument() {
    assertThrows(NullPointerException.class, () -> system.calculate(null, DEFAULT_PRECISION));
  }

  @Test
  void shouldNotAcceptNullPrecision() {
    BigDecimal arg = new BigDecimal(-2);
    assertThrows(NullPointerException.class, () -> system.calculate(arg, null));
  }

  @ParameterizedTest
  @MethodSource("illegalPrecisions")
  void shouldNotAcceptIncorrectPrecisions(final BigDecimal precision) {
    BigDecimal arg = new BigDecimal(-2);
    assertThrows(ArithmeticException.class, () -> system.calculate(arg, precision));
  }

  @ParameterizedTest
  @ValueSource(doubles = {-Math.PI / 2, -3 * Math.PI / 2, -5 * Math.PI / 2})
  void shouldNotAcceptCosineZeros(double x) {
    BigDecimal arg = BigDecimal.valueOf(x).setScale(10, RoundingMode.HALF_EVEN);
    assertThrows(ArithmeticException.class, () -> system.calculate(arg, DEFAULT_PRECISION));
  }

  @Test
  void shouldNotAccept1() {
    Throwable exception = assertThrows(ArithmeticException.class, () -> system.calculate(BigDecimal.ONE, DEFAULT_PRECISION));
    String msg = format("У функции нет значения при x = %s", BigDecimal.ONE);
    assertEquals(msg, exception.getMessage());
  }

  @ParameterizedTest(name = "f({0}) = {1}")
  @CsvFileSource(resources = "/system.csv", numLinesToSkip = 1, delimiter = ',')
  void testSystem(BigDecimal x, BigDecimal y) {
    assertEquals(y, system.calculate(x, DEFAULT_PRECISION));
  }

  private static Stream<Arguments> illegalPrecisions() {
    return Stream.of(
      Arguments.of(BigDecimal.valueOf(1)),
      Arguments.of(BigDecimal.valueOf(0)),
      Arguments.of(BigDecimal.valueOf(1.01)),
      Arguments.of(BigDecimal.valueOf(-0.01))
    );
  }
}
