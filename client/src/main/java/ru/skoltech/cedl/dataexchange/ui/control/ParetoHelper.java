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

import java.util.Collection;
import java.util.HashSet;

/**
 * This helper give some methods useful for Pareto efficiency check.
 *
 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
 */
public class ParetoHelper {

    /**
     * This method looks for the individuals of the Pareto frontier, considering
     * we are looking for maximal individuals.
     *
     * @param population the individuals to check
     * @param comparator the Pareto comparator to use
     * @return the individuals at the Pareto frontier
     */
    public static <Individual> Collection<Individual> getMaximalFrontierOf(
            final Collection<Individual> population,
            final ParetoComparator<Individual> comparator) {
        OrderChecker<Individual> checker = new OrderChecker<Individual>() {

            public boolean canOrderAs(Individual i1, Individual i2) {
                return comparator.compare(i1, i2) < 0;
            }
        };

        return getFrontierOf(population, checker);
    }

    /**
     * This method looks for the individuals of the Pareto frontier, considering
     * we are looking for minimal individuals.
     *
     * @param population the individuals to check
     * @param comparator the Pareto comparator to use
     * @return the individuals at the Pareto frontier
     */
    public static <Individual> Collection<Individual> getMinimalFrontierOf(
            final Collection<Individual> population,
            final ParetoComparator<Individual> comparator) {
        OrderChecker<Individual> checker = new OrderChecker<Individual>() {

            public boolean canOrderAs(Individual i1, Individual i2) {
                return comparator.compare(i1, i2) > 0;
            }
        };
        return getFrontierOf(population, checker);
    }

    /**
     * This method is the common part of
     * {@link #getMaximalFrontierOf(Collection, ParetoComparator)} and
     * {@link #getMinimalFrontierOf(Collection, ParetoComparator)}.
     *
     * @param population the population to check
     * @param checker    the checker to use
     * @return the individuals of the frontier identified by the checker
     */
    private static <Individual> Collection<Individual> getFrontierOf(
            final Collection<Individual> population,
            OrderChecker<Individual> checker) {
        Collection<Individual> frontier = new HashSet<Individual>();
        for (Individual i1 : population) {
            Boolean add = true;
            for (Individual i2 : population) {
                if (checker.canOrderAs(i1, i2)) {
                    add = false;
                    break;
                }
            }
            if (add) {
                frontier.add(i1);
            }
        }
        return frontier;
    }

    /**
     * A basic interface to factor comparisons.
     *
     * @param <Individual> The type of individuals checked.
     * @author Matthieu Vergne <vergne@fbk.eu>
     */
    private interface OrderChecker<Individual> {
        /**
         * Tell if both individuals can be ordered as i1 worst than i2.
         *
         * @return true if i1 is worst than i2, false otherwise
         */
        public boolean canOrderAs(Individual i1, Individual i2);
    }
}
