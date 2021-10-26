package bufferAndManagers;

import mortgage.Request;
import tools.Report;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Buffer {
  private final List<Request> buffer;
  private int occupiedCells;
  private final Report report;

  private final Object bufferNotEmptyNotifier;

  public Buffer(int size, Report report, Object bufferNotEmptyNotifier) {
    this.buffer = new ArrayList<>(Collections.nCopies(size, null));
    this.report = report;
    this.bufferNotEmptyNotifier = bufferNotEmptyNotifier;
  }

  public Request get(int index) {
    Request request = buffer.get(index);
    buffer.set(index, null);
    --occupiedCells;
    return request;
  }

  public void set(int index, Request request) {
    Request oldRequest = buffer.get(index);
    buffer.set(index, request);
    if (oldRequest == null) {
      ++occupiedCells;
      synchronized (bufferNotEmptyNotifier) {
        bufferNotEmptyNotifier.notify();
      }
    }
    else {
      //TODO: отправка сообщения юзеру об отказе заявки
      report.incrementRejectedRequestCount();
      report.addTimeInSystem(System.currentTimeMillis() - request.getArrivalTime());
    }
    report.incrementTotalRequestCount();
  }

  public synchronized boolean isEmpty() {
    return occupiedCells == 0;
  }

  public synchronized boolean isFull() {
    return occupiedCells == buffer.size();
  }

  public BigDecimal getTimeInSystemRemainingRequests(long endSystemWorkTime) {
    BigDecimal timeInSystemRemainingRequests = new BigDecimal(0);
    for (Request request : buffer) {
      if (request != null) {
        timeInSystemRemainingRequests = timeInSystemRemainingRequests.add(BigDecimal.valueOf(endSystemWorkTime - request.getArrivalTime()));
      }
    }
    return timeInSystemRemainingRequests;
  }

  protected List<Request> getRequestsList() {
    return buffer;
  }
}
