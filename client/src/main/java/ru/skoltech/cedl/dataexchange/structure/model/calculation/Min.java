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

package ru.skoltech.cedl.dataexchange.structure.model.calculation;

import java.util.Arrays;
import java.util.OptionalDouble;

/**
 * Created by D.Knoll on 26.09.2015.
 */
public class Min extends Operation {

    @Override
    public String name() {
        return "Min";
    }

    @Override
    public String description() {
        return "This operation finds the minimum among all arguments";
    }

    @Override
    public String argumentName(int index) {
        if (index < 0)
            throw new IllegalArgumentException("invalid argument index " + index);
        return "argument " + index;
    }

    @Override
    public int minArguments() {
        return 2;
    }

    @Override
    public int maxArguments() {
        return Integer.MAX_VALUE;
    }

    @Override
    public double apply(double[] arguments) {
        OptionalDouble optionalDouble = Arrays.stream(arguments).min();
        return optionalDouble.getAsDouble();
    }
}
