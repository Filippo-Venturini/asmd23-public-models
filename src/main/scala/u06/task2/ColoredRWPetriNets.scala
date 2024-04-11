package scala.u06.task2

import pc.modelling.PetriNet

import scala.u06.modelling.ExtendedPetriNet
import scala.u06.modelling.ExtendedPetriNet.Color.*
import scala.u06.modelling.ExtendedPetriNet.Elem
import scala.u06.task1.ReadersWritersPetriNet.Place

object ColoredRWPetriNets:

  enum Place:
    case Idle, ChooseAction, ReadyToRead, ReadyToWrite, Reading, Writing, HasPermission

  export Place.*
  export scala.u06.modelling.ExtendedPetriNet.*
  export pc.modelling.SystemAnalysis.*
  export pc.utils.MSet

  def pnRWPriorities = ExtendedPetriNet[Place](
    MSet(Elem(Idle)) ~~> MSet(Elem(ChooseAction)),
    MSet(Elem(ChooseAction)) ~~> MSet(Elem(ReadyToRead)) priority 5,
    MSet(Elem(ChooseAction)) ~~> MSet(Elem(ReadyToWrite)) priority 2,
    MSet(Elem(ReadyToRead), Elem(HasPermission)) ~~> MSet(Elem(Reading), Elem(HasPermission)),
    MSet(Elem(Reading)) ~~> MSet(Elem(Idle)),
    MSet(Elem(ReadyToWrite), Elem(HasPermission)) ~~> MSet(Elem(Writing)) ^^^ MSet(Elem(Reading)),
    MSet(Elem(Writing)) ~~> MSet(Elem(Idle), Elem(HasPermission))
  ).toSystem

  def pnRWColored = ExtendedPetriNet[Place](
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