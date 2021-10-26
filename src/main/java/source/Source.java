package source;

import bufferAndManagers.BufferManager;
import mortgage.Request;

import static tools.ConstantsAndParameters.SOURCE_MAX_DELAY;
import static tools.ConstantsAndParameters.SOURCE_MIN_DELAY;

public class Source implements Runnable {
  private final BufferManager bufferManager;

  public Source(BufferManager bufferManager) {
    this.bufferManager = bufferManager;
  }

  @Override
  public void run() {
    while (!Thread.currentThread().isInterrupted()) {
      Request request = Generator.generate();
      bufferManager.emplace(request);

      try {
        Thread.sleep((long) (Math.random() * (SOURCE_MAX_DELAY - SOURCE_MIN_DELAY + 1)) + SOURCE_MIN_DELAY);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      }

    }
  }
}
