package source;

import bufferAndManagers.BufferManager;
import mortgage.Request;
import tools.Report;

import static tools.ConstantsAndParameters.SOURCE_MAX_DELAY;
import static tools.ConstantsAndParameters.SOURCE_MIN_DELAY;

public class Source implements Runnable {
  private static int count;

  private final int number;
  private final BufferManager bufferManager;
  private final Report report;

  private final Object stepReportSynchronizer;

  public Source(BufferManager bufferManager, Report report, Object stepReportSynchronizer) {
    this.number = count++;
    this.bufferManager = bufferManager;
    this.report = report;
    this.stepReportSynchronizer = stepReportSynchronizer;
  }

  public int getNumber() {
    return number;
  }

  @Override
  public void run() {
    while (!Thread.currentThread().isInterrupted()) {
      synchronized (stepReportSynchronizer) {
        try {
          stepReportSynchronizer.wait();
        } catch (InterruptedException ignored) {
          break;
        }
      }
      Request request = Generator.generate(number);
      System.out.println("Source " + number + ": generate request " + request.getNumber());
      bufferManager.emplace(request);

      report.incrementGeneratedRequestCount(number);

      try {
        Thread.sleep((long) (Math.random() * (SOURCE_MAX_DELAY - SOURCE_MIN_DELAY + 1)) + SOURCE_MIN_DELAY);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      }

    }
  }
}
