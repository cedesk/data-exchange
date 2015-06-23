package ru.skoltech.cedl.dataexchange.structure.model;

import org.hibernate.envers.RevisionType;
import ru.skoltech.cedl.dataexchange.db.CustomRevisionEntity;

import java.util.Date;

/**
 * Created by D.Knoll on 23.06.2015.
 */
public class ParameterRevision extends ParameterModel {

    private int revisionId;

    private Date revisionDate;

    private String revisionAuthor;

    private RevisionType revisionType;

    public ParameterRevision(ParameterModel versionedParameterModel, CustomRevisionEntity revisionEntity, RevisionType revisionType) {
        this.revisionId = revisionEntity.getId();
        this.revisionDate = revisionEntity.getRevisionDate();
        this.revisionAuthor = revisionEntity.getUsername();
        this.revisionType = revisionType;

        setId(versionedParameterModel.getId());
        setName(versionedParameterModel.getName());
        setValue(versionedParameterModel.getValue());
        setDescription(versionedParameterModel.getDescription());
        setType(versionedParameterModel.getType());
        setIsShared(versionedParameterModel.getIsShared());
    }

    public int getRevisionId() {
        return revisionId;
    }

    public void setRevisionId(int revisionId) {
        this.revisionId = revisionId;
    }

    public Date getRevisionDate() {
        return revisionDate;
    }

    public void setRevisionDate(Date revisionDate) {
        this.revisionDate = revisionDate;
    }

    public String getRevisionAuthor() {
        return revisionAuthor;
    }

    public void setRevisionAuthor(String revisionAuthor) {
        this.revisionAuthor = revisionAuthor;
    }

    public RevisionType getRevisionType() {
        return revisionType;
    }

    public void setRevisionType(RevisionType revisionType) {
        this.revisionType = revisionType;
    }

    @Override
    public int compareTo(ParameterModel o) {
        if (this == o) return 0;
        if (!(o instanceof ParameterRevision)) return -1;

        ParameterRevision that = (ParameterRevision) o;
        int comp = Integer.compare(this.revisionId, that.revisionId);
        if (comp != 0) return comp;

        return super.compareTo(that);
    }
}
