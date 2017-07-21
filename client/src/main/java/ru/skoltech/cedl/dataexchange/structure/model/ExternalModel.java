package ru.skoltech.cedl.dataexchange.structure.model;

import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.persistence.*;
import javax.xml.bind.annotation.*;
import java.util.Arrays;
import java.util.UUID;

/**
 * Created by D.Knoll on 02.07.2015.
 */
@XmlType(propOrder = {"name", "lastModification", "uuid"})
@XmlAccessorType(XmlAccessType.FIELD)
@Entity
@Access(AccessType.PROPERTY)
@Audited
public class ExternalModel implements Comparable<ExternalModel>, ModificationTimestamped, PersistedEntity {

    @XmlTransient
    private long id;

    @XmlAttribute
    private String uuid = UUID.randomUUID().toString();

    @XmlID
    @XmlAttribute
    private String name;

    @XmlTransient
    private byte[] attachment;

    @XmlTransient
    private long version;

    @XmlAttribute
    private Long lastModification;

    @XmlTransient
    private ModelNode parent;

    public ExternalModel() {
    }

    @Override
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(length = 100 * 1024 * 1024) // 100MB
    @Lob
    public byte[] getAttachment() {
        return attachment;
    }

    public void setAttachment(byte[] attachment) {
        this.attachment = attachment;
    }

    @Version()
    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    @Override
    public Long getLastModification() {
        return lastModification;
    }

    @Override
    public void setLastModification(Long lastModification) {
        this.lastModification = lastModification;
    }

    @ManyToOne(targetEntity = ModelNode.class)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    public ModelNode getParent() {
        return parent;
    }

    public void setParent(ModelNode parent) {
        this.parent = parent;
    }

    /*
     * The comparison is done only based on the name, so it enables sorting of external models parameters by name.
     */
    @Override
    public int compareTo(ExternalModel o) {
        return name.compareTo(o.name);
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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ExternalModel{");
        sb.append("id=").append(id);
        sb.append(", name='").append(name).append('\'');
        sb.append(", attachment-length=").append(attachment != null ? attachment.length : null);
        sb.append(", version=").append(version);
        sb.append(", lastModification=").append(lastModification);
        if (parent != null) {
            sb.append(", parent=").append(parent.getNodePath());
        }
        sb.append('}');
        return sb.toString();
    }

    @Transient
    public String getNodePath() {
        return parent.getNodePath() + "#" + name;
    }
}
