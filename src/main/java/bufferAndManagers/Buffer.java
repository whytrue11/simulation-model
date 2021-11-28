package bufferAndManagers;

import mortgage.Request;
import tools.Report;
import tools.ResponsesWriter;

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
    System.out.println("Buffer  : request " + request.getNumber() + " taken for processing");
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
      ResponsesWriter.requestRejection(oldRequest);

      report.incrementRejectedRequestCount(oldRequest.getSourceNumber());
      report.addRequestTimeInBuffer(oldRequest.getSourceNumber(),System.currentTimeMillis() - oldRequest.getArrivalTime());
      System.out.println("Request " + oldRequest.getNumber() + " refused");
    }
    System.out.println("Buffer  : request " + request.getNumber() + " placed");
  }

  public synchronized boolean isEmpty() {
    return occupiedCells == 0;
  }

  public synchronized boolean isFull() {
    return occupiedCells == buffer.size();
  }

  List<Request> getRequestsList() {
    return buffer;
  }
}
