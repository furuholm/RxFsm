package rxfsm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    private final Map<State, List<State>> stateAncestorMap;
    private final List<State> topStates;

	public Fsm(State initialState, List<Transition> transitions, List<State> topStates, Map<State, List<State>> stateAncestorMap) {
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
        this.topStates = topStates;
        if (topStates == null || topStates.isEmpty()) {
            throw new IllegalArgumentException("Top states needs to be provided");
        }
        this.stateAncestorMap = stateAncestorMap;
	}

	public void activate() {
		enter(initialState);
	}

    private void switchState(State newState) {
        TransitionPath path =
                TransitionPathCalculator.calculateTransitionPath(
                        stateAncestorMap.get(currentState),
                        stateAncestorMap.get(newState));

        deactivateTransitions();
        exit(currentState);
        exitStates(path.getStatesToExit());
        enterStates(path.getStatesToEnter());
        enter(newState);
    }

	private void enter(State state) {
		state.enter();

        State initialSubState = state.getInitialSubState();
        if (initialSubState != null)
        {
            enter(initialSubState);
        }
        else
        {
            // The base case
            currentState = state;
            activateTransitions();
        }

	}

    private void exit(State state) {
        state.exit();
    }

    private void exitStates(List<State> statesToExit) {
        for (State s : statesToExit) {
            s.exit();
        }
    }

    private void enterStates(List<State> statesToEnter) {
        for (State s: statesToEnter) {
            s.enter();
        }
    }

    private void activateTransitions() {
		List<Transition> toActivate = transitions.get(currentState);

		if (toActivate != null && !toActivate.isEmpty()) {
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
