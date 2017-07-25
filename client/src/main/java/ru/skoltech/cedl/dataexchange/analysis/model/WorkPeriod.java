/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange.analysis.model;

public class WorkPeriod {

    private String usernname;

    private Long startTimestamp;

    private Long stopTimestamp;

    private Long parameterModifications = 0L;

    public WorkPeriod(String username, Long startTimestamp) {
        this.usernname = username;
        this.startTimestamp = startTimestamp;
    }

    public String getUsernname() {
        return usernname;
    }

    public void setUsernname(String usernname) {
        this.usernname = usernname;
    }

    public Long getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(Long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public Long getStopTimestamp() {
        return stopTimestamp;
    }

    public void setStopTimestamp(Long stopTimestamp) {
        this.stopTimestamp = stopTimestamp;
    }

    public Long getParameterModifications() {
        return parameterModifications;
    }

    public void setParameterModifications(Long parameterModifications) {
        this.parameterModifications = parameterModifications;
    }

    public void incrementParameterModifications() {
        parameterModifications++;
    }

    @Override
    public String toString() {
        return "WorkPeriod{" +
                "username='" + usernname + "'" +
                ", startTimestamp=" + startTimestamp +
                ", stopTimestamp=" + stopTimestamp +
                ", parameterModifications=" + parameterModifications +
                '}';
    }

}
