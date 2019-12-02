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

import java.util.Comparator;
import java.util.LinkedList;

/**
 * A Pareto comparator allows to compare multidimensional individuals in a
 * Pareto way. Look at the Javadoc of {@link #compare(Object, Object)} for the
 * formalization.<br/>
 * <br/>
 * An individual is considered better than another regarding the comparators
 * given to the Pareto comparator (one for each dimension). There is no
 * constraint about which direction (positive or negative comparison) tells
 * which one is the best (so you can decide for the most natural way), but
 * <b>all the comparators have to be consistent</b>: if one comparator uses a
 * positive value to say A is better than B, the others must use the same
 * convention.<br/>
 * <br/>
 * <b>ATTENTION</b> Two individuals said equivalent through this comparator can
 * be different (a.equals(b) == <code>false</code>)!
 *
 * @param <Individual> The individuals to compare.
 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
 */
@SuppressWarnings("serial")
public class ParetoComparator<Individual> extends
        LinkedList<Comparator<Individual>> implements Comparator<Individual> {

    /**
     * Compare multidimensional individuals in a Pareto way :
     * <ul>
     * <li>if A is better than B on all the dimensions (some can be equivalent),
     * A is considered as the best one</li>
     * <li>if A is equivalent to B on all the dimensions, A and B are considered
     * as equivalent</li>
     * <li>if A is better than B on at least one dimension and worst on at least
     * one another, A and B are considered as equivalent, as we cannot decide
     * which one is better</li>
     * </ul>
     */
    public int compare(Individual a, Individual b) {
        int reference = 0;
        for (Comparator<Individual> comparator : this) {
            if (reference == 0) {
                reference = (int) Math.signum(comparator.compare(a, b));
            } else {
                int comparison = (int) Math.signum(comparator.compare(a, b));
                if (comparison * reference < 0) {
                    // one better, another worst : cannot decide
                    return 0;
                }
            }
        }
        return reference;
    }

}
