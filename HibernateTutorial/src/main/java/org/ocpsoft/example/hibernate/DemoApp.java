package org.ocpsoft.example.hibernate;

import org.ocpsoft.example.hibernate.model.ModelNode;
import org.ocpsoft.example.hibernate.model.ModelParameter;
import org.ocpsoft.example.hibernate.util.EntityManagerUtil;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

/**
 * Created by dknoll on 22/05/15.
 */
public class DemoApp {

    public static void main(String... args) {
        EntityManager em = EntityManagerUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        transaction.begin();

        ModelNode modelNode1 = new ModelNode();
        modelNode1.setName("SpaceCraft");
        modelNode1.setDescription("beschreibung");

        ModelParameter parameter = new ModelParameter();
        parameter.setName("parameter-1");
        parameter.setDescription("par desc 1");
        parameter.setNode(modelNode1);
        modelNode1.getParameterList().add(parameter);

        parameter = new ModelParameter();
        parameter.setName("parameter-2");
        parameter.setDescription("par desc 2");
        parameter.setNode(modelNode1);
        modelNode1.getParameterList().add(parameter);

        parameter = new ModelParameter();
        parameter.setName("parameter-3");
        parameter.setDescription("par desc 3");
        parameter.setNode(modelNode1);
        modelNode1.getParameterList().add(parameter);

        em.persist(modelNode1);
        final Long modelNode1Id = modelNode1.getId();

        transaction.commit();

        ModelNode modelNode2 = em.getReference(ModelNode.class, modelNode1Id);
        System.out.println("DemoApp.main");
        System.out.println("modelNode2 = " + modelNode2);

        em.close();
        System.exit(0);
    }
}
