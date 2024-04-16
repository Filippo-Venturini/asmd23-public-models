package scala.u07.task2

import u07.examples.StochasticMutualExclusion.Place
import u07.utils.MSet
import u07.modelling.SPN
import u07.modelling.SPN.toCTMC

import java.util.Random
import scala.u07.task2.BrussellatorPN.brussellatorPN
import BrussellatorPN.Place.*

object BrussellatorPN:
  enum Place:
    case A, B, D, E, X, Y

  export Place.*
  export u07.modelling.CTMCSimulation.*
  export u07.modelling.SPN.*

  val brussellatorPN = SPN[Place](
    Trn(MSet(A), m => 1.0, MSet(X), MSet()),
    Trn(MSet(X, X, Y), m => 1.0, MSet(X, X, X), MSet()),
    Trn(MSet(B, X), m => 1.0, MSet(Y, D), MSet()),
    Trn(MSet(X), m => 1.0, MSet(E), MSet())
  )

@main def mainBrusselatorPN =

  val execution = toCTMC(brussellatorPN).newSimulationTrace(MSet(X,Y,A,B,B,B),new Random)
      .take(10)
  //val AEvolution = execution.foreach(e => e.s_2.countOccurrences(A))