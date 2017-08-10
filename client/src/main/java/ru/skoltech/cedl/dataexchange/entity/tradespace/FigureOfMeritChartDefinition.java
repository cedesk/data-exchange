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

/**
 * Created by d.knoll on 6/23/2017.
 */
public class FigureOfMeritChartDefinition {

    private FigureOfMeritDefinition axis1;

    private FigureOfMeritDefinition axis2;

    public FigureOfMeritChartDefinition(FigureOfMeritDefinition axis1, FigureOfMeritDefinition axis2) {
        this.axis1 = axis1;
        this.axis2 = axis2;
    }

    public FigureOfMeritDefinition getAxis1() {
        return axis1;
    }

    public void setAxis1(FigureOfMeritDefinition axis1) {
        this.axis1 = axis1;
    }

    public FigureOfMeritDefinition getAxis2() {
        return axis2;
    }

    public void setAxis2(FigureOfMeritDefinition axis2) {
        this.axis2 = axis2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FigureOfMeritChartDefinition that = (FigureOfMeritChartDefinition) o;

        if (!axis1.equals(that.axis1)) return false;
        return axis2.equals(that.axis2);
    }

    @Override
    public int hashCode() {
        int result = axis1.hashCode();
        result = 31 * result + axis2.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "FigureOfMeritChartDefinition{" +
                "axis1=" + axis1 +
                ", axis2=" + axis2 +
                '}';
    }
}
