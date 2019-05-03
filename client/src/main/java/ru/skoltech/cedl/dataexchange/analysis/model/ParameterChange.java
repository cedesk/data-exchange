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

package ru.skoltech.cedl.dataexchange.analysis.model;

import ru.skoltech.cedl.dataexchange.EnumUtil;
import ru.skoltech.cedl.dataexchange.entity.ParameterNature;
import ru.skoltech.cedl.dataexchange.entity.ParameterValueSource;

import java.io.Serializable;
import java.math.BigInteger;

/**
 * Created by D.Knoll on 27.12.2016.
 */
public class ParameterChange implements Serializable {

    public final Long timestamp;

    public final Long revisionId;

    public final Long parameterId;

    public final Long valueLinkId;

    public final Long nodeId;

    public final ParameterNature nature;

    public final ParameterValueSource valueSource;

    public final String parameterName;

    public final String nodeName;

    public ParameterChange(Object revId, Object paramId, Object valueLinkId, Object nodeId, Object timestamp, Object nature, Object valueSource, Object parName, Object nodeName) {
        this.revisionId = convert(revId);
        this.parameterId = convert(paramId);
        this.valueLinkId = convert(valueLinkId);
        this.nodeId = convert(nodeId);
        this.timestamp = convert(timestamp);
        this.nature = EnumUtil.lookupEnum(ParameterNature.class, convert(nature));
        this.valueSource = EnumUtil.lookupEnum(ParameterValueSource.class, convert(valueSource));
        this.parameterName = String.valueOf(parName);
        this.nodeName = String.valueOf(nodeName);
    }

    public String asText() {
        return String.format("[%d]%s::%s", parameterId, nodeName, parameterName);
    }

    private static Long convert(Object o) {
        if (o instanceof Integer) {
            return ((Integer) o).longValue();
        } else if (o instanceof BigInteger) {
            return ((BigInteger) o).longValue();
        } else {
            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ParameterChange that = (ParameterChange) o;

        if (!revisionId.equals(that.revisionId)) return false;
        if (!parameterId.equals(that.parameterId)) return false;
        if (!valueLinkId.equals(that.valueLinkId)) return false;
        return nodeId.equals(that.nodeId);
    }

    @Override
    public int hashCode() {
        int result = revisionId.hashCode();
        result = 31 * result + parameterId.hashCode();
        result = 31 * result + valueLinkId.hashCode();
        result = 31 * result + nodeId.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ParameterChange{" +
                "timestamp=" + timestamp +
                ", revisionId=" + revisionId +
                ", parameterId=" + parameterId +
                ", valueLinkId=" + valueLinkId +
                ", nodeId=" + nodeId +
                ", nature=" + nature +
                ", valueSource=" + valueSource +
                ", parameterName='" + parameterName + '\'' +
                ", nodeName='" + nodeName + '\'' +
                '}';
    }
}

