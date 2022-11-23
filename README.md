# history

The `com.wormtrader.history` package is a key component of the *WormTrader*
 responsible for representing the price history of all securities traded
 by the user.

The main function of the WormTrader's AutoTrader is to observe tapes and
 to initate and exit trades based on the most recent price action as interpreted
 by indicators.

This package also contains the UI to show the prices to the user.

There is also code to fetch price data from third parties such as
 *Indepeendent Brokers* and *Yahoo*. *Note:* Yahoo no longer maintains
 this free data, but similar sources are available by subscription.

## Tapes
Tape is short for *ticker tape*, which is commonly displayed on graph of
 of either OHLC bars or candlestick bars &mdash; see `com.shanebow.ui.graph.TGGraph`
 *Tape Graph*.

`class Tape`: A tape is a collection of market bars for a particular barsize.
 A tape also maintains a list of studies (aka indicators, e.g. *50-day moving
 average*) as well as a list of event listeners.

`class TripleTape`: A collection of three Tape objects for a given symbol:

*  Each tape holds a different bar size (i.e. 5 min, 60 min, 1 day).
*  Supports simulations (back testing and paper trading) as well as live data.

`interface TapeStudy`: All of the classes in `com.wormtrader.indicators`
 implement `TapeStudy` which allows them to be parametized (e.g. 50 bar moving
 average vs 120 bar MA) and used in trading stragies. Note that most indicators
 also implement `ui.graph.GraphClipRenderer` which allows them to be displayed
 in a clip below the bars on an `SBGraph`.

The following `TapeEvent`s are reported to `TapeListener` objects:
*	BAR_ERROR = 0;
*  REALTIME_BAR = 1;
*  BAR_ADDED = 2; // history - not realtime
*  BAR_INSERTED = 3;
*  BARS_MODIFIED = 4;
*  BARS_CLEARED = 5;
*  HISTORY_DONE = 6;
*  BACKFILLED = 7;
*  SYMBOL_CHANGING = 8;

When a bar comes in, the studies are updated first, then the bar is
 sent to the listeners. Exit methods (stops) that fire off the tape
 should register themselves using addPriorityTapeListener which will
 force them to see the tape before other listeners like graphs, worms.

See the packages under `com.wortrader.history.ui` to see how price charts
 (and tables) are displayed to and configured by the user.

## Scanners
`class Scanner` is the super class for all stock scanners. A scan is passed a
 tape via its toHit method which decides whether the last bar is a hit.

To write a specific scaner subclass, extend Scanner and place class in:
~~~
package com.wormtrader.custom.scans.
~~~

Override the following methods:

*  `toString()`
 *  return the user friendly name of this scanner
*  `initialize(Tape tape)`
 *  called before any processing for each run. Use this method to register any needed studies on the tape.
 *  Ex: MovingAverage m_ma = (MovingAverage)tape.addStudy ( "SMA", "5,C" );
 *  If no studies are used, then it is not necessary to override this method.
*  `doIt(Tape tape)` - called once per symbol per run. In general, examine the last bar in the tape to determine if this symbol is a hit. If so, call  add(tape.getSymbol()) to record the hit.

