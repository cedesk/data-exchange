/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange.analysis.model;

import ru.skoltech.cedl.dataexchange.Utils;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Date;

import static java.time.temporal.ChronoField.*;

/**
 * Created by D.Knoll on 26.07.2017.
 */
public class Period {

    private static DateTimeFormatter TIME_FORMATTER = new DateTimeFormatterBuilder()
            .appendValue(HOUR_OF_DAY, 2).appendLiteral(':')
            .appendValue(MINUTE_OF_HOUR, 2).optionalStart().appendLiteral(':')
            .appendValue(SECOND_OF_MINUTE, 2).toFormatter();

    protected Long startTimestamp;

    protected Long stopTimestamp;

    public Period(Long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public Period(Long startTimestamp, Long stopTimestamp) {
        this.startTimestamp = startTimestamp;
        this.stopTimestamp = stopTimestamp;
    }

    public Long getDuration() {
        if (isOpen()) {
            return Long.MAX_VALUE;
        } else {
            return stopTimestamp - startTimestamp;
        }
    }

    public String getDurationFormatted() {
        if (stopTimestamp == null) return " ";
        return formatDurationMillis(getDuration());
    }

    public Long getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(Long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public String getStartTimestampFormatted() {
        return Utils.TIME_AND_DATE_FOR_USER_INTERFACE.format(new Date(startTimestamp));
    }

    public Long getStopTimestamp() {
        return stopTimestamp;
    }

    public void setStopTimestamp(Long stopTimestamp) {
        this.stopTimestamp = stopTimestamp;
    }

    public String getStopTimestampFormatted() {
        if (stopTimestamp == null) return " ";
        return Utils.TIME_AND_DATE_FOR_USER_INTERFACE.format(new Date(stopTimestamp));
    }

    public boolean isOpen() {
        return stopTimestamp == null;
    }

    public static String formatDurationMillis(long duration) {
        LocalTime diffTime = LocalTime.ofSecondOfDay(duration / 1000);
        return TIME_FORMATTER.format(diffTime);
    }

    public void enlarge(Period other) {
        if (hasOverlap(other)) {
            this.startTimestamp = Math.min(this.startTimestamp, other.startTimestamp);
            this.stopTimestamp = Math.max(this.stopTimestamp, other.stopTimestamp);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Period period = (Period) o;

        if (!startTimestamp.equals(period.startTimestamp)) return false;
        return stopTimestamp != null ? stopTimestamp.equals(period.stopTimestamp) : period.stopTimestamp == null;
    }

    public boolean hasOverlap(Period other) {
        if (this.isOpen() || other.isOpen()) {
            return false;
        }
        if (this.startTimestamp >= other.startTimestamp && this.startTimestamp <= other.stopTimestamp) { // this starts before other
            return true;
        } else if (this.startTimestamp <= other.startTimestamp && this.stopTimestamp >= other.startTimestamp) { // this starts after other
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = startTimestamp.hashCode();
        result = 31 * result + (stopTimestamp != null ? stopTimestamp.hashCode() : 0);
        return result;
    }

    public long overlapValue(Period other) {
        if (this.isOpen() || other.isOpen()) {
            return 0;
        }
        return Math.max(0, Math.min(this.stopTimestamp, other.stopTimestamp) - Math.max(this.startTimestamp, other.startTimestamp));
    }
}
