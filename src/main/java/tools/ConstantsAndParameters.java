package tools;

public class ConstantsAndParameters {
  public static final int MILLION = 1000000;
  public static final int MONTHS_PER_YEAR = 12;
  public static final int MILLISECONDS_PER_SECOND = 1000;

  public static final int SIMULATION_TIME = 10 * MILLISECONDS_PER_SECOND;
  public static final int BUFFER_SIZE = 20;
  public static final int SOURCES_COUNT = 10;
  public static final int DEVICES_COUNT = 5;

  public static final long SOURCE_MAX_DELAY = 5 * MILLISECONDS_PER_SECOND;
  public static final long SOURCE_MIN_DELAY = 2 * MILLISECONDS_PER_SECOND;

  public static final int GENERATOR_MAX_ESTATE_COST = 20 * MILLION;
  public static final int GENERATOR_MIN_ESTATE_COST = 3 * MILLION;
  public static final int GENERATOR_MAX_PERIOD = 50;
  public static final int GENERATOR_MIN_PERIOD = 10;
  public static final int GENERATOR_MAX_INTEREST_RATE = 20;
  public static final int GENERATOR_MIN_INTEREST_RATE = 5;

  public static final long DEVICE_WORK_IMITATION_TIME = (long) (Math.exp(SOURCES_COUNT
      / (((double) (SOURCE_MAX_DELAY - SOURCE_MIN_DELAY) / 2 + SOURCE_MIN_DELAY) / MILLISECONDS_PER_SECOND))
      * MILLISECONDS_PER_SECOND);
}
