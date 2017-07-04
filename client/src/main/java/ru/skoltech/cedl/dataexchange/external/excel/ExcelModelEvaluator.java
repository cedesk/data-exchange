package ru.skoltech.cedl.dataexchange.external.excel;

import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.external.ExternalModelEvaluator;
import ru.skoltech.cedl.dataexchange.external.ExternalModelException;
import ru.skoltech.cedl.dataexchange.external.ExternalModelFileHandler;
import ru.skoltech.cedl.dataexchange.external.SpreadsheetCoordinates;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.model.ExternalModel;

import java.text.ParseException;

/**
 * Created by D.Knoll on 08.07.2015.
 */
public class ExcelModelEvaluator extends ExcelModelAccessor implements ExternalModelEvaluator {

    private static Logger logger = Logger.getLogger(ExcelModelEvaluator.class);

    public ExcelModelEvaluator(ExternalModel externalModel, ExternalModelFileHandler externalModelFileHandler) {
        super.externalModel = externalModel;
        super.externalModelFileHandler = externalModelFileHandler;
    }

    @Override
    public Double getValue(Project project, String target) throws ExternalModelException {
        if (target == null)
            throw new ExternalModelException("target is null");
        try {
            SpreadsheetCoordinates coordinates = SpreadsheetCoordinates.valueOf(target);
            SpreadsheetCellValueAccessor spreadsheetAccessor = getSpreadsheetAccessor(project);
            return spreadsheetAccessor.getNumericValue(coordinates);
        } catch (ParseException e) {
            logger.error("error parsing coordinates: " + target);
            return null;
        }
    }


}
