package scala.u06.task1

export pc.modelling.PetriNet
import pc.utils.MSet

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

  def isMutuallyExclusive(initialState: MSet[Place], depth: Int): Boolean =
    val statesMutualExclusion: Seq[Boolean] =
      for
        p <- pnRW.paths(initialState, depth)
        s <- p
      yield s.diff(MSet(Reading, Writing)).size == s.size - 2 || s.diff(MSet(Writing, Writing)).size == s.size - 2
    statesMutualExclusion.forall(identity)

  def isReachable(initialState: MSet[Place], depth: Int): Boolean =
    val allReachedStates: Set[Place] =
      (for
        path <- pnRW.paths(initialState, depth)
        state <- path
        place <- state.asList
      yield place).toSet

    allReachedStates == Set(Idle, ChooseAction, ReadyToRead, ReadyToWrite, Reading, Writing, HasPermission)


  @main def mainPNMutualExclusion =
  println(pnRW.paths(MSet(Idle, Idle, HasPermission), 3).toList.mkString("\n"))
