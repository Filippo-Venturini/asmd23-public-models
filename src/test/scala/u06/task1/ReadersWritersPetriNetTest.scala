package scala.u06.task1

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers.not
import org.scalatest.matchers.should.Matchers.should

class ReadersWritersPetriNetTest extends AnyFunSuite:
  import ReadersWritersPetriNet.*

  test("Mutual exclusion"):
    isMutuallyExclusive(MSet(Idle, Idle, HasPermission), 100)

  test("Reachability"):
    isReachable(MSet(Idle, Idle, HasPermission), 10)



