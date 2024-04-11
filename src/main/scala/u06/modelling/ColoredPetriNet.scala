package scala.u06.modelling

import pc.modelling.System
import pc.utils.MSet

import scala.collection.immutable.Set
import scala.math.Ordering

object ColoredPetriNet:
  enum Color:
    case Black, Red

  case class Elem[P](place: P, color: Color = Color.Black)

  // pre-conditions, effects, inhibition, priority
  type Token[P] = Elem[P]
  case class Trn[P](cond: MSet[Token[P]], eff: MSet[Token[P]], inh: MSet[Token[P]], priority: Int = 1, inColor: Color = Color.Black, outColor: Color = Color.Black)
  type ColoredPetriNet[P] = Set[Trn[P]]
  type Marking[P] = MSet[Token[P]]

  // factory of A Petri Net
  def apply[P](transitions: Trn[P]*): ColoredPetriNet[P] = transitions.toSet


  // factory of a System, as a toSystem method
  extension [P](pn: ColoredPetriNet[P])
    def toSystem: System[Marking[P]] = m =>
      val allTransitions =
        for
          Trn(cond, eff, inh, priority, inColor, outColor) <- pn   // get any transition
          if m disjoined inh          // check inhibition
          out <- m extract cond.filter(t => t.color == inColor)       // remove precondition
        yield (priority, out union eff)

      val maxPriority = allTransitions.map(_._1).max
      allTransitions.filter((p, _) => p == maxPriority).map(_._2)

  // fancy syntax to create transition rules
  extension [P](self: Marking[P])
    def ~~> (y: Marking[P]) = Trn(self, y, MSet())
  extension [P](self: Trn[P])
    def ^^^ (z: Marking[P]) = self.copy(inh = z)
    def priority(p: Int) = self.copy(priority = p)
    def inColor(c: Color) = self.copy(inColor = c)
    def outColor(c: Color) = self.copy(outColor = c)
