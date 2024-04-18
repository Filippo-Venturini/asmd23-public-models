package u07.examples

import u07.utils.Time
import java.util.Random
import u07.examples.StochasticChannel.*

def averageCommunicationDoneTime(nRun: Int): Double =
  (0 to nRun).foldLeft(0.0)((z, _) => z + stocChannel.newSimulationTrace(IDLE, new Random)
                                            .take(10)
                                            .toList
                                            .find(e => e.state == DONE).map(e => e.time).getOrElse(0.0)) / nRun

def relativeFailTime(nRun: Int): Double =
  val totalTimes = (0 to nRun).foldLeft((0.0, 0.0)) ((acc, _) => {
    val (failTime, totTime) = stocChannel.newSimulationTrace(IDLE, new Random)
      .take(10)
      .toList
      .sliding(2)
      .foldLeft((0.0, 0.0)) ( (z, s) => if (s(0).state == FAIL) (z._1 + (s(1).time - s(0).time), s(1).time) else (z._1, s(1).time))

    (acc._1 + failTime, acc._2 + totTime)
  })

  totalTimes._1 / totalTimes._2


@main def mainStochasticChannelSimulation =

  println("Avarerage communication time: " + averageCommunicationDoneTime(5))

  println("Relative fail percentage time: " + relativeFailTime(10))