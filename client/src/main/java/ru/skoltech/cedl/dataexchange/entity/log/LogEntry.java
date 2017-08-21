/*
 * Copyright 2017 Skolkovo Institute of Science and Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.skoltech.cedl.dataexchange.entity.log;

import ru.skoltech.cedl.dataexchange.Utils;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by dknoll on 04/12/15.
 */
@Entity
@Access(AccessType.PROPERTY)
public class LogEntry implements Serializable {

    private long id;

    private Long logTimestamp;

    private String user;

    private String client;

    private String action;

    private String description;

    private Long studyId;

    public LogEntry() {
        logTimestamp = System.currentTimeMillis();
    }

    public LogEntry(String user, String client, String action, String description, Long studyId) {
        this();
        this.user = user;
        this.client = client;
        this.action = action;
        this.description = description;
        this.studyId = studyId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
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

    @Transient
    public String getLogTimestampFormatted() {
        return Utils.TIME_AND_DATE_FOR_USER_INTERFACE.format(new Date(logTimestamp));
    }

    public Long getStudyId() {
        return studyId;
    }

    public void setStudyId(Long studyId) {
        this.studyId = studyId;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
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
