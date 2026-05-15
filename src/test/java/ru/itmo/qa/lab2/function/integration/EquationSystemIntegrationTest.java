package ru.itmo.qa.lab2.function.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.itmo.qa.lab2.EquationSystem;
import ru.itmo.qa.lab2.log.BaseNLogarithm;
import ru.itmo.qa.lab2.trig.Cosine;
import ru.itmo.qa.lab2.trig.Secant;
import ru.itmo.qa.lab2.trig.Sine;

import java.math.BigDecimal;

import static java.math.BigDecimal.ZERO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Интеграционное тестирование системы функций (вариант 46879).
 *
 *  Стратегия: восходящая интеграция «по 1 модулю».
 *  Ступень 0 — все 5 модулей замоканы (проверяем только ядро EquationSystem).
 *  Каждая последующая ступень заменяет на реальную реализацию ещё один модуль:
 *   - для ветви x ≤ 0: cos → cos+sec → (всё реальное);
 *   - для ветви x > 0: log2 → log2+log5 → (всё реальное).
 *  Порядок вызовов модулей проверяется через InOrder — соответствует формуле:
 *   - x ≤ 0: cos, sec;
 *   - x > 0: log2, log5, log10.
 */
@ExtendWith(MockitoExtension.class)
class EquationSystemIntegrationTest {

  private static final BigDecimal PRECISION = new BigDecimal("0.0000001");

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

  // ───────────────────────── Проверка порядка вызовов (InOrder) ─────────────────────────

  @Test
  @DisplayName("x ≤ 0 → вызываются только cos, sec в правильном порядке")
  void shouldCallTrigInOrderForNegative() {
    Cosine spyCos = spy(new Cosine(new Sine()));
    Secant spySec = spy(new Secant(spyCos));
    BaseNLogarithm spyLog2 = spy(new BaseNLogarithm(2));
    BaseNLogarithm spyLog5 = spy(new BaseNLogarithm(5));
    BaseNLogarithm spyLg = spy(new BaseNLogarithm(10));

    EquationSystem system = new EquationSystem(spyCos, spySec, spyLog2, spyLog5, spyLg);
    system.calculate(new BigDecimal("-1"), PRECISION);

    InOrder order = inOrder(spyCos, spySec);
    order.verify(spyCos).calculate(any(BigDecimal.class), any(BigDecimal.class));
    order.verify(spySec).calculate(any(BigDecimal.class), any(BigDecimal.class));
    verifyNoInteractions(spyLog2);
    verifyNoInteractions(spyLog5);
    verifyNoInteractions(spyLg);
  }

  @Test
  @DisplayName("x > 0 → вызываются только log2, log5, log10 в правильном порядке")
  void shouldCallLogsInOrderForPositive() {
    Cosine spyCos = spy(new Cosine(new Sine()));
    Secant spySec = spy(new Secant(spyCos));
    BaseNLogarithm spyLog2 = spy(new BaseNLogarithm(2));
    BaseNLogarithm spyLog5 = spy(new BaseNLogarithm(5));
    BaseNLogarithm spyLg = spy(new BaseNLogarithm(10));

    EquationSystem system = new EquationSystem(spyCos, spySec, spyLog2, spyLog5, spyLg);
    system.calculate(new BigDecimal("5"), PRECISION);

    InOrder order = inOrder(spyLog2, spyLog5, spyLg);
    order.verify(spyLog2).calculate(any(BigDecimal.class), any(BigDecimal.class));
    order.verify(spyLog5).calculate(any(BigDecimal.class), any(BigDecimal.class));
    order.verify(spyLg).calculate(any(BigDecimal.class), any(BigDecimal.class));
    verifyNoInteractions(spyCos);
    verifyNoInteractions(spySec);
  }

  // ───────────────────── Ступень 0: все модули замоканы (база) ─────────────────────

