package bufferAndManagers;

import device.Device;
import mortgage.Request;
import tools.Report;
import tools.ResponsesWriter;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class DeviceManager implements Runnable {
  private final Vector<Device> devices;
  private final Buffer buffer;
  private volatile int devicePointer;
  private volatile int requestPointer;

  private final Report report;
  private final Object bufferNotEmptyNotifier;
  private final Object stepReportSynchronizer;

  public DeviceManager(Buffer buffer, Vector<Device> devices, Report report, Object bufferNotEmptyNotifier, Object stepReportSynchronizer) {
    this.devices = devices;
    this.buffer = buffer;
    this.report = report;
    this.bufferNotEmptyNotifier = bufferNotEmptyNotifier;
    this.stepReportSynchronizer = stepReportSynchronizer;
  }

  public List<Boolean> getDeviceStatuses() {
    List<Boolean> deviceStatuses = new ArrayList<>(devices.size());
    synchronized (devices) {
      for (Device device : devices) {
        deviceStatuses.add(device.isFree());
      }
    }
    return deviceStatuses;
  }

  public int getRequestPointer() {
    return requestPointer;
  }

  public int getDevicePointer() {
    return devicePointer;
  }

  public String bufferOutput() {
    StringBuilder stringBuilder = new StringBuilder();
    synchronized (buffer.getRequestsList()) {
      int i = 0;
      for (Request request : buffer.getRequestsList()) {
        stringBuilder
            .append(requestPointer == i ? "*" : "")
            .append(request == null ? null : request.getNumber())
            .append(" | ");
        ++i;
      }
    }
    return stringBuilder.toString();
  }

  @Override
  public void run() {
    while (!Thread.currentThread().isInterrupted()) {
      if (buffer.isEmpty()) {
        System.out.println("Buffer empty");
        try {
          synchronized (bufferNotEmptyNotifier) {
            bufferNotEmptyNotifier.wait();
          }
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          break;
        }
      }

      Device device = null;
      try {
        device = selectDevice();
      } catch (Exception e) {
        continue;
      }

      synchronized (stepReportSynchronizer) {
        try {
          stepReportSynchronizer.wait();
        } catch (InterruptedException ignored) {
        }
      }
      Request request = null;
      try {
        request = selectRequest();
      } catch (Exception e) {
        System.out.println(e.getMessage());
        continue;
      }

      device.requestProcessing(request);
    }
    long endWorkTime = System.currentTimeMillis();
    buffer.getRequestsList().forEach(request -> {
      if (request != null) {
        ResponsesWriter.requestRejection(request);

        report.incrementRejectedRequestCount(request.getSourceNumber());
        report.addRequestTimeInBuffer(request.getSourceNumber(), endWorkTime - request.getArrivalTime());
        System.out.println("Request " + request.getNumber() + " refused");
      }
    });
  }

  private Device selectDevice() throws Exception {
    Device device = null;
    synchronized (devices) {
      for (int i = devicePointer; i < devices.size(); i++) {
        if ((device = devices.get(i)).isFree()) {
          devicePointer = i;
          return device;
        }
      }
      for (int i = 0; i < devicePointer; i++) {
        if ((device = devices.get(i)).isFree()) {
          devicePointer = i;
          return device;
        }
      }
    }
    throw new Exception("No free devices");
  }

  private Request selectRequest() throws Exception {
    Request request = null;
    synchronized (buffer.getRequestsList()) {
      List<Request> requestsList = buffer.getRequestsList();
      for (int i = requestPointer; i < requestsList.size(); i++) {
        if (requestsList.get(i) != null) {
          requestPointer = i;
          request = buffer.get(i);
          return request;
        }
      }
      for (int i = 0; i < requestPointer; i++) {
        if (requestsList.get(i) != null) {
          requestPointer = i;
          request = buffer.get(i);
          return request;
        }
      }
      throw new Exception("No requests in buffer");
    }
  }
}
