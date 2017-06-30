package ru.skoltech.cedl.dataexchange.structure.model;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.AbstractDatabaseTest;
import ru.skoltech.cedl.dataexchange.db.DatabaseRepository;
import ru.skoltech.cedl.dataexchange.repository.RepositoryException;
import ru.skoltech.cedl.dataexchange.structure.model.calculation.Argument;
import ru.skoltech.cedl.dataexchange.structure.model.calculation.Sum;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.xml.bind.JAXBException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

/**
 * Created by D.Knoll on 13.05.2015.
 */
public class CalculationPersistenceTest extends AbstractDatabaseTest {

    private DatabaseRepository databaseRepository;

    @Before
    public void prepare() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        databaseRepository = (DatabaseRepository) repository;
    }

    @Test
    public void simpleTest() throws JAXBException, RepositoryException {
        Calculation calculation1 = getCalc();

        EntityManager entityManager = null;
        try {
            entityManager = databaseRepository.getEntityManager();
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


}
