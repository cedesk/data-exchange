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

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.envers.RelationTargetAuditMode;
import ru.skoltech.cedl.dataexchange.ExternalModelAdapter;
import ru.skoltech.cedl.dataexchange.Utils;
import ru.skoltech.cedl.dataexchange.entity.model.ModelNode;
import ru.skoltech.cedl.dataexchange.external.ExternalModelCacheState;
import ru.skoltech.cedl.dataexchange.external.ExternalModelException;
import ru.skoltech.cedl.dataexchange.structure.Project;

import javax.persistence.*;
import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.util.*;

import static ru.skoltech.cedl.dataexchange.external.ExternalModelCacheState.*;

/**
 * Created by D.Knoll on 02.07.2015.
 */
@Entity
@Audited
@Inheritance
@DiscriminatorColumn(name="TYPE", columnDefinition = "VARCHAR(31) DEFAULT 'EXCEL'")
@XmlJavaTypeAdapter(ExternalModelAdapter.class)
@XmlType(propOrder = {"name", "lastModification", "uuid"})
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class ExternalModel implements Comparable<ExternalModel>, PersistedEntity {

    private static Logger logger = Logger.getLogger(ExternalModel.class);

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

    @XmlAttribute
    private Long lastModification;

    @ManyToOne(targetEntity = ModelNode.class)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @XmlTransient
    private ModelNode parent;

    @Transient
    @XmlTransient
    protected File cacheFile;

    @Transient
    @XmlTransient
    private File timestampFile;

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

    /**
     * Initialize external model cache file from current fields.
     * Must be performed after creation of a new external model instance, so
     * it will be ready to handle the cache file.
     */
    @PostPersist
    @PostLoad
    public void init() {
        if (this.name == null || this.getParent() == null || this.getParent().getNodePath() == null) {
            this.cacheFile = null;
            this.timestampFile = null;
            return;
        }
        String projectHome = System.getProperty(Project.PROJECT_HOME_PROPERTY);
        String userHome = System.getProperty("user.home");
        String base = projectHome != null ? projectHome : userHome;
        String path = this.getParent().getNodePath();
        this.cacheFile = createCacheFile(base, path, this.id, this.name);
        this.timestampFile = new File(cacheFile.getAbsolutePath() + ".tstamp");
    }

    /**
     * Initialize External Model fields by parameters taken from the cache file.
     *
     * @param cacheFile a file to take parameters for external model
     * @throws IOException is access to the file is impossible for some reason
     */
    public void initByCacheFile(File cacheFile) throws IOException {
        Objects.requireNonNull(cacheFile);
        this.setName(cacheFile.getName());
        this.setLastModification(cacheFile.lastModified());
        this.setAttachment(Files.readAllBytes(Paths.get(cacheFile.getAbsolutePath())));
        this.init();
    }

    private static File createCacheFile(String projectHome, String path, long id, String name) {
        String nodePath = path.replace(' ', '_').replace(ModelNode.NODE_SEPARATOR, File.separator);;
        File nodeHome = new File(projectHome, nodePath);
        String rectifiedFileName = id + "_" + name.replace(' ', '_');
        return new File(nodeHome, rectifiedFileName);
    }

    public File getCacheFile() {
        return cacheFile;
    }

    public File getTimestampFile() {
        return timestampFile;
    }


    public String getNodePath() {
        return parent.getNodePath() + "#" + name;
    }

    /**
     * Runtime determination of actual state of external model.
     *
     * @return actual state of external model
     */
    public ExternalModelCacheState cacheState() {
        if (this.name == null && this.lastModification == null && this.attachment == null && this.getParent() == null) {
            return EMPTY;
        } else if (this.name == null || this.lastModification == null || this.attachment == null
                || this.getParent() == null || this.getParent().getNodePath() == null) {
            return INCORRECT;
        } else if (this.cacheFile == null || this.timestampFile == null) {
            return UNINITIALIZED;
        } else if (this.cacheFile.exists()) {
            if (!this.timestampFile.exists()) {
                //TODO than?
                logger.error("External model is missing checkout timestamp");
            }
            long checkoutTime = this.timestampFile.lastModified();
            long fileLastModified = this.cacheFile.lastModified();
            boolean newerInRepository = this.getLastModification() > checkoutTime;
            boolean locallyModified = checkoutTime < fileLastModified;
            if (newerInRepository) {
                if (locallyModified) {
                    return CACHED_CONFLICTING_CHANGES;
                } else {
                    return CACHED_OUTDATED;
                }
            } else {
                if (locallyModified) {
                    return CACHED_MODIFIED_AFTER_CHECKOUT;
                } else {
                    return CACHED_UP_TO_DATE;
                }
            }
        }
        return ExternalModelCacheState.NOT_CACHED;
    }

    /**
     * Retrieve an external model attachment as stream.
     *
     * @return In case of non-empty and correct cache state (both not INCORRECT and not UNINITIALIZED)
     * it returns {@link FileInputStream} of cache file if it is in some CACHED_* state
     * and {@link ByteArrayOutputStream} of attachment if it is not cached.
     * In case of EMPTY cache state returns null.
     * @throws IOException in case of incorrect cache state (INCORRECT or UNINITIALIZED)
     */
    protected InputStream getAttachmentAsInputStream() throws IOException {
        switch (this.cacheState()) {
            case EMPTY:
                return null;
            case INCORRECT:
                throw new IOException("External model must be in correct state");
            case UNINITIALIZED:
                throw new IOException("External model must be initialized");
            case NOT_CACHED:
                return new ByteArrayInputStream(this.getAttachment());
            default:
                return new FileInputStream(this.getCacheFile());
        }
    }

    protected OutputStream getAttachmentAsOutputStream() throws IOException {
        switch (this.cacheState()) {
            case EMPTY:
                return null;
            case INCORRECT:
                throw new IOException("External model must be in correct state");
            case UNINITIALIZED:
                throw new IOException("External model must be initialized");
            case NOT_CACHED:
                return new ByteArrayOutputStream(this.attachment.length);
            default:
                return new FileOutputStream(this.cacheFile);
        }
    }

    public void updateAttachment() throws IOException {
        switch (this.cacheState()) {
            case CACHED_MODIFIED_AFTER_CHECKOUT: {
                Path path = Paths.get(cacheFile.getAbsolutePath());
                this.setAttachment(Files.readAllBytes(path));
                this.setLastModification(cacheFile.lastModified());
            }
            case CACHED_CONFLICTING_CHANGES: {
                logger.warn(this.getNodePath() + " has conflicting changes locally and in repository");
            }
        }
    }

    public void updateCache() throws IOException {
        switch (this.cacheState()) {
            case NOT_CACHED:
            case CACHED_OUTDATED:
            case CACHED_MODIFIED_AFTER_CHECKOUT:
            case CACHED_CONFLICTING_CHANGES: {
                this.createDirectory();
                Files.write(cacheFile.toPath(), this.getAttachment(), StandardOpenOption.CREATE);
                this.updateTimestamp();
                break;
            }
        }
    }

    public void updateTimestamp() throws IOException {
        ExternalModelCacheState cacheState = this.cacheState();
        switch (cacheState) {
            case CACHED_UP_TO_DATE:
            case CACHED_MODIFIED_AFTER_CHECKOUT:
            case CACHED_OUTDATED:
            case CACHED_CONFLICTING_CHANGES: {
                this.updateTimestampFile();
                if (logger.isDebugEnabled()) {
                    DateFormat formatter = Utils.TIME_AND_DATE_FOR_USER_INTERFACE;
                    String modelModification = formatter.format(new Date(this.getLastModification()));
                    String fileModification = formatter.format(new Date(timestampFile.lastModified()));
                    logger.debug("Stored external model '" + name + "' (model: " + modelModification + ", file: " + fileModification + ")");
                    logger.debug(this.getNodePath() + " is now in state " + cacheState);
                }
            }
        }
    }


    private void createDirectory() {
        File path = cacheFile.getParentFile();
        if (!path.exists()) {
            logger.info("Creating directory: " + cacheFile.getParentFile().toString());
            boolean created = path.mkdirs();
            if (!created) {
                logger.error("unable to create directory: " + path.getAbsolutePath());
            }
        }
        if (!path.canRead() || !path.canWrite()) {
            logger.error("Warning: Directory is not usable: " + path.toString());
        }
    }

    private void updateTimestampFile() throws IOException {
        try {
            if (!timestampFile.exists()) {
                // create file marking the checkout time of the ExternalModel file
                boolean created = timestampFile.createNewFile();
                if (!created) {
                    throw new IOException("Cannot create timestamp file.");
                }
            }
            boolean modified = timestampFile.setLastModified(System.currentTimeMillis());
            String lastModified = Utils.TIME_AND_DATE_FOR_USER_INTERFACE.format(new Date(timestampFile.lastModified()));
            logger.debug(timestampFile.getAbsolutePath() + " (" + lastModified + ") " + modified);
        } catch (IOException e) {
            throw new IOException("Cannot create timestamp file: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieve a value from external model. Provided target value defines location of the value to be retrieved
     * from the particular implementation.
     *
     * @param target defines an exact location to retrieve a value form particular implementation of accessor
     * @return a value
     * @throws ExternalModelException if access in impossible
     */
    public abstract Double getValue(String target) throws ExternalModelException;

    public abstract List<Double> getValues(List<String> targets) throws ExternalModelException;

    /**
     * Place a provided value to the external model. Provided target value defines location of the value to be placed
     * inside the particular implementation.
     *
     * @param target defines an exact location to place a value inside particular implementation of accessor
     * @param value  a value
     * @throws ExternalModelException if access in impossible
     */
    public abstract void setValue(String target, Double value) throws ExternalModelException;

    public abstract void setValues(List<Pair<String, Double>> values) throws ExternalModelException;

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
        sb.append(", lastModification=").append(lastModification);
        if (parent != null) {
            sb.append(", parent=").append(parent.getNodePath());
        }
        sb.append('}');
        return sb.toString();
    }
}
