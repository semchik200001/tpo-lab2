package ru.itmo.qa.lab2.trig.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.itmo.qa.lab2.trig.Cosine;
import ru.itmo.qa.lab2.trig.Cotangent;
import ru.itmo.qa.lab2.trig.Sine;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CotangentIntegrationTest {

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
  @DisplayName("Test 1: Call sine and cosine in order")
  void shouldCallSineAndCosineFunction() {
    Cotangent cot = new Cotangent(spySin, spyCos);
    cot.calculate(new BigDecimal(979), PRECISION);

    InOrder order = inOrder(spySin, spyCos);
    order.verify(spySin).calculate(any(BigDecimal.class), any(BigDecimal.class));
    order.verify(spyCos).calculate(any(BigDecimal.class), any(BigDecimal.class));
  }

  @ParameterizedTest(name = "mock.cot({0}) = {1}")
  @DisplayName("Test 2: Call cotangent")
  @CsvFileSource(resources = "/integration/cotIT.csv", numLinesToSkip = 1, delimiter = ',')
  void shouldCallCotangentFunction(BigDecimal x, BigDecimal y) {
    when(mockSin.calculate(eq(x), any()))
        .thenReturn(new BigDecimal(Math.sin(x.doubleValue())));
    when(mockCos.calculate(eq(x), any()))
        .thenReturn(new BigDecimal(Math.cos(x.doubleValue())));

    Cotangent cot = new Cotangent(mockSin, mockCos);
    assertEquals(y, cot.calculate(x, PRECISION));
  }

  @Test
  @DisplayName("Cotangent throws exception when sin(x) = 0")
  void shouldThrowWhenSineIsZero() {
    BigDecimal x = BigDecimal.valueOf(Math.PI); // sin(π) = 0
    when(mockSin.calculate(eq(x), any())).thenReturn(BigDecimal.ZERO);
    when(mockCos.calculate(eq(x), any())).thenReturn(BigDecimal.ONE);

    Cotangent cot = new Cotangent(mockSin, mockCos);
    assertThrows(
        ArithmeticException.class,
        () -> cot.calculate(x, PRECISION),
        "Should throw when sin(x) = 0");
  }
}
