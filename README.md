RxFsm is a hierarchical finite state machine (FSM) library built on top of RxJava.
Although not (yet) feature complete, the FSM implementation adheres to the [UML state
machine notation](https://en.wikipedia.org/wiki/UML_state_machine) and strives to
implement the semantics outlined in "Practical UML Statecharts in C/C++, 2nd
Edition" by Miro Samek (which is a must read for those interested in the subject).

## Status
This library is under development.

## Getting started
##### Creating an FSM
Let's start by creating an FSM with a couple of states.

```Java
  Fsm fsm = Fsm.create()
      .withInitialState("/s1")
      .withTopStates(
          new State("s1")
          .withTransition("/s2", t1, s -> result.add("t1 triggered: " + s)),
          new State("s2")
          .withTransition("/s1", t2, s -> result.add("t2 triggered: " + s)));
```

The Fsm consists of one or more top states that can have transitions, actions
and sub states. An initial state always have to be specified as well.

The withTransition method is used to register transitions. Transitions
are specified using the path of the target state.

The withTransition method takes the following parameters: source state, target state,
the event and an action to invoke as the transition is executed. You can also provide
a guard that has to evaluate to true for the transition to be allowed. The event is
represented by an Observable that the Fsm will subscribe to when a state from which
the transition can be triggered become active. The Observable will be unsubscribed
as soon as any (non-internal) transition is triggered.

##### Actions
You can register onEntry and onExit actions which are functions that are
executed as the state is entered or exited.

```Java
  State s1 = new State("s1")
      .withOnEntry(() -> result.add("enter s2"))
      .withOnExit(() -> result.add("exit s2"));
```

##### Sub states
Sub states are added using the withSubState method

```Java
 
  PublishSubject<String> t1 = PublishSubject.create();
  PublishSubject<String> t2 = PublishSubject.create();
  PublishSubject<String> t3 = PublishSubject.create();

  State s2 = new State("s1")
      .withInitialSubState(
          new State("s1_1")
          .withOnEntry( () -> result.add("enter s1.1") )
          .withOnExit(() -> result.add("exit s1.1")))
      .withSubState(
          new State("s1_2")
          .withOnEntry( () -> result.add("enter s1.2") )
          .withOnExit(() -> result.add("exit s1.2"))
          .withTransition("/s1/s1_1", t1, s -> result.add("t1 triggered: " + s), s -> s.equals("c"))
          .withTransition("/s1/s1_2", t2, s -> result.add("t2 triggered from s1.2: " + s)));
```

When adding sub states you have to provide exactly one initial sub state. The
initial sub state is the sub state that will be entered if a transition to the
super state is triggered. In the example above a transition to state s1 would
result in a transition sequence where s1 is first being entered followed by s1_1.
At this point both s1 and s1_1 are being active (In an hierarchical FSM, more than
one state can be active at once). For comparison a transition directly to s1_2
would result in s1 and s1_2 being active instead.

##### Interal transitions
You can also register internal transitions

```Java
  State s1_2 = new State("s1_2")
      .withInternalTransition(
          t5, s -> result.add("t5 triggered internal transition from s1_2: " + s), s -> s.equals("i"));
```

An internal transition is a transition that does not cause the current state to
be entered nor exited when triggered.

##### Activate the FSM
Finally you need to activate the Fsm.

```Java
  fsm.activate();
```

This will make the Fsm activate the initial state.

## Examples
See unit tests for examples (FsmTests.java) and an illustration of supported features.

## TODO
- Clean up code
- Transition to history
- Improved documentation
- More examples
- Orthogonal regions

## Contact
You can find me on twitter [@tobiasfuruholm](http://twitter.com/tobiasfuruholm)
