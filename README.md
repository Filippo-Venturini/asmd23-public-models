# Lab 06 - Modelling
## Task 1 - Verifier

For perform this task, an implementation of the Readers and Writers Petri Net is developed using the already specified **DSL**, and a valid name for each place of the net (eg: ChooseAction, ReadyToRead, Reading etc).

The implementation can be found in the path *scala.u06.task1.ReadersWritersPetriNet.scala*

For reach the goals of the task three **API** are written for tests three safety propreties of **any** Petri Nets: **Mutual Exclusion**, **Reachability** and **Boundedness**.

The overall idea of the three methods is to iterate over all the possible paths of a given lenght and with a given starting configuration and prove that the safety property is never violated.

Of course the lenght of the paths examinated must be big enough for assume that the property is never violated but also for do not let the execution takes to much time.

### Mutual exclusion

**Def:** In all the possible path of a given length there must not be one token in Reading and one token in Writing or two or more token in Writing.

```
def isMutuallyExclusive(initialState: Marking[P], depth: Int, criticalStates: MSet[P]*): Boolean =
      (for
        p <- pn.toSystem.paths(initialState, depth)
        s <- p
      yield criticalStates.forall(criticalPlaces => s.diff(criticalPlaces).size != s.size - 2)).reduce(_ && _)
```

The idea for verify the mutual exclusion property is the following:
- iterate in all the possible path with a given depth
- get each state of the net in a single path
- check if any state reached contains the critical states that violates the mutual exclusion (In case of the RW net two Writers or Readers and Writers)
- then all the states examinated should respect the condition

### Reachability

  **Def:** Given all the possible paths of a given length, every state must be reached **at least one time**.
```
def isReachable(initialState: Marking[P], depth: Int): Boolean =
      (for
        path <- pn.toSystem.paths(initialState, depth)
        state <- path
        place <- state.asList
      yield place).toSet == Place.values.toSet
```

Also here the idea is to iterate all the paths and the states, but in this case we are accumulating all the places encountered. Then we convert the Sequence into a Set and we check that all the States are reached by using all the values present in the enumeration.

### Boundedness

**Def:** Given all the possible path of a given length, the number of tokens in each place remains bounded, preventing resource exhaustion or overflow.

```
def isBounded(initialState: Marking[P], depth: Int, maxTokenInPN: Int): Boolean =
      (for
        path: Path[Marking[P]] <- pn.toSystem.paths(initialState, depth)
        state <- path
      yield state.size <= maxTokenInPN).reduce(_ && _)
```

In this solution we iterate over all the states and we check that the total number of the token in each state is less equal than the maximum possible number of the tokens in the net. Note that this solution is correct only if we apply it to a Readers and Writers Petri Net, because we know that in this type of nets no more tokens are generated after the initial configuration, so we can know exactly the max possible amount of tokens (k + HasPermission). For others Petri Nets we need to have a boundary to take into account for check if the number of tokens is increasing to infinite or not.

At the path *package scala.u06.task1.ReadersWritersPetriNetTest.scala* a simple test is performed using `ScalaTest` for verify all the priorities with a given starting configuration and a given depth.

We limited the length of the paths taken into account at 10, because with longest paths the execution requires a lot of time.

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

# Lab 07 - Stochastic Modelling

## Task 1 - Simulator

In this task we wrote two functions for compute some analysis on the `StochasticChannel`.

The code can be found at the path: *package u07.examples.StochasticChannelSimulation.scala*

The first function is necessary for compute the avarage time at which the communication is done across n runs.

```
def averageCommunicationDoneTime(nRun: Int): Double =
  (0 to nRun).foldLeft(0.0)((z, _) => z + stocChannel.newSimulationTrace(IDLE, new Random)
                                            .take(10)
                                            .toList
                                            .find(e => e.state == DONE).map(e => e.time).getOrElse(0.0)) / nRun
```

The goal is achieved by computing a simulation of the communication n times, and for each time we accumulate the time at which the state `DONE` is reached by the `foldLeft` operator. After that we have the sum of all the times and we compute the average just dividing by the total numbers of run.

The second function is a bit more complex and it computes the percentage of time in which the system stay in the `FAIL` state until it succesfully end the communication.
