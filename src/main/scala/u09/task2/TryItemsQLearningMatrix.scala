package scala.u09.task2

import scala.u09.task2.ExtendedQMatrix.Facade
import scala.u09.task2.ExtendedQMatrix.Move
import scala.u09.task2.ExtendedQMatrix.Move.*

object TryItemsQLearningMatrix extends App:
  var totalItems = Set((1,1), (3,3), (7,2))
  var remainingItems = Set((1,1), (3,3), (7,2))
  val rlItems: ExtendedQMatrix.Facade = Facade(
    width = 9,
    height = 6,
    initial = (0, 1),
    terminal = {case (7,2) => true; case _ => false},
    jumps = { PartialFunction.empty },
    obstacles = Set.empty,
    itemsToCollect = remainingItems,
    gamma = 0.9, //Future reward importance
    alpha = 0.5, //Past knowledge importance
    epsilon = 0.8, //Exploration factor
    resetMap = () => {remainingItems = remainingItems ++ totalItems;},
    v0 = 1
  )

  rlItems.reward = {
    case (s, a) if totalItems.contains(s) && !remainingItems.contains(s) => (totalItems.size - remainingItems.size + 1) * -4
    case (s, a) if remainingItems.contains(s) =>
      remainingItems = remainingItems - s
      (totalItems.size - remainingItems.size + 1) * 20
    case ((x, y), a) if (x == 0 && a == LEFT) || (y == 0 && a == UP) || (x == rlItems.width-1 && a == RIGHT) || (y == rlItems.height-1 && a == DOWN) =>
      -10
    case _ => 0
  }

  val q0 = rlItems.qFunction
  println(rlItems.show(q0.vFunction, "%2.2f"))
  val q1 = rlItems.makeLearningInstance().learn(10000, 1000, q0)
  println(rlItems.show(q1.vFunction, "%2.2f"))
  println(rlItems.show(s => if rlItems.itemsToCollect.contains(s) then "$" else q1.bestPolicy(s).toString, "%7s"))

