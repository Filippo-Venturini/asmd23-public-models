package scala.u09.task2

import scala.u09.task2.ExtendedQMatrix.Facade
import scala.u09.task2.ExtendedQMatrix.Move.*

object TryEnemyQLearningMatrix extends App:
  val rlEnemy: ExtendedQMatrix.Facade = Facade(
    width = 10,
    height = 10,
    initial = (0, 4),
    terminal = { case (9,4) => true; case _ => false },
    terminalValue = 10.0,
    jumps = { PartialFunction.empty },
    obstacles = Set.empty,
    itemsToCollect = Set.empty,
    gamma = 0.9, //Future reward importance
    alpha = 0.5, //Past knowledge importance
    epsilon = 0.8, //Exploration factor
    v0 = 1
  )

  rlEnemy.reward = {
    case ((9,4), _) => 10
    case ((x, y), a) if (x == 0 && a == LEFT) || (y == 0 && a == UP) || (x == rlEnemy.width - 1 && a == RIGHT) || (y == rlEnemy.height - 1 && a == DOWN) =>
      -1
    case (s, a) if rlEnemy.getNeighbors(rlEnemy.enemy, 1).contains(s) => -100
    case _ => 0
  }

  rlEnemy.resetMap = () => { rlEnemy.enemy = (rlEnemy.width / 2, rlEnemy.height / 2); rlEnemy.enemyPositions = List.empty; rlEnemy.patrolPattern = LazyList.continually(List(LEFT, LEFT, LEFT, UP, UP, UP, RIGHT, RIGHT, RIGHT, DOWN, DOWN, DOWN)).flatten }

  val q0 = rlEnemy.qFunction
  println(rlEnemy.show(q0.vFunction, "%2.2f"))
  val q1 = rlEnemy.makeLearningInstance().learn(10000, 100, q0)
  println(rlEnemy.show(q1.vFunction, "%2.2f"))

  println("\n############################ BEST POLICY ##############################\n")
  println(rlEnemy.show(s => q1.bestPolicy(s).toString, "%7s"))

  val agentPath = rlEnemy.qSystem.run(q1.bestPolicy).take(30)

  agentPath.toList.zipWithIndex.map {
    case ((e1, e2), index) => (e1, if(index == 0) e2 else agentPath(index - 1)._2)
  }

  println("################################ RUN ##################################\n")

  println(rlEnemy.show(s => {
    if rlEnemy.enemyPositions.contains(s) then "@" else agentPath.find((ac, st) => st == s).map((ac, st) => ac).getOrElse(".")
  }, "%7s"))