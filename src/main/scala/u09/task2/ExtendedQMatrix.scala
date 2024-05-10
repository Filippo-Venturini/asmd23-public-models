package scala.u09.task2

import u09.model.{QRLImpl, ResetFunction}

import scala.collection.immutable.Map

object ExtendedQMatrix:
  type Node = (Int, Int)

  enum Move:
    case LEFT, RIGHT, UP, DOWN
    override def toString = Map(LEFT -> "<", RIGHT -> ">", UP -> "^", DOWN -> "v")(this)

  import Move.*

  case class Facade(
                     width: Int,
                     height: Int,
                     initial: Node,
                     terminal: PartialFunction[Node, Boolean],
                     terminalValue: Double,
                     jumps: PartialFunction[(Node, Move), Node],
                     obstacles: Set[Node],
                     itemsToCollect: Set[Node],
                     gamma: Double,
                     alpha: Double,
                     epsilon: Double = 0.0,
                     v0: Double) extends QRLImpl:
    type State = Node
    type Action = Move
    type Enemy = Node

    var reward: PartialFunction[(Node, Move), Double] = PartialFunction.empty
    var resetMap: ResetFunction = () => ()
    var enemy: Enemy = (width / 2, height / 2)
    var enemyPositions: List[Enemy] = List.empty

    private def getRandomAction: Action =
      Move.values.toList(util.Random.nextInt(Move.values.length))

    var patrolPattern: LazyList[Action] = LazyList.continually(List(LEFT, LEFT, LEFT, UP, UP, UP, RIGHT, RIGHT, RIGHT, DOWN, DOWN, DOWN)).flatten
    def getPatrolAction: Action =
      val head = patrolPattern.head
      patrolPattern = patrolPattern.tail
      head

    private def move(s: Node, a: Move): Node = (s, a) match
        case ((n1, n2), UP) => (n1, (n2 - 1) max 0)
        case ((n1, n2), DOWN) => (n1, (n2 + 1) min (height - 1))
        case ((n1, n2), LEFT) => ((n1 - 1) max 0, n2)
        case ((n1, n2), RIGHT) => ((n1 + 1) min (width - 1), n2)

    def getNeighbors(n: Node, radius: Int): List[Node] =
      val neighbors = for {
        i <- (n._1 - radius) to (n._1 + radius)
        j <- (n._2 - radius) to (n._2 + radius)
        if i >= 0 && i < width && j >= 0 && j < height
        if math.abs(n._1 - i) + math.abs(n._2 - j) <= radius
      } yield (i, j)
      neighbors.toList

    def qEnvironment(): Environment = (s: Node, a: Move) =>
      // applies direction, without escaping borders
      val n2: Node = move(s, a)

      enemyPositions = enemyPositions :+ enemy
      enemy = move(enemy, getPatrolAction)

      // computes rewards, and possibly a jump
      (reward.apply((s, a)), jumps.orElse[(Node, Move), Node](_ => n2)(s, a))

    def qFunction = QFunction(Move.values.toSet, v0, terminal, terminalValue)
    def qSystem = QSystem(environment = qEnvironment(), initial, terminal, resetMap)
    def makeLearningInstance() = QLearning(qSystem, gamma, alpha, epsilon, qFunction)

    def show[E](v: Node => E, formatString: String): String =
      (for
        row <- 0 until height
        col <- 0 until width
      yield formatString.format(v((col, row))) + (if (col == width - 1) "\n" else "\t"))
        .mkString("")
