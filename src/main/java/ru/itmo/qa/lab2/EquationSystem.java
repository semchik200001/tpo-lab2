package ru.itmo.qa.lab2;

import lombok.AllArgsConstructor;
import ru.itmo.qa.lab2.function.AbstractFunction;
import ru.itmo.qa.lab2.log.BaseNLogarithm;
import ru.itmo.qa.lab2.trig.Cosine;
import ru.itmo.qa.lab2.trig.Secant;
import ru.itmo.qa.lab2.trig.Sine;

import java.math.BigDecimal;
import java.math.MathContext;

import static java.lang.String.format;
import static java.math.BigDecimal.ZERO;
import static java.math.MathContext.DECIMAL128;
import static java.math.RoundingMode.HALF_EVEN;

/**
 * Вариант 46879.
 *
 *  x ≤ 0 : (((cos(x) + cos(x)) * sec(x)^3) + cos(x))
 *  x > 0 : ((((log_2(x) * log_5(x) / log_2(x)) * log_2(x))^2) + (log_2(x) / log_10(x)))
 */
@AllArgsConstructor
public class EquationSystem extends AbstractFunction {
  private final Cosine cos;
  private final Secant sec;

  private final BaseNLogarithm log2;
  private final BaseNLogarithm log5;
  private final BaseNLogarithm lg;

  public EquationSystem() {
    super();
    Sine sin = new Sine();
    cos = new Cosine(sin);
    sec = new Secant(cos);

    log2 = new BaseNLogarithm(2);
    log5 = new BaseNLogarithm(5);
    lg = new BaseNLogarithm(10);
  }

  @Override
  public BigDecimal calculate(BigDecimal x, BigDecimal precision) {
    isValid(x, precision);
    final MathContext mc = new MathContext(DECIMAL128.getPrecision(), HALF_EVEN);
    final BigDecimal p = precision.setScale(precision.scale() + 10, HALF_EVEN);

    if (x.compareTo(ZERO) <= 0) {
      try {
        BigDecimal cosX = c(cos, x, p);
        BigDecimal secX = c(sec, x, p);

        BigDecimal sumCos = cosX.add(cosX, mc);
        BigDecimal secCubed = secX.pow(3, mc);
        BigDecimal product = sumCos.multiply(secCubed, mc);
        return product.add(cosX, mc).setScale(precision.scale(), HALF_EVEN);
      } catch (ArithmeticException e) {
        throw new ArithmeticException(format("У функции нет значения при x = %s", x));
      }
    }

    try {
      BigDecimal log2x = c(log2, x, p);
      BigDecimal log5x = c(log5, x, p);
      BigDecimal log10x = c(lg, x, p);

      if (log10x.abs().compareTo(p) < 0) {
        throw new ArithmeticException(format("У функции нет значения при x = %s", x));
      }

      BigDecimal numInner = log2x.multiply(log5x, mc);
      BigDecimal divInner = numInner.divide(log2x, mc.getPrecision(), HALF_EVEN);
      BigDecimal mulLog2 = divInner.multiply(log2x, mc);
      BigDecimal squared = mulLog2.pow(2, mc);
      BigDecimal ratio = log2x.divide(log10x, mc.getPrecision(), HALF_EVEN);
      return squared.add(ratio, mc).setScale(precision.scale(), HALF_EVEN);
    } catch (ArithmeticException e) {
      throw new ArithmeticException(format("У функции нет значения при x = %s", x));
    }
  }

  private BigDecimal c(AbstractFunction function, BigDecimal x, BigDecimal precision) {
    return function.calculate(x, precision);
  }
}
