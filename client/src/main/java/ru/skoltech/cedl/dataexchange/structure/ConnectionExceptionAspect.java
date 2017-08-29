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

package ru.skoltech.cedl.dataexchange.structure;

import org.springframework.transaction.CannotCreateTransactionException;
import ru.skoltech.cedl.dataexchange.StatusLogger;

import java.net.ConnectException;

/**
 * Aspect for notifying through status bar for connection error.
 *
 * Created by Nikolay Groshkov on 29-Aug-17.
 */
public class ConnectionExceptionAspect {

    private StatusLogger statusLogger;

    public void setStatusLogger(StatusLogger statusLogger) {
        this.statusLogger = statusLogger;
    }

    public void log(CannotCreateTransactionException exception) {
        if (exception.getRootCause() instanceof ConnectException) {
            statusLogger.error("Repository connection is not available!");
        }
    }

}
