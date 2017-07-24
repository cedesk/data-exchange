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
