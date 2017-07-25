/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange.logging;

import javax.persistence.*;

/**
 * Created by dknoll on 04/12/15.
 */
@Entity
@Access(AccessType.PROPERTY)
public class LogEntry {

    private long id;

    private Long logTimestamp;

    private String user;

    private String client;

    private String action;

    private String description;

    public LogEntry() {
        logTimestamp = System.currentTimeMillis();
    }

    public LogEntry(String user, String client, String action, String description) {
        this();
        this.user = user;
        this.client = client;
        this.action = action;
        this.description = description;
    }

    @Id
    @GeneratedValue
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Long getLogTimestamp() {
        return logTimestamp;
    }

    public void setLogTimestamp(Long logTimestamp) {
        this.logTimestamp = logTimestamp;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LogEntry{");
        sb.append("id=").append(id);
        sb.append(", logTimestamp=").append(logTimestamp);
        sb.append(", user='").append(user).append('\'');
        sb.append(", client='").append(client).append('\'');
        sb.append(", action='").append(action).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
