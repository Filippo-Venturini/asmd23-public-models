package scala.u07.task2

import u06.modelling.PetriNet.Marking
import u07.modelling.SPN
import u07.modelling.SPN.toCTMC
import u07.utils.MSet

import scala.u07.task2.StochasticRWPN.{Place, isNotReadingOrWriting, isReading, isWriting, stochasticRWPN}
import StochasticRWPN.Place.*

object StochasticRWPN:
  enum Place:
    case Idle, ChooseAction, ReadyToRead, ReadyToWrite, Reading, Writing, HasPermission

  export Place.*
  export u07.modelling.CTMCSimulation.*
  export u07.modelling.SPN.*

  val isReading: MSet[Place] => Boolean = m => m(Reading) > 0
  val isWriting: MSet[Place] => Boolean = m => m(Writing) > 0
  val isNotReadingOrWriting: MSet[Place] => Boolean = m => m(Writing) == 0 && m(Reading) == 0

  val stochasticRWPN = SPN[Place](
    Trn(MSet(Idle), m => 1.0, MSet(ChooseAction), MSet()),
    Trn(MSet(ChooseAction), m => 200000, MSet(ReadyToRead), MSet()),
    Trn(MSet(ChooseAction), m => 100000, MSet(ReadyToWrite), MSet()),
    Trn(MSet(ReadyToRead, HasPermission), m => 100000, MSet(Reading, HasPermission), MSet()),
    Trn(MSet(Reading), m => 0.1 * m(Reading), MSet(Idle), MSet()),
    Trn(MSet(ReadyToWrite, HasPermission), m => 100000, MSet(Writing), MSet(Reading)),
    Trn(MSet(Writing), m => 0.2, MSet(Idle, HasPermission), MSet())
  )

@main def mainStochasticRWPNSimulation =
  val nRuns: Int = 10
  val startingState: MSet[Place] = MSet(Idle, Idle, Idle, Idle, Idle, HasPermission)
  
  println("Average time in READING: " + toCTMC(stochasticRWPN).relativeTimeInCondition(nRuns, startingState, isReading))

  println("Average time in WRITING: " + toCTMC(stochasticRWPN).relativeTimeInCondition(nRuns, startingState, isWriting))

  println("Average time not READING or WRITING: " + toCTMC(stochasticRWPN).relativeTimeInCondition(nRuns, startingState, isNotReadingOrWriting))
