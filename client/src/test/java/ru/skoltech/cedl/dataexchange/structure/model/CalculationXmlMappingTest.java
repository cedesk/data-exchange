/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange.structure.model;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.AbstractApplicationContextTest;
import ru.skoltech.cedl.dataexchange.services.FileStorageService;
import ru.skoltech.cedl.dataexchange.structure.model.calculation.Argument;
import ru.skoltech.cedl.dataexchange.structure.model.calculation.Sum;

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

    @Before
    public void prepare() {
        fileStorageService = context.getBean(FileStorageService.class);
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
