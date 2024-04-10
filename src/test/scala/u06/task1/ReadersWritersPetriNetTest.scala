package scala.u06.task1

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers.not
import org.scalatest.matchers.should.Matchers.{should, shouldBe}

class ReadersWritersPetriNetTest extends AnyFunSuite:
  import ReadersWritersPetriNet.*

  test("Mutual exclusion"):
    isMutuallyExclusive(MSet(Idle, Idle, HasPermission), 10) shouldBe true

  test("Not in mutual exclusion"):
    isMutuallyExclusive(MSet(Idle, Idle, Reading, Writing), 100) shouldBe false

  test("Reachability"):
    isReachable(MSet(Idle, Idle, HasPermission), 100) shouldBe true

  test("Boundness"):
    isBounded(MSet(Idle, Idle, HasPermission), 100) shouldBe true

