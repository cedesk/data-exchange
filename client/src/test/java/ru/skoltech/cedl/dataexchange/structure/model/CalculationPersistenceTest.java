package ru.skoltech.cedl.dataexchange.structure.model;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.db.DatabaseStorage;
import ru.skoltech.cedl.dataexchange.repository.RepositoryException;
import ru.skoltech.cedl.dataexchange.structure.model.calculation.Argument;
import ru.skoltech.cedl.dataexchange.structure.model.calculation.Sum;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.xml.bind.JAXBException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by D.Knoll on 13.05.2015.
 */
public class CalculationPersistenceTest {

    private EntityManagerFactory emf;

    @After
    public void tearDown() {
        releaseEntityManagerFactory();
    }

    @Test
    public void simpleTest() throws JAXBException, RepositoryException {
        Calculation calculation1 = getCalc();

        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();
            EntityTransaction transaction = entityManager.getTransaction();
            transaction.begin();
            entityManager.persist(calculation1);
            transaction.commit();
            long id = calculation1.getId();

            Calculation calculation2 = entityManager.find(Calculation.class, id);
            Assert.assertEquals(calculation1, calculation2);

        } catch (Exception e) {
            throw new RepositoryException("Storing failed.", e);
        } finally {
            try {
                if (entityManager != null)
                    entityManager.close();
            } catch (Exception ignore) {
            }
        }
    }

    private Calculation getCalc() {
        Calculation calc = new Calculation();
        calc.setOperation(new Sum());

        ArrayList<Argument> args = new ArrayList<>();
        args.add(new Argument.Literal(1.0));
        args.add(new Argument.Literal(3.0));
        args.add(new Argument.Parameter(new ParameterModel("par", 12.34, ParameterNature.OUTPUT, ParameterValueSource.MANUAL)));
        calc.setArguments(args);
        return calc;
    }

    private EntityManager getEntityManager() throws RepositoryException {
        if (emf == null) {
            try {
                Map<String, Object> properties = new HashMap<>();
                emf = Persistence.createEntityManagerFactory(DatabaseStorage.MEM_PERSISTENCE_UNIT_NAME, properties);
            } catch (Exception e) {
                System.err.println("connecting to database failed!");
                e.printStackTrace();
                throw new RepositoryException("database connection failed");
            }
        }
        return emf.createEntityManager();
    }

    private void releaseEntityManagerFactory() {
        if (emf != null) {
            try {
                emf.close();
            } catch (Exception ignore) {
            }
            emf = null;
        }
    }
}
