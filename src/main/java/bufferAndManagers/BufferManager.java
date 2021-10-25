package bufferAndManagers;

import mortgage.Request;

import java.util.List;

public class BufferManager {
  private final Buffer buffer;
  private int indexOfLastRequest;

  public BufferManager(Buffer buffer) {
    this.buffer = buffer;
  }

  public void emplace(Request request) {
    synchronized (buffer) {
      List<Request> requestsList = buffer.getRequestsList();
      for (int i = 0; i < requestsList.size(); i++) {
        if (requestsList.get(i) == null) {
          buffer.set(i, request);
          indexOfLastRequest = i;
          return;
        }
      }
      buffer.set(indexOfLastRequest, request);
    }
  }
}
