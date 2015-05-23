package ru.skoltech.cedl.dataexchange.structure.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Created by dknoll on 23/05/15.
 */
@Entity(name = "CEDESK_Study")
public class Study {

    @Id
    @GeneratedValue
    private long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Study{");
        sb.append("id=").append(id);
        sb.append('}');
        return sb.toString();
    }
}
