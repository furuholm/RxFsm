package rxfsm;

import java.util.ArrayList;
import java.util.List;

import rx.functions.Action0;

import rxfsm.Transition;

public class State {

	private Action0 onEnter; // TODO: make final once a factory is used to create states
	private Action0 onExit; // TODO: make final once a factory is used to create states
	private final List<Transition> transitions;

	public State() {
		this.onEnter = null;
		this.onExit = null;
		this.transitions = new ArrayList<Transition>();
	}

    private State(Action0 onEnter, Action0 onExit, List<Transition> transitions) {
        this.onEnter = onEnter;
        this.onExit = onExit;
        this.transitions = transitions;
    }

    public State withOnEnter(Action0 action) {
        if (onEnter == null)
        {
            return new State(action, onExit, transitions);
        }
        else
        {
            throw new IllegalStateException("There can only be one onEnter function");
        }
    }

    public State withOnExit(Action0 action) {
        if (onExit == null)
        {
            return new State(onEnter, action, transitions);
        }
        else
        {
            throw new IllegalStateException("There can only be one onExit function");
        }
    }

	//TODO: remove once a factory is used to create states
	public void setOnEnter(Action0 action) {
		this.onEnter = action;
	}

    //TODO: remove once a factory is used to create states
    public void setOnExit(Action0 action) {
        this.onExit = action;
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

	void addTransition(Transition t) {
		transitions.add(t);
	}

	List<Transition> transitions() {
		return transitions;
	}
}
