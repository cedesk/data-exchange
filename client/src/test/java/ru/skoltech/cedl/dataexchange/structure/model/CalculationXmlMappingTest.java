package ru.skoltech.cedl.dataexchange.structure.model;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.repository.FileStorage;
import ru.skoltech.cedl.dataexchange.structure.model.calculation.Argument;
import ru.skoltech.cedl.dataexchange.structure.model.calculation.Sum;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.io.IOException;

/**
 * Created by D.Knoll on 13.05.2015.
 */
public class CalculationXmlMappingTest {

    @Before
    public void setup() {
    }

    @Test
    public void simpleTest() throws JAXBException {
        Calculation calculation = getCalc();
        final Class[] MC = Calculation.getEntityClasses();

        JAXBContext jc = JAXBContext.newInstance(MC);
        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, "");
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(calculation, System.out);
    }

    @Test
    public void exportXmlAndReimport() throws IOException {
        Calculation calc = getCalc();

        FileStorage fs = new FileStorage();
        File file = new File("target", "Calculation.xml");
        // Export
        fs.storeCalculation(calc, file);
        // Re-import
        Calculation recalc = fs.loadCalculation(file);

        Assert.assertEquals(calc, recalc);
    }

    private Calculation getCalc() {
        Calculation calc = new Calculation();
        calc.setOperation(new Sum());

        Argument[] arguments = new Argument[3];
        arguments[0] = new Argument.Literal(1.0);
        arguments[1] = new Argument.Literal(3.0);
        arguments[2] = new Argument.Parameter(new ParameterModel("par", 12.34, ParameterNature.OUTPUT, ParameterValueSource.MANUAL));
        calc.setArguments(arguments);
        return calc;
    }
}
