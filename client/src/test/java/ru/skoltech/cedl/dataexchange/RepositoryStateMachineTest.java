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

package ru.skoltech.cedl.dataexchange;

import org.junit.Test;
import ru.skoltech.cedl.dataexchange.db.RepositoryStateMachine;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Nikolay Groshkov on 07-Feb-18.
 */
public class RepositoryStateMachineTest {

    private RepositoryStateMachine repositoryStateMachine;

    @Test
    public void testRepositoryStateMachineTest() {
        RepositoryStateMachine repositoryStateMachine = new RepositoryStateMachine();

        assertFalse(repositoryStateMachine.hasModifications());
        assertFalse(repositoryStateMachine.wasLoadedOrSaved());
        assertTrue(repositoryStateMachine.canNewProperty().get());
        assertFalse(repositoryStateMachine.canSaveProperty().get());
        assertTrue(repositoryStateMachine.canLoadProperty().get());
        assertFalse(repositoryStateMachine.canDiffProperty().get());

        repositoryStateMachine.performAction(RepositoryStateMachine.RepositoryActions.LOAD);
        assertFalse(repositoryStateMachine.hasModifications());
        assertTrue(repositoryStateMachine.wasLoadedOrSaved());
        assertTrue(repositoryStateMachine.canNewProperty().get());
        assertFalse(repositoryStateMachine.canSaveProperty().get());
        assertTrue(repositoryStateMachine.canLoadProperty().get());
        assertTrue(repositoryStateMachine.canDiffProperty().get());

        repositoryStateMachine.performAction(RepositoryStateMachine.RepositoryActions.MODIFY);
        assertTrue(repositoryStateMachine.hasModifications());
        assertFalse(repositoryStateMachine.wasLoadedOrSaved());
        assertTrue(repositoryStateMachine.canNewProperty().get());
        assertTrue(repositoryStateMachine.canSaveProperty().get());
        assertTrue(repositoryStateMachine.canLoadProperty().get());
        assertTrue(repositoryStateMachine.canDiffProperty().get());

        repositoryStateMachine.performAction(RepositoryStateMachine.RepositoryActions.SAVE);
        assertFalse(repositoryStateMachine.hasModifications());
        assertTrue(repositoryStateMachine.wasLoadedOrSaved());
        assertTrue(repositoryStateMachine.canNewProperty().get());
        assertFalse(repositoryStateMachine.canSaveProperty().get());
        assertTrue(repositoryStateMachine.canLoadProperty().get());
        assertTrue(repositoryStateMachine.canDiffProperty().get());

        repositoryStateMachine.performAction(RepositoryStateMachine.RepositoryActions.NEW);
        assertTrue(repositoryStateMachine.hasModifications());
        assertFalse(repositoryStateMachine.wasLoadedOrSaved());
        assertTrue(repositoryStateMachine.canNewProperty().get());
        assertTrue(repositoryStateMachine.canSaveProperty().get());
        assertTrue(repositoryStateMachine.canLoadProperty().get());
        assertTrue(repositoryStateMachine.canDiffProperty().get());
    }

}
