package ru.itmo.qa.lab2;

import lombok.AllArgsConstructor;
import ru.itmo.qa.lab2.function.AbstractFunction;
import ru.itmo.qa.lab2.log.BaseNLogarithm;
import ru.itmo.qa.lab2.log.NaturalLogarithm;
import ru.itmo.qa.lab2.trig.*;

import java.math.BigDecimal;
import java.math.MathContext;

import static java.lang.String.format;
import static java.math.BigDecimal.ZERO;
import static java.math.MathContext.DECIMAL128;
import static java.math.RoundingMode.HALF_EVEN;

@AllArgsConstructor
public class EquationSystem extends AbstractFunction {
  private final Sine sin;
  private final Cosine cos;
  private final Secant sec;
  private final Tangent tan;
  private final Cotangent cot;

  private final NaturalLogarithm ln;
  private final BaseNLogarithm log2;
  private final BaseNLogarithm lg;

  public EquationSystem() {
    super();
    sin = new Sine();
    cos = new Cosine();
    sec = new Secant();
    tan = new Tangent();
    cot = new Cotangent();

    ln = new NaturalLogarithm();
    log2 = new BaseNLogarithm(2);
    lg = new BaseNLogarithm(10);
  }

  @Override
  public BigDecimal calculate(BigDecimal x, BigDecimal precision) {
    final MathContext mc = new MathContext(DECIMAL128.getPrecision(), HALF_EVEN);
    final BigDecimal p = precision.setScale(precision.scale() + 10, HALF_EVEN);

    if (x.compareTo(ZERO) <= 0) {
      // x <= 0 : ((((cot(x)/cos(x)) * cot(x))^2) - ((sec(x) + sec(x)) * (sin(x)/tan(x)))) + cot(x)^2
      // Note: sin(x)/tan(x) = cos(x)
      try {
        BigDecimal sinX = c(sin, x, p);
        BigDecimal cotX = c(cot, x, p);
        BigDecimal cosX = c(cos, x, p);
        BigDecimal secX = c(sec, x, p);
        BigDecimal tanX = c(tan, x, p);

        // (cot(x)/cos(x)) * cot(x)
        BigDecimal inner = cotX.divide(cosX, mc.getPrecision(), HALF_EVEN).multiply(cotX, mc);
        // inner^2
        BigDecimal term1 = inner.pow(2, mc);
        // sin(x)/tan(x)  — вычисляем напрямую, без упрощений
        BigDecimal sinOverTan = sinX.divide(tanX, mc.getPrecision(), HALF_EVEN);
        BigDecimal term2 = secX.add(secX, mc).multiply(sinOverTan, mc);
        // cot(x)^2
        BigDecimal term3 = cotX.pow(2, mc);

        return term1.subtract(term2, mc).add(term3, mc).setScale(precision.scale(), HALF_EVEN);
      } catch (ArithmeticException e) {
        throw new ArithmeticException(format("У функции нет значения при x = %s", x));
      }

    } else {
      // x > 0 : ((((log_10(x) + log_2(x))^3) * log_2(x)) / log_10(x)) + ln(x)
      try {
        BigDecimal log10x = c(lg, x, p);
        BigDecimal log2x = c(log2, x, p);
        BigDecimal lnx = c(ln, x, p);

        if (log10x.abs().compareTo(p) < 0) {
          throw new ArithmeticException(format("У функции нет значения при x = %s", x));
        }

        // (log_10(x) + log_2(x))^3
        BigDecimal sumLogs = log10x.add(log2x, mc);
        BigDecimal sumCubed = sumLogs.pow(3, mc);
        // * log_2(x)
        BigDecimal numerator = sumCubed.multiply(log2x, mc);
        // / log_10(x)
        BigDecimal divided = numerator.divide(log10x, mc.getPrecision(), HALF_EVEN);
        // + ln(x)
        return divided.add(lnx, mc).setScale(precision.scale(), HALF_EVEN);
      } catch (ArithmeticException e) {
        throw new ArithmeticException(format("У функции нет значения при x = %s", x));
      }
    }
  }

  private BigDecimal c(AbstractFunction function, BigDecimal x, BigDecimal precision) {
    return function.calculate(x, precision);
  }
}
