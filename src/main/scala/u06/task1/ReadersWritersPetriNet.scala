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
    //pnRW.paths(initialState, depth).flatMap(p => p.filter(s => s.diff(MSet(Reading, Writing)).size == s.size - 2 || s.diff(MSet(Writing, Writing)).size == s.size - 2)).isEmpty
    !statesMutualExclusion.contains(true)

  def isReachable(initialState: MSet[Place], depth: Int): Boolean =
    val allReachedStates: Seq[Place] =
      for
        path <- pnRW.paths(initialState, depth)
        state <- path
        place <- state.asList
      yield place

    val m1 = allReachedStates.toList.distinct
    val m2 = Place.values.toSeq
    m1 == m2

  def maxTokenInPN(initialState: MSet[Place]): Int =
    if initialState.matches(MSet(HasPermission)) then initialState.size else initialState.size + 1

  def isBounded(initialState: MSet[Place], depth: Int): Boolean =
    val maxSize = maxTokenInPN(initialState)
    val allReachedStates: Seq[Boolean] =
      for
        path <- pnRW.paths(initialState, depth)
        state <- path
      yield state.size <= maxSize

    allReachedStates.forall(identity)

  def isBounded2(initialState: MSet[Place], depth: Int): Boolean =
    (for
      path: Path[Marking[Place]] <- pnRW.paths(initialState, depth)
      state <- path
    yield state.size <= maxTokenInPN(initialState)).reduce(_ & _)


  @main def mainPNMutualExclusion =
  println(pnRW.paths(MSet(Idle, Idle, HasPermission), 3).toList.mkString("\n"))
