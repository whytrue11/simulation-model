package device;

import mortgage.Request;
import mortgage.PaymentType;

import static tools.ConstantsAndParameters.MONTHS_PER_YEAR;

public class MortgageCalculator {

  public static String calculation(Request request) {
    StringBuilder stringBuilder = new StringBuilder();

    final double MONTHLY_RATE = request.getInterestRate() / MONTHS_PER_YEAR / 100;
    final double TOTAL_RATE = Math.pow((1 + MONTHLY_RATE), request.getPeriod() * MONTHS_PER_YEAR);
    final double MONTHLY_PAYMENT = (request.getEstateCost() - request.getInitialPayment())
        * MONTHLY_RATE * TOTAL_RATE / (TOTAL_RATE - 1);

    double remainingCredit = request.getEstateCost() - request.getInitialPayment();

    CustomFunction customFunction = null;
    if (request.getPaymentType() == PaymentType.ANNUITY) {
      customFunction = new AnnuityPayment();
    } else if (request.getPaymentType() == PaymentType.DIFFERENTIATED) {
      customFunction = new DifferentiatedPayment();
    } else {
      assert (true);
    }

    stringBuilder.append(String.format("%5s", "Month")
        + String.format(" | %20s", "Percent Part")
        + String.format(" | %20s", "Main Part")
        + String.format(" | %22s", "Remaining Credit")
        + String.format(" %2s", "|\n"));
    stringBuilder.append("------------------------------------------------------------------------------\n");

    StorageStructure storageStructure = new StorageStructure(
        remainingCredit / (request.getPeriod() * MONTHS_PER_YEAR), MONTHLY_PAYMENT);
    double totalPayment = 0;
    double percentPaymentSum = 0;

    for (int month = 1; month <= request.getPeriod() * MONTHS_PER_YEAR; month++) {
      storageStructure.percentPart = remainingCredit * MONTHLY_RATE;
      customFunction.execute(storageStructure);
      totalPayment += storageStructure.mainPart;
      percentPaymentSum += storageStructure.percentPart;
      remainingCredit -= storageStructure.mainPart;

      stringBuilder.append(String.format("%5s", month)
          + String.format(" | %20s", storageStructure.percentPart)
          + String.format(" | %20s", storageStructure.mainPart)
          + String.format(" | %22s", remainingCredit)
          + String.format(" %2s", "|\n"));
    }

    totalPayment += percentPaymentSum;
    if (request.getPaymentType() != PaymentType.DIFFERENTIATED) {
      stringBuilder.append("Monthly payment: " + MONTHLY_PAYMENT);
    }
    stringBuilder.append("\nOverpayment: " + percentPaymentSum +
        "\nTotal payment: " + totalPayment);
    return stringBuilder.toString();
  }

  private static class StorageStructure {
    double mainPart;
    double percentPart;
    double MONTHLY_PAYMENT;

    public StorageStructure(double mainPart, double MONTHLY_PAYMENT) {
      this.mainPart = mainPart;
      this.MONTHLY_PAYMENT = MONTHLY_PAYMENT;
    }
  }

  private interface CustomFunction {
    void execute(StorageStructure storageStructure);
  }

  private static class DifferentiatedPayment implements CustomFunction {

    @Override
    public void execute(StorageStructure storageStructure) {
    }
  }

  private static class AnnuityPayment implements CustomFunction {

    @Override
    public void execute(StorageStructure storageStructure) {
      storageStructure.mainPart = storageStructure.MONTHLY_PAYMENT - storageStructure.percentPart;
    }
  }
}
