package ru.skoltech.cedl.dataexchange.structure;

import java.util.EnumSet;
import java.util.Observable;

/**
 * Created by D.Knoll on 14.05.2015.
 */
public class RemoteStateMachine extends Observable {

    private RemoteState state;

    public RemoteStateMachine() {
        state = RemoteState.UNCOUPLED;
    }

    public RemoteState getState() {
        return state;
    }

    private void setState(RemoteState state) {
        if (this.state != state) {
            this.state = state;
            setChanged();
            notifyObservers(state);
        }
    }

    public void performAction(RemoteActions action) {
        RemoteState newState = this.state.performAction(action);
        setState(newState);
    }

    public EnumSet<RemoteActions> possibleActions() {
        return state.possibleActions();
    }

    public boolean isActionPossible(RemoteActions action) {
        return state.possibleActions().contains(action);
    }

    public enum RemoteState {

        UNCOUPLED {
            @Override
            EnumSet<RemoteActions> possibleActions() {
                return EnumSet.of(RemoteActions.CHECKOUT);
            }

            @Override
            RemoteState performAction(RemoteActions action) {
                switch (action) {
                    case CHECKOUT:
                        return CLEAN;
                    default:
                        throw new IllegalStateException("in state " + this.toString() + " action " + action.toString() + " is not allowed.");
                }
            }
        },
        CLEAN {
            @Override
            EnumSet<RemoteActions> possibleActions() {
                return EnumSet.of(RemoteActions.LOCAL_CHANGE, RemoteActions.REMOTE_CHANGE);
            }

            @Override
            RemoteState performAction(RemoteActions action) {
                switch (action) {
                    case LOCAL_CHANGE:
                        return ADVANCED;
                    case REMOTE_CHANGE:
                        return OUTDATED;
                    default:
                        throw new IllegalStateException("in state " + this.toString() + " action " + action.toString() + " is not allowed.");
                }
            }
        },
        ADVANCED {
            @Override
            EnumSet<RemoteActions> possibleActions() {
                return EnumSet.of(RemoteActions.COMMIT, RemoteActions.REMOTE_CHANGE);
            }

            @Override
            RemoteState performAction(RemoteActions action) {
                switch (action) {
                    case COMMIT:
                        return CLEAN;
                    case REMOTE_CHANGE:
                        return CONFLICTED;
                    default:
                        throw new IllegalStateException("in state " + this.toString() + " action " + action.toString() + " is not allowed.");
                }
            }
        },
        OUTDATED {
            @Override
            EnumSet<RemoteActions> possibleActions() {
                return EnumSet.of(RemoteActions.LOCAL_CHANGE, RemoteActions.UPDATE);
            }

            @Override
            RemoteState performAction(RemoteActions action) {
                switch (action) {
                    case LOCAL_CHANGE:
                        return CONFLICTED;
                    case UPDATE:
                        return CLEAN;
                    default:
                        throw new IllegalStateException("in state " + this.toString() + " action " + action.toString() + " is not allowed.");
                }
            }
        },
        CONFLICTED {
            @Override
            EnumSet<RemoteActions> possibleActions() {
                return EnumSet.of(RemoteActions.MERGE, RemoteActions.LOCAL_CHANGE, RemoteActions.REMOTE_CHANGE);
            }

            @Override
            RemoteState performAction(RemoteActions action) {
                switch (action) {
                    case MERGE:
                        return CLEAN;
                    case LOCAL_CHANGE:
                    case REMOTE_CHANGE:
                        return CONFLICTED;
                    default:
                        throw new IllegalStateException("in state " + this.toString() + " action " + action.toString() + " is not allowed.");
                }

            }
        };

        abstract EnumSet<RemoteActions> possibleActions();

        abstract RemoteState performAction(RemoteActions action);
    }

    public enum RemoteActions {
        CHECKOUT,
        COMMIT,
        UPDATE,
        MERGE,
        LOCAL_CHANGE,
        REMOTE_CHANGE
    }
}
