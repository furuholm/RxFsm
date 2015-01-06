import static org.junit.Assert.assertEquals;

import org.junit.Test;

import rxfsm.*;
import rx.subjects.*;

public class FsmTests {

	@Test
	public void switchState() {
		State s1 = new State();
		State s2 = new State();
		s1.setOnEnter( () -> System.out.println("s1"));

		BehaviorSubject<String> o1 = BehaviorSubject.create();

		Transition t1 = Transition.create(o1, s1, ( () -> System.out.println("trans1")));

		assertEquals("one", "one");
	}
}