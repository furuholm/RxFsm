package rxfsm;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.HashMap;

import rx.Observable;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

public class Fsm {

	private final State initialState;
	private State currentState;
	private final CompositeSubscription transitionsSubscriptions;
	private final HashMap<State, List<Transition>> transitions;

	public Fsm(State initialState, List<Transition> transitions) {
		this.initialState = initialState;

		this.transitions = new HashMap<State, List<Transition>>();
        for (Transition t: transitions) {
            if (!this.transitions.containsKey(t.source()))
            {
                List<Transition> transitionList = new ArrayList();
                transitionList.add(t);
                this.transitions.put(t.source(), transitionList);
            }
            else
            {
                this.transitions.get(t.source()).add(t);
            }
        }

		this.currentState = null;
		this.transitionsSubscriptions = new CompositeSubscription();
	}

	public void activate() {
		enter(initialState);
	}

    private void switchState(State newState) {
        deactivateTransitions();
        exit(currentState);
        enter(newState);
    }

	private void enter(State state) {
		currentState = state;
		currentState.enter();
		activateTransitions();
	}

    private void exit(State state) {
        state.exit();
    }

    private void activateTransitions() {
		List<Transition> toActivate = transitions.get(currentState);

                System.out.println("activating");
		if (!toActivate.isEmpty()) {
			List<Observable<State>> observables
				= toActivate
					.stream()
					.map(t -> t.observable())
					.collect(Collectors.toList());

			Subscription s = Observable
				.merge(observables)
				.subscribe(newState -> switchState(newState));

			transitionsSubscriptions.add(s);
		}
	}

    private void deactivateTransitions() {
        transitionsSubscriptions.clear();
    }
}
