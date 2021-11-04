package bufferAndManagers;

import device.Device;
import mortgage.Request;
import tools.Report;
import tools.ResponsesWriter;

import java.util.List;
import java.util.Vector;

public class DeviceManager implements Runnable {
  private final Vector<Device> devices;
  private final Buffer buffer;
  private int devicePointer;
  private int requestPointer;

  private final Report report;
  private final Object bufferNotEmptyNotifier;

  public DeviceManager(Buffer buffer, Vector<Device> devices, Report report, Object bufferNotEmptyNotifier) {
    this.devices = devices;
    this.buffer = buffer;
    this.report = report;
    this.bufferNotEmptyNotifier = bufferNotEmptyNotifier;
  }

  @Override
  public void run() {
    while (!Thread.currentThread().isInterrupted()) {
      if (buffer.isEmpty()) {
        try {
          synchronized (bufferNotEmptyNotifier) {
            bufferNotEmptyNotifier.wait();
          }
        } catch (InterruptedException e) {
          break;
        }
      }

      Device device = null;
      try {
        device = selectDevice();
      } catch (Exception e) {
        continue;
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
    synchronized (buffer) {
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
