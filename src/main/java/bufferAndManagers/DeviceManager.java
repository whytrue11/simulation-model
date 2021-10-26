package bufferAndManagers;

import device.Device;
import mortgage.Request;

import java.util.List;
import java.util.Vector;

import static tools.ConstantsAndParameters.DEVICE_WORK_IMITATION_TIME;

public class DeviceManager implements Runnable {
  private final Vector<Device> devices;
  private final Buffer buffer;
  private int devicePointer;
  private int requestPointer;

  private final Object bufferNotEmptyNotifier;

  public DeviceManager(Buffer buffer, Vector<Device> devices, Object bufferNotEmptyNotifier) {
    this.devices = devices;
    this.buffer = buffer;
    this.bufferNotEmptyNotifier = bufferNotEmptyNotifier;
  }

  @Override
  public void run() {
    Request request = null;
    Device device = null;
    while (!Thread.currentThread().isInterrupted()) {
      if (buffer.isEmpty()) {
        try {
          synchronized (bufferNotEmptyNotifier) {
            System.out.println("Empty buffer");
            bufferNotEmptyNotifier.wait();
          }
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          break;
        }
      }

      try {
        device = selectDevice();
      } catch (Exception e) {
        //System.out.println(e.getMessage());
        /*try {
          Thread.sleep(DEVICE_WORK_IMITATION_TIME / 3);
        } catch (InterruptedException interruptedException) {
          Thread.currentThread().interrupt();
          break;
        }*/
        continue;
      }

      try {
        request = selectRequest();
      } catch (Exception e) {
        System.out.println(e.getMessage());
        continue;
      }

      device.requestProcessing(request);
      synchronized (buffer) {
        buffer.getRequestsList().forEach(System.out::print);
        System.out.println("");
      }
    }
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
