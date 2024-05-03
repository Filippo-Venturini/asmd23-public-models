package scala.u09.task2

object TryExtendedQLearningMatrix extends App:

  import scala.u09.task2.ExtendedQMatrix.Move.*
  import scala.u09.task2.ExtendedQMatrix.*
  val mapObstacles = Set((3,0), (5,1))

  val rl: ExtendedQMatrix.Facade = Facade(
    width = 10,
    height = 4,
    initial = (0,1),
    terminal = {case _=>false},
    jumps = { PartialFunction.empty },
    reward = { case((9, 1), _) => 1; case (s, _) if mapObstacles.contains(s) => -10; case _ => 0},
    obstacles = mapObstacles,
    gamma = 0.9, //Future reward importance
    alpha = 0.5, //Past knowledge importance
    epsilon = 0.3, //Exploration factor
    v0 = 1
  )

  val q0 = rl.qFunction
  println(rl.show(q0.vFunction,"%2.2f"))
  val q1 = rl.makeLearningInstance().learn(10000,100,q0)
  println(rl.show(q1.vFunction,"%2.2f"))
  println(rl.show(s => if rl.obstacles.contains(s) then "*" else q1.bestPolicy(s).toString,"%7s"))
