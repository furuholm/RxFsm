package rxfsm;

import rx.Observable;
import rx.functions.Action0;

import java.util.*;

public class FsmBuilder {
    private final State initialState;
    private final List<Transition> transitions;
    private List<State> topStates;

    public FsmBuilder(State initalState){
        this.initialState = initalState;
        this.transitions = new ArrayList<Transition>();
        this.topStates = null;
    }

    public Fsm build()
    {
        if (topStates == null || topStates.isEmpty()) {
            throw new IllegalArgumentException("Top states needs to be provided");
        }

        Map<State, List<State>> stateAncestorMap = generateStateAncestorMap(topStates);
        return new Fsm(initialState, transitions, topStates, stateAncestorMap);
    }

    public FsmBuilder withTopStates(State... topStates) {
        if (this.topStates != null) {
            throw new IllegalArgumentException("Top states can only be declared once");
        }

        return new FsmBuilder(initialState, transitions, Arrays.asList(topStates));
    }

    public <T> FsmBuilder withTransition(State source, State target, Observable<T> event, Action0 action)
    {
        List<Transition> newTransitions = new ArrayList<Transition>(transitions);
        newTransitions.add(Transition.create(event, source, target, action));
        return new FsmBuilder(initialState, newTransitions, topStates);
    }

    private FsmBuilder(State initialState, List<Transition> transitions, List<State> topStates) {
        this.initialState = initialState;
        this.transitions = transitions;
        this.topStates = topStates;
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
