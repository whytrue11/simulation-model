package device;

import mortgage.Request;
import tools.Report;
import tools.ResponsesWriter;

import java.io.IOException;

import static tools.ConstantsAndParameters.MILLISECONDS_PER_SECOND;

public class Device implements Runnable {
  private static int count;

  private final int number;
  private volatile Request request;
  private volatile boolean isFree;
  private final Object newRequestNotifier;

  private final Report report;
  private final Object stepReportSynchronizer;

  public Device(Report report, Object stepReportSynchronizer) {
    this.number = count++;
    this.report = report;
    this.isFree = true;
    this.newRequestNotifier = new Object();
    this.stepReportSynchronizer = stepReportSynchronizer;
  }

  public int getNumber() {
    return number;
  }

  public boolean isFree() {
    return isFree;
  }

  public void requestProcessing(Request request) {
    synchronized (stepReportSynchronizer) {
      try {
        stepReportSynchronizer.wait();
      } catch (InterruptedException ignored) {
      }
    }
    this.request = request;
    synchronized (newRequestNotifier) {
      isFree = false;
      newRequestNotifier.notify();
    }
    System.out.println("Device " + number + ": start process request " + request.getNumber());
  }

  @Override
  public void run() {
    long startBusyTime = 0;
    long startDownTime = 0;
    while (!Thread.currentThread().isInterrupted()) {
      synchronized (newRequestNotifier) {
        try {
          startDownTime = System.currentTimeMillis();
          newRequestNotifier.wait();
          startBusyTime = System.currentTimeMillis();
          report.addDeviceDownTime(number, startBusyTime - startDownTime);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          report.addDeviceDownTime(number, System.currentTimeMillis() - startDownTime);
          break;
        }
      }

      try {
        Thread.sleep((long) (Math.exp(Math.random()) * MILLISECONDS_PER_SECOND));
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        report.incrementRejectedRequestCount(request.getSourceNumber());
        report.addRequestTimeInBuffer(request.getSourceNumber(), startBusyTime - request.getArrivalTime());

        ResponsesWriter.requestRejection(request);
        System.out.println("Request " + request.getNumber() + " refused");
        break;
      }
      try {
        synchronized (ResponsesWriter.getWorkbook()) {
          MortgageCalculator.fileOutputCalculation(request, ResponsesWriter.getWorkbook(), ResponsesWriter.getResponsesFileName());
        }
      } catch (IOException e) {
        e.printStackTrace();
      }

      report.incrementProcessedRequestCount(request.getSourceNumber());
      report.addRequestServiceTime(request.getSourceNumber(), System.currentTimeMillis() - request.getArrivalTime());

      report.addDeviceBusyTime(number, System.currentTimeMillis() - startBusyTime);
      System.out.println("Device " + number + ": finish process request " + request.getNumber());
      isFree = true;
    }
  }
}
