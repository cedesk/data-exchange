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

package ru.skoltech.cedl.dataexchange.entity.calculation.operation;

/**
 * Created by D.Knoll on 23.09.2015.
 */
public class Sum extends Operation {

    @Override
    public double apply(double[] arguments) {
        double result = 0;
        for (Double arg : arguments) {
            result += arg;
        }
        return result;
    }

    @Override
    public String argumentName(int index) {
        if (index < 0)
            throw new IllegalArgumentException("invalid argument index " + index);
        return "summand " + index;
    }

    @Override
    public String description() {
        return "This operation sums all arguments";
    }

    @Override
    public int maxArguments() {
        return Integer.MAX_VALUE;
    }

    @Override
    public int minArguments() {
        return 2;
    }

    @Override
    public String name() {
        return "Sum";
    }
}
