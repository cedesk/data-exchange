package ru.skoltech.cedl.dataexchange.structure;

import org.hibernate.envers.Audited;

import javax.persistence.*;

/**
 * Created by D.Knoll on 02.07.2015.
 */
@Entity
@Access(AccessType.PROPERTY)
@Audited
public class ExternalModel {

    protected long id;

    private byte[] attachment;

    @Id
    @GeneratedValue
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Column(length = 100 * 1024 * 1024)
    public byte[] getAttachment() {
        return attachment;
    }

    public void setAttachment(byte[] attachment) {
        this.attachment = attachment;
    }
}
