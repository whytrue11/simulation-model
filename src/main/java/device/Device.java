package device;

import mortgage.Request;
import tools.Report;

import static tools.ConstantsAndParameters.DEVICE_WORK_IMITATION_TIME;

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
    while (!Thread.currentThread().isInterrupted())
    {
      synchronized (pause) {
        try {
          long startDownTime = System.currentTimeMillis();
          pause.wait();
          startBusyTime = System.currentTimeMillis();
          report.addDeviceDownTime(startBusyTime - startDownTime);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          break;
        }
      }
      System.out.println(request.getNumber() + " start");
      MortgageCalculator.calculation(request);

      try {
        Thread.sleep(DEVICE_WORK_IMITATION_TIME);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      report.incrementProcessedRequestCount();
      report.addTimeInSystem(System.currentTimeMillis() - request.getArrivalTime());

      System.out.println(request.getNumber() + " finish");
      report.addDeviceBusyTime(System.currentTimeMillis() - startBusyTime);
      isFree = true;
    }
  }
}
