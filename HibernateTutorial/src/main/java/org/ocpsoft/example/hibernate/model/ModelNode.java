package org.ocpsoft.example.hibernate.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

@Entity
@Table
public class ModelNode implements Serializable {
    private static final long serialVersionUID = -2862671438138322400L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id = null;

    @Version
    @Column(name = "version")
    private int version = 0;

    @Column
    private String name;

    @Column
    private String description;

    @OneToMany(targetEntity = ModelParameter.class, mappedBy = "node", cascade = CascadeType.ALL)
    private List<ModelParameter> parameterList = new LinkedList<ModelParameter>();

    public Long getId() {
        return this.id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public int getVersion() {
        return this.version;
    }

    public void setVersion(final int version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<ModelParameter> getParameterList() {
        return parameterList;
    }

    public void setParameterList(List<ModelParameter> parameterList) {
        this.parameterList = parameterList;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ModelNode{");
        sb.append("id=").append(id);
        sb.append(", version=").append(version);
        sb.append(", name='").append(name).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", parameterList=").append(parameterList);
        sb.append('}');
        return sb.toString();
    }
}