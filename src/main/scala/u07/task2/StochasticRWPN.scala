package scala.u07.task2

import u07.modelling.SPN
import u07.modelling.SPN.toCTMC
import u07.utils.MSet

import scala.u07.task2.StochasticRWPN.stochasticRWPN
import StochasticRWPN.Place.*

object StochasticRWPN:
  enum Place:
    case Idle, ChooseAction, ReadyToRead, ReadyToWrite, Reading, Writing, HasPermission

  export Place.*
  export u07.modelling.CTMCSimulation.*
  export u07.modelling.SPN.*

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
  println("Average time in READING: " + toCTMC(stochasticRWPN).relativeTimeInCondition(10, MSet(Idle, Idle, Idle, Idle, Idle, HasPermission), m => m(Reading) > 0))

  println("Average time in WRITING: " + toCTMC(stochasticRWPN).relativeTimeInCondition(10, MSet(Idle, Idle, Idle, Idle, Idle, HasPermission), m => m(Writing) > 0))

