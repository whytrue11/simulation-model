package device;

import mortgage.Request;

import static tools.ConstantsAndParameters.DEVICE_WORK_IMITATION_TIME;

public class Device implements Runnable {
  private volatile Request request;
  private volatile boolean isFree;
  private final Object pause;

  public Device() {
    this.isFree = true;
    this.pause = new Object();
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
    while (true)
    {
      synchronized (pause) {
        try {
          pause.wait();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
      System.out.println(request.getNumber() + " start");
      MortgageCalculator.calculation(request);

      try {
        Thread.sleep(DEVICE_WORK_IMITATION_TIME);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      System.out.println(request.getNumber() + " finish");
      isFree = true;
    }
  }
}
