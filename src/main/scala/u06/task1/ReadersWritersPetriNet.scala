package scala.u06.task1

import u06.modelling.PetriNet

import u06.modelling.PetriNet
import scala.collection.immutable.LazyList

object ReadersWritersPetriNet:
  enum Place:
    case Idle, ChooseAction, ReadyToRead, ReadyToWrite, Reading, Writing, HasPermission

  export Place.*
  export u06.modelling.PetriNet.*
  export u06.modelling.SystemAnalysis.*
  export u06.utils.MSet

  def pnRW = PetriNet[Place](
    MSet(Idle) ~~> MSet(ChooseAction),
    MSet(ChooseAction) ~~> MSet(ReadyToRead),
    MSet(ChooseAction) ~~> MSet(ReadyToWrite),
    MSet(ReadyToRead, HasPermission) ~~> MSet(Reading, HasPermission),
    MSet(Reading) ~~> MSet(Idle),
    MSet(ReadyToWrite, HasPermission) ~~> MSet(Writing) ^^^ MSet(Reading),
    MSet(Writing) ~~> MSet(Idle, HasPermission)
  )

  @main def mainPNMutualExclusion =
    println(pnRW.toSystem.paths(MSet(Idle, Idle, HasPermission), 3).toList.mkString("\n"))
