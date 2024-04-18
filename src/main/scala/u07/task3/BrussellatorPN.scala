package scala.u07.task3

import u07.examples.StochasticMutualExclusion.Place
import u07.utils.MSet
import u07.modelling.SPN
import u07.modelling.SPN.toCTMC

import org.knowm.xchart.{QuickChart, SwingWrapper, XYChart}
import java.util.Random
import scala.u07.task3.BrussellatorPN.brussellatorPN
import BrussellatorPN.Place.*

object BrussellatorPN:
  enum Place:
    case A, B, D, E, X, Y

  export Place.*
  export u07.modelling.CTMCSimulation.*
  export u07.modelling.SPN.*

  val brussellatorPN = SPN[Place](
    Trn(MSet(), m => 10, MSet(A), MSet()),
    Trn(MSet(), m => 10, MSet(B), MSet()),
    Trn(MSet(A), m => m(A) * (1 + 0.1 / math.exp(m(X))), MSet(X), MSet()),
    Trn(MSet(X, X, Y), m =>  m(X) * m(Y) * (1 + 0.1 / math.exp(m(X))), MSet(X, X, X), MSet()),
    Trn(MSet(B, X), m => m(B) * m(X) * (1 + 0.1 * math.exp(m(X))), MSet(Y, D), MSet()),
    Trn(MSet(X), m => m(X) * 1, MSet(E), MSet())
  )

@main def mainBrusselatorPN =

  val simulation = toCTMC(brussellatorPN).newSimulationTrace(MSet(A,B,B,B,X,Y), new Random)
    .take(50)
    .toList
  simulation.foreach(println)

  val times = simulation.map(_._1).toArray
  val xCounts = simulation.map(_._2.countOccurrences(X)).map(_.toDouble).toArray
  val yCounts = simulation.map(_._2.countOccurrences(Y)).map(_.toDouble).toArray

  val chart = QuickChart.getChart("Brusselator Simulation", "Time", "Count", "X", times, xCounts)
  chart.addSeries("Y", times, yCounts)
  chart.getStyler.setLegendVisible(true)

  new SwingWrapper[XYChart](chart).displayChart().setTitle("Brusselator Simulation")