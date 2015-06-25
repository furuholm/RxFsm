package rxfsm;

import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;

import java.util.ArrayList;
import java.util.List;

public class StateBuilder {

    private final Action0 onEntry;
    private final Action0 onExit;
    private final List<State> subStates;
    private final State initialSubState;
    private final List<Transition> internalTransitions;

    public StateBuilder() {
        this.onEntry = null;
        this.onExit = null;
        this.subStates = new ArrayList<State>();
        this.initialSubState = null;
        this.internalTransitions = new ArrayList<Transition>();
    }

    public State build() {
        return new State(onEntry, onExit, subStates, initialSubState, internalTransitions);
    }

    private StateBuilder(Action0 onEntry, Action0 onExit, List<State> subStates, State initialSubState, List<Transition> internalTransitions) {
        this.onEntry = onEntry;
        this.onExit = onExit;
        this.subStates = subStates;
        this.initialSubState = initialSubState;
        this.internalTransitions = internalTransitions;
    }

    public StateBuilder withOnEntry(Action0 action) {
        if (onEntry == null)
        {
            return new StateBuilder(action, onExit, subStates, initialSubState, internalTransitions);
        }
        else
        {
            throw new IllegalStateException("There can only be one onEntry function");
        }
    }

    public StateBuilder withOnExit(Action0 action) {
        if (onExit == null)
        {
            return new StateBuilder(onEntry, action, subStates, initialSubState, internalTransitions);
        }
        else
        {
            throw new IllegalStateException("There can only be one onExit function");
        }
    }

    public <T> StateBuilder withInternalTransition(Observable<T> event, Action1<T> action)
    {
        List<Transition> newInternalTransitions = new ArrayList<Transition>(internalTransitions);
        newInternalTransitions.add(Transition.create(event, null, null, action));
        return new StateBuilder(onEntry, onExit, subStates, initialSubState, newInternalTransitions);
    }

    public StateBuilder withInitialSubState(State subState) {
        List<State> newSubStates = new ArrayList<State>(subStates);
        newSubStates.add(subState);
        return new StateBuilder(onEntry, onExit, newSubStates, subState, internalTransitions);
    }

    public StateBuilder withSubState(State subState) {
        List<State> newSubStates = new ArrayList<State>(subStates);
        newSubStates.add(subState);
        return new StateBuilder(onEntry, onExit, newSubStates, initialSubState, internalTransitions);
    }
}
