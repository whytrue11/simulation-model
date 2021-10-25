package mortgage;

public class Request {
  private int number;
  private int estateCost;
  private int initialPayment;
  private int period;
  private double interestRate;
  private PaymentType paymentType;

  public Request(int number, int estateCost, int initialPayment, int period, double interestRate, PaymentType paymentType) {
    this.number = number;
    this.estateCost = estateCost;
    this.initialPayment = initialPayment;
    this.period = period;
    this.interestRate = interestRate;
    this.paymentType = paymentType;
  }

  public int getNumber() {
    return number;
  }

  public int getEstateCost() {
    return estateCost;
  }

  public int getInitialPayment() {
    return initialPayment;
  }

  public int getPeriod() {
    return period;
  }

  public double getInterestRate() {
    return interestRate;
  }

  public PaymentType getPaymentType() {
    return paymentType;
  }

  @Override
  public String toString() {
    return "{" + number + "} ";
  }
}
