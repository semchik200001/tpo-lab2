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
  @MethodSource("illegalValuesLt0")
  void shouldNotAcceptIncorrectValuesForLt0(final BigDecimal x) {
    assertThrows(ArithmeticException.class, () -> system.calculate(x, DEFAULT_PRECISION));
  }

  @ParameterizedTest
  @ValueSource(doubles = {-Math.PI, 2 * -Math.PI, 3 * -Math.PI})
  void shouldNotAcceptAsymptotes(double x) {
    BigDecimal arg = new BigDecimal(x);
    assertThrows(ArithmeticException.class, () -> system.calculate(arg, DEFAULT_PRECISION));
  }

  @Test
  void shouldCalculateForGt0() {
    BigDecimal arg = BigDecimal.valueOf(1000);
    assertEquals(new BigDecimal("7094.9719199"), system.calculate(arg, DEFAULT_PRECISION));
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

  private static Stream<Arguments> illegalValuesLt0() {
    return Stream.of(
      // x = 0: cot undefined (sin(0)=0)
      Arguments.of(BigDecimal.ZERO),
      // x = -π/2: cos=0 → sec, cot/cos, tan undefined
      Arguments.of(BigDecimal.valueOf(-Math.PI / 2).setScale(10, RoundingMode.HALF_EVEN)),
      // x = -π: sin=0 → cot, cot/cos undefined
      Arguments.of(BigDecimal.valueOf(-Math.PI).setScale(10, RoundingMode.HALF_EVEN)),
      // x = -3π/2: cos=0 → sec, cot/cos, tan undefined
      Arguments.of(BigDecimal.valueOf(-3 * Math.PI / 2).setScale(10, RoundingMode.HALF_EVEN)),
      // x = -2π: sin=0 → cot undefined
      Arguments.of(BigDecimal.valueOf(-2 * Math.PI).setScale(10, RoundingMode.HALF_EVEN))
    );
  }
}
