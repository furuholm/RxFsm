package rxfsm;

import rx.functions.Action0;

import java.util.ArrayList;
import java.util.List;

public class StateBuilder {

    private final Action0 onEnter;
    private final Action0 onExit;
    private final List<State> subStates;
    private final State initialSubState;

    public StateBuilder() {
        this.onEnter = null;
        this.onExit = null;
        this.subStates = new ArrayList<State>();
        this.initialSubState = null;
    }

    public State build() {
        return new State(onEnter, onExit, subStates, initialSubState);
    }

    private StateBuilder(Action0 onEnter, Action0 onExit, List<State> subStates, State initialSubState) {
        this.onEnter = onEnter;
        this.onExit = onExit;
        this.subStates = subStates;
        this.initialSubState = initialSubState;
    }

    public StateBuilder withOnEnter(Action0 action) {
        if (onEnter == null)
        {
            return new StateBuilder(action, onExit, subStates, initialSubState);
        }
        else
        {
            throw new IllegalStateException("There can only be one onEnter function");
        }
    }

    public StateBuilder withOnExit(Action0 action) {
        if (onExit == null)
        {
            return new StateBuilder(onEnter, action, subStates, initialSubState);
        }
        else
        {
            throw new IllegalStateException("There can only be one onExit function");
        }
    }

    public StateBuilder withInitialSubState(State subState) {
        List<State> newSubStates = new ArrayList<State>(subStates);
        newSubStates.add(subState);
        return new StateBuilder(onEnter, onExit, newSubStates, subState);
    }

    public StateBuilder withSubState(State subState) {
        List<State> newSubStates = new ArrayList<State>(subStates);
        newSubStates.add(subState);
        return new StateBuilder(onEnter, onExit, newSubStates, initialSubState);
    }
}
