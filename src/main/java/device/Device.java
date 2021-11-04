package device;

import mortgage.Request;
import tools.Report;
import tools.ResponsesWriter;

import java.io.IOException;

import static tools.ConstantsAndParameters.MILLISECONDS_PER_SECOND;

public class Device implements Runnable {
  private final int number;
  private volatile Request request;
  private volatile boolean isFree;
  private final Object pause;

  private final Report report;

  public Device(int number, Report report) {
    this.number = number;
    this.report = report;
    this.isFree = true;
    this.pause = new Object();
  }

  public int getNumber() {
    return number;
  }

  public synchronized boolean isFree() {
    return isFree;
  }

  public synchronized void requestProcessing(Request request) {
    this.request = request;
    synchronized (pause) {
      isFree = false;
      pause.notify();
    }
  }

  @Override
  public void run() {
    long startBusyTime = 0;
    long startDownTime = 0;
    while (!Thread.currentThread().isInterrupted()) {
      synchronized (pause) {
        try {
          startDownTime = System.currentTimeMillis();
          pause.wait();
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
      isFree = true;
    }
  }
}
