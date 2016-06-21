package ru.skoltech.cedl.dataexchange.repository;

import java.util.EnumSet;
import java.util.Observable;

/**
 * Created by D.Knoll on 04.05.2015.
 */
public class RepositoryStateMachine extends Observable {

    private RepositoryState state;

    private boolean wasLoadedOrSaved = false;

    public RepositoryStateMachine() {
        state = RepositoryState.INITIAL;
    }

    @Override
    public String toString() {
        return "RepositoryStateMachine{" +
                "state=" + state +
                '}';
    }

    public RepositoryState getState() {
        return state;
    }

    private void setState(RepositoryState state) {
        switch (state) {
            case INITIAL:
                wasLoadedOrSaved = false;
                break;
            case SAVED:
                wasLoadedOrSaved = true;
                break;
        }
        if (this.state != state) {
            this.state = state;
            setChanged();
            notifyObservers(state);
        }
    }

    public void performAction(RepositoryActions action) {
        RepositoryState newState = state.performAction(action);
        setState(newState);
    }

    public EnumSet<RepositoryActions> possibleActions() {
        return state.possibleActions();
    }

    public boolean isActionPossible(RepositoryActions action) {
        return state.possibleActions().contains(action);
    }

    public void reset() {
        this.setState(RepositoryState.INITIAL);
    }

    public boolean wasLoadedOrSaved() {
        return wasLoadedOrSaved;
    }

    public boolean hasModifications() {
        return state == RepositoryState.DIRTY;
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

        abstract EnumSet<RepositoryActions> possibleActions();

        abstract RepositoryState performAction(RepositoryActions action);
    }

    public enum RepositoryActions {
        NEW,
        MODIFY,
        LOAD,
        SAVE
    }
}
