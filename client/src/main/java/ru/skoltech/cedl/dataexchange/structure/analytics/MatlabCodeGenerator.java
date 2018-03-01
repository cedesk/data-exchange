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

package ru.skoltech.cedl.dataexchange.structure.analytics;

import edu.carleton.tim.jdsm.RealNumberDSM;
import org.jscience.mathematics.number.Real;

import java.util.Locale;
import java.util.Map;

/**
 * This class encapsulates the generation of code usable with a Matlab library for DSM optimization. <br>
 * The library was written by Ronnie E. Thebeau, available at http://www.dsmweb.org/?id=121
 * <p>
 * Created by D.Knoll on 01.03.2018.
 */
public class MatlabCodeGenerator {

    /**
     * Generates code for a Matlab script to process DSM data.
     *
     * @return the Matlab code.
     */
    public static String transformDSM(RealNumberDSM dsm, boolean weighted) {
        int matrixSize = dsm.getPositionNameMappings().size();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("DSM_size = %d;\n", matrixSize));
        sb.append("DSMLABEL = cell(DSM_size,1);\n\n");
        Map<Integer, String> positionNameMapping = dsm.getPositionNameMappings();
        for (int rowIndex = 0; rowIndex < matrixSize; rowIndex++) {
            sb.append(String.format("DSMLABEL{%d,1} = '%s';", rowIndex + 1, positionNameMapping.get(rowIndex)));
            sb.append("\n");
        }
        sb.append("\nDSM = zeros(DSM_size);\n\n");
        Real[][] dependencyMatrix = dsm.getMap();
        for (int rowIndex = 0; rowIndex < matrixSize; rowIndex++) {
            for (int columnIndex = 0; columnIndex < matrixSize; columnIndex++) {
                double floatValue = dependencyMatrix[rowIndex][columnIndex].doubleValue();
                if (floatValue > 0) {
                    Double weight = weighted ? floatValue : 1f;
                    sb.append(String.format(Locale.ENGLISH, "DSM(%d,%d) = %f;", rowIndex, columnIndex, weight));
                    sb.append("\n");
                }
            }
        }
        return sb.toString();
    }
}
