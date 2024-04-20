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

The code can be found at the path: *u07.modelling.CTMCSimulation.scala* and *u07.examples.StochasticChannelSimulation.scala*

I extracted three API's for perform check and analisys on a generic CTMC, these API are then used for test the required properties.

The first function is necessary for compute the average time at which a CTMC reach a defined state.

```
def averageTimeToReachState(nRun: Int, initialState: S, stateToCheck: S): Double =
      (0 to nRun).foldLeft(0.0)((z, _) => z + self.newSimulationTrace(initialState, new Random)
        .take(10)
        .toList
        .find(e => e.state == stateToCheck).map(e => e.time).getOrElse(0.0)) / nRun
```

The goal is achieved by computing a simulation of the communication n times, and for each time we accumulate the time at which the state that we want to check is reached by the `foldLeft` operator. After that we have the sum of all the times and we compute the average just dividing by the total numbers of run.

The second function is a bit more complex and it computes the percentage of time in which the system respect a defined condition (expressed by f).

```
def relativeTimeInCondition(nRun: Int, initialState: S, f: S => Boolean): Double =
      val totalTimes = (0 to nRun).foldLeft((0.0, 0.0))((acc, _) => {
        val (stateTime, totTime) = self.newSimulationTrace(initialState, new Random)
          .take(10)
          .toList
          .sliding(2)
          .foldLeft((0.0, 0.0))((z, s) => if (f(s(0).state)) (z._1 + (s(1).time - s(0).time), s(1).time) else (z._1, s(1).time))

        (acc._1 + stateTime, acc._2 + totTime)
      })

      totalTimes._1 / totalTimes._2
```

The function generate n runs, and for each run it accumulate a tuple `(conditionTime, totalTime)` by iterating the single simulation and considering a couple of `Event`. If the current event is respecting the condition it accumulate the time by calculating the subtraction between the next event time and the current one. After we have for a single simulation the couple `(conditionTime, totalTime)` we accumulate it with an external `foldLeft` and then we just need to divide the total time with the condition respected and the total time of all the simulations to get the percentage.

The third function is used to check the relative time in which the CTMC stays in a defined state, just by invoking the previous function.

```
def relativeTimeInState(nRun: Int, initialState: S, stateToCheck: S): Double =
      relativeTimeInCondition(nRun, initialState, _ == stateToCheck)
```

Eventually we can use these general API for test the StochasticChannel in this way:

```
println("Avarerage communication done time: " + stocChannel.averageTimeToReachState(10, IDLE, DONE))
println("Relative fail percentage time: " + stocChannel.relativeTimeInState(10, IDLE, FAIL))
```

Obtaining:
- Average communication done time: 1.564279627167613 time units
- Relative fail percentage time: 6.15260578867424E-7 time units

## Task 2 - Guru

In this task an implementation of a Stochastic Readers and Writers Petri Net is developed. The code can be found at: *scala.u07.task2.StochasticRWPN.scala*.

```
val stochasticRWPN = SPN[Place](
    Trn(MSet(Idle), m => 1.0, MSet(ChooseAction), MSet()),
    Trn(MSet(ChooseAction), m => 200000, MSet(ReadyToRead), MSet()),
    Trn(MSet(ChooseAction), m => 100000, MSet(ReadyToWrite), MSet()),
    Trn(MSet(ReadyToRead, HasPermission), m => 100000, MSet(Reading, HasPermission), MSet()),
    Trn(MSet(Reading), m => 0.1 * m(Reading), MSet(Idle), MSet()),
    Trn(MSet(ReadyToWrite, HasPermission), m => 100000, MSet(Writing), MSet(Reading)),
    Trn(MSet(Writing), m => 0.2, MSet(Idle, HasPermission), MSet())
  )
```

After implementing the Petri Net different simulation were performed and the API `relativeTimeInCondition` written in the first task was used for calculating the average time of the net in Reading and Writing state, trying different combinations of rates. All the simulation are executed with **5 tokens** in the **IDLE** state.

| Reading rate | Writing rate | % of time in Reading | % of time in Writing |
|--------------|--------------|----------------------|----------------------|
| 400000       | 100000       | 87.2%                | 9.5%                 |
| 300000       | 100000       | 81.9%                | 12.3%                |
| 200000       | 100000       | 68.5%                | 23.4%                |
| 100000       | 100000       | 50.2%                | 44.0%                |
| 100000       | 200000       | 39.3%                | 54.7%                |
| 100000       | 300000       | 33.0%                | 58.0%                |
| 100000       | 400000       | 22.3%                | 73.0%                |

