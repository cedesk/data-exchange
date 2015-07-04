package ru.skoltech.cedl.dataexchange.structure;

import org.hibernate.envers.Audited;
import ru.skoltech.cedl.dataexchange.structure.model.ModificationTimestamped;

import javax.persistence.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Created by D.Knoll on 02.07.2015.
 */
@Entity
@Access(AccessType.PROPERTY)
@Audited
public class ExternalModel implements ModificationTimestamped {

    private long id;

    private String name;

    private byte[] attachment;

    private Long lastModification;

    @Id
    @GeneratedValue
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(length = 100 * 1024 * 1024)
    public byte[] getAttachment() {
        return attachment;
    }

    public void setAttachment(byte[] attachment) {
        this.attachment = attachment;
    }

    @Override
    public Long getLastModification() {
        return lastModification;
    }

    @Override
    public void setLastModification(Long lastModification) {
        this.lastModification = lastModification;
    }

    @Transient
    public InputStream getAttachmentAsStream() {
        return new ByteArrayInputStream(attachment);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExternalModel that = (ExternalModel) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return Arrays.equals(attachment, that.attachment);

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (attachment != null ? Arrays.hashCode(attachment) : 0);
        return result;
    }
}
