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
  stocChannel.newSimulationTrace(IDLE, new Random)
    .take(10)
    .toList

  1.0


@main def mainStochasticChannelSimulation =

  println(averageCommunicationDoneTime(5))

  Time.timed:
    println:
      stocChannel.newSimulationTrace(IDLE, new Random)
        .take(10)
        .toList
        .mkString("\n")