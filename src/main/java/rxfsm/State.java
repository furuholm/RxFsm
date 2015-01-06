package rxfsm;

import java.util.ArrayList;
import java.util.List;

import rx.functions.Action0;

import rxfsm.Transition;

public class State {

	private Action0 onEnter; // TODO: make final
	private final Action0 onExit;
	private final List<Transition> transitions;

	public State() {
		this.onEnter = null;
		this.onExit = null;
		this.transitions = new ArrayList();
	}

	//TODO: remove once a factory is used to create states
	public void setOnEnter(Action0 action) {
		this.onEnter = action;
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