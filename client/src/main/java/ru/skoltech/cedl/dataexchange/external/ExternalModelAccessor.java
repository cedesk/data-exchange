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

package ru.skoltech.cedl.dataexchange.external;

import java.io.Closeable;
import java.io.Flushable;

/**
 * By use of this interface access to the the external model can be established.
 * Required values can be retrieved or stored according to the provided target
 * which has its meaning only in the particular implementation of an accessor.
 * Changes takes place only after {@link Flushable#flush()} invocation.
 * Clients can explicitly close an accessor after use, implementations must release any acquired
 * resources in this case.
 *
 * Created by D.Knoll on 23.07.2015.
 */
public interface ExternalModelAccessor extends Flushable, Closeable {

    /**
     * Retrieve a value from external model. Provided target value defines location of the value to be retrieved
     * from the particular implementation.
     *
     * @param target defines an exact location to retrieve a value form particular implementation of accessor
     * @return a value
     * @throws ExternalModelException if access in impossible
     */
    Double getValue(String target) throws ExternalModelException;

    /**
     * Place a provided value to the external model. Provided target value defines location of the value to be placed
     * inside the particular implementation.
     *
     * @param target defines an exact location to place a value inside particular implementation of accessor
     * @param value a value
     * @throws ExternalModelException if access in impossible
     */
    void setValue(String target, Double value) throws ExternalModelException;

}
