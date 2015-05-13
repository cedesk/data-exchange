package ru.skoltech.cedl.dataexchange.structure;

import java.util.Observable;
import java.util.Observer;

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

    public LocalActions[] possibleActions() {
        return state.possibleActions();
    }

    public boolean isActionPossible(LocalActions action) {
        for (LocalActions pa : state.possibleActions()) {
            if (pa == action) return true;
        }
        return false;
    }

    public enum LocalState {

        INITIAL {
            @Override
            LocalActions[] possibleActions() {
                return new LocalActions[]{LocalActions.NEW, LocalActions.LOAD};
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
            LocalActions[] possibleActions() {
                return new LocalActions[]{LocalActions.MODIFY, LocalActions.LOAD, LocalActions.SAVE};
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
            LocalActions[] possibleActions() {
                return new LocalActions[]{LocalActions.NEW, LocalActions.MODIFY};
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

        abstract LocalActions[] possibleActions();

        abstract LocalState performAction(LocalActions action);
    }

    public enum LocalActions {
        NEW,
        MODIFY,
        LOAD,
        SAVE
    }
}
