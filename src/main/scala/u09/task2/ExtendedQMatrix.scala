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
                     reward: PartialFunction[(Node, Move), Double],
                     jumps: PartialFunction[(Node, Move), Node],
                     obstacles: Set[Node],
                     itemsToCollect: Set[Node],
                     gamma: Double,
                     alpha: Double,
                     epsilon: Double = 0.0,
                     prova: ResetFunction,
                     v0: Double) extends QRLImpl:
    type State = Node
    type Action = Move

    def qEnvironment(): Environment = (s: Node, a: Move) =>
      // applies direction, without escaping borders
      val n2: Node = (s, a) match
        case ((n1, n2), UP) => (n1, (n2 - 1) max 0)
        case ((n1, n2), DOWN) => (n1, (n2 + 1) min (height - 1))
        case ((n1, n2), LEFT) => ((n1 - 1) max 0, n2)
        case ((n1, n2), RIGHT) => ((n1 + 1) min (width - 1), n2)
        //case _ => ???
      // computes rewards, and possibly a jump
      (reward.apply((s, a)), jumps.orElse[(Node, Move), Node](_ => n2)(s, a))

    def qFunction = QFunction(Move.values.toSet, v0, terminal, 140)
    def qSystem = QSystem(environment = qEnvironment(), initial, terminal, prova)
    def makeLearningInstance() = QLearning(qSystem, gamma, alpha, epsilon, qFunction)

    def show[E](v: Node => E, formatString: String): String =
      (for
        row <- 0 until height
        col <- 0 until width
      yield formatString.format(v((col, row))) + (if (col == width - 1) "\n" else "\t"))
        .mkString("")
