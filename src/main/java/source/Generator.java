package source;

import mortgage.Request;
import mortgage.PaymentType;

import java.util.concurrent.atomic.AtomicInteger;

import static tools.ConstantsAndParameters.*;

public class Generator {
  private static final AtomicInteger number = new AtomicInteger();

  public static Request generate() {
    int estateCost = ((int) (Math.random() * (GENERATOR_MAX_ESTATE_COST - GENERATOR_MIN_ESTATE_COST + 1))
        + GENERATOR_MIN_ESTATE_COST);

    int initialPayment = (int) (Math.random() * (GENERATOR_MIN_ESTATE_COST + 1));
    int period = (int) (Math.random() * (GENERATOR_MAX_PERIOD - GENERATOR_MIN_PERIOD + 1)) + GENERATOR_MIN_PERIOD;

    double interestRate = Math.random() * (GENERATOR_MAX_INTEREST_RATE - GENERATOR_MIN_INTEREST_RATE + 1)
        + GENERATOR_MIN_INTEREST_RATE;

    PaymentType paymentType = (((int) (Math.random() * 2)) == 1) ? PaymentType.ANNUITY : PaymentType.DIFFERENTIATED;

    return new Request(number.incrementAndGet(), estateCost, initialPayment, period, interestRate, paymentType, System.currentTimeMillis());
  }
}
