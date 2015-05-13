package ru.skoltech.cedl.dataexchange.structure;

import java.util.EnumSet;
import java.util.Observable;

/**
 * Created by D.Knoll on 04.05.2015.
 */
public class LocalStateMachine extends Observable {

    private LocalState state;

    public LocalStateMachine() {
        state = LocalState.INITIAL;
    }

    public LocalState getState() {
        return state;
    }

    private void setState(LocalState state) {
        if (this.state != state) {
            this.state = state;
            setChanged();
            notifyObservers(state);
        }
    }

    public void performAction(LocalActions action) {
        LocalState newState = this.state.performAction(action);
        setState(newState);
    }

    public EnumSet<LocalActions> possibleActions() {
        return state.possibleActions();
    }

    public boolean isActionPossible(LocalActions action) {
        return state.possibleActions().contains(action);
    }

    public enum LocalState {

        INITIAL {
            @Override
            EnumSet<LocalActions> possibleActions() {
                return EnumSet.of(LocalActions.NEW, LocalActions.LOAD);
            }

            @Override
            LocalState performAction(LocalActions action) {
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
            EnumSet<LocalActions> possibleActions() {
                return EnumSet.of(LocalActions.NEW, LocalActions.MODIFY, LocalActions.LOAD, LocalActions.SAVE);
            }

            @Override
            LocalState performAction(LocalActions action) {
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
            EnumSet<LocalActions> possibleActions() {
                return EnumSet.of(LocalActions.NEW, LocalActions.MODIFY, LocalActions.LOAD);
            }

            @Override
            LocalState performAction(LocalActions action) {
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

        abstract EnumSet<LocalActions> possibleActions();

        abstract LocalState performAction(LocalActions action);
    }

    public enum LocalActions {
        NEW,
        MODIFY,
        LOAD,
        SAVE
    }
}
