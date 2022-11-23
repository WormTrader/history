package com.wormtrader.history.query;
/********************************************************************
* @(#)QueryWorker.java 1.00 20120615
* Copyright © 2012 by Richard T. Salamone, Jr. All rights reserved.
*
* QueryWorker: Runs a query on historical data in the background.
*
* @author Rick Salamone
* @version 1.00
* 20120804 rts created from ScanWorker
* 20120903 rts added the post processing using the TallyUpDown class
* 20120904 rts modified and improved linkage between this, Query, and QueryHit
* 20120905 rts added runs tallying using fractals
* 20120907 rts added the time of day filter
* 20120908 rts fixed to handle missing days
* 20120916 rts decoupled TallyUpDown
* 20130404 rts decoupled hit processing - HitProcessor specified in ctor
*******************************************************/
import com.wormtrader.bars.Bar;
import com.wormtrader.bars.BarList;
import com.wormtrader.bars.BarSize;
import com.wormtrader.bars.symbols.SymbolList;
import com.wormtrader.history.Tape;
import com.shanebow.util.SBDate;
import com.shanebow.util.SBLog;
import java.awt.Toolkit;
import javax.swing.SwingWorker;

public class QueryWorker
	extends SwingWorker<String, QueryHit>
	{
	/**
	* fQuery is the user written (and dynamically compiled) query that will
	* process the user specified symbols over the user specified date range
	* and fire a hit when it's criteria are met.
	*/
	private final Query fQuery;

	/**
	* fHitProcessor is the user written code that gets called for each hit
	* to do custom processing and analysis.
	*/
	private final HitProcessor fHitProcessor;

	/**
	* fSymbolList is the list of symbols that will be sent to the query in turn.
	*/
	private final SymbolList fSymbolList;

	/**
	* fDateRange is an array with two longs that specify the range of dates
	* (inclusive) that will be sent to fQuery for each symbol in fSymbolList
	* - note that this range does not include backfill.
	*/
	private final long[] fDateRange;

	/**
	* fTimes is a two entry array. The first entry is the index of the first
	* five minute bar to send to the Query's isHit() method each day. For instance,
	* if fTimes[0] == 0, then the 9:30 bar is sent to isHit(), but if fTimes[0] ==
	* 1, then the 9:30 bars will not be sent to isHit().
	* The second index is similar, but is the number of five minute bars at the
	* end of the day that are not sent to isHit(). For instance, if fTimes[1] == 2,
	* then the 3:55 and 3:50 bars would not be sent to isHit() for processing.
	* On half days, we still skip fTimes[1] bars at the end of the half day.
	*/
	private final int[] fTimes;

	/**
	* When fQuery signals a hit, this code determines the price delta for the
	* ensuing run which ends at the next fractal. If fSkipRuns is true, fQuery
	* will NOT be sent the bars in the run; when false, fQuery will be sent all
	* bars as usual after a hit.
	* In other words, this setting allows the user to avoid reporting hits on
	* consecutive bars that meet the query criteria - when true, only the first
	* hit is reported until the run is finished.
	*/
	boolean fSkipRuns = true;

	/**
	* barsD1 is the list of daily bars that are feed to the Query.
	* At the end of each day the "next" bar from this list is sent to tapeD1:
	* Note that barsD1 always holds (all) the bars for fDateRange, whereas
	* tapeD1 only contains those bars that have been processed to date (plus
	* some additional backfill bars that are not in this list).
	*
	* When the Query identifies a hit, we look into this list to determine the
	* price action following the hit.
	*/
	private final BarList barsD1 = new BarList();

	/**
	* These are the lists of bars that are feed to the Query.
	* When the Query identifies a hit, we look into these lists
	* to determine the price action following the hit.
	*/
	private final BarList barsM5 = new BarList();

	/**
	* indexM5 is the current index into barsM5
	*/
	private int indexM5;

	/**
	* fTapeM5 is the five minute Tape which is populated with bars from
	* barsM5 (plus backfill) - It is initialized with indicators by the
	* Query, during the later's init() method.
	*/
	Tape fTapeM5  = new Tape(BarSize.FIVE_MIN);

	protected int fNumSymbolsTotal;
	protected int fNumSymbolsDone;
	protected int fNumDays;
	protected int fNumHits;

	QueryWorker(SymbolList aSymbolList, long[] aDateRange, int[] aTimes,
		Query aQuery, HitProcessor aHitProcessor)
		{
		fSymbolList = aSymbolList;
		fDateRange = aDateRange;
		fTimes = aTimes;
		fQuery = aQuery;
		fHitProcessor = aHitProcessor;
		}

	protected String details()
		{
		return fHitProcessor.summary(fNumHits, fNumDays);
		}

	@Override protected String doInBackground() throws Exception
		{
		fHitProcessor.startRun(barsM5);
		Tape tapeD1  = new Tape(BarSize.ONE_DAY);
		Tape tapeM30 = new Tape(BarSize.THIRTY_MIN);
		fTapeM5.addTapeListener(tapeM30);
		fQuery.init(tapeD1, tapeM30, fTapeM5);
		fNumSymbolsTotal = fSymbolList.size();

		long[] prefill = new long[2];
		prefill[0] = SBDate.addWeekDays(fDateRange[0], -15);
		prefill[1] = SBDate.addWeekDays(fDateRange[0], -1);
		for (String symbol : fSymbolList)
			{
			if ( isCancelled()) return "";
			if (tapeD1.reset( symbol, prefill ) == 0)
				log("No D1 backfill available for " + symbol);
			if (tapeM30.reset( symbol, prefill ) == 0)
				log("No M30 backfill available for " + symbol);
			barsD1.clear();
			int nDays = barsD1.fetchD1( symbol, fDateRange );
			if ( nDays <= 0)
				{
				log("No daily history found for " + symbol);
				continue;
				}

			// @TODO: has to be preopen - so subtract one in following line - fix reset!!
			long preopen = barsD1.get(0).getTime()-1; // 
			fTapeM5.reset(symbol, preopen);
		log("fTapeM5 reset to: " + symbol +  SBDate.yyyymmdd__hhmmss(preopen));
		fTapeM5.dump();
			for (int indexD1 = 0; indexD1 < nDays; indexD1++ )
				{
				if ( isCancelled())
					return "";
				Bar barD1 = barsD1.get(indexD1);
				processIntraday(symbol, barD1.yyyymmdd());
				tapeD1.realtimeBar(barD1);
				}
			++fNumSymbolsDone;
			}
		return "<html><b>" + fNumSymbolsDone + "</b> symbols";
		}

	private void processIntraday(String symbol, String yyyymmdd)
		{
		barsM5.clear();
		int nM5Bars = barsM5.fetchM5(symbol, yyyymmdd);
		if (nM5Bars <= 0)
			{
			log("No history found for " + symbol + " on " + yyyymmdd);
			return;
			}

		if (fTapeM5.size() <= 0) // if no backfill yet, use this day as backfill
			{
			log(symbol + " " + yyyymmdd + " data used as backfill");
			for (Bar bar : barsM5)
				fTapeM5.realtimeBar(bar);
			return;
			}

		++fNumDays;
		fHitProcessor.startDay(symbol, yyyymmdd);

		// Query skips fTimes[0] bars at the start of the day, but tape needs em
		for (indexM5 = 0; indexM5 < fTimes[0]; indexM5++ )
			fTapeM5.realtimeBar(barsM5.get(indexM5));

		// after skipping, Query processes bars up to index nM5Bars - fTimes[1]
		for ( ; indexM5 < nM5Bars-fTimes[1]; indexM5++ )
			{
			Bar barM5 = barsM5.get(indexM5);
			fTapeM5.realtimeBar(barM5);
			int tapeIndexM5 = fTapeM5.size() - 1; // index into tape != index into bars
			if (fQuery.isHit(barM5, tapeIndexM5))
				addHit(symbol, barM5);
			}

		// send end of day bars to tape that were not processed by Query
		for (; indexM5 < nM5Bars; indexM5++ )
			fTapeM5.realtimeBar(barsM5.get(indexM5));
		}

	/**
	* @TODO 20121017 - working on the way runs are measured & changed to after 15 min
	*/
	private void addHit(String aSymbol, Bar aBar)
		{
		QueryHit hit = fHitProcessor.createHit(aSymbol, aBar, indexM5);
		if (hit == null) // hit processor vetoed the hit (e.g. never "entered trade")
			return;

		int runEnd = indexM5 + hit.getRunLength();

		if (fSkipRuns && runEnd != indexM5)
			{
			while (++indexM5 < runEnd )
				{
				Bar barM5 = barsM5.get(indexM5);
				fTapeM5.realtimeBar(barM5);
				}
			}
		++fNumHits;
		publish(hit);
		}

	private void log(String fmt, Object... args) { SBLog.format(fmt, args); }
	}
