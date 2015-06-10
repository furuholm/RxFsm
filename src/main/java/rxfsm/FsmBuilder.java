package rxfsm;

import rx.Observable;
import rx.functions.Action0;

import java.util.List;

public class FsmBuilder {
    private State initialState;
    private List<Transition> transitions;

    public FsmBuilder(State initalState){
        this.initialState = initalState;
    }

    public Fsm build()
    {
        return new Fsm(initialState, transitions);
    }

    public <T> FsmBuilder withTransition(State source, State target, Observable<T> event, Action0 action)
    {
        transitions.add(Transition.create(event, source, target, action));
        return this;
    }

    //private class
}
