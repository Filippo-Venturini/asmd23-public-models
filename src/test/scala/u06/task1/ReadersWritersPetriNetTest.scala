package scala.u06.task1

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers.not
import org.scalatest.matchers.should.Matchers.{should, shouldBe}
import u06.modelling.PetriNet

class ReadersWritersPetriNetTest extends AnyFunSuite:
  import ReadersWritersPetriNet.*

  val safeInitialState: Marking[Place] = MSet(Idle, Idle, HasPermission)
  val notSafeInitialState: Marking[Place] = MSet(Idle, Idle, Reading, Writing)
  val depth: Int = 10
  val maxNumberOfTokens = safeInitialState.size + 1

  test("Mutual exclusion"):
    pnRW.isMutuallyExclusive(safeInitialState, depth, MSet(Writing, Writing), MSet(Reading, Writing)) shouldBe true

  test("Not in mutual exclusion"):
    pnRW.isMutuallyExclusive(notSafeInitialState, depth, MSet(Writing, Writing), MSet(Reading, Writing)) shouldBe false

  test("Reachability"):
    pnRW.isReachable(safeInitialState, depth) shouldBe true

  test("Boundedness"):
    pnRW.isBounded(safeInitialState, depth, maxNumberOfTokens) shouldBe true

