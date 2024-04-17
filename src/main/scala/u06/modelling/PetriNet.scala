package u06.modelling

import u06.utils.MSet

import scala.u06.task1.ReadersWritersPetriNet.{Place, pnRW}
import u06.modelling.SystemAnalysis.{Path, paths}

object PetriNet:
  // pre-conditions, effects, inhibition
  case class Trn[P](cond: MSet[P], eff: MSet[P], inh: MSet[P])
  type PetriNet[P] = Set[Trn[P]]
  type Marking[P] = MSet[P]

  // factory of A Petri Net
  def apply[P](transitions: Trn[P]*): PetriNet[P] = transitions.toSet
  // factory of a System, as a toSystem method
  extension [P](pn: PetriNet[P])
    def toSystem: System[Marking[P]] = m =>
      for
        Trn(cond, eff, inh) <- pn   // get any transition
        if m disjoined inh          // check inhibition
        out <- m extract cond       // remove precondition
      yield out union eff           // add effect

    def isMutuallyExclusive(initialState: Marking[P], depth: Int, criticalStates: MSet[P]*): Boolean =
      (for
        p <- pn.toSystem.paths(initialState, depth)
        s <- p
      yield criticalStates.forall(criticalPlaces => s.diff(criticalPlaces).size != s.size - 2)).reduce(_ && _)

    def isReachable(initialState: Marking[P], depth: Int): Boolean =
      (for
        path <- pn.toSystem.paths(initialState, depth)
        state <- path
        place <- state.asList
      yield place).toSet == Place.values.toSet

    def isBounded(initialState: Marking[P], depth: Int, maxTokenInPN: Int): Boolean =
      (for
        path: Path[Marking[P]] <- pn.toSystem.paths(initialState, depth)
        state <- path
      yield state.size <= maxTokenInPN).reduce(_ && _)

  // fancy syntax to create transition rules
  extension [P](self: Marking[P])
    def ~~> (y: Marking[P]) = Trn(self, y, MSet())
  extension [P](self: Trn[P])
    def ^^^ (z: Marking[P]) = self.copy(inh = z)