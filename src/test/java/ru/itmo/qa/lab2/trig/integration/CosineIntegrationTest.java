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

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CosineIntegrationTest {

  private static final BigDecimal PRECISION = new BigDecimal("0.0000001");

  @Mock
  private Sine mockSin;

  @Spy
  private Sine spySin;

  @Test
  @DisplayName("Test 1: Call sine")
  void shouldCallSineFunction() {
    Cosine cos = new Cosine(spySin);
    cos.calculate(new BigDecimal(972), PRECISION);
    verify(spySin, atLeastOnce()).calculate(any(BigDecimal.class), any(BigDecimal.class));
  }

  @ParameterizedTest(name = "mock.cos({0}) = {1}")
  @DisplayName("Test 2: Call cosine with mocked sine")
  @CsvFileSource(resources = "/integration/cosIT.csv", numLinesToSkip = 1, delimiter = ',')
  void shouldCallCosineFunction(BigDecimal x, BigDecimal y) {
    // cos(x) = sin(π/2 - x), so mock sine to answer any call with Math.sin
    when(mockSin.calculate(any(BigDecimal.class), any(BigDecimal.class)))
        .thenAnswer(invocation -> {
          BigDecimal arg = invocation.getArgument(0);
          BigDecimal prec = invocation.getArgument(1);
          return BigDecimal.valueOf(Math.sin(arg.doubleValue()))
              .setScale(prec.scale(), java.math.RoundingMode.HALF_EVEN);
        });

    Cosine cos = new Cosine(mockSin);
    assertEquals(y, cos.calculate(x, PRECISION));
  }
}
