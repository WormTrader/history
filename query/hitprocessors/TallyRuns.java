package com.wormtrader.history.query.hitprocessors;
/********************************************************************
* @(#)TallyRuns.java 1.00 20130404
* Copyright © 2013 by Richard T. Salamone, Jr. All rights reserved.
*
* TallyRuns: A HitProcessor that calculates the price
* change for each hit after 15 minutes, at the end of the day, and
* at the end of the run.
*
* The end of the run is defined as the next fractal bar following
* the hit.
*
* @author Rick Salamone
* @version 1.00
* 20130404 rts created
*******************************************************/
import com.wormtrader.bars.Bar;
import com.wormtrader.bars.BarList;
import com.wormtrader.dao.PNL;
import com.wormtrader.dao.USD;
import com.wormtrader.dao.TallyUpDown;
import com.wormtrader.history.indicators.Fractals;
import com.wormtrader.history.query.*;
import com.shanebow.util.SBDate;
import com.shanebow.util.SBFormat;

public final class TallyRuns
	implements HitProcessor
	{
	public static final String[] HEADERS =
		{
		"Symbol", "When", "Base $", "5M\u0394$", "EOD \u0394$", "Run \u0394$", "Run Bars"
		};
	public static final Class[] CLASSES =
		{
		String.class, String.class, USD.class, PNL.class, PNL.class, PNL.class, Integer.class
		};

	public final Class[]  classes() { return CLASSES; }
	public final String[] headers() { return HEADERS; }

	/**
	* These are the lists of bars that are feed to the Query.
	* When the Query identifies a hit, we look into these lists
	* to determine the price action following the hit.
	*/
	private BarList barsM5;
	Fractals fractals;

	TallyUpDown atEOD = new TallyUpDown("at End of Day");
	TallyUpDown after5M = new TallyUpDown("after 15 min");
	TallyUpDown runs = new TallyUpDown("5 min runs");

	/**
	* @param BarList aM5Bars - this list will be refilled every day with
	* with the bars the query is going to feed to the Tape for that day
	*/
	public void startRun(BarList aM5Bars)
		{
		barsM5 = aM5Bars;
		fractals = new Fractals(barsM5);
		}

	/**
	* @param BarList aM5Bars - this list will be refilled every day with
	* with the bars the query is going to feed to the Tape for that day
	*/
	public void startDay(String symbol, String yyyymmdd)
		{
		fractals.dataChanged(0);
		}

	/**
	* @return a HTML formatted String summarizing all the hits
	*/
	public String summary(int aNumHits, int aNumDays)
		{
		int hitsPerDayX100 = (aNumDays == 0)? 0 : ((100 * aNumHits)/aNumDays);
		return "<html><code><b>" + aNumHits + "</b> hits in <b>"
		     + aNumDays + "</b> days = <b>" + SBFormat.toDollarString(hitsPerDayX100)
				+ "</b> hits/day/symbol"
		     +"<br>"+ after5M.html() +"<br>"+ atEOD.html() +"<br>"+ runs.html();
		}

	/**
	* @return the index int bars after skipping the run
	*/
	public QueryHit createHit(String aSymbol, Bar aBar, int indexM5)
		{
//		if ( barsM5.get(indexM5+1).getHigh() <= aBar.getHigh()) // didn't enter
//			return null;
		int priceNow = aBar.getClose();
		TRHPQueryHit hit = new TRHPQueryHit(aSymbol, aBar.getTime(), priceNow);
		hit.process(barsM5, aBar, indexM5, this); // aHit.setRunLength(i-indexM5);
		return hit;
		}

private final class TRHPQueryHit
	extends QueryHit
	{
	private PNL fEodDelta;
	private PNL f5mDelta;
	private PNL fRunDelta;

	public TRHPQueryHit(String aSymbol, long aTime, int aBasePrice)
		{
		super(aSymbol, aTime, aBasePrice);
		}

	@Override public Object field(int aIndex)
		{
		return (aIndex == 0)? fSymbol
		     : (aIndex == 1)? SBDate.yyyymmdd__hhmmss(fTime)
		     : (aIndex == 2)? fBasePrice
		     : (aIndex == 3)? f5mDelta
		     : (aIndex == 4)? fEodDelta
		     : (aIndex == 5)? fRunDelta
		     : (aIndex == 6)? getRunLength()
		     : "?";
		}

	private void setEodDelta(int cents) { fEodDelta = new PNL(cents); }
	private void set5mDelta(int cents) { f5mDelta = new PNL(cents); }
	private void setRunDelta(int cents) { fRunDelta = new PNL(cents); }

	@Override public String toString()
		{
		return fSymbol + " " + SBDate.yyyymmdd__hhmmss(fTime)
				+ fBasePrice
		     + ((f5mDelta==null)? "--" : f5mDelta.toString())
		     + ((fEodDelta==null)? "--" : fEodDelta.toString());
		}

	void process(BarList barsM5, Bar aBar, int indexM5, TallyRuns QW)
		{
		int priceNow = fBasePrice.cents();
		int sizeM5 = barsM5.size();
		int price5M = barsM5.get(indexM5+1).getClose();
		set5mDelta(after5M.delta(price5M,priceNow));
		int priceEOD = barsM5.get(sizeM5-1).getClose();
		setEodDelta(atEOD.delta(priceEOD,priceNow));
		int nextUp = fractals.nextUp(indexM5);
		int nextDn = fractals.nextDown(indexM5);
		int runTo = (nextUp < nextDn)? barsM5.get(nextUp).getHigh()
		          : (nextDn < nextUp)? barsM5.get(nextDn).getLow()
		          :                    barsM5.get(nextUp).midpoint();
		setRunDelta(runs.delta(runTo,priceNow));
		int runEnd = Math.min(nextUp, nextDn);
		setRunLength(runEnd-indexM5);
		}
	}
	}
