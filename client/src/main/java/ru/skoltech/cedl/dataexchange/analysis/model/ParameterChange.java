/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange.analysis.model;

import ru.skoltech.cedl.dataexchange.EnumUtil;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterNature;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterValueSource;

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
        if (!nodeId.equals(that.nodeId)) return false;

        return true;
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

