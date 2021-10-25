import bufferAndManagers.Buffer;
import bufferAndManagers.BufferManager;
import bufferAndManagers.DeviceManager;
import device.Device;
import device.MortgageCalculator;
import mortgage.PaymentType;
import mortgage.Request;
import source.Source;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

public class Main {

  public static final int BUFFER_SIZE = 20;
  public static final int SOURCES_COUNT = 5;
  public static final int DEVICES_COUNT = 5;

  public static void main(String[] args) {
    //Buffer
    Object bufferNotEmptyNotifier = new Object();
    Buffer buffer = new Buffer(BUFFER_SIZE, bufferNotEmptyNotifier);

    //Buffer Manager
    BufferManager bufferManager = new BufferManager(buffer);

    //Devices
    Vector<Device> devices = new Vector<>(DEVICES_COUNT);
    List<Thread> devicesThreads = new ArrayList<>(DEVICES_COUNT);
    for (int i = 0; i < DEVICES_COUNT; i++) {
      Device device = new Device();
      devices.add(device);
      Thread thread = new Thread(device);
      thread.start();
      devicesThreads.add(thread);
    }

    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    //Device Manager
    DeviceManager deviceManager = new DeviceManager(buffer, devices, bufferNotEmptyNotifier);
    Thread deviceManagerThread = new Thread(deviceManager);
    deviceManagerThread.start();

    //Sources
    List<Thread> sourcesThreads = new ArrayList<>(SOURCES_COUNT);
    for (int i = 0; i < SOURCES_COUNT; i++) {
      Thread thread = new Thread(new Source(bufferManager));
      thread.start();
      sourcesThreads.add(thread);
    }


    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    System.out.println();
  }
}
