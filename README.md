# Lab 06 - Modelling
## Task 1 - Verifier

For perform this task, an implementation of the Readers and Writers Petri Net is developed using the already specified **DSL**, and a valid name for each place of the net (eg: ChooseAction, ReadyToRead, Reading etc).

The implementation can be found in the path *scala.u06.task1.ReadersWritersPetriNet.scala*

For reach the goals of the task three methods are written for tests three safety propreties of the RW Petri Net: **Mutual Exclusion**, **Reachability** and **Boundedness**.

The overall idea of the three methods is to iterate over all the possible paths of a given lenght and with a given starting configuration and prove that the safety property is never violated.

Of course the lenght of the paths examinated must be big enough for assume that the property is never violated but also for do not let the execution takes to much time.

### Mutual exclusion

**Def:** In all the possible path of a given length there must not be one token in Reading and one token in Writing or two or more token in Writing.

```
def isMutuallyExclusive(initialState: MSet[Place], depth: Int): Boolean =
    (for
        p <- pnRW.paths(initialState, depth)
        s <- p
      yield s.diff(MSet(Reading, Writing)).size != s.size - 2 && s.diff(MSet(Writing, Writing)).size != s.size - 2).reduce(_ && _)
```

The idea for verify the mutual exclusion property is the following:
- iterate in all the possible path with a given depth
- get each state of the net in a single path
- check the two condition that violates the mutual exclusion (multiple writers or reader and writer) by checking the size of the difference between the actual state and the multiset of the wrong condition
- then all the states examinated should respect the condition

### Reachability

  **Def:** Given all the possible paths of a given length, every state must be reached **at least one time**.
```
def isReachable(initialState: MSet[Place], depth: Int): Boolean =
    (for
      path <- pnRW.paths(initialState, depth)
      state <- path
      place <- state.asList
    yield place).toSet == Place.values.toSet
```

Also here the idea is to iterate all the paths and the states, but in this case we are accumulating all the places encountered. Then we convert the Sequence into a Set and we check that all the States are reached by using all the values present in the enumeration.

### Boundedness

**Def:** Given all the possible path of a given length, the number of tokens in each place remains bounded, preventing resource exhaustion or overflow.

```
def isBounded(initialState: MSet[Place], depth: Int): Boolean =
    (for
      path: Path[Marking[Place]] <- pnRW.paths(initialState, depth)
      state <- path
    yield state.size <= maxTokenInPN(initialState)).reduce(_ && _)
```

In this solution we iterate over all the states and we check that the total number of the token in each state is less equal than the maximum possible number of the tokens in the net. Note that this solution is correct only if we apply it to a Readers and Writers Petri Net, because we know that in this type of nets no more tokens are generated after the initial configuration, so we can know exactly the max possible amount of tokens (k + HasPermission)

At the path *package scala.u06.task1.ReadersWritersPetriNetTest.scala* a simple test is performed using `ScalaTest` for verify all the priorities with a given starting configuration and a given depth.

## Task 2 - Artist

In this task the implementation of the Petri Net is extended by adding the concepts of priority and colors or the tokens and the transitions.

The code can be found at the path *scala.u06.modelling.ExtendedPetriNet.scala*.

### Priorities

Here the idea is to add to the Trn case class an Int field for the priority, with the default value at 1 for so if the user doesn't want to use priorities everything works as before.

```
case class Trn[P](cond: MSet[P], eff: MSet[P], inh: MSet[P], priority: Int = 1)
```

Then we have to modify the toSystem method for make it fire only the transitions with the highest priority.

```
def toSystem: System[Marking[P]] = m =>
      val allTransitions =
        for
          Trn(cond, eff, inh, priority) <- pn   // get any transition
          if m disjoined inh          // check inhibition
          out <- m extract cond       // remove precondition
        yield (priority, out union eff)

      val maxPriority = allTransitions.map(_._1).max
      allTransitions.filter((p, _) => p == maxPriority).map(_._2)
```

The idea here is to keep the same behaviour as before but extracting the all the priorities from the transitions. Then the max priority is found so the method can filter and keep only the transitions with the biggest priority. 

Then of course a new operator is added to the DSL for enabling the using of the priority with the DSL's notation and a creation of a transition with a certain amount of priority looks like that:

```
MSet(*(ChooseAction)) ~~> MSet(*(ReadyToRead)) priority 5,
```

For check the behaviur we can assign an higher priority in the branch of the reading part and as expected the tokens keeps just reading:

```
List({*(HasPermission,Black)|*(Idle,Black)}, {*(HasPermission,Black)|*(ChooseAction,Black)}, {*(HasPermission,Black)|*(ReadyToRead,Black)}, {*(HasPermission,Black)|*(Reading,Black)}, {*(HasPermission,Black)|*(Idle,Black)})
```

### Colors

We want to extend the model of the Petri Net by adding the possibility to have colored tokens (eg: Black or Red) and each transition also can accept only tokens of a certain color and when fired change the color of the token that goes through.

For do that an enum for colors is created and than a new case class represent the couple (place, color):

```
@targetName("Token")
  case class *[P](place: P, color: Color = Color.Black)
```

Note that the default color is Black so we're still able to design an extended Petri Net just with priorities, without using colors.

So now when we create a new PetriNet the istantiation of a transition looks like that:

```
MSet(*(Idle, Red)) ~~> MSet(*(ChooseAction, Red)),
```

This means that the transition goes from **Idle** place to **ChooseActionPlace**, accept only red tokens and the token keep being red. For change the color of the token is enough to change the color of the second *.

For demonstrate the correct behaviour we can print all the possible path with depth 5, in a PetriNet that has Red transitions only in the red branch and if we use just one red token we obtain the following path in which it keeps just reading:

```
List({*(HasPermission,Black)|*(Idle,Red)}, {*(HasPermission,Black)|*(ChooseAction,Red)}, {*(HasPermission,Black)|*(ReadyToRead,Red)}, {*(HasPermission,Black)|*(Reading,Red)}, {*(HasPermission,Black)|*(Idle,Red)})
```

