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
import ru.skoltech.cedl.dataexchange.external.ExternalModelState;
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
import java.util.*;
import java.util.stream.Collectors;

import static ru.skoltech.cedl.dataexchange.external.ExternalModelState.*;

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
     * Initialize external model cache initFile from current fields.
     * Must be performed after creation of a new external model instance, so
     * it will be ready to handle the cache initFile.
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
        this.cacheFile = initCacheFile(base, path, this.id, this.name);
        this.timestampFile = new File(cacheFile.getAbsolutePath() + ".tstamp");
    }

    /**
     * Initialize External Model fields by parameters taken from the cache initFile.
     *
     * @param initFile a initFile to take parameters for external model
     * @throws ExternalModelException is access to the initFile is impossible for some reason
     */
    public void initByFile(File initFile) throws ExternalModelException {
        try {
            Objects.requireNonNull(initFile);
            this.setName(initFile.getName());
            this.setLastModification(initFile.lastModified());
            this.setAttachment(Files.readAllBytes(Paths.get(initFile.getAbsolutePath())));
            this.init();
        } catch (IOException e) {
            throw new ExternalModelException("Cannot initialize external model by file" + initFile.getAbsolutePath(), e);
        }
    }

    /**
     * This method only forms the full path where the external model would be cached.<br/>
     * It does not actually assure the file nor the folder exist.
     *
     * @param projectHome project home directory
     * @param path path to the cache file
     * @param name name of cache file
     * @return a file of the location where the external model would be stored.
     */
    private static File initCacheFile(String projectHome, String path, long id, String name) {
        String nodePath = path.replace(' ', '_').replace(ModelNode.NODE_SEPARATOR, File.separator);
        File nodeHome = new File(projectHome, nodePath);
        String rectifiedFileName = id + "_" + name.replace(' ', '_');
        return new File(nodeHome, rectifiedFileName);
    }

    public String getNodePath() {
        return parent.getNodePath() + "#" + name;
    }

    public List<ParameterModel> getReferencedParameterModels() {
        return this.getParent().getParameters().stream()
                .filter(parameterModel -> parameterModel.getValueSource() == ParameterValueSource.REFERENCE &&
                        parameterModel.getValueReference() != null &&
                        parameterModel.getValueReference().getExternalModel() != null &&
                        parameterModel.getValueReference().getExternalModel().getName().equals(name))
                .collect(Collectors.toList());
    }

    public File getCacheFile() {
        return cacheFile;
    }

    File getTimestampFile() {
        return timestampFile;
    }

    /**
     * Runtime determination of actual state of external model.
     *
     * @return actual state of external model
     */
    public ExternalModelState state() {
        if (this.name == null && this.lastModification == null && this.attachment == null && this.getParent() == null) {
            return EMPTY;
        } else if (this.name == null || this.lastModification == null || this.attachment == null
                || this.getParent() == null || this.getParent().getNodePath() == null) {
            return INCORRECT;
        } else if (this.cacheFile == null || this.timestampFile == null) {
            return UNINITIALIZED;
        } else if (this.cacheFile.exists() && this.timestampFile.exists()) {
            long checkoutTime = this.timestampFile.lastModified();
            long fileLastModified = this.cacheFile.lastModified();
            boolean newerInRepository = this.getLastModification() > checkoutTime;
            boolean locallyModified = checkoutTime < fileLastModified;
            if (newerInRepository) {
                if (locallyModified) {
                    return CACHE_CONFLICT;
                } else {
                    return CACHE_OUTDATED;
                }
            } else {
                if (locallyModified) {
                    return CACHE_MODIFIED;
                } else {
                    return CACHE;
                }
            }
        }
        return ExternalModelState.NO_CACHE;
    }

    /**
     * Retrieve an external model attachment as input stream.
     *
     * @return In case of non-empty and correct cache state (both not INCORRECT and not UNINITIALIZED)
     * it returns {@link FileInputStream} of cache file if it is in some CACHE* state
     * and {@link ByteArrayInputStream} of attachment if there is no cache.
     * In case of EMPTY cache state returns <i>null</i>.
     * @throws IOException in case of incorrect cache state (INCORRECT or UNINITIALIZED)
     */
    protected InputStream getAttachmentAsInputStream() throws IOException {
        switch (this.state()) {
            case EMPTY:
                return null;
            case INCORRECT:
                throw new IOException("External model must be in correct state");
            case UNINITIALIZED:
                throw new IOException("External model must be initialized");
            case NO_CACHE:
                return new ByteArrayInputStream(this.getAttachment());
            default:
                return new FileInputStream(this.getCacheFile());
        }
    }

    /**
     * Retrieve an external model attachment as output stream.
     *
     * @return In case of non-empty and correct cache state (both not INCORRECT and not UNINITIALIZED)
     * it returns {@link FileOutputStream} of cache initFile if it is in some CACHE* state
     * and {@link ByteArrayOutputStream} of attachment if there is no cache.
     * In case of EMPTY cache state returns <i>null</i>.
     * @throws IOException in case of incorrect cache state (INCORRECT or UNINITIALIZED)
     */
    protected OutputStream getAttachmentAsOutputStream() throws IOException {
        switch (this.state()) {
            case EMPTY:
                return null;
            case INCORRECT:
                throw new IOException("External model must be in correct state");
            case UNINITIALIZED:
                throw new IOException("External model must be initialized");
            case NO_CACHE:
                return new ByteArrayOutputStream(this.attachment.length);
            default:
                return new FileOutputStream(this.cacheFile);
        }
    }

    /**
     * Update external model attachment with content of current cache file.
     * Last modification field and timestamp file are updated as well.
     *
     * @throws ExternalModelException if external model is not initialized or there is no cache ot timestamp file.
     */
    public void updateAttachmentFromCache() throws ExternalModelException {
        if (this.cacheFile == null || this.timestampFile == null) {
            throw new ExternalModelException("External model is empty or uninitialized");
        }
        if (!this.cacheFile.exists() || !this.timestampFile.exists()) {
            throw new ExternalModelException("External model is not cached");
        }
        try {
            Path path = Paths.get(cacheFile.getAbsolutePath());
            this.setAttachment(Files.readAllBytes(path));
            this.setLastModification(cacheFile.lastModified());
            boolean updated = timestampFile.setLastModified(cacheFile.lastModified());
            if (!updated) {
                throw new ExternalModelException("Cannot update timestamp file: " + timestampFile.getAbsolutePath());
            }
        } catch (IOException e) {
            throw new ExternalModelException("Cannot update the cache: " + e.getMessage(), e);
        }
    }

    /**
     * Update external model cache with content of current attachment field.
     * If any cache or timestamp file is not exist it immediately creates.
     *
     * @throws ExternalModelException if external model is empty, incorrect or not initialized.
     */
    public void updateCacheFromAttachment() throws ExternalModelException {
        if (this.name == null || this.lastModification == null || this.attachment == null
                || this.getParent() == null || this.getParent().getNodePath() == null) {
            throw new ExternalModelException("External model is empty or incorrect");
        }
        if (this.cacheFile == null || this.timestampFile == null) {
            throw new ExternalModelException("External model is not initialized");
        }
        this.updateDirectory();
        this.updateCacheFile();
        this.updateTimestampFile();
    }

    private void updateDirectory() throws ExternalModelException {
        File path = cacheFile.getParentFile();
        if (!path.exists()) {
            logger.info("Creating directory: " + cacheFile.getParentFile().toString());
            boolean created = path.mkdirs();
            if (!created) {
                throw new ExternalModelException("Unable to create directory: " + path.getAbsolutePath());
            }
        }
        if (!path.canRead() || !path.canWrite()) {
            throw new ExternalModelException("Unable to use directory: " + path.toString());
        }
    }

    private void updateCacheFile() throws ExternalModelException {
        if (this.cacheFile == null) {
            throw new ExternalModelException("External model is empty, incorrect or not initialized.");
        }
        try {
            if (!this.cacheFile.exists()) {
                boolean created = this.cacheFile.createNewFile();
                if (!created) {
                    throw new ExternalModelException("Cannot create cache file.");
                }
            }
            Files.write(this.cacheFile.toPath(), this.getAttachment(), StandardOpenOption.CREATE);
            logger.debug(this.cacheFile.getAbsolutePath() + " updated");
        } catch (IOException e) {
            throw new ExternalModelException("Cannot create cache file: " + e.getMessage(), e);
        }
    }

    private void updateTimestampFile() throws ExternalModelException {
        if (this.timestampFile == null) {
            throw new ExternalModelException("External model is empty, incorrect or not initialized.");
        }
        try {
            if (!this.timestampFile.exists()) {
                boolean created = this.timestampFile.createNewFile();
                if (!created) {
                    throw new ExternalModelException("Cannot create timestamp file.");
                }
            }
            long currentTimeMillis = System.currentTimeMillis();
            boolean modified = this.timestampFile.setLastModified(currentTimeMillis);
            String lastModified = Utils.TIME_AND_DATE_FOR_USER_INTERFACE.format(new Date(this.timestampFile.lastModified()));
            logger.debug(this.timestampFile.getAbsolutePath() + " (" + lastModified + ") " + modified);
        } catch (IOException e) {
            throw new ExternalModelException("Cannot create timestamp file: " + e.getMessage(), e);
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
