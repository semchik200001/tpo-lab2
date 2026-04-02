package ru.itmo.qa.lab2.function;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.itmo.qa.lab2.log.BaseNLogarithm;
import ru.itmo.qa.lab2.log.NaturalLogarithm;
import ru.itmo.qa.lab2.trig.*;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static java.math.BigDecimal.ONE;
import static org.junit.jupiter.api.Assertions.*;

class FunctionRuleTest {

  private static final BigDecimal PRECISION = new BigDecimal("0.000001");
  private static final BigDecimal NEGATIVE_PRECISION = PRECISION.negate();
  private static final BigDecimal POSITIVE_PRECISION = PRECISION.add(ONE);

  @ParameterizedTest
  @MethodSource("functions")
  void shouldNotAcceptNullArg(final FunctionRule function) {
    Throwable exception = assertThrows(NullPointerException.class, () -> function.calculate(null, PRECISION));
    assertEquals("Аргумент не должен быть null", exception.getMessage());
  }

  @ParameterizedTest
  @MethodSource("functions")
  void shouldNotAcceptNullPrecision(final FunctionRule function) {
    Throwable exception = assertThrows(NullPointerException.class, () -> function.calculate(ONE, null));
    assertEquals("Точность не должна быть null", exception.getMessage());
  }

  @ParameterizedTest
  @MethodSource("functions")
  void shouldNotAcceptOutside0And1(final FunctionRule function) {
    assertAll(
      () -> {
        Throwable exception = assertThrows(ArithmeticException.class,
          () -> function.calculate(ONE, NEGATIVE_PRECISION));
        assertEquals("Значение точности должно быть между 0 и 1 включительно", exception.getMessage());
      },
      () -> {
        Throwable exception = assertThrows(ArithmeticException.class,
          () -> function.calculate(ONE, POSITIVE_PRECISION));
        assertEquals("Значение точности должно быть между 0 и 1 включительно", exception.getMessage());
      });
  }

  @ParameterizedTest
  @MethodSource("functions")
  void shouldAcceptArgAndPrecision(final FunctionRule function) {
    assertDoesNotThrow(() -> function.calculate(ONE, PRECISION));
  }

  private static Stream<Arguments> functions() {
    return Stream.of(
      Arguments.of(new Sine()),
      Arguments.of(new Cosine()),
      Arguments.of(new Secant()),
      Arguments.of(new Cosecant()),
      Arguments.of(new Tangent()),
      Arguments.of(new Cotangent()),
      Arguments.of(new NaturalLogarithm()),
      Arguments.of(new BaseNLogarithm(2)),
      Arguments.of(new BaseNLogarithm(3)),
      Arguments.of(new BaseNLogarithm(5)),
      Arguments.of(new BaseNLogarithm(10))
    );
  }
}
