package mortgage;

public class Request {
  private int number;
  private int estateCost;
  private int initialPayment;
  private int period;
  private double interestRate;
  private PaymentType paymentType;
  private long arrivalTime;

  public Request(int number, int estateCost, int initialPayment, int period, double interestRate, PaymentType paymentType, long arrivalTime) {
    this.number = number;
    this.estateCost = estateCost;
    this.initialPayment = initialPayment;
    this.period = period;
    this.interestRate = interestRate;
    this.paymentType = paymentType;
    this.arrivalTime = arrivalTime;
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

  public long getArrivalTime() {
    return arrivalTime;
  }

  @Override
  public String toString() {
    return "{" + number + "} ";
  }
}
