package rxfsm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.stream.Stream;

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

    private void switchState(State targetState) {
        State actualTargetState = targetState;
        while (actualTargetState.getInitialSubState() != null) {
            actualTargetState = actualTargetState.getInitialSubState();
        }

        TransitionPath path =
                TransitionPathCalculator.calculateTransitionPath(
                        stateAncestorMap.get(currentState),
                        stateAncestorMap.get(actualTargetState));

        deactivateTransitions();
        exit(currentState);
        exitStates(path.getStatesToExit());
        enterStates(path.getStatesToEnter());
        enter(actualTargetState);
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

    // http://stackoverflow.com/questions/27870136/java-lambda-stream-distinct-on-arbitrary-key
    private static <T> Predicate<T> distinctByKey(Function<? super T,Object> keyExtractor) {
        Map<Object,Boolean> seen = new HashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    private void activateTransitions() {
        Stream<State> statesWhosTransitionsToActivate
                = Stream.concat(Stream.of(currentState),
                                stateAncestorMap.get(currentState).stream());

		List<Transition> toActivate
                = statesWhosTransitionsToActivate
                    .filter(state -> transitions.get(state) != null)
                    .flatMap(state -> transitions.get(state).stream())
                    .collect(Collectors.toList());

		if (toActivate != null && !toActivate.isEmpty()) {
			List<Observable<State>> observablesToSubscribeTo
				= toActivate
					.stream()
                    // Filter out those observables who's event is already handled by another observable.
                    // This is to handle "overriding" of event handling (ultimate hook pattern)
                    .filter(distinctByKey(transition -> transition.event()))
					.map(transition -> transition.observable())
					.collect(Collectors.toList());

			Subscription s = Observable
				.merge(observablesToSubscribeTo)
				.subscribe(newState -> switchState(newState));

			transitionsSubscriptions.add(s);
		}
	}

    private void deactivateTransitions() {
        transitionsSubscriptions.clear();
    }
}
