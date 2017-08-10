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

package ru.skoltech.cedl.dataexchange.entity.tradespace;

import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by d.knoll on 6/23/2017.
 */
public class TradespaceFactory {

    public static MultitemporalTradespace readValuesForEpochFromCSV(File file, int epoch) {
        MultitemporalTradespace multitemporalTradespace = new MultitemporalTradespace();
        List<Epoch> epoches = Epoch.buildEpochs(epoch);
        Epoch currentEpoch = epoches.get(0);
        multitemporalTradespace.setEpochs(epoches);

        List<String> lines = getLines(file);
        String[] parts = lines.get(0).split(",");

        List<FigureOfMeritDefinition> definitions = FigureOfMeritDefinition
                .buildFigureOfMeritDefinitions(ArrayUtils.subarray(parts, 1, parts.length));
        multitemporalTradespace.setDefinitions(definitions);

        List<DesignPoint> designPoints = new LinkedList<>();
        for (int row = 1; row < lines.size(); row++) {
            parts = lines.get(row).split(",");
            List<FigureOfMeritValue> values = new ArrayList<>(parts.length - 1);
            for (int col = 1; col < parts.length; col++) {
                Double value = Double.valueOf(parts[col]);
                FigureOfMeritValue figureOfMeritValue = new FigureOfMeritValue(definitions.get(col - 1), value);
                values.add(figureOfMeritValue);
            }
            DesignPoint designPoint = new DesignPoint(currentEpoch, values);
            designPoints.add(designPoint);
        }
        multitemporalTradespace.setDesignPoints(designPoints);
        return multitemporalTradespace;
    }

    private static List<String> getLines(File file) {
        List<String> collect = new LinkedList<>();
        try {
            collect = Files.lines(file.toPath()).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return collect;
    }

}
