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

package ru.skoltech.cedl.dataexchange.entity;

import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.envers.RelationTargetAuditMode;
import ru.skoltech.cedl.dataexchange.entity.model.ModelNode;
import ru.skoltech.cedl.dataexchange.external.ExternalModelException;

import javax.persistence.*;
import javax.xml.bind.annotation.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.UUID;

/**
 * Created by D.Knoll on 02.07.2015.
 */
@Entity
@Audited
@XmlType(propOrder = {"name", "lastModification", "uuid"})
@XmlAccessorType(XmlAccessType.FIELD)
public class ExternalModel implements Comparable<ExternalModel>, PersistedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    @XmlTransient
    private long id;

    @Revision
    @NotAudited
    @XmlTransient
    private int revision;

    @XmlAttribute
    private String uuid = UUID.randomUUID().toString();

    @XmlID
    @XmlAttribute
    private String name;

    @Column(length = 100 * 1024 * 1024) // 100MB
    @Lob
    @XmlTransient
    private byte[] attachment;

    @Version
    @XmlTransient
    private long version;

    @XmlAttribute
    private Long lastModification;

    @ManyToOne(targetEntity = ModelNode.class)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @XmlTransient
    private ModelNode parent;

    @Override
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getRevision() {
        return revision;
    }

    public void setRevision(int revision) {
        this.revision = revision;
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

    public byte[] getAttachment() {
        return attachment;
    }

    public void setAttachment(byte[] attachment) {
        this.attachment = attachment;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public Long getLastModification() {
        return lastModification;
    }

    public void setLastModification(Long lastModification) {
        this.lastModification = lastModification;
    }

    public ModelNode getParent() {
        return parent;
    }

    public void setParent(ModelNode parent) {
        this.parent = parent;
    }

    public String getNodePath() {
        return parent.getNodePath() + "#" + name;
    }

    public InputStream getAttachmentAsStream() throws ExternalModelException {
        if (this.getAttachment() == null) {
            throw new ExternalModelException("external model has empty attachment");
        }
        return new ByteArrayInputStream(this.getAttachment());
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

        return (name != null ? name.equals(that.name) : that.name == null)
                && Arrays.equals(attachment, that.attachment);
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
}
