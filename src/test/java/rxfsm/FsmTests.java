import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.*;

import rxfsm.*;
import rx.subjects.*;

public class FsmTests {

    @Test
    public void switchState() {
        List<String> result = new ArrayList<String>();
        State s1 = new StateBuilder()
            .withOnEnter( () -> result.add("enter s1") )
            .withOnExit(() -> result.add("exit s1"))
            .build();
        State s2 = new StateBuilder().build();

        BehaviorSubject<String> o1 = BehaviorSubject.create();

        FsmBuilder builder = new FsmBuilder(s1)
                .withTransition(s1, s2, o1, () -> result.add("t1"));

        Fsm fsm = builder
                .withTopStates(s1)
                .build();
        fsm.activate();

        o1.onNext("");

        List<String> expected = new ArrayList<String>();
        expected.add("enter s1");
        expected.add("t1");
        expected.add("exit s1");

        assertEquals(expected, result);
    }

    @Test
    public void switchMoreStates() {

        List<String> result = new ArrayList<String>();

        State s1 = new StateBuilder()
            .withOnEnter( () -> result.add("enter s1") )
            .withOnExit(() -> result.add("exit s1"))
            .build();

        State s2 = new StateBuilder()
            .withOnEnter( () -> result.add("enter s2") )
            .withOnExit(() -> result.add("exit s2"))
            .build();

        PublishSubject<String> o1 = PublishSubject.create();
        PublishSubject<String> o2 = PublishSubject.create();

        FsmBuilder builder = new FsmBuilder(s1)
                .withTopStates(s1, s2)
                .withTransition(s1, s2, o1, () -> result.add("t1 triggered"))
                .withTransition(s2, s1, o2, () -> result.add("t2 triggered"));

        Fsm fsm = builder.build();
        fsm.activate();

        o1.onNext("");
        o2.onNext("");

        List<String> expected = new ArrayList<String>();
        expected.add("enter s1");
        expected.add("t1 triggered");
        expected.add("exit s1");
        expected.add("enter s2");
        expected.add("t2 triggered");
        expected.add("exit s2");
        expected.add("enter s1");

        assertEquals(expected, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void topStatesAreRequired() {
        State s = new StateBuilder().build();
        new FsmBuilder(s).build();
    }

    @Test
    public void switchSubState() {
        List<String> result = new ArrayList<String>();

        State s1_1 = new StateBuilder()
                .withOnEnter( () -> result.add("enter s1.1") )
                .withOnExit(() -> result.add("exit s1.1"))
                .build();

        State s1_2 = new StateBuilder()
                .withOnEnter( () -> result.add("enter s1.2") )
                .withOnExit(() -> result.add("exit s1.2"))
                .build();

        State s1 = new StateBuilder()
                .withOnEnter( () -> result.add("enter s1") )
                .withOnExit(() -> result.add("exit s1"))
                .withInitialSubState(s1_1)
                .withSubState(s1_2)
                .build();

        State s2_1 = new StateBuilder()
                .withOnEnter( () -> result.add("enter s2.1") )
                .withOnExit(() -> result.add("exit s2.1"))
                .build();

        State s2_2 = new StateBuilder()
                .withOnEnter( () -> result.add("enter s2.2") )
                .withOnExit(() -> result.add("exit s2.2"))
                .build();

        State s2 = new StateBuilder()
                .withOnEnter( () -> result.add("enter s2") )
                .withOnExit(() -> result.add("exit s2"))
                .withInitialSubState(s2_1)
                .withSubState(s2_2)
                .build();


        PublishSubject<String> t1 = PublishSubject.create();
        PublishSubject<String> t2 = PublishSubject.create();
        PublishSubject<String> t3 = PublishSubject.create();

        FsmBuilder builder = new FsmBuilder(s1)
                .withTopStates(s1, s2)
                .withTransition(s1_1, s2_2, t1, () -> result.add("t1 triggered"))
                .withTransition(s2_2, s2_1, t2, () -> result.add("t2 triggered"))
                .withTransition(s2, s1, t3, () -> result.add("t3 triggered from s2"))
                .withTransition(s2_2, s1_2, t3, () -> result.add("t3 triggered from s2.2"));

        Fsm fsm = builder.build();
        fsm.activate();

        // Should transition to s2_2
        t1.onNext("");
        // Should transition to s2_1
        t2.onNext("");
        // Should transition to s1_1 since s2 has registered a transition to s1
        // (which should result in a transition to its initial sub state s1_1)
        t3.onNext("");
        // Should transition to s2_2
        t1.onNext("");
        // Should transition to s1_2 since s2_2 has "overridden" t3
        t3.onNext("");

        List<String> expected = new ArrayList<String>();
        expected.add("enter s1");
        expected.add("enter s1.1");
        // t1
        expected.add("t1 triggered");
        expected.add("exit s1.1");
        expected.add("exit s1");
        expected.add("enter s2");
        expected.add("enter s2.2");
        // t2
        expected.add("t2 triggered");
        expected.add("exit s2.2");
        expected.add("enter s2.1");
        // t3
        expected.add("t3 triggered from s2");
        expected.add("exit s2.1");
        expected.add("exit s2");
        expected.add("enter s1");
        expected.add("enter s1.1");
        expected.add("t1 triggered");
        expected.add("exit s1.1");
        expected.add("exit s1");
        expected.add("enter s2");
        expected.add("enter s2.2");
        expected.add("t3 triggered from s2.2");
        expected.add("exit s2.2");
        expected.add("exit s2");
        expected.add("enter s1");
        expected.add("enter s1.2");

        assertEquals(expected, result);
    }

    @Test
    public void testCompleteHSM() {
        // Test Miro's HSM example

    }

}
