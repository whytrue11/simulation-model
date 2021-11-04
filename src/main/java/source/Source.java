package source;

import bufferAndManagers.BufferManager;
import mortgage.Request;
import tools.Report;

import static tools.ConstantsAndParameters.SOURCE_MAX_DELAY;
import static tools.ConstantsAndParameters.SOURCE_MIN_DELAY;

public class Source implements Runnable {
  private final int number;
  private final BufferManager bufferManager;
  private final Report report;

  public Source(int number, BufferManager bufferManager, Report report) {
    this.number = number;
    this.bufferManager = bufferManager;
    this.report = report;
  }

  public int getNumber() {
    return number;
  }

  @Override
  public void run() {
    while (!Thread.currentThread().isInterrupted()) {
      Request request = Generator.generate(number);
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
