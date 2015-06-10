import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import rxfsm.*;
import rx.subjects.*;

public class FsmTests {

    @Test
    public void switchState() {
        List<String> result = new ArrayList<String>();
        State s1 = new State()
            .withOnEnter( () -> result.add("enter s1") )
            .withOnExit(() -> result.add("exit s1"));
        State s2 = new State();

        BehaviorSubject<String> o1 = BehaviorSubject.create();

        FsmBuilder builder = new FsmBuilder(s1)
                .withTransition(s1, s2, o1, () -> result.add("t1"));

        Fsm fsm = builder.build();
        fsm.activate();

        o1.onNext("");

        List<String> expected = new ArrayList<String>();
        expected.add("enter s1");
        expected.add("t1");
        expected.add("exit s1");

        assertEquals(expected, result);
    }

    @Test
    public void simpleFsm() {

        List<String> result = new ArrayList<String>();
        State s1 = new State()
            .withOnEnter( () -> result.add("enter s1") )
            .withOnExit(() -> result.add("exit s1"));
        State s2 = new State()
            .withOnEnter( () -> result.add("enter s2") )
            .withOnExit(() -> result.add("exit s2"));

        PublishSubject<String> o1 = PublishSubject.create();
        PublishSubject<String> o2 = PublishSubject.create();

        FsmBuilder builder = new FsmBuilder(s1)
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

        // auto secondState =
        //     FsmState("second")
        //     .withOnEntry([&result]()
        //                  {
        //                      result.push_back("entering second state");
        //                  })
        //     .withOnExit([&result]()
        //                 {
        //                     result.push_back("exiting second state");
        //                 });

        // Fsm fsm = new Fsm(s1).

        // auto fsm = FsmBuilder(firstState)
        //     .withTransition(firstState, secondState, trigger1, "trigger1")
        //     .withTransition(secondState, firstState, trigger2, "trigger2")
        //     .build();

        // fsm.activate();
        // observer1.onNext(true);
        // observer2.onNext(true);
        // observer2.onNext(true);

        // std::vector<std::string> expected = {"entering first state",
        //                                      "exiting first state",
        //                                      "entering second state",
        //                                      "exiting second state",
        //                                      "entering first state"};

        // EXPECT_THAT(result, ContainerEq(expected));
    }

}
