package rxfsm;

import rx.functions.Action0;

import java.util.ArrayList;
import java.util.List;

public class StateBuilder {

    private final Action0 onEntry;
    private final Action0 onExit;
    private final List<State> subStates;
    private final State initialSubState;

    public StateBuilder() {
        this.onEntry = null;
        this.onExit = null;
        this.subStates = new ArrayList<State>();
        this.initialSubState = null;
    }

    public State build() {
        return new State(onEntry, onExit, subStates, initialSubState);
    }

    private StateBuilder(Action0 onEntry, Action0 onExit, List<State> subStates, State initialSubState) {
        this.onEntry = onEntry;
        this.onExit = onExit;
        this.subStates = subStates;
        this.initialSubState = initialSubState;
    }

    public StateBuilder withOnEntry(Action0 action) {
        if (onEntry == null)
        {
            return new StateBuilder(action, onExit, subStates, initialSubState);
        }
        else
        {
            throw new IllegalStateException("There can only be one onEntry function");
        }
    }

    public StateBuilder withOnExit(Action0 action) {
        if (onExit == null)
        {
            return new StateBuilder(onEntry, action, subStates, initialSubState);
        }
        else
        {
            throw new IllegalStateException("There can only be one onExit function");
        }
    }

    public StateBuilder withInitialSubState(State subState) {
        List<State> newSubStates = new ArrayList<State>(subStates);
        newSubStates.add(subState);
        return new StateBuilder(onEntry, onExit, newSubStates, subState);
    }

    public StateBuilder withSubState(State subState) {
        List<State> newSubStates = new ArrayList<State>(subStates);
        newSubStates.add(subState);
        return new StateBuilder(onEntry, onExit, newSubStates, initialSubState);
    }
}
