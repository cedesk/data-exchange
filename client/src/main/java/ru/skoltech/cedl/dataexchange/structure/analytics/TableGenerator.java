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

import java.util.Map;

/**
 * This class encapsulates the generation of a table representing the DSM
 * <p>
 * Created by D.Knoll on 06.02.2019.
 */
public class TableGenerator {

    /**
     * Generates a table in TAB format of the DSM.
     *
     * @return the table.
     */
    public static String transformDSM(RealNumberDSM dsm, boolean weighted) {
        int matrixSize = dsm.getPositionNameMappings().size();
        StringBuilder sb = new StringBuilder();
        Map<Integer, String> positionNameMapping = dsm.getPositionNameMappings();
        sb.append('\t'); // empty corner cell
        for (int rowIndex = 0; rowIndex < matrixSize; rowIndex++) {
            sb.append(positionNameMapping.get(rowIndex));
            sb.append('\t');
        }
        sb.append('\n');
        Real[][] dependencyMatrix = dsm.getMap();
        for (int rowIndex = 0; rowIndex < matrixSize; rowIndex++) {
            sb.append(positionNameMapping.get(rowIndex));
            sb.append('\t');
            for (int columnIndex = 0; columnIndex < matrixSize; columnIndex++) {
                int intValue = dependencyMatrix[rowIndex][columnIndex].intValue();
                if (intValue > 0) {
                    int weight = weighted ? intValue : 1;
                    sb.append(weight);
                    sb.append('\t');
                }
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}
