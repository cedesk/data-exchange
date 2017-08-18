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
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by d.knoll on 6/23/2017.
 */
public class TradespaceFactory {

    /**
     * This method reads the data from a CSV file (sep=, and digit=.) assuming that the first column is the the
     * design points description (a product name), and the last column contains the epochs year. (see GPUdataset_2013-2016.csv)
     *
     * @param file
     * @return
     */
    public static MultitemporalTradespace readValuesForEpochFromCSV(File file) {
        MultitemporalTradespace multitemporalTradespace = new MultitemporalTradespace();
        Map<Integer, Epoch> epochMap = new HashMap<>();

        List<String> lines = getLines(file);
        String[] parts = lines.get(0).split(",");

        List<FigureOfMeritDefinition> definitions = FigureOfMeritDefinition
                .buildFigureOfMeritDefinitions(ArrayUtils.subarray(parts, 1, parts.length - 1));
        multitemporalTradespace.setDefinitions(definitions);

        List<DesignPoint> designPoints = new LinkedList<>();
        for (int row = 1; row < lines.size(); row++) {
            parts = lines.get(row).split(",");
            List<FigureOfMeritValue> values = new ArrayList<>(parts.length - 2);
            Integer year = Integer.valueOf(parts[parts.length - 1]);
            Epoch currentEpoch = epochMap.merge(year, new Epoch(year), (epoch1, epoch2) -> epoch2);
            for (int col = 1; col < parts.length - 1; col++) {
                Double value = Double.valueOf(parts[col]);
                FigureOfMeritValue figureOfMeritValue = new FigureOfMeritValue(definitions.get(col - 1), value);
                values.add(figureOfMeritValue);
            }
            DesignPoint designPoint = new DesignPoint(parts[0], currentEpoch, values);
            designPoints.add(designPoint);
        }
        multitemporalTradespace.setDesignPoints(designPoints);

        ArrayList<Epoch> epochList = new ArrayList<>(epochMap.values());
        epochList.sort(Comparator.naturalOrder());
        multitemporalTradespace.setEpochs(epochList);
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
