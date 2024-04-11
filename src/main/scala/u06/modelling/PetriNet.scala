package pc.modelling

import pc.utils.MSet

import scala.collection.SortedSet
import scala.collection.immutable.TreeSet
import scala.u06.task1.ReadersWritersPetriNet.{Place, pnRWPriorities}

object PetriNet:

  // pre-conditions, effects, inhibition, priority
  case class Trn[P](cond: MSet[P], eff: MSet[P], inh: MSet[P], priority: Int = 1)
  type PetriNet[P] = Set[Trn[P]]
  type Marking[P] = MSet[P]

  // factory of A Petri Net
  def apply[P](transitions: Trn[P]*): PetriNet[P] = transitions.toSet

  // factory of a System, as a toSystem method
  extension [P](pn: PetriNet[P])
    def toSystem: System[Marking[P]] = m =>
      val allTransitions =
        for
          Trn(cond, eff, inh, priority) <- pn   // get any transition
          if m disjoined inh          // check inhibition
          out <- m extract cond       // remove precondition
        yield (priority, out union eff)

      val maxPriority = allTransitions.map(_._1).max
      allTransitions.filter((p, _) => p == maxPriority).map(_._2)

  // fancy syntax to create transition rules
  extension [P](self: Marking[P])
    def ~~> (y: Marking[P]) = Trn(self, y, MSet())
  extension [P](self: Trn[P])
    def ^^^ (z: Marking[P]) = self.copy(inh = z)
    def priority(p: Int) = self.copy(priority = p)