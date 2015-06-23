package rxfsm;

import rx.Observable;
import rx.functions.Action1;

import rxfsm.State;

public class Transition {

    private final State source;
    private final State target;
	// private final String name;
    private final Object event;
	private final Observable<State> observable;

	public final static <T> Transition create(Observable<T> event, State source, State target, Action1<T> action) {
		Observable<State> o = event.map((T t) -> {
			action.call(t);
			return target;
		});
		return new Transition(event, source, target, o);
	}

	private <T> Transition(Observable<T> event, State source, State target, Observable<State> o) {
		this.target = target;
        this.source = source;
		// this.name = null;
		this.observable = o;
        this.event = event;
    }

    State source() { return source; }

	State target() {
		return target;
	}

	Observable<State> observable() {
		return observable;
	}

    Object event() {
        return this.event;
    }
}