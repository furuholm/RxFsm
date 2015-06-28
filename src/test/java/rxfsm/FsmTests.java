package rxfsm;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.*;

import rx.subjects.*;

public class FsmTests {

    @Test
    public void switchStates() {

        List<String> result = new ArrayList<String>();

        PublishSubject<String> t1 = PublishSubject.create();
        PublishSubject<String> t2 = PublishSubject.create();

        State s1 = new State("s1")
            .withOnEntry(() -> result.add("enter s1"))
            .withOnExit(() -> result.add("exit s1"))
            .withTransition("/s2", t1, s -> result.add("t1 triggered: " + s));

        State s2 = new State("s2")
            .withOnEntry(() -> result.add("enter s2"))
            .withOnExit(() -> result.add("exit s2"))
            .withTransition("/s1", t2, s -> result.add("t2 triggered: " + s));

        Fsm fsm = Fsm.create().withInitialState("/s1").withTopStates(s1, s2);

        fsm.activate();

        t1.onNext("a");
        t2.onNext("b");

        List<String> expected = new ArrayList<String>();
        expected.add("enter s1");
        expected.add("t1 triggered: a");
        expected.add("exit s1");
        expected.add("enter s2");
        expected.add("t2 triggered: b");
        expected.add("exit s2");
        expected.add("enter s1");

        assertEquals(expected, result);
    }

    @Test
    public void switchSubStates() {
        List<String> result = new ArrayList<String>();

        PublishSubject<String> t1 = PublishSubject.create();
        PublishSubject<String> t2 = PublishSubject.create();
        PublishSubject<String> t3 = PublishSubject.create();
        PublishSubject<String> t4 = PublishSubject.create();
        PublishSubject<String> t5 = PublishSubject.create();

        State s1_1 = new State("s1_1")
                .withOnEntry( () -> result.add("enter s1.1") )
                .withOnExit(() -> result.add("exit s1.1"))
                .withTransition("/s2/s2_2", t1, s -> result.add("t1 triggered: " + s));

        State s1_2 = new State("s1_2")
                .withOnEntry(() -> result.add("enter s1.2") )
                .withOnExit(() -> result.add("exit s1.2"))
                .withInternalTransition(
                        t5, s -> result.add("t5 triggered internal transition from s1_2: " + s), s -> s.equals("i"));

        State s1 = new State("s1")
                .withOnEntry(() -> result.add("enter s1") )
                .withOnExit(() -> result.add("exit s1"))
                .withInternalTransition(t4, s -> result.add("t4 triggered internal transition from s1: " + s))
                .withInternalTransition(t5, s -> result.add("t5 triggered internal transition from s1: " + s))
                .withInitialSubState(s1_1)
                .withSubState(s1_2);

        State s2 = new State("s2")
                .withOnEntry(() -> result.add("enter s2") )
                .withOnExit(() -> result.add("exit s2"))
                .withInitialSubState(
                    new State("s2_1")
                            .withOnEntry( () -> result.add("enter s2.1") )
                            .withOnExit(() -> result.add("exit s2.1")))
                .withSubState(
                    new State("s2_2")
                            .withOnEntry( () -> result.add("enter s2.2") )
                            .withOnExit(() -> result.add("exit s2.2"))
                            .withTransition("/s2/s2_1", t2, s -> result.add("t2 triggered: " + s), s -> s.equals("c"))
                            .withTransition("/s1/s1_2", t3, s -> result.add("t3 triggered from s2.2: " + s)))
                .withTransition("/s1", t3, s -> result.add("t3 triggered from s2: " + s));


        Fsm fsm = Fsm.create()
            .withInitialState("/s1")
            .withTopStates(s1, s2);

        fsm.activate();

        // Should transition to s2_2
        t1.onNext("a");
        // Should not transition since guard function will not be evaluated to true
        t2.onNext("b");
        // Should transition to s2_1
        t2.onNext("c");
        // Should transition to s1_1 since s2 has registered a transition to s1
        // (which should result in a transition to its initial sub state s1_1)
        t3.onNext("d");
        // Should transition to s2_2
        t1.onNext("e");
        // Should transition to s1_2 since s2_2 has "overridden" t3
        t3.onNext("f");
        // Should trigger internal transition
        t4.onNext("g");
        // Should trigger internal transition from s1_2, which should "override" internal transition in s1
        t5.onNext("h");
        // Should trigger not internal transition since guard will fail
        t5.onNext("i");

        List<String> expected = new ArrayList<String>();
        expected.add("enter s1");
        expected.add("enter s1.1");
        // t1
        expected.add("t1 triggered: a");
        expected.add("exit s1.1");
        expected.add("exit s1");
        expected.add("enter s2");
        expected.add("enter s2.2");
        // t2
        expected.add("t2 triggered: c");
        expected.add("exit s2.2");
        expected.add("enter s2.1");
        // t3
        expected.add("t3 triggered from s2: d");
        expected.add("exit s2.1");
        expected.add("exit s2");
        expected.add("enter s1");
        expected.add("enter s1.1");
        expected.add("t1 triggered: e");
        expected.add("exit s1.1");
        expected.add("exit s1");
        expected.add("enter s2");
        expected.add("enter s2.2");
        expected.add("t3 triggered from s2.2: f");
        expected.add("exit s2.2");
        expected.add("exit s2");
        expected.add("enter s1");
        expected.add("enter s1.2");
        // t4
        expected.add("t4 triggered internal transition from s1: g");
        // t5
        expected.add("t5 triggered internal transition from s1_2: i");

        assertEquals(expected, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void topStatesAreRequired() {
        State s = new State("s");
        Fsm.create().withInitialState("s").activate();
    }

}
