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

package ru.skoltech.cedl.dataexchange.db;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.EnumSet;

/**
 * Created by D.Knoll on 04.05.2015.
 */
public class RepositoryStateMachine {

    private ObjectProperty<RepositoryState> stateProperty = new SimpleObjectProperty<>(RepositoryState.INITIAL);

    public boolean hasModifications() {
        return stateProperty.get() == RepositoryState.DIRTY;
    }

    public boolean wasLoadedOrSaved() {
        return stateProperty.get() == RepositoryState.SAVED;
    }

    public void reset() {
        stateProperty.set(RepositoryState.INITIAL);
    }

    public void performAction(RepositoryActions action) {
        stateProperty.set(stateProperty.get().performAction(action));
    }

    public BooleanBinding canNewProperty() {
        return Bindings.createBooleanBinding(() ->
                stateProperty.get().possibleActions().contains(RepositoryActions.NEW), stateProperty);
    }

    public BooleanBinding canLoadProperty() {
        return Bindings.createBooleanBinding(() ->
                stateProperty.get().possibleActions().contains(RepositoryActions.LOAD), stateProperty);
    }

    public BooleanBinding canSaveProperty() {
        return Bindings.createBooleanBinding(() ->
                stateProperty.get().possibleActions().contains(RepositoryActions.SAVE), stateProperty);
    }

    public BooleanBinding canDiffProperty() {
        return stateProperty.isNotEqualTo(RepositoryState.INITIAL);
    }

    @Override
    public String toString() {
        return "RepositoryStateMachine{" +
                "state=" + stateProperty.get() +
                '}';
    }

    public enum RepositoryState {

        INITIAL {
            @Override
            EnumSet<RepositoryActions> possibleActions() {
                return EnumSet.of(RepositoryActions.NEW, RepositoryActions.LOAD);
            }

            @Override
            RepositoryState performAction(RepositoryActions action) {
                switch (action) {
                    case LOAD:
                        return SAVED;
                    case NEW:
                        return DIRTY;
                    default:
                        throw new IllegalStateException("in state " + this.toString() + " action " + action.toString() + " is not allowed.");
                }
            }
        },
        DIRTY {
            @Override
            EnumSet<RepositoryActions> possibleActions() {
                return EnumSet.of(RepositoryActions.NEW, RepositoryActions.MODIFY, RepositoryActions.LOAD, RepositoryActions.SAVE);
            }

            @Override
            RepositoryState performAction(RepositoryActions action) {
                switch (action) {
                    case NEW:
                    case MODIFY:
                        return DIRTY;
                    case LOAD:
                    case SAVE:
                        return SAVED;
                    default:
                        throw new IllegalStateException("in state " + this.toString() + " action " + action.toString() + " is not allowed.");
                }
            }
        },
        SAVED {
            @Override
            EnumSet<RepositoryActions> possibleActions() {
                return EnumSet.of(RepositoryActions.NEW, RepositoryActions.MODIFY, RepositoryActions.LOAD);
            }

            @Override
            RepositoryState performAction(RepositoryActions action) {
                switch (action) {
                    case NEW:
                    case MODIFY:
                        return DIRTY;
                    case LOAD:
                        return SAVED;
                    default:
                        throw new IllegalStateException("in state " + this.toString() + " action " + action.toString() + " is not allowed.");
                }
            }
        };

        abstract RepositoryState performAction(RepositoryActions action);

        abstract EnumSet<RepositoryActions> possibleActions();
    }

    public enum RepositoryActions {
        NEW,
        MODIFY,
        LOAD,
        SAVE
    }
}
