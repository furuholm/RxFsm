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
    private final HashMap<State, List<Transition>> internalTransitions;
    private final Map<State, List<State>> stateAncestorMap;
    private final List<State> topStates;

	public Fsm(State initialState, List<Transition> transitions, List<Transition> internalTransitions, List<State> topStates) {
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

        this.internalTransitions = new HashMap<State, List<Transition>>();
        for (Transition t: internalTransitions) {
            if (!this.internalTransitions.containsKey(t.source()))
            {
                List<Transition> transitionList = new ArrayList();
                transitionList.add(t);
                this.internalTransitions.put(t.source(), transitionList);
            }
            else
            {
                this.internalTransitions.get(t.source()).add(t);
            }
        }

		this.currentState = null;
		this.transitionsSubscriptions = new CompositeSubscription();
        this.topStates = topStates;
        if (topStates == null || topStates.isEmpty()) {
            throw new IllegalArgumentException("Top states needs to be provided");
        }
        this.stateAncestorMap = generateStateAncestorMap(topStates);;
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

        // Transitions
        List<Observable<State>> observableTransitions
                = generateObservableTransitionList(currentState, stateAncestorMap.get(currentState), transitions);

        if (!observableTransitions.isEmpty()) {
            Subscription s = Observable
                    .merge(observableTransitions)
                    .subscribe(newState -> switchState(newState));

            transitionsSubscriptions.add(s);
        }

        // Internal transitions
        List<Observable<State>> observableInternalTransitions
                = generateObservableTransitionList(currentState, stateAncestorMap.get(currentState), internalTransitions);

        if (!observableInternalTransitions.isEmpty()) {
            Subscription s = Observable
                    .merge(observableInternalTransitions)
                    .subscribe(newState -> {
                        // Do nothing, this subscription is only here to
                        // enable actions to be executed when the transition triggers
                    });

            transitionsSubscriptions.add(s);
        }

    }

    private static List<Observable<State>> generateObservableTransitionList(
            State sourceState, List<State> ancestors, Map<State, List<Transition>> transitionMap) {
        Stream<State> statesWhosTransitionsToActivate
                = Stream.concat(Stream.of(sourceState), ancestors.stream());

        List<Transition> toActivate
                = statesWhosTransitionsToActivate
                .filter(state -> transitionMap.get(state) != null)
                .flatMap(state -> transitionMap.get(state).stream())
                .collect(Collectors.toList());

        if (toActivate != null && !toActivate.isEmpty()) {
            List<Observable<State>> observableTransitions
                    = toActivate
                    .stream()
                            // Filter out those observables who's event is already handled by another observable.
                            // This is to handle "overriding" of event handling (ultimate hook pattern)
                    .filter(distinctByKey(transition -> transition.event()))
                    .map(transition -> transition.observable())
                    .collect(Collectors.toList());

            return observableTransitions;
        }
        else {
            return new ArrayList<Observable<State>>();
        }
    }


        private void deactivateTransitions() {
        transitionsSubscriptions.clear();
    }

    // Generates a map where each state in the FSM is mapped against a list of its ancestors
    private static Map<State, List<State>> generateStateAncestorMap(List<State> topStates) {
        Map<State, List<State>> stateAncestorMap = new HashMap<State, List<State>>();
        for (State state: topStates) {
            stateAncestorMap.putAll(generateStateAncestorMap(state, new ArrayList<State>()));
        }

        return stateAncestorMap;
    }

    private static Map<State, List<State>> generateStateAncestorMap(State state, List<State> ancestors) {
        Map<State, List<State>> stateAncestorMap = new HashMap<State, List<State>>();
        stateAncestorMap.put(state, ancestors);

        List<State> subStateAncestors = new ArrayList<State>(ancestors);
        subStateAncestors.add(state);

        for (State subState: state.getSubStates())
        {
            stateAncestorMap.putAll(generateStateAncestorMap(subState, subStateAncestors));
        }

        return stateAncestorMap;
    }
}
