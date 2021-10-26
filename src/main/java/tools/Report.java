package tools;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Report {
  private int totalRequestCount;
  private int processedRequestCount;
  private int rejectedRequestCount;
  private BigDecimal totalRequestTimeInSystem;
  private BigDecimal totalDeviceDownTime;
  private BigDecimal totalDeviceBusyTime;

  private double averageRequestTimeInSystem;

  public Report() {
    this.totalRequestTimeInSystem = new BigDecimal(0);
    this.totalDeviceDownTime = new BigDecimal(0);
    this.totalDeviceBusyTime = new BigDecimal(0);
  }

  public synchronized void incrementTotalRequestCount() {
    ++totalRequestCount;
  }

  public synchronized void incrementProcessedRequestCount() {
    ++processedRequestCount;
  }

  public synchronized void incrementRejectedRequestCount() {
    ++rejectedRequestCount;
  }

  public synchronized void addTimeInSystem(long timeInSystem) {
    totalRequestTimeInSystem = totalRequestTimeInSystem.add(BigDecimal.valueOf(timeInSystem));
  }

  public synchronized void addDeviceDownTime(long deviceDownTime) {
    totalDeviceDownTime = totalDeviceDownTime.add(BigDecimal.valueOf(deviceDownTime));
  }

  public synchronized void addDeviceBusyTime(long deviceBusyTime) {
    totalDeviceBusyTime = totalDeviceBusyTime.add(BigDecimal.valueOf(deviceBusyTime));
  }

  public synchronized double getSystemWorkload() {
    return totalDeviceBusyTime.divide(totalDeviceDownTime.add(totalDeviceBusyTime), 5, RoundingMode.HALF_EVEN).doubleValue();
  }

  public synchronized double calculateAverageRequestTimeInSystem(BigDecimal timeRemainingRequestsInBuffer) {
    totalRequestTimeInSystem = totalRequestTimeInSystem.add(timeRemainingRequestsInBuffer);
    return averageRequestTimeInSystem = totalRequestTimeInSystem.divide(BigDecimal.valueOf(totalRequestCount), 5, RoundingMode.HALF_EVEN).doubleValue();
  }

  @Override
  public String toString() {
    return "Report{" +
        "totalRequestCount=" + totalRequestCount +
        ", processedRequestCount=" + processedRequestCount +
        ", rejectedRequestCount=" + rejectedRequestCount +
        ", totalRequestTimeInSystem=" + totalRequestTimeInSystem +
        ", totalDeviceDownTime=" + totalDeviceDownTime +
        ", totalDeviceBusyTime=" + totalDeviceBusyTime +
        ", systemWorkload=" + getSystemWorkload() +
        ", averageRequestTimeInSystem=" + averageRequestTimeInSystem +
        '}';
  }
}
