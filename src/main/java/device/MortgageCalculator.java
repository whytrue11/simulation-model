package device;

import mortgage.PaymentType;
import mortgage.Request;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static tools.ConstantsAndParameters.MONTHS_PER_YEAR;

public class MortgageCalculator {

  public static String consoleOutputCalculation(Request request) {
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

  public static void fileOutputCalculation(Request request, Workbook workbook, String fileName) throws IOException {
    Sheet sheet = workbook.createSheet("Request " + request.getNumber());

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

    int rowCounter = 0;
    Row row = sheet.createRow(rowCounter++);
    List<Cell> cells = new ArrayList<>(8);
    for (int i = 0; i < 4; i++) {
      cells.add(row.createCell(i));
    }
    cells.get(0).setCellValue("Month");
    cells.get(1).setCellValue("Percent Part");
    cells.get(2).setCellValue("Main Part");
    cells.get(3).setCellValue("Remaining Credit");

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

      row = sheet.createRow(rowCounter++);
      cells.clear();
      for (int i = 0; i < 4; i++) {
        cells.add(row.createCell(i));
      }
      cells.get(0).setCellValue(month);
      cells.get(1).setCellValue(storageStructure.percentPart);
      cells.get(2).setCellValue(storageStructure.mainPart);
      cells.get(3).setCellValue(remainingCredit);
    }

    totalPayment += percentPaymentSum;
    if (request.getPaymentType() != PaymentType.DIFFERENTIATED) {
      row = sheet.createRow(rowCounter++);
      cells.clear();
      for (int i = 0; i < 4; i++) {
        cells.add(row.createCell(i));
      }
      cells.get(0).setCellValue("Monthly payment:");
      cells.get(1).setCellValue(MONTHLY_PAYMENT);
    }
    row = sheet.createRow(rowCounter++);
    cells.clear();
    for (int i = 0; i < 2; i++) {
      cells.add(row.createCell(i));
    }
    cells.get(0).setCellValue("Overpayment:");
    cells.get(1).setCellValue(percentPaymentSum);

    row = sheet.createRow(rowCounter++);
    cells.clear();
    for (int i = 0; i < 2; i++) {
      cells.add(row.createCell(i));
    }
    cells.get(0).setCellValue("Total payment:");
    cells.get(1).setCellValue(totalPayment);

    for (int i = 0; i < 4; i++) {
      sheet.autoSizeColumn(i);
    }
    workbook.write(new FileOutputStream(fileName));
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
