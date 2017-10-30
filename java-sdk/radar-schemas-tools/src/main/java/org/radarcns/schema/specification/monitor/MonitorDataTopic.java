package org.radarcns.schema.specification.monitor;

import org.radarcns.schema.specification.AppDataTopic;

public class MonitorDataTopic extends AppDataTopic {
    public enum RadarSourceTypes {
        EXTERNAL_TIME, RECORD_COUNTS, SERVER_STATUS, UPTIME
    }
}
