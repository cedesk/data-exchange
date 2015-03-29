package ru.skoltech.cedl.dataexchange.structure.model;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by D.Knoll on 11.03.2015.
 */
@XmlRootElement
public class SystemModel extends CompositeModelNode<SubSystemModel> {

    public SystemModel() {
        super();
    }

    public SystemModel(String name) {
        super(name);
    }

}