  @ParameterizedTest(name = "[stage 0 | all mocks] f({0}) = {1}")
  @DisplayName("Ступень 0: все модули замоканы")
  @CsvFileSource(resources = "/integration/systemIT.csv", numLinesToSkip = 1, delimiter = ',')
  void stage0_allMocks(BigDecimal x, BigDecimal y) {
    stubAccordingToBranch(x);

    EquationSystem system = new EquationSystem(mockCos, mockSec, mockLog2, mockLog5, mockLg);
    assertEquals(y, system.calculate(x, PRECISION));
  }

  // ───────────────────── Ветвь x ≤ 0: восходящая интеграция ─────────────────────

  @ParameterizedTest(name = "[stage 1 | real cos] f({0}) = {1}")
  @DisplayName("Ступень 1 (x ≤ 0): реальный cos, sec — мок (с поведением реального sec)")
  @CsvFileSource(resources = "/system.csv", numLinesToSkip = 1, delimiter = ',')
  void stage1_realCos_negative(BigDecimal x, BigDecimal y) {
    assumeTrue(x.compareTo(ZERO) <= 0);

    Cosine realCos = new Cosine(new Sine());
    stubSecLikeReal(x);

    EquationSystem system = new EquationSystem(realCos, mockSec, mockLog2, mockLog5, mockLg);
    assertEquals(y, system.calculate(x, PRECISION));
  }

  @ParameterizedTest(name = "[stage 2 | real cos+sec] f({0}) = {1}")
  @DisplayName("Ступень 2 (x ≤ 0): реальные cos и sec, логарифмы — моки")
  @CsvFileSource(resources = "/system.csv", numLinesToSkip = 1, delimiter = ',')
  void stage2_realCosSec_negative(BigDecimal x, BigDecimal y) {
    assumeTrue(x.compareTo(ZERO) <= 0);

    Cosine realCos = new Cosine(new Sine());
    Secant realSec = new Secant(realCos);

    EquationSystem system = new EquationSystem(realCos, realSec, mockLog2, mockLog5, mockLg);
    assertEquals(y, system.calculate(x, PRECISION));
  }

  @ParameterizedTest(name = "[stage 3 | all real] f({0}) = {1}")
  @DisplayName("Ступень 3 (x ≤ 0): все модули реальные")
  @CsvFileSource(resources = "/system.csv", numLinesToSkip = 1, delimiter = ',')
  void stage3_allReal_negative(BigDecimal x, BigDecimal y) {
    assumeTrue(x.compareTo(ZERO) <= 0);

    Cosine realCos = new Cosine(new Sine());
    Secant realSec = new Secant(realCos);
    BaseNLogarithm realLog2 = new BaseNLogarithm(2);
    BaseNLogarithm realLog5 = new BaseNLogarithm(5);
    BaseNLogarithm realLg = new BaseNLogarithm(10);

    EquationSystem system = new EquationSystem(realCos, realSec, realLog2, realLog5, realLg);
    assertEquals(y, system.calculate(x, PRECISION));
  }

  // ───────────────────── Ветвь x > 0: восходящая интеграция ─────────────────────

  @ParameterizedTest(name = "[stage 1 | real log2] f({0}) = {1}")
  @DisplayName("Ступень 1 (x > 0): реальный log2, log5/lg — моки (с поведением реальных)")
  @CsvFileSource(resources = "/system.csv", numLinesToSkip = 1, delimiter = ',')
  void stage1_realLog2_positive(BigDecimal x, BigDecimal y) {
    assumeTrue(x.compareTo(ZERO) > 0);

    BaseNLogarithm realLog2 = new BaseNLogarithm(2);
    stubLog5LikeReal(x);
    stubLgLikeReal(x);

    EquationSystem system = new EquationSystem(mockCos, mockSec, realLog2, mockLog5, mockLg);
    assertEquals(y, system.calculate(x, PRECISION));
  }

  @ParameterizedTest(name = "[stage 2 | real log2+log5] f({0}) = {1}")
  @DisplayName("Ступень 2 (x > 0): реальные log2 и log5, lg — мок (с поведением реального)")
  @CsvFileSource(resources = "/system.csv", numLinesToSkip = 1, delimiter = ',')
  void stage2_realLog2Log5_positive(BigDecimal x, BigDecimal y) {
    assumeTrue(x.compareTo(ZERO) > 0);

    BaseNLogarithm realLog2 = new BaseNLogarithm(2);
    BaseNLogarithm realLog5 = new BaseNLogarithm(5);
    stubLgLikeReal(x);

    EquationSystem system = new EquationSystem(mockCos, mockSec, realLog2, realLog5, mockLg);
    assertEquals(y, system.calculate(x, PRECISION));
  }

