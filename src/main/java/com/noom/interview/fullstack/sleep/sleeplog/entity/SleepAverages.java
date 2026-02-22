package com.noom.interview.fullstack.sleep.sleeplog.entity;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Aggregated sleep statistics over a date range.
 *
 * <p>Holds average bed time, average wake time, average total time in bed,
 * and morning feeling frequency counts computed from a list of {@link SleepLog} entries.
 *
 * <p><b>Mathematical Note:</b>
 * This class uses two different types of averaging:
 * <ul>
 *   <li>{@code averageTotalTimeInBed} is a <b>linear arithmetic mean</b> of durations.</li>
 *   <li>{@code averageBedTime} and {@code averageWakeTime} are <b>circular trigonometric means</b>.</li>
 * </ul>
 * Because of the geometric difference between a flat line (duration) and a circle (clock time),
 * the difference between the average wake and bed times may not exactly equal the average duration.
 * This discrepancy is a normal result of circular statistics and increases with the variance
 * in the user's sleep schedule.
 *
 * @param from                      start of the date range (exclusive)
 * @param to                        end of the date range (inclusive)
 * @param averageTotalTimeInBed     average duration between bed time and wake time
 * @param averageBedTime            average clock time the user went to bed (circular average, see below)
 * @param averageWakeTime           average clock time the user woke up (circular average, see below)
 * @param morningFeelingFrequencies count of each {@link MorningFeeling} value in the range
 */
public record SleepAverages(
        LocalDate from,
        LocalDate to,
        Duration averageTotalTimeInBed,
        LocalTime averageBedTime,
        LocalTime averageWakeTime,
        Map<MorningFeeling, Long> morningFeelingFrequencies
) {
    /**
     * Creates a {@link SleepAverages} from a list of sleep logs.
     *
     * <p>If the list is empty, returns zeroed/null averages with an empty frequency map.
     *
     * @param sleepLogs the sleep log entries to aggregate
     * @param from      start of the date range (exclusive)
     * @param to        end of the date range (inclusive)
     * @return aggregated sleep statistics
     */
    public static SleepAverages fromSleepLogs(List<SleepLog> sleepLogs, LocalDate from, LocalDate to) {
        if (sleepLogs.isEmpty()) {
            return new SleepAverages(from, to, Duration.ZERO, null, null, Map.of());
        }

        Duration averageTotalTimeInBed = averageSleepDuration(sleepLogs);
        LocalTime averageBedTime = averageClockTime(sleepLogs, SleepLog::bedTime);
        LocalTime averageWakeTime = averageClockTime(sleepLogs, SleepLog::wakeTime);
        Map<MorningFeeling, Long> morningFeelingFrequencies = computeMorningFeelingFrequencies(sleepLogs);

        return new SleepAverages(from, to, averageTotalTimeInBed, averageBedTime, averageWakeTime, morningFeelingFrequencies);
    }

    /**
     * Calculates the average time-in-bed duration across all logs.
     *
     * <p>Sums the {@link Duration} between each log's bed time and wake time,
     * then divides by the number of logs.
     */
    private static Duration averageSleepDuration(List<SleepLog> sleepLogs) {
        Duration total = sleepLogs.stream()
                .map(log -> Duration.between(log.bedTime(), log.wakeTime()))
                .reduce(Duration.ZERO, Duration::plus);
        return total.dividedBy(sleepLogs.size());
    }

    private static final double TWO_PI = 2 * Math.PI;
    private static final long SECONDS_IN_DAY = Duration.ofDays(1).toSeconds();

    /**
     * Calculates the average clock time using circular statistics (vector averaging).
     *
     * <p>A standard arithmetic average fails for times because clock time "wraps around"
     * at midnight. For example, the average of 23:00 and 01:00 should be 00:00,
     * but a simple average would incorrectly return 12:00 (noon).
     *
     * <p>To solve this, this method treats the 24-hour clock as a circle:
     * <ol>
     *   <li><b>Mapping:</b> each time is mapped to a point on a 24-hour clock face.</li>
     *   <li><b>Trigonometry:</b> each point is converted into its horizontal (cosine)
     *       and vertical (sine) coordinates.</li>
     *   <li><b>Averaging:</b> the "center of gravity" is found by averaging all the
     *       horizontal and vertical coordinates separately.</li>
     *   <li><b>Reconstruction:</b> the average coordinates are used to find the resulting
     *       angle and convert that angle back into a 24-hour {@link LocalTime}.</li>
     * </ol>
     *
     * @param sleepLogs     the list of sleep records to analyze
     * @param timeExtractor a function to pull either the bed time or wake time from a log
     * @return the circular average time; if the times are perfectly opposed (e.g., 06:00 and 18:00),
     *         the result is mathematically ambiguous and depends on floating-point rounding
     */
    private static LocalTime averageClockTime(List<SleepLog> sleepLogs, Function<SleepLog, LocalDateTime> timeExtractor) {
        double avgAngle = sleepLogs.stream()
                .map(timeExtractor)
                .map(LocalDateTime::toLocalTime)
                .map(SleepAverages::timeToAngle)
                .collect(Collectors.teeing(
                        Collectors.summingDouble(Math::sin),
                        Collectors.summingDouble(Math::cos),
                        (sinSum, cosSum) -> Math.atan2(sinSum / sleepLogs.size(), cosSum / sleepLogs.size())
                ));
        return angleToTime(avgAngle);
    }

    /**
     * Places a specific time on a 24-hour circular clock face as an angle.
     *
     * <p>Imagine a clock where the top is midnight (0 radians). As the day progresses,
     * the "hand" moves around the circle. This method calculates how far
     * (in radians) that hand has traveled.
     *
     * <p>Key positions:
     * <ul>
     *   <li>00:00 (midnight) → 0 radians (0°)</li>
     *   <li>06:00 (morning)  → π/2 radians (90°)</li>
     *   <li>12:00 (noon)     → π radians (180°)</li>
     *   <li>18:00 (evening)  → 3π/2 radians (270°)</li>
     * </ul>
     *
     * @param time the clock time to convert
     * @return the angle in radians, ranging from 0 (inclusive) to 2π (exclusive)
     */
    private static double timeToAngle(LocalTime time) {
        return (time.toSecondOfDay() / (double) SECONDS_IN_DAY) * TWO_PI;
    }

    /**
     * Converts a circular angle back into a human-readable 24-hour clock time.
     *
     * <p>This is the reverse of {@link #timeToAngle}. It takes a direction on the
     * 24-hour circle and determines which time that direction points to.
     *
     * <p>Technical note: this method automatically handles negative angles (which
     * often occur when calculating averages with {@link Math#atan2}) by wrapping
     * them back into the positive 0 to 2π range.
     *
     * @param angle the angle in radians representing a position on the clock face
     * @return the {@link LocalTime} that corresponds to that angle, rounded to
     *         the nearest second
     */
    private static LocalTime angleToTime(double angle) {
        if (angle < 0) {
            angle += TWO_PI;
        }
        long secondOfDay = Math.round((angle / TWO_PI) * SECONDS_IN_DAY);
        return LocalTime.ofSecondOfDay(secondOfDay % SECONDS_IN_DAY);
    }

    /**
     * Counts occurrences of each {@link MorningFeeling} value across all logs.
     */
    private static Map<MorningFeeling, Long> computeMorningFeelingFrequencies(List<SleepLog> sleepLogs) {
        return sleepLogs.stream()
                .collect(Collectors.groupingBy(SleepLog::morningFeeling, Collectors.counting()));
    }
}
