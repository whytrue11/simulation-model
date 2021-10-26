import bufferAndManagers.Buffer;
import bufferAndManagers.BufferManager;
import bufferAndManagers.DeviceManager;
import device.Device;
import source.Source;
import tools.Report;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import static tools.ConstantsAndParameters.*;

public class Main {

  public static void main(String[] args) {
    Report report = new Report();

    //Buffer
    Object bufferNotEmptyNotifier = new Object();
    Buffer buffer = new Buffer(BUFFER_SIZE, report, bufferNotEmptyNotifier);

    //Buffer Manager
    BufferManager bufferManager = new BufferManager(buffer);

    //Sources
    List<Thread> sourcesThreads = new ArrayList<>(SOURCES_COUNT);
    for (int i = 0; i < SOURCES_COUNT; i++) {
      sourcesThreads.add(new Thread(new Source(bufferManager)));
    }

    //Devices
    Vector<Device> devices = new Vector<>(DEVICES_COUNT);
    List<Thread> devicesThreads = new ArrayList<>(DEVICES_COUNT);
    for (int i = 1; i <= DEVICES_COUNT; i++) {
      Device device = new Device(i, report);
      devices.add(device);
      devicesThreads.add(new Thread(device));
    }

    //Device Manager
    DeviceManager deviceManager = new DeviceManager(buffer, devices, bufferNotEmptyNotifier);
    Thread deviceManagerThread = new Thread(deviceManager);


    long simulationTime = SIMULATION_TIME;
    if (SIMULATION_TIME < DEVICE_WORK_IMITATION_TIME) {
      simulationTime = DEVICE_WORK_IMITATION_TIME + MILLISECONDS_PER_SECOND;
    }

    long startSystemWorkTime = System.currentTimeMillis();
    //Start threads
    deviceManagerThread.start();
    sourcesThreads.forEach(Thread::start);
    devicesThreads.forEach(Thread::start);

    try {
      Thread.sleep(simulationTime);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    sourcesThreads.forEach(Thread::interrupt);
    deviceManagerThread.interrupt();
    devicesThreads.forEach(Thread::interrupt);

    long endSystemWorkTime = System.currentTimeMillis();
    report.calculateAverageRequestTimeInSystem(buffer.getTimeInSystemRemainingRequests(endSystemWorkTime));

    System.out.println(report.toString());
  }
}
