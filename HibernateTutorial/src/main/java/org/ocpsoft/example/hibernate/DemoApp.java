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

        ModelParameter par1 = new ModelParameter();
        par1.setName("parameter-1");
        par1.setDescription("par desc 1");

        modelNode1.getParameterList().add(par1);
        par1.setNode(modelNode1);
        //em.persist(par1);

        em.persist(modelNode1);
        transaction.commit();

        ModelNode modelNode2 = em.getReference(ModelNode.class, 1L);

        em.close();
        System.exit(0);
    }
}
