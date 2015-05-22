package org.ocpsoft.example.hibernate.model;

import javax.persistence.*;

/**
 * Created by dknoll on 22/05/15.
 */
@Entity
public class ModelParameter {

    @Id
    @GeneratedValue
    private int id;

    @Version
    private int version;

    @Column
    private String name;

    @Column
    private String description;

    @Column
    private boolean shared;

    @ManyToOne(targetEntity = ModelNode.class)
    @JoinColumn(name = "modelId")
    private ModelNode node;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
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

    public boolean isShared() {
        return shared;
    }

    public void setShared(boolean shared) {
        this.shared = shared;
    }

    public ModelNode getNode() {
        return node;
    }

    public void setNode(ModelNode node) {
        this.node = node;
    }
}
