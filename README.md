# Lab 06 - Modelling
## Task 1 - Verifier

For perform this task, an implementation of the Readers and Writers Petri Net is developed using the already specified **DSL**, and a valid name for each place of the net (eg: ChooseAction, ReadyToRead, Reading etc).

The implementation can be found in the path *scala.u06.task1.ReadersWritersPetriNet.scala*

For reach the goals of the task three methods are written for tests three safety propreties of the RW Petri Net: **Mutual Exclusion**, **Reachability** and **Boundedness**.

The overall idea of the three methods is to iterate over all the possible paths of a given lenght and with a given starting configuration and prove that the safety property is never violated.

Of course the lenght of the paths examinated must be big enough for assume that the property is never violated but also for do not let the execution takes to much time.

### Mutual exclusion

```
def isMutuallyExclusive(initialState: MSet[Place], depth: Int): Boolean =
    val statesMutualExclusion: Seq[Boolean] =
      for
        p <- pnRW.paths(initialState, depth)
        s <- p
      yield s.diff(MSet(Reading, Writing)).size == s.size - 2 || s.diff(MSet(Writing, Writing)).size == s.size - 2

    !statesMutualExclusion.contains(true)
```

The idea for the mutual exclusion is to iterate in all the possible path with a given depth and for each state of the net perform a difference between multiset that include conditions that violates the mutual exclusion
