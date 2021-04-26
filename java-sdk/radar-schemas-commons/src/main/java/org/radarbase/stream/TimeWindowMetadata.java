package org.radarbase.stream;

import java.util.concurrent.TimeUnit;
import org.radarcns.catalogue.TimeWindow;

/** TimeWindow labels and window time period. */
public enum TimeWindowMetadata {
    TEN_SECOND(TimeWindow.TEN_SECOND, TimeUnit.SECONDS.toMillis(10), "_10sec"),
    ONE_MIN(TimeWindow.ONE_MIN, TimeUnit.MINUTES.toMillis(1), "_1min"),
    TEN_MIN(TimeWindow.TEN_MIN, TimeUnit.MINUTES.toMillis(10), "_10min"),
    ONE_HOUR(TimeWindow.ONE_HOUR, TimeUnit.HOURS.toMillis(1), "_1hour"),
    ONE_DAY(TimeWindow.ONE_DAY, TimeUnit.DAYS.toMillis(1), "_1day"),
    ONE_WEEK(TimeWindow.ONE_WEEK, TimeUnit.DAYS.toMillis(7), "_1week");

    private final TimeWindow timeWindow;
    private final long intervalInMilliSec;
    private final String label;

    TimeWindowMetadata(TimeWindow timeWindow, long intervalInMilliSec, String label) {
        this.timeWindow = timeWindow;
        this.intervalInMilliSec = intervalInMilliSec;
        this.label = label;
    }

    public TimeWindow getTimeWindow() {
        return timeWindow;
    }

    public long getIntervalInMilliSec() {
        return intervalInMilliSec;
    }

    public String getTopicLabel(String topic) {
        return topic + label;
    }

    /**
     * Get the timewindow metadata for given TimeWindow.
     * @param timeWindow time window to look up
     * @return metadata or {@code null} if given TimeWindow is unknown.
     */
    public static TimeWindowMetadata getMetadata(TimeWindow timeWindow) {
        switch (timeWindow) {
            case TEN_SECOND:
                return TEN_SECOND;
            case ONE_MIN:
                return ONE_MIN;
            case TEN_MIN:
                return TEN_MIN;
            case ONE_HOUR:
                return ONE_HOUR;
            case ONE_DAY:
                return ONE_DAY;
            case ONE_WEEK:
                return ONE_WEEK;
            default:
                return null;
        }
    }
}
