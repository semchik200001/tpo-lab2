package ru.itmo.qa.lab2.trig.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import ru.itmo.qa.lab2.trig.Cosine;
import ru.itmo.qa.lab2.trig.Sine;
import ru.itmo.qa.lab2.trig.Tangent;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TangentIntegrationTest {
  private static final BigDecimal PRECISION = new BigDecimal("0.0000001");

  @Mock
  private Sine mockSin;

  @Spy
  private Sine spySin;

  @Mock
  private Cosine mockCos;

  @Spy
  private Cosine spyCos;

  @Test
  @DisplayName("Test 1: Call both sine and cosine")
  void shouldCallSineAndCosineFunction() {
    Tangent tan = new Tangent(spySin, spyCos);
    tan.calculate(new BigDecimal(972), PRECISION);
    verify(spySin, atLeastOnce()).calculate(any(BigDecimal.class), any(BigDecimal.class));
    verify(spyCos, atLeastOnce()).calculate(any(BigDecimal.class), any(BigDecimal.class));
  }

  @ParameterizedTest(name = "mock.tan({0}) = {1}")
  @DisplayName("Test 2: Call tangent")
  @CsvFileSource(resources = "/integration/tanIT.csv", numLinesToSkip = 1, delimiter = ',')
  void shouldCallTangentFunction(BigDecimal x, BigDecimal y) {
    when(mockSin.calculate(eq(x), any()))
        .thenReturn(new BigDecimal(Math.sin(x.doubleValue())));
    when(mockCos.calculate(eq(x), any()))
        .thenReturn(new BigDecimal(Math.cos(x.doubleValue())));

    Tangent tan = new Tangent(mockSin, mockCos);
    assertEquals(y, tan.calculate(x, PRECISION));
  }

  @Test
  @DisplayName("Tangent throws exception when cos(x) = 0")
  void shouldThrowWhenSineIsZero() {
    BigDecimal x = BigDecimal.valueOf(Math.PI).divide(BigDecimal.valueOf(2)); // cos(π/2) = 0
    when(mockSin.calculate(eq(x), any())).thenReturn(BigDecimal.ONE);
    when(mockCos.calculate(eq(x), any())).thenReturn(BigDecimal.ZERO);

    Tangent tan = new Tangent(mockSin, mockCos);
    assertThrows(
        ArithmeticException.class,
        () -> tan.calculate(x, PRECISION),
        "Should throw when cos(x) = 0");
  }
}
