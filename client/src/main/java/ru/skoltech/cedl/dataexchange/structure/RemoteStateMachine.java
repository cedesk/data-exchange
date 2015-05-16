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

    public void initialize(boolean hasRepository, boolean localChange, boolean remoteChange) {
        if (!localChange && !remoteChange) {
            state = RemoteState.CLEAN;
        } else if (localChange && !remoteChange) {
            state = RemoteState.ADVANCED;
        } else if (!localChange && remoteChange) {
            state = RemoteState.OUTDATED;
        } else { // localChange && remoteChange
            state = RemoteState.CONFLICTED;
        }
    }

    public enum RemoteState {

        /**
         * local working copy has not been connected to a remote repository
         */
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
        /**
         * working copy is clean
         */
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
        /**
         * working copy has been changed after last checkout
         */
        ADVANCED {
            @Override
            EnumSet<RemoteActions> possibleActions() {
                return EnumSet.of(RemoteActions.COMMIT, RemoteActions.LOCAL_CHANGE, RemoteActions.REMOTE_CHANGE);
            }

            @Override
            RemoteState performAction(RemoteActions action) {
                switch (action) {
                    case COMMIT:
                        return CLEAN;
                    case LOCAL_CHANGE:
                        return ADVANCED;
                    case REMOTE_CHANGE:
                        return CONFLICTED;
                    default:
                        throw new IllegalStateException("in state " + this.toString() + " action " + action.toString() + " is not allowed.");
                }
            }
        },
        /**
         * the repository has newer revisions than the working copy
         */
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
        /**
         * working copy has been changed after last checkout, and the repository has newer revisions
         */
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
        /**
         * check out clean working copy
         */
        CHECKOUT,
        /**
         * committing changes on the working copy to the repository
         */
        COMMIT,
        /**
         * updating the working copy to the latest revision in the repository
         */
        UPDATE,
        /**
         * consolidate changes made on the working copy with new revisions made on the repository
         */
        MERGE,
        /**
         * change to the local working copy
         */
        LOCAL_CHANGE,
        /**
         * new revision appeared in repository
         */
        REMOTE_CHANGE
    }
}
