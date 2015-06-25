package rxfsm;

import java.util.ArrayList;
import java.util.List;

import rx.functions.Action0;

import rxfsm.Transition;

public class State {

	private final Action0 onEnter;
	private final Action0 onExit;
    private final List<State> subStates;
    private final State initialSubState;
    private final List<Transition> internalTransitions;

    State(Action0 onEnter, Action0 onExit, List<State> subStates, State initialSubState, List<Transition> internalTransitions) {
        if (initialSubState == null && subStates.size() > 0) {
            throw new IllegalStateException("If there are any sub states, one of them has to be the initial sub state");
        }
        this.onEnter = onEnter;
        this.onExit = onExit;
        this.subStates = subStates;
        this.initialSubState = initialSubState;
        this.internalTransitions = internalTransitions;
    }

	public void enter()
	{
		if (onEnter != null)
		{
			onEnter.call();
		}
	}

	public void exit()
	{
		if (onExit != null)
		{
			onExit.call();
		}
	}

    public List<State> getSubStates() {
        return subStates;
    }

    public State getInitialSubState() {
        return initialSubState;
    }

    public List<Transition> getInternalTransitions() {
        return internalTransitions;
    }
}
