package scala.u06.task2

import scala.u06.modelling.ColoredPetriNet
import scala.u06.modelling.ColoredPetriNet.Elem

object ColoredRWPetriNets:

  enum Place:
    case Idle, ChooseAction, ReadyToRead, ReadyToWrite, Reading, Writing, HasPermission

  export Place.*
  export scala.u06.modelling.ColoredPetriNet.*
  export pc.modelling.SystemAnalysis.*
  export pc.utils.MSet

  def pnRW = ColoredPetriNet[Place](
    MSet(Elem(Idle)) ~~> MSet(Elem(ChooseAction)),
    MSet(Elem(ChooseAction)) ~~> MSet(Elem(ReadyToRead)),
    MSet(Elem(ChooseAction)) ~~> MSet(Elem(ReadyToWrite)),
    MSet(Elem(ReadyToRead), Elem(HasPermission)) ~~> MSet(Elem(Reading), Elem(HasPermission)),
    MSet(Elem(Reading)) ~~> MSet(Elem(Idle)),
    MSet(Elem(ReadyToWrite), Elem(HasPermission)) ~~> MSet(Elem(Writing)) ^^^ MSet(Elem(Reading)),
    MSet(Elem(Writing)) ~~> MSet(Elem(Idle), Elem(HasPermission))
  ).toSystem