package rxfsm;

import rx.Observable;
import rx.functions.Action0;

import rxfsm.State;

public class Transition {
	
	private final State target;
	// private final String name;
	private final Observable<State> observable;

	public final static <T> Transition create(Observable<T> event, State target, Action0 action) {
		Observable<State> o = event.map((T t) -> {
			action.call();
			return target;
		});
		return new Transition(target, o);
	}

	private Transition(State target, Observable<State> o) {
		this.target = target;
		// this.name = null;
		this.observable = o;
	}

	State target() {
		return target;
	}

	Observable<State> observable() {
		return observable;
	}
}