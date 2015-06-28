package rxfsm;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TransitionPathCalculatorTests {

    @Test
    public void calculateTransitionPath() {
        State s1 = new State("s1");
        State s1_1 = new State("s1_1");
        State s1_1_1 = new State("s1_1_1");
        State s2 = new State("s2");
        State s2_1 = new State("s2_1");

        List<State> stateConfig1 = new ArrayList<State>(Arrays.asList(s1));
        List<State> stateConfig2 = new ArrayList<State>(Arrays.asList(s2));
        List<State> stateConfig3 = new ArrayList<State>(Arrays.asList(s1, s1_1, s1_1_1));
        List<State> stateConfig4 = new ArrayList<State>(Arrays.asList(s2, s2_1));

        List<State> emptyList = new ArrayList<State>();
        TransitionPath emptyTransitionPath = new TransitionPath(emptyList, emptyList);

        // Transition to self should not yield any entry or exit states
        TransitionPath path1 = TransitionPathCalculator.calculateTransitionPath(stateConfig1, stateConfig1);
        assertEquals(emptyList, path1.getStatesToEnter());
        assertEquals(emptyList, path1.getStatesToExit());

        // Transition between two top states should yield exit from first state and entry to the other
        TransitionPath path2 = TransitionPathCalculator.calculateTransitionPath(stateConfig1, stateConfig2);
        assertEquals(Arrays.asList(s1), path2.getStatesToExit());
        assertEquals(Arrays.asList(s2), path2.getStatesToEnter());

        // Transition between two nested states with different
        // top states should yield an exit list from the bottom
        // to top of the source config and entry list from top
        // to bottom of the target state config
        TransitionPath path3 = TransitionPathCalculator.calculateTransitionPath(stateConfig3, stateConfig4);
        assertEquals(Arrays.asList(s1_1_1, s1_1, s1), path3.getStatesToExit());
        assertEquals(Arrays.asList(s2, s2_1), path3.getStatesToEnter());
    }

}
