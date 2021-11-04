import bufferAndManagers.Buffer;
import bufferAndManagers.BufferManager;
import bufferAndManagers.DeviceManager;
import device.Device;
import source.Source;
import tools.Report;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import static tools.ConstantsAndParameters.*;

public class Main {

  public static void main(String[] args) {
    Report report = new Report(SOURCES_COUNT, DEVICES_COUNT, BUFFER_SIZE, "Report.xls");

    //Buffer
    Object bufferNotEmptyNotifier = new Object();
    Buffer buffer = new Buffer(BUFFER_SIZE, report, bufferNotEmptyNotifier);

    //Buffer Manager
    BufferManager bufferManager = new BufferManager(buffer);

    //Sources
    List<Thread> sourcesThreads = new ArrayList<>(SOURCES_COUNT);
    for (int i = 0; i < SOURCES_COUNT; i++) {
      sourcesThreads.add(new Thread(new Source(i, bufferManager, report)));
    }

    //Devices
    Vector<Device> devices = new Vector<>(DEVICES_COUNT);
    List<Thread> devicesThreads = new ArrayList<>(DEVICES_COUNT);
    for (int i = 0; i < DEVICES_COUNT; i++) {
      Device device = new Device(i, report);
      devices.add(device);
      devicesThreads.add(new Thread(device));
    }

    //Device Manager
    DeviceManager deviceManager = new DeviceManager(buffer, devices, report, bufferNotEmptyNotifier);
    Thread deviceManagerThread = new Thread(deviceManager);

    //Start threads
    try {
      devicesThreads.forEach(Thread::start);
      Thread.sleep(1);
      sourcesThreads.forEach(Thread::start);
      Thread.sleep(1);
      deviceManagerThread.start();

      for (int i = 0; i < STEP_COUNT; i++) {
        Thread.sleep(SIMULATION_TIME / STEP_COUNT);
        report.writeStepReport();
      }
    } catch (InterruptedException | IOException e) {
      e.printStackTrace();
    }

    sourcesThreads.forEach(Thread::interrupt);
    devicesThreads.forEach(Thread::interrupt);
    deviceManagerThread.interrupt();

    try {
      Thread.sleep(10);
      report.writeStepReport();
      report.writeTotalReport();
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }
  }
}
