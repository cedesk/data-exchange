/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange.structure.model;

import ru.skoltech.cedl.dataexchange.AbstractApplicationContextTest;
import ru.skoltech.cedl.dataexchange.structure.model.calculation.Argument;
import ru.skoltech.cedl.dataexchange.structure.model.calculation.Sum;

import java.util.ArrayList;

/**
 * Created by D.Knoll on 13.05.2015.
 */
public class CalculationPersistenceTest extends AbstractApplicationContextTest {

//    TODO: rewrite - test of persistence API is quite obvious
//    @Test
//    public void simpleTest() throws JAXBException, RepositoryException {
//        Calculation calculation1 = createCalculation();
//
//        EntityManager entityManager = null;
//        try {
//            entityManager = databaseRepository.getEntityManager();
//            EntityTransaction transaction = entityManager.getTransaction();
//            transaction.begin();
//            entityManager.persist(calculation1);
//            transaction.commit();
//            long id = calculation1.getId();
//
//            Calculation calculation2 = entityManager.find(Calculation.class, id);
//            Assert.assertEquals(calculation1, calculation2);
//
//        } catch (Exception e) {
//            throw new RepositoryException("Storing failed.", e);
//        } finally {
//            try {
//                if (entityManager != null)
//                    entityManager.close();
//            } catch (Exception ignore) {
//            }
//        }
//    }

    private Calculation createCalculation() {
        ArrayList<Argument> args = new ArrayList<>();
        args.add(new Argument.Literal(1.0));
        args.add(new Argument.Literal(3.0));
        args.add(new Argument.Parameter(new ParameterModel("par", 12.34, ParameterNature.OUTPUT, ParameterValueSource.MANUAL)));

        Calculation calculation = new Calculation();
        calculation.setOperation(new Sum());
        calculation.setArguments(args);
        return calculation;
    }


}
