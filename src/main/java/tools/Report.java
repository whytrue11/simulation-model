package tools;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Report {
  private final List<DeviceReport> deviceReports;
  private final List<SourceReport> sourceReports;
  private final int bufferSize;

  private int totalRequestCount;
  private double failureProbability;
  private BigDecimal totalRequestTimeInSystem;
  private BigDecimal totalDeviceDownTime;
  private BigDecimal totalDeviceBusyTime;

  private double systemWorkload;
  private double averageRequestTimeInSystem;

  private final Workbook workbook;
  private int stepCount;
  private final String reportFileName;

  public Report(int sourceCount, int deviceCount, int bufferSize, String reportFileName) {
    this.sourceReports = new ArrayList<>(sourceCount);
    this.deviceReports = new ArrayList<>(deviceCount);
    this.bufferSize = bufferSize;

    for (int i = 0; i < sourceCount; i++) {
      sourceReports.add(new SourceReport(i));
    }
    for (int i = 0; i < deviceCount; i++) {
      deviceReports.add(new DeviceReport(i));
    }

    this.totalRequestTimeInSystem = new BigDecimal(0);
    this.totalDeviceDownTime = new BigDecimal(0);
    this.totalDeviceBusyTime = new BigDecimal(0);

    //Create table
    this.workbook = new HSSFWorkbook();

    this.reportFileName = reportFileName;
  }

  //Source reports
  public void incrementGeneratedRequestCount(int number) {
    sourceReports.get(number).incrementGeneratedRequestCount();
  }

  public void incrementProcessedRequestCount(int number) {
    sourceReports.get(number).incrementProcessedRequestCount();
  }

  public void incrementRejectedRequestCount(int number) {
    sourceReports.get(number).incrementRejectedRequestCount();
  }

  public void addRequestServiceTime(int number, long requestServiceTime) {
    sourceReports.get(number).addRequestServiceTime(requestServiceTime);
  }

  public void addRequestTimeInBuffer(int number, long requestTimeInBuffer) {
    sourceReports.get(number).addRequestTimeInBuffer(requestTimeInBuffer);
  }

  //Device reports
  public void addDeviceDownTime(int number, long downTime) {
    deviceReports.get(number).addDownTime(downTime);
  }

  public void addDeviceBusyTime(int number, long busyTime) {
    deviceReports.get(number).addBusyTime(busyTime);
  }


  public synchronized void writeStepReport() throws IOException {
    Sheet sheet = workbook.createSheet("Step report " + (++stepCount));
    int rowCounter = 0;
    Row row = sheet.createRow(rowCounter++);
    List<Cell> cells = new ArrayList<>(6);
    for (int i = 0; i < 6; i++) {
      cells.add(row.createCell(i));
    }
    cells.get(0).setCellValue("Source number");
    cells.get(1).setCellValue("Generated request count");
    cells.get(2).setCellValue("Failure probability");
    cells.get(3).setCellValue("Requests service time");
    cells.get(4).setCellValue("Requests time in buffer");
    cells.get(5).setCellValue("Total time in system");

    for (SourceReport report : sourceReports) {
      row = sheet.createRow(rowCounter++);
      cells.clear();
      for (int j = 0; j < 6; j++) {
        cells.add(row.createCell(j));
      }
      cells.get(0).setCellValue("Source " + report.getNumber());
      cells.get(1).setCellValue(report.getGeneratedRequestCount());
      cells.get(2).setCellValue((report.getProcessedRequestCount() + report.getRejectedRequestCount() == 0)
          ? 0
          : (double) report.getRejectedRequestCount() / (report.getProcessedRequestCount() + report.getRejectedRequestCount()));
      cells.get(3).setCellValue(report.getRequestServiceTime().longValue());
      cells.get(4).setCellValue(report.getRequestTimeInBuffer().longValue());
      cells.get(5).setCellValue(report.getRequestServiceTime().add(report.getRequestTimeInBuffer()).longValue());
    }

    sheet.createRow(rowCounter++);

    row = sheet.createRow(rowCounter++);
    cells.clear();
    for (int i = 0; i < 2; i++) {
      cells.add(row.createCell(i));
    }
    cells.get(0).setCellValue("Device number");
    cells.get(1).setCellValue("Use factor");

    for (DeviceReport report : deviceReports) {
      row = sheet.createRow(rowCounter++);
      cells.clear();
      for (int j = 0; j < 2; j++) {
        cells.add(row.createCell(j));
      }
      cells.get(0).setCellValue("Device " + report.getNumber());
      cells.get(1).setCellValue(report.getUseFactor());
    }

    for (int i = 0; i < 6; i++) {
      sheet.autoSizeColumn(i);
    }

    workbook.write(new FileOutputStream(reportFileName));
  }

  public synchronized void writeTotalReport() throws IOException {
    calculate();
    Sheet sheet = workbook.createSheet("Total report");
    int rowCounter = 0;
    Row row = sheet.createRow(rowCounter++);
    List<Cell> cells = new ArrayList<>(8);
    for (int i = 0; i < 8; i++) {
      cells.add(row.createCell(i));
    }
    cells.get(0).setCellValue("Source count");
    cells.get(1).setCellValue("Device count");
    cells.get(2).setCellValue("Buffer size");
    cells.get(3).setCellValue("Total request count");
    cells.get(4).setCellValue("Failure probability");
    cells.get(5).setCellValue("Average request time in system");
    cells.get(6).setCellValue("System work time");
    cells.get(7).setCellValue("System workload");

    row = sheet.createRow(rowCounter++);
    cells.clear();
    for (int i = 0; i < 8; i++) {
      cells.add(row.createCell(i));
      sheet.autoSizeColumn(i);
    }
    cells.get(0).setCellValue(sourceReports.size());
    cells.get(1).setCellValue(deviceReports.size());
    cells.get(2).setCellValue(bufferSize);
    cells.get(3).setCellValue(totalRequestCount);
    cells.get(4).setCellValue(failureProbability);
    cells.get(5).setCellValue(averageRequestTimeInSystem);
    cells.get(6).setCellValue(totalDeviceBusyTime.add(totalDeviceBusyTime).longValue());
    cells.get(7).setCellValue(systemWorkload);

    workbook.write(new FileOutputStream(reportFileName));
  }

  //Other
  private synchronized void calculate() {

    AtomicInteger processedRequestCount = new AtomicInteger();
    AtomicInteger rejectedRequestCount = new AtomicInteger();

    sourceReports.forEach(sourceReport -> {
      totalRequestCount += sourceReport.getGeneratedRequestCount();
      processedRequestCount.addAndGet(sourceReport.getProcessedRequestCount());
      rejectedRequestCount.addAndGet(sourceReport.getRejectedRequestCount());
      totalRequestTimeInSystem = totalRequestTimeInSystem.add(sourceReport.getRequestTimeInBuffer().add(sourceReport.getRequestServiceTime()));
    });

    failureProbability = (double) rejectedRequestCount.get() / (rejectedRequestCount.get() + processedRequestCount.get());

    deviceReports.forEach(deviceReport -> {
      totalDeviceBusyTime = totalDeviceBusyTime.add(deviceReport.getBusyTime());
      totalDeviceDownTime = totalDeviceDownTime.add(deviceReport.getDownTime());
    });

    systemWorkload = totalDeviceBusyTime.divide(totalDeviceDownTime.add(totalDeviceBusyTime), 5, RoundingMode.HALF_EVEN).doubleValue();
    averageRequestTimeInSystem = totalRequestTimeInSystem
        .divide(BigDecimal.valueOf(totalRequestCount), 5, RoundingMode.HALF_EVEN).doubleValue();
  }
}
