package ru.itmo.qa.lab2.trig.module;

import ch.obermuhlner.math.big.BigDecimalMath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.ValueSource;
import ru.itmo.qa.lab2.trig.Sine;

import java.math.BigDecimal;
import java.math.MathContext;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static java.math.MathContext.DECIMAL128;
import static java.math.RoundingMode.HALF_EVEN;
import static org.junit.jupiter.api.Assertions.*;

class SineTest {

  private static final BigDecimal PRECISION = new BigDecimal("0.0000001");

  private Sine sin;

  @BeforeEach
  void init() {
    sin = new Sine();
  }

  @Test
  void shouldCalculateForZero() {
    assertEquals(ZERO.setScale(7, HALF_EVEN), sin.calculate(ZERO, PRECISION));
  }

  @Test
  void shouldCalculateForPiHalf() {
    final MathContext mc = new MathContext(DECIMAL128.getPrecision());
    final BigDecimal piHalf = BigDecimalMath.pi(mc).divide(BigDecimal.valueOf(2), DECIMAL128.getPrecision(), HALF_EVEN);
    final BigDecimal expected = ONE.setScale(7, HALF_EVEN);
    assertAll(
        () -> assertEquals(expected, sin.calculate(piHalf, PRECISION)),
        () -> assertEquals(expected.negate(), sin.calculate(piHalf.negate(), PRECISION)));
  }

  @Test
  void shouldCalculateForPi() {
    final MathContext mc = new MathContext(DECIMAL128.getPrecision());
    final BigDecimal pi = BigDecimalMath.pi(mc);
    final BigDecimal expected = ZERO.setScale(7, HALF_EVEN);
    assertAll(
        () -> assertEquals(expected, sin.calculate(pi, PRECISION)),
        () -> assertEquals(expected, sin.calculate(pi.negate(), PRECISION)));
  }

  @Test
  void shouldCalculateForTwoPi() {
    final MathContext mc = new MathContext(DECIMAL128.getPrecision());
    final BigDecimal twoPi = BigDecimalMath.pi(mc).multiply(BigDecimal.valueOf(2));
    final BigDecimal expected = ZERO.setScale(7, HALF_EVEN);
    assertAll(
        () -> assertEquals(expected, sin.calculate(twoPi, PRECISION)),
        () -> assertEquals(expected, sin.calculate(twoPi.negate(), PRECISION)));
  }

  @ParameterizedTest(name = "sin({0}) is odd")
  @ValueSource(doubles = {0.3, 0.7853982, 1.2, 2.0})
  void shouldBeOddFunction(double x) {
    BigDecimal arg = BigDecimal.valueOf(x);
    BigDecimal positive = sin.calculate(arg, PRECISION);
    BigDecimal negative = sin.calculate(arg.negate(), PRECISION);
    assertEquals(positive.negate(), negative);
  }

  @ParameterizedTest(name = "sin({0})")
  @CsvFileSource(resources = "/sin.csv", numLinesToSkip = 1, delimiter = ',')
  void testSin(BigDecimal x, BigDecimal y) {
    assertEquals(y, sin.calculate(x, PRECISION));
  }
}
