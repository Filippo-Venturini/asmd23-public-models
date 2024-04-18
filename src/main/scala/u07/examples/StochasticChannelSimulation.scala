package u07.examples

import u07.utils.Time
import java.util.Random
import u07.examples.StochasticChannel.*

@main def mainStochasticChannelSimulation =

  println("Avarerage communication done time: " + stocChannel.averageTimeToReachState(10, IDLE, DONE))

  println("Relative fail percentage time: " + stocChannel.relativeTimeInState(10, IDLE, FAIL))