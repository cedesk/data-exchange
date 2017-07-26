/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange.analysis.model;

import ru.skoltech.cedl.dataexchange.Utils;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * Created by D.Knoll on 26.07.2017.
 */
public class Period {

    private static DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_TIME;

    protected Long startTimestamp;

    protected Long stopTimestamp;

    public Period(Long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public Period(Long startTimestamp, Long stopTimestamp) {
        this.startTimestamp = startTimestamp;
        this.stopTimestamp = stopTimestamp;
    }

    public Long getStartTimestamp() {
        return startTimestamp;
    }

    public Long getStopTimestamp() {
        return stopTimestamp;
    }

    public void setStartTimestamp(Long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public void setStopTimestamp(Long stopTimestamp) {
        this.stopTimestamp = stopTimestamp;
    }

    public String getStartTimestampFormatted() {
        return Utils.TIME_AND_DATE_FOR_USER_INTERFACE.format(new Date(startTimestamp));
    }

    public String getStopTimestampFormatted() {
        if (stopTimestamp == null) return " ";
        return Utils.TIME_AND_DATE_FOR_USER_INTERFACE.format(new Date(stopTimestamp));
    }

    public boolean hasOverlap(Period other) {
        if (this.stopTimestamp == null || other.stopTimestamp == null) {
            return false;
        }
        if (this.startTimestamp >= other.startTimestamp && this.startTimestamp <= other.stopTimestamp) { // this starts before other
            return true;
        } else if (this.startTimestamp <= other.startTimestamp && this.stopTimestamp >= other.startTimestamp) { // this starts after other
            return true;
        }
        return false;
    }

    public long overlapValue(Period other) {
        if (this.stopTimestamp == null || other.stopTimestamp == null) {
            return 0;
        }
        return Math.max(0, Math.min(this.stopTimestamp, other.stopTimestamp) - Math.max(this.startTimestamp, other.startTimestamp));
    }

    public void enlarge(Period other) {
        if (hasOverlap(other)) {
            this.startTimestamp = Math.min(this.startTimestamp, other.startTimestamp);
            this.stopTimestamp = Math.max(this.stopTimestamp, other.stopTimestamp);
        }
    }

    public String getDurationFormatted() {
        if (stopTimestamp == null) return " ";
        Instant startInstant = Instant.ofEpochMilli(startTimestamp);
        Instant stopInstant = Instant.ofEpochMilli(stopTimestamp);
        Duration duration = Duration.between(startInstant, stopInstant);
        LocalTime diffTime = LocalTime.ofNanoOfDay(duration.toNanos());
        return TIME_FORMATTER.format(diffTime);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Period period = (Period) o;

        if (!startTimestamp.equals(period.startTimestamp)) return false;
        return stopTimestamp != null ? stopTimestamp.equals(period.stopTimestamp) : period.stopTimestamp == null;
    }

    @Override
    public int hashCode() {
        int result = startTimestamp.hashCode();
        result = 31 * result + (stopTimestamp != null ? stopTimestamp.hashCode() : 0);
        return result;
    }
}