  @ParameterizedTest(name = "[stage 3 | all real] f({0}) = {1}")
  @DisplayName("Ступень 3 (x > 0): все модули реальные")
  @CsvFileSource(resources = "/system.csv", numLinesToSkip = 1, delimiter = ',')
  void stage3_allReal_positive(BigDecimal x, BigDecimal y) {
    assumeTrue(x.compareTo(ZERO) > 0);

    Cosine realCos = new Cosine(new Sine());
    Secant realSec = new Secant(realCos);
    BaseNLogarithm realLog2 = new BaseNLogarithm(2);
    BaseNLogarithm realLog5 = new BaseNLogarithm(5);
    BaseNLogarithm realLg = new BaseNLogarithm(10);

    EquationSystem system = new EquationSystem(realCos, realSec, realLog2, realLog5, realLg);
    assertEquals(y, system.calculate(x, PRECISION));
  }

  // ───────────────────── helpers: стабы моков ─────────────────────

  private void stubAccordingToBranch(BigDecimal x) {
    if (x.compareTo(ZERO) > 0) {
      stubLog2Mock(x);
      stubLog5Mock(x);
      stubLgMock(x);
    } else {
      stubCosMock(x);
      stubSecMock(x);
    }
  }

  private void stubCosMock(BigDecimal x) {
    when(mockCos.calculate(eq(x), any(BigDecimal.class)))
        .thenReturn(BigDecimal.valueOf(Math.cos(x.doubleValue())));
  }

  private void stubSecMock(BigDecimal x) {
    when(mockSec.calculate(eq(x), any(BigDecimal.class)))
        .thenReturn(BigDecimal.valueOf(1.0 / Math.cos(x.doubleValue())));
  }

  private void stubLog2Mock(BigDecimal x) {
    when(mockLog2.calculate(eq(x), any(BigDecimal.class)))
        .thenReturn(BigDecimal.valueOf(Math.log(x.doubleValue()) / Math.log(2)));
  }

  private void stubLog5Mock(BigDecimal x) {
    when(mockLog5.calculate(eq(x), any(BigDecimal.class)))
        .thenReturn(BigDecimal.valueOf(Math.log(x.doubleValue()) / Math.log(5)));
  }

  private void stubLgMock(BigDecimal x) {
    when(mockLg.calculate(eq(x), any(BigDecimal.class)))
        .thenReturn(BigDecimal.valueOf(Math.log10(x.doubleValue())));
  }

  // ── Стабы, воспроизводящие поведение реальных модулей (для смешанных ступеней).
  //    Мок делегирует реальной реализации: это позволяет проверять, что подключение
  //    очередного «настоящего» модуля не ломает результат полностью реальной системы.

  private void stubSecLikeReal(BigDecimal x) {
    Secant realSec = new Secant(new Cosine(new Sine()));
    when(mockSec.calculate(eq(x), any(BigDecimal.class)))
        .thenAnswer(invocation -> realSec.calculate(invocation.getArgument(0), invocation.getArgument(1)));
  }

  private void stubLog5LikeReal(BigDecimal x) {
    BaseNLogarithm realLog5 = new BaseNLogarithm(5);
    when(mockLog5.calculate(eq(x), any(BigDecimal.class)))
        .thenAnswer(invocation -> realLog5.calculate(invocation.getArgument(0), invocation.getArgument(1)));
  }

  private void stubLgLikeReal(BigDecimal x) {
    BaseNLogarithm realLg = new BaseNLogarithm(10);
    when(mockLg.calculate(eq(x), any(BigDecimal.class)))
        .thenAnswer(invocation -> realLg.calculate(invocation.getArgument(0), invocation.getArgument(1)));
  }
}
