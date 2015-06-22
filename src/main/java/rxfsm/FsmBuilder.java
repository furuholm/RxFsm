package rxfsm;

import rx.Observable;
import rx.functions.Action0;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

        return new Fsm(initialState, transitions, topStates);
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
}
