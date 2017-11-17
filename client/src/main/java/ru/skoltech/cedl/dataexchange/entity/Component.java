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

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import ru.skoltech.cedl.dataexchange.entity.model.ModelNode;

import javax.persistence.*;
import java.util.Date;

/**
 * A component entity.
 * <p/>
 * Created by Nikolay Groshkov on 17-Nov-17.
 */
@Entity
@Table
@Audited
public class Component {

    @Id
    @GeneratedValue
    private long id;

    @Revision
    @NotAudited
    private int revision;

    @NotFound(action = NotFoundAction.EXCEPTION)
    @OneToOne(fetch = FetchType.EAGER)
    private ModelNode modelNode;

    private String author;

    private Date date;

    private String category;

    public Component() {
    }

    public Component(ModelNode modelNode) {
        this.modelNode = modelNode;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public ModelNode getModelNode() {
        return modelNode;
    }

    public void setModelNode(ModelNode modelNode) {
        this.modelNode = modelNode;
    }

    public int getRevision() {
        return revision;
    }

    public void setRevision(int revision) {
        this.revision = revision;
    }
}
