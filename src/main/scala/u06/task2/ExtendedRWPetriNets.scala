package scala.u06.task2

import scala.u06.modelling.ExtendedPetriNet
import scala.u06.modelling.ExtendedPetriNet.Color.*
import scala.u06.modelling.ExtendedPetriNet.*
import u06.modelling.SystemAnalysis.*
import u06.utils.MSet

object ExtendedRWPetriNets:

  enum Place:
    case Idle, ChooseAction, ReadyToRead, ReadyToWrite, Reading, Writing, HasPermission

  export Place.*
  export scala.u06.modelling.ExtendedPetriNet.*
  export u06.modelling.SystemAnalysis.*
  export u06.utils.MSet

  def pnRWPriorities = ExtendedPetriNet[Place](
    MSet(*(Idle)) ~~> MSet(*(ChooseAction)),
    MSet(*(ChooseAction)) ~~> MSet(*(ReadyToRead)) priority 5,
    MSet(*(ChooseAction)) ~~> MSet(*(ReadyToWrite)) priority 2,
    MSet(*(ReadyToRead), *(HasPermission)) ~~> MSet(*(Reading), *(HasPermission)),
    MSet(*(Reading)) ~~> MSet(*(Idle)),
    MSet(*(ReadyToWrite), *(HasPermission)) ~~> MSet(*(Writing)) ^^^ MSet(*(Reading)),
    MSet(*(Writing)) ~~> MSet(*(Idle), *(HasPermission))
  ).toSystem

  def pnRWColored = ExtendedPetriNet[Place](
    MSet(*(Idle, Red)) ~~> MSet(*(ChooseAction, Red)),
    MSet(*(Idle, Black)) ~~> MSet(*(ChooseAction, Black)),
    MSet(*(ChooseAction, Red)) ~~> MSet(*(ReadyToRead, Red)),
    MSet(*(ChooseAction, Black)) ~~> MSet(*(ReadyToWrite, Black)),
    MSet(*(ReadyToRead, Red), *(HasPermission, Black)) ~~> MSet(*(Reading, Red), *(HasPermission, Black)),
    MSet(*(Reading, Red)) ~~> MSet(*(Idle, Black)),
    MSet(*(ReadyToWrite, Black), *(HasPermission, Black)) ~~> MSet(*(Writing, Black)) ^^^ MSet(*(Reading, Black)),
    MSet(*(Writing, Black)) ~~> MSet(*(Idle, Red), *(HasPermission, Black))
  ).toSystem

  @main def mainExtendedPN =
    println(pnRWPriorities.paths(MSet(*(Idle), *(HasPermission)),5).toList.mkString("\n"))
    println(pnRWColored.paths(MSet(*(Idle, Red), *(HasPermission, Black)),9).toList.mkString("\n"))