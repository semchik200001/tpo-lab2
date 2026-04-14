package ru.itmo.qa.lab2.function.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.itmo.qa.lab2.EquationSystem;
import ru.itmo.qa.lab2.log.BaseNLogarithm;
import ru.itmo.qa.lab2.trig.Cosine;
import ru.itmo.qa.lab2.trig.Secant;

import java.math.BigDecimal;

import static java.math.BigDecimal.ZERO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Интеграционное тестирование системы функций (вариант 46879).
 *
 *  Стратегия: восходящая интеграция «по 1 модулю».
 *  Каждый последующий тест добавляет к уже проверенным реальным модулям ещё один,
 *  заменяя оставшиеся заглушками (Mockito @Mock). Это позволяет:
 *   1) контролировать, что в нужной ветви системы (x ≤ 0 / x > 0)
 *      вызываются ИМЕННО те модули, которые предусмотрены формулой;
 *   2) последовательно подключать реальные реализации к проверенному ядру
 *      и убедиться, что итоговый результат совпадает с эталоном.
 */
@ExtendWith(MockitoExtension.class)
class EquationSystemIntegrationTest {

  private static final BigDecimal PRECISION = new BigDecimal("0.0000001");

  @Spy
  private Cosine spyCos;
  @Spy
  private Secant spySec;
  @Spy
  private BaseNLogarithm spyLog2;
  @Spy
  private BaseNLogarithm spyLog5;
  @Spy
  private BaseNLogarithm spyLg;

  @Mock
  private Cosine mockCos;
  @Mock
  private Secant mockSec;
  @Mock
  private BaseNLogarithm mockLog2;
  @Mock
  private BaseNLogarithm mockLog5;
  @Mock
  private BaseNLogarithm mockLg;

  @Test
  @DisplayName("x ≤ 0 → вызываются только тригонометрические модули (cos, sec)")
  void shouldCallOnlyTrigForNegative() {
    EquationSystem system = new EquationSystem(spyCos, spySec, spyLog2, spyLog5, spyLg);
    system.calculate(new BigDecimal("-1"), PRECISION);

    verify(spyCos, atLeastOnce()).calculate(any(BigDecimal.class), any(BigDecimal.class));
    verify(spySec, atLeastOnce()).calculate(any(BigDecimal.class), any(BigDecimal.class));
    verifyNoInteractions(spyLog2);
    verifyNoInteractions(spyLog5);
    verifyNoInteractions(spyLg);
  }

  @Test
  @DisplayName("x > 0 → вызываются только логарифмические модули (log2, log5, log10)")
  void shouldCallOnlyLogForPositive() {
    EquationSystem system = new EquationSystem(spyCos, spySec, spyLog2, spyLog5, spyLg);
    system.calculate(new BigDecimal("5"), PRECISION);

    verify(spyLog2, atLeastOnce()).calculate(any(BigDecimal.class), any(BigDecimal.class));
    verify(spyLog5, atLeastOnce()).calculate(any(BigDecimal.class), any(BigDecimal.class));
    verify(spyLg, atLeastOnce()).calculate(any(BigDecimal.class), any(BigDecimal.class));
    verifyNoInteractions(spyCos);
    verifyNoInteractions(spySec);
  }

  @ParameterizedTest(name = "f({0}) = {1}")
  @DisplayName("Интеграция системы с заглушками базовых модулей")
  @CsvFileSource(resources = "/integration/systemIT.csv", numLinesToSkip = 1, delimiter = ',')
  void shouldCalculateWithMockFunctions(BigDecimal x, BigDecimal y) {
    if (x.compareTo(ZERO) > 0) {
      double xd = x.doubleValue();
      when(mockLog2.calculate(eq(x), any(BigDecimal.class)))
          .thenReturn(BigDecimal.valueOf(Math.log(xd) / Math.log(2)));
      when(mockLog5.calculate(eq(x), any(BigDecimal.class)))
          .thenReturn(BigDecimal.valueOf(Math.log(xd) / Math.log(5)));
      when(mockLg.calculate(eq(x), any(BigDecimal.class)))
          .thenReturn(BigDecimal.valueOf(Math.log10(xd)));
    } else {
      double xd = x.doubleValue();
      when(mockCos.calculate(eq(x), any(BigDecimal.class)))
          .thenReturn(BigDecimal.valueOf(Math.cos(xd)));
      when(mockSec.calculate(eq(x), any(BigDecimal.class)))
          .thenReturn(BigDecimal.valueOf(1.0 / Math.cos(xd)));
    }

    EquationSystem system = new EquationSystem(mockCos, mockSec, mockLog2, mockLog5, mockLg);
    assertEquals(y, system.calculate(x, PRECISION));
  }
}
