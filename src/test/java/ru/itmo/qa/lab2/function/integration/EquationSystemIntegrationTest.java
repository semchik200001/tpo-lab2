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
import ru.itmo.qa.lab2.log.NaturalLogarithm;
import ru.itmo.qa.lab2.trig.*;

import java.math.BigDecimal;

import static java.math.BigDecimal.ZERO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EquationSystemIntegrationTest {

  private static final BigDecimal PRECISION = new BigDecimal("0.0000001");

  @Spy
  private Sine spySin;
  @Spy
  private Cosine spyCos;
  @Spy
  private Secant spySec;
  @Spy
  private Tangent spyTan;
  @Spy
  private Cotangent spyCot;
  @Spy
  private NaturalLogarithm spyLn;
  @Spy
  private BaseNLogarithm spyLog2;
  @Spy
  private BaseNLogarithm spyLg;

  @Mock
  private Sine mockSin;
  @Mock
  private Cosine mockCos;
  @Mock
  private Secant mockSec;
  @Mock
  private Tangent mockTan;
  @Mock
  private Cotangent mockCot;
  @Mock
  private NaturalLogarithm mockLn;
  @Mock
  private BaseNLogarithm mockLog2;
  @Mock
  private BaseNLogarithm mockLg;

  @Test
  void shouldCallAllTrigFunctions() {
    EquationSystem system = new EquationSystem(spySin, spyCos, spySec, spyTan, spyCot, spyLn, spyLog2, spyLg);
    system.calculate(new BigDecimal(-5), new BigDecimal("0.0001"));
    verify(spySin, atLeastOnce()).calculate(any(BigDecimal.class), any(BigDecimal.class));
    verify(spyCos, atLeastOnce()).calculate(any(BigDecimal.class), any(BigDecimal.class));
    verify(spySec, atLeastOnce()).calculate(any(BigDecimal.class), any(BigDecimal.class));
    verify(spyTan, atLeastOnce()).calculate(any(BigDecimal.class), any(BigDecimal.class));
    verify(spyCot, atLeastOnce()).calculate(any(BigDecimal.class), any(BigDecimal.class));
    verifyNoInteractions(spyLn);
    verifyNoInteractions(spyLog2);
    verifyNoInteractions(spyLg);
  }

  @Test
  void shouldCallAllLogFunctions() {
    EquationSystem system = new EquationSystem(spySin, spyCos, spySec, spyTan, spyCot, spyLn, spyLog2, spyLg);
    system.calculate(new BigDecimal(5), new BigDecimal("0.0001"));
    verify(spyLn, atLeastOnce()).calculate(any(BigDecimal.class), any(BigDecimal.class));
    verify(spyLog2, atLeastOnce()).calculate(any(BigDecimal.class), any(BigDecimal.class));
    verify(spyLg, atLeastOnce()).calculate(any(BigDecimal.class), any(BigDecimal.class));
    verifyNoInteractions(spySin);
    verifyNoInteractions(spyCos);
    verifyNoInteractions(spySec);
    verifyNoInteractions(spyTan);
    verifyNoInteractions(spyCot);
  }

  @ParameterizedTest(name = "f({0}) = {1}")
  @DisplayName("Test 3: Call function")
  @CsvFileSource(resources = "/integration/systemIT.csv", numLinesToSkip = 1, delimiter = ',')
  void shouldCalculateWithMockFunctions(BigDecimal x, BigDecimal y) {
    if (x.compareTo(ZERO) > 0) {
      when(mockLn.calculate(eq(x), any(BigDecimal.class))).thenReturn(BigDecimal.valueOf(Math.log(x.doubleValue())));
      when(mockLog2.calculate(eq(x), any(BigDecimal.class)))
          .thenReturn(BigDecimal.valueOf(Math.log(x.doubleValue()) / Math.log(2)));
      when(mockLg.calculate(eq(x), any(BigDecimal.class))).thenReturn(BigDecimal.valueOf(Math.log10(x.doubleValue())));
    } else {
      double xd = x.doubleValue();
      when(mockSin.calculate(eq(x), any(BigDecimal.class))).thenReturn(BigDecimal.valueOf(Math.sin(xd)));
      when(mockCos.calculate(eq(x), any(BigDecimal.class))).thenReturn(BigDecimal.valueOf(Math.cos(xd)));
      when(mockSec.calculate(eq(x), any(BigDecimal.class)))
          .thenReturn(BigDecimal.valueOf(1.0 / Math.cos(xd)));
      when(mockTan.calculate(eq(x), any(BigDecimal.class))).thenReturn(BigDecimal.valueOf(Math.tan(xd)));
      when(mockCot.calculate(eq(x), any(BigDecimal.class)))
          .thenReturn(BigDecimal.valueOf(Math.cos(xd) / Math.sin(xd)));
    }
    EquationSystem system = new EquationSystem(mockSin, mockCos, mockSec, mockTan, mockCot, mockLn, mockLog2, mockLg);
    assertEquals(y, system.calculate(x, PRECISION));
  }
}
