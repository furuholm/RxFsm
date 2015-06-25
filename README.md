RxFsm is a hierarchical finite state machine (FSM) library built on top of RxJava. 
Although (not yet) feature complete, the FSM implementation adheres to the [UML state 
machine notation](https://en.wikipedia.org/wiki/UML_state_machine) and strives to 
implement the semantics outlined in "Practical UML Statecharts in C/C++, 2nd 
Edition" by Miro Samek (which is a must read for those interested in the subject).

## Getting started
##### StateBuilder
Let's start by creating a few states using the StateBuilder class.

```Java
  State s1_1 = new StateBuilder().build();
  State s1_2 = new StateBuilder().build();
```

##### Register onEntry and onExit actions
The StateBuilder class allows you to register onEntry and onExit actions which 
are functions that are executed as the state is entered or exited. 

```Java
  // Create a state with onEntry and onExit actions.
  State s1_3 = new StateBuilder()
                .withOnEntry( () -> result.add("enter s1_2") )
                .withOnExit(() -> result.add("exit s1_2"))
                .build();
```

##### Sub states
Sub states are added using the withSubState method

```Java
  // Create a state with three sub states
  State s1 = new StateBuilder()
                .withInitialSubState(s1_1)
                .withSubState(s1_2)
                .withSubState(s1_3)
                .build();
```

When adding sub states you have to provide exactly one initial sub state. The 
initial sub state is the sub state that will be entered if a transition to the
super state is triggered. In the example above a transition to state s1 would
result in a transition sequence where s1 is first being entered followed by s1_1.
At this point both s1 and s1_1 are being active (In an hierarchical FSM, more than 
one state can be active at once). For comparison a transition directly to s1_2 
would result in s1 and s1_2 being active instead.

##### Interal transitions
StateBuilder can also be used to register internal transitions

```Java
  State s2 = new StateBuilder()
                .withInternalTransition(t, (s) -> result.add("t triggered internal transition from s4: " + s))
                .build();
```

An internal transition is a transition that does not cause the current state to
be entered nor exited when triggered.

##### FsmBuilder
Once you have created the states that are to be included in the FSM it is time
to put them to use in an actual FSM.

```Java
    Fsm fsm1 = new FsmBuilder()
        .withInitialState(s1)
        .withTopStates(s1, s2)
        .build();
```

You have to provide an initial state and one or more top states for an Fsm to
be valid. 

##### Register transitions
For an Fsm to do anything interesting you also have to to add some transitions. 
The withTransition method is used to register transitions.

```Java
    Fsm fsm2 = new FsmBuilder()
            .withInitialState(s1)
            .withTopStates(s1, s2)
            .withTransition(s1, s2, t1, s -> result.add("t1 triggered: " + s), s -> s.equals("c"))
            .withTransition(s2, s1, t2, s -> result.add("t2 triggered: " + s))
            .build();
```

The withTransition method takes the following parameters: source state, target state,
the event and an action to invoke as the transition is executed. You can also provide 
a guard that has to evaluate to true for the transition to be allowed. The event is
represented by an Observable that the Fsm will subscribe to when a state from which
the transition can be triggered become active. The Observable will be unsubscribed
as soon as any (non-internal) transition is triggered.

##### Activate the FSM
Finally you need to activate the Fsm.

```Java
    fsm3.activate();
```

This will make the Fsm activate the initial state.

## Examples
See unit tests for examples (FsmTests.java) and an illustration of supported features.

## TODO
- Add guard for internal transitions
- Generate SCXML from FSM
- Transition to history
- Improved documentation
- More examples
- Orthogonal regions

## Contact
You can find me on twitter [@tobiasfuruholm](http://twitter.com/tobiasfuruholm)
