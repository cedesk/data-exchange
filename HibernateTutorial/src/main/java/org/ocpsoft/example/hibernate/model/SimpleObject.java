package org.ocpsoft.example.hibernate.model;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table
public class SimpleObject implements Serializable {
    private static final long serialVersionUID = -2862671438138322400L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id = null;

    @Version
    @Column(name = "version")
    private int version = 0;

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
}