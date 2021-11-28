package mortgage;

public class Request {
  private int number;
  private int sourceNumber;
  private int estateCost;
  private int initialPayment;
  private int period;
  private double interestRate;
  private PaymentType paymentType;
  private long arrivalTime;

  public Request(int number, int sourceNumber, int estateCost, int initialPayment, int period, double interestRate, PaymentType paymentType, long arrivalTime) {
    this.number = number;
    this.sourceNumber = sourceNumber;
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

  public int getSourceNumber() {
    return sourceNumber;
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
}
