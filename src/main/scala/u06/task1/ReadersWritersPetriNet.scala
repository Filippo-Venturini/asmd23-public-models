package scala.u06.task1

export pc.modelling.PetriNet
import pc.utils.MSet

import scala.collection.immutable.LazyList

object ReadersWritersPetriNet:
  enum Place:
    case Idle, ChooseAction, ReadyToRead, ReadyToWrite, Reading, Writing, HasPermission

  export Place.*
  export pc.modelling.PetriNet.*
  export pc.modelling.SystemAnalysis.*
  export pc.utils.MSet

  def pnRW = PetriNet[Place](
    MSet(Idle) ~~> MSet(ChooseAction),
    MSet(ChooseAction) ~~> MSet(ReadyToRead),
    MSet(ChooseAction) ~~> MSet(ReadyToWrite),
    MSet(ReadyToRead, HasPermission) ~~> MSet(Reading, HasPermission),
    MSet(Reading) ~~> MSet(Idle),
    MSet(ReadyToWrite, HasPermission) ~~> MSet(Writing) ^^^ MSet(Reading),
    MSet(Writing) ~~> MSet(Idle, HasPermission)
  ).toSystem

  def pnRWPriorities = PetriNet[Place](
    MSet(Idle) ~~> MSet(ChooseAction),
    MSet(ChooseAction) ~~> MSet(ReadyToRead) priority 5,
    MSet(ChooseAction) ~~> MSet(ReadyToWrite) priority 2,
    MSet(ReadyToRead, HasPermission) ~~> MSet(Reading, HasPermission),
    MSet(Reading) ~~> MSet(Idle),
    MSet(ReadyToWrite, HasPermission) ~~> MSet(Writing) ^^^ MSet(Reading),
    MSet(Writing) ~~> MSet(Idle, HasPermission)
  ).toSystem

  def isMutuallyExclusive(initialState: MSet[Place], depth: Int): Boolean =
    (for
        p <- pnRW.paths(initialState, depth)
        s <- p
      yield s.diff(MSet(Reading, Writing)).size != s.size - 2 && s.diff(MSet(Writing, Writing)).size != s.size - 2).reduce(_ && _)

    /*val paths = pnRW.paths(initialState, depth)
    !paths.exists { path =>
      path.exists { state =>
        val diffReadingWriting = state.diff(MSet(Reading, Writing)).size
        val diffWritingWriting = state.diff(MSet(Writing, Writing)).size
        diffReadingWriting == state.size - 2 || diffWritingWriting == state.size - 2
      }
    }*/
    //pnRW.paths(initialState, depth).flatMap(p => p.filter(s => s.diff(MSet(Reading, Writing)).size == s.size - 2 || s.diff(MSet(Writing, Writing)).size == s.size - 2)).isEmpty

  def isReachable(initialState: MSet[Place], depth: Int): Boolean =
    (for
      path <- pnRW.paths(initialState, depth)
      state <- path
      place <- state.asList
    yield place).toSet == Place.values.toSet


  def maxTokenInPN(initialState: MSet[Place]): Int =
    if initialState.matches(MSet(HasPermission)) then initialState.size else initialState.size + 1

  def isBounded(initialState: MSet[Place], depth: Int): Boolean =
    (for
      path: Path[Marking[Place]] <- pnRW.paths(initialState, depth)
      state <- path
    yield state.size <= maxTokenInPN(initialState)).reduce(_ && _)


  @main def mainPNMutualExclusion =
    println(pnRWPriorities.paths(MSet(Idle, ChooseAction, HasPermission),5).toList.mkString("\n"))
    //println(pnRW.paths(MSet(Idle, Idle, HasPermission), 3).toList.mkString("\n"))
