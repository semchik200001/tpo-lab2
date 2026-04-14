package ru.itmo.qa.lab2.trig;

import ru.itmo.qa.lab2.function.AbstractFunction;

import java.math.BigDecimal;

import static java.lang.String.format;
import static java.math.RoundingMode.HALF_EVEN;

public class Tangent extends AbstractFunction {

  private final Sine sine;
  private final Cosine cosine;

  public Tangent() {
    super();
    this.sine = new Sine();
    this.cosine = new Cosine(this.sine);
  }

  public Tangent(Sine sine, Cosine cosine) {
    this.sine = sine;
    this.cosine = cosine;
  }

  @Override
  public BigDecimal calculate(BigDecimal x, BigDecimal precision) throws ArithmeticException {
    isValid(x, precision);
    BigDecimal sin = sine.calculate(x, precision.setScale(precision.scale() + 5, HALF_EVEN));
    BigDecimal cos = cosine.calculate(x, precision.setScale(precision.scale() + 5, HALF_EVEN));

    if (cos.abs().compareTo(precision) < 0) {
      throw new ArithmeticException(format("У тангенса нет значения при x = %s", x));
    }

    return sin.divide(cos, precision.scale(), HALF_EVEN);
  }
}
