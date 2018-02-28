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

package ru.skoltech.cedl.dataexchange.ui.control;

import com.sun.javafx.charts.Legend;
import javafx.beans.NamedArg;
import javafx.collections.ObservableList;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;

/**
 * Extension of {@link LineChart} for display additionally a Pareto front series and utopia point
 * (these data is hidden in legend).
 * <p>
 * Created by Nikolay Groshkov on 28-Feb-18.
 */
public class TradespaceLineChart<X, Y> extends LineChart<X, Y> {

    public static final String PARETO_SERIES_NAME = "PF";
    public static final String UTOPIA_SERIES_NAME = "Utopia Point";


    public TradespaceLineChart(@NamedArg("xAxis") Axis<X> xAxis, @NamedArg("yAxis") Axis<Y> yAxis) {
        super(xAxis, yAxis);
    }

    public TradespaceLineChart(@NamedArg("xAxis") Axis<X> xAxis, @NamedArg("yAxis") Axis<Y> yAxis, @NamedArg("data") ObservableList<Series<X, Y>> data) {
        super(xAxis, yAxis, data);
    }

    @Override
    protected void updateLegend() {
        super.updateLegend();
        Legend legend = (Legend) getLegend();
        if (legend == null) {
            return;
        }
        legend.getItems().removeIf(legendItem -> PARETO_SERIES_NAME.equals(legendItem.getText()));
        legend.getItems().removeIf(legendItem -> UTOPIA_SERIES_NAME.equals(legendItem.getText()));
    }
}
