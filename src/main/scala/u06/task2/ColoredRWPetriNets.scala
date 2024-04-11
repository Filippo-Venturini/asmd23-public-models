package scala.u06.task2

import scala.u06.modelling.ColoredPetriNet
import scala.u06.modelling.ColoredPetriNet.Color.*
import scala.u06.modelling.ColoredPetriNet.Elem

object ColoredRWPetriNets:

  enum Place:
    case Idle, ChooseAction, ReadyToRead, ReadyToWrite, Reading, Writing, HasPermission

  export Place.*
  export scala.u06.modelling.ColoredPetriNet.*
  export pc.modelling.SystemAnalysis.*
  export pc.utils.MSet

  def pnRWColored = ColoredPetriNet[Place](
    MSet(Elem(Idle, Red)) ~~> MSet(Elem(ChooseAction, Red)),
    MSet(Elem(ChooseAction, Red)) ~~> MSet(Elem(ReadyToRead, Red)),
    MSet(Elem(ChooseAction, Black)) ~~> MSet(Elem(ReadyToWrite, Black)),
    MSet(Elem(ReadyToRead, Red), Elem(HasPermission, Black)) ~~> MSet(Elem(Reading, Red), Elem(HasPermission, Black)),
    MSet(Elem(Reading, Red)) ~~> MSet(Elem(Idle, Red)),
    MSet(Elem(ReadyToWrite, Black), Elem(HasPermission, Black)) ~~> MSet(Elem(Writing, Black)) ^^^ MSet(Elem(Reading, Black)),
    MSet(Elem(Writing, Black)) ~~> MSet(Elem(Idle, Black), Elem(HasPermission, Black))
  ).toSystem

  @main def mainPNMutualExclusion =
    println(pnRWColored.paths(MSet(Elem(Idle, Red), Elem(HasPermission, Black)),5).toList.mkString("\n"))