In this table is shown how the % of time variates according to the rates chosen for the two correspondent transitions. For example obviusly if we put a really high rate on Reading we see that we have 80% of probability that a token goes to the Reading branch and so the total amount of time spent in the Reading state is much higher then the reading one.

Another rate that can be tuned (while keeping fixed the others) is the one that regulate the transition between `Idle` and `ChooseAction` that influence for example the time spent by the net without Reading or Writing.

| Idle rate    | % of time not Reading or Writing |
|--------------|----------------------------------|
| 1.0          | 28.1%                            |
| 0.1          | 56.3%                            |
| 0.01         | 94.2%                            |

By decreasing it we see that we are making the transition really slow and we are making the net spending more time without Reading or Writing

## Task 3 - Chemist

The code of this task can be found at the path: *scala.u07.task3.BrussellatorPN.scala*

Here I modeled the Brussellator chemical model ad the following Stochastic Petri Net:

```
val brussellatorPN = SPN[Place](
    Trn(MSet(), m => 1, MSet(A), MSet()),
    Trn(MSet(), m => 1, MSet(B), MSet()),
    Trn(MSet(A), m => 1, MSet(X), MSet()),
    Trn(MSet(X, X, Y), m =>  m(Y), MSet(X, X, X), MSet()),
    Trn(MSet(B, X), m => m(X) * 0.5, MSet(Y, D), MSet()),
    Trn(MSet(X), m => m(X) * 0.5, MSet(E), MSet())
  )
```

The difficult part of this task is to balance correctly the rates of the transitions for obtaining an oscillation in terms of X and Y. 

First of all regarding the chemical reaction they assume that reagents A and B are constant so we need to model it by the first two transitions that with a certain probability are producing the reagents.

Then the best balance found for the rates follows these ideas:

- Let the rate of the transition that consume Y and produce X depends on the number of Y, so if the net contains a lot of Y the rate is bigger and viceversa.
- Let the rates of the transitions that consume X depends on the numer of X, so if the net contains a lot of X the rate is bigger and viceversa.
- Given the Brussellator has two transitions that consume X and only one that consume Y, both the transitions that consumes X are multiplied by 0.5.

By using these ideas we obtain the following result:

![Brussellator graph](https://github.com/Filippo-Venturini/asmd23-public-models/blob/master/Brussellator.png)

# Lab 08 - Stochastic Analysis

## Task 1 - PRISM

In this task we used PRISM Model Checker for verify some propreties of the Readers and Writers Stochastic Petri Net.

First of all this is the model used for the simulations:

```
ctmc
const int N = 20;
module RW
p1 : [0..N] init N;
p2 : [0..N] init 0;
p3 : [0..N] init 0;
p4 : [0..N] init 0;
p5 : [0..N] init 1;
p6 : [0..N] init 0;
p7 : [0..N] init 0;
[t1] p1>0 & p2<N -> 1 : (p1'=p1-1)&(p2'=p2+1);
[t2] p2>0 & p3<N -> 200000 : (p2'=p2-1) & (p3'=p3+1);
[t3] p2>0 & p4<N -> 100000 : (p2'=p2-1) & (p4'=p4+1);
[t4] p3>0 & p5>0 & p6<N -> 100000 : (p3'=p3-1) & (p6'=p6+1);
[t5] p4>0 & p5>0 & p6=0 & p7<N -> 100000 : (p4'=p4-1) & (p5'=p5-1) & (p7'=p7+1);
[t6] p6>0 & p1<N -> p6*1 : (p6'=p6-1) & (p1'=p1+1);
[t7] p7>0 & p5<N & p1<N -> 0.5 : (p7'=p7-1) & (p1'=p1+1) & (p5'=p5+1);
endmodule
```

We tried to investigate the probability that in k steps at least one process will be able to read:

```
P=? [(true) U<=k (p6>0)]
```

And plotting a graphic this is the trend of the probability in 10 steps:


Then we performed the same investigation but considering the probability that at least one process will be able to write:

```
P=? [(true) U<=k (p7>0)]
```

And this is the trend of the probability in 10 steps:


So we can notice that of course, in the write graphic the curve is less steep, because of the rates assigned.

Then another property that is interesting to verify is that the mutual exclusion is never violated:

```
 P=? [(true) U<=k (p6>0) & (p7>0)]
```

And using the **verify** option of PRISM we obtain the following result:


