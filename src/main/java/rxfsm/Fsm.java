package rxfsm;

import java.util.List;
import java.util.stream.Collectors;

import rx.Observable;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

import rxfsm.State;
import rxfsm.Transition;

public class Fsm {

	private final State initialState;
	private State currentState;
	private final CompositeSubscription transitionsSubscriptions;

	public Fsm(State initialState) {
		this.initialState = initialState;
		this.currentState = null;
		this.transitionsSubscriptions = new CompositeSubscription();
	}

	public void activate() {
		enter(initialState);
	}

	private void enter(State state) {
		currentState = state;
		currentState.enter();
		activateTransitions();
	}

	private void exit(State state) {
		state.exit();
	}

	private void switchState(State newState) {
		transitionsSubscriptions.unsubscribe();
		exit(currentState);
		enter(newState);
	}

	private void activateTransitions() {
		List<Transition> transitions = currentState.transitions();
		
		if (!transitions.isEmpty()) {
			List<Observable<State>> observables 
				= transitions
					.stream()
					.map(t -> t.observable())
					.collect(Collectors.toList());

			Subscription s = Observable
				.merge(observables)
				.subscribe(newState -> switchState(newState));

			transitionsSubscriptions.add(s);
		}
	}
}