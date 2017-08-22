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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.entity.ParameterModel;
import ru.skoltech.cedl.dataexchange.entity.ParameterNature;
import ru.skoltech.cedl.dataexchange.entity.ParameterValueSource;
import ru.skoltech.cedl.dataexchange.entity.calculation.Argument;
import ru.skoltech.cedl.dataexchange.entity.calculation.Calculation;
import ru.skoltech.cedl.dataexchange.entity.calculation.operation.Sum;
import ru.skoltech.cedl.dataexchange.init.AbstractApplicationContextTest;
import ru.skoltech.cedl.dataexchange.service.FileStorageService;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by D.Knoll on 13.05.2015.
 */
public class CalculationXmlMappingTest extends AbstractApplicationContextTest {

    private FileStorageService fileStorageService;

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

    @Before
    public void prepare() {
        fileStorageService = context.getBean(FileStorageService.class);
    }

    @Test
    public void testExportXmlAndReimport() throws IOException {
        Calculation calc = getCalc();

        File file = new File("target", "Calculation.xml");
        // Export
        fileStorageService.storeCalculation(calc, file);
        // Re-import
        Calculation recalc = fileStorageService.loadCalculation(file);

        Assert.assertNotEquals(calc, recalc);
    }

    @Test
    public void testSimpleTest() throws JAXBException {
        Calculation calculation = getCalc();
        final Class[] MC = Calculation.getEntityClasses();

        JAXBContext jc = JAXBContext.newInstance(MC);
        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, "");
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(calculation, System.out);
    }
}
