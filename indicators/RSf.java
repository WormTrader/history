package com.wormtrader.history.indicators;
/********************************************************************
* @(#)Tape.java 1.00 20120615
* Copyright © 2012-2013 by Richard T. Salamone, Jr. All rights reserved.
*
* RSf: Computes the relative strength of a security vs the SPY.
*
* CURRENTLY ONLY WORKS FOR 1 TIME FRAME SINCE THE SPY BARS ARE STATIC!!
* Formula given: (((meNow ÷ meThen) ÷ (spNow ÷ spThen)) -1) × 100
*  which simplifies to ((meNow * spThen / (meThen * spNow)) - 1) * 100;
*
* @version 1.00
* @author Rick Salamone
* 20120615 rts using BarList to replace vector of Bar
*******************************************************/
import com.wormtrader.bars.Bar;
import com.wormtrader.bars.BarList;
import com.wormtrader.bars.BarSize;
import com.wormtrader.history.TapeStudy;
import com.shanebow.ui.graph.TGGraph;
import com.shanebow.ui.graph.GraphClipRenderer;
import com.shanebow.util.SBDate;
import java.awt.Color;

public final class RSf
	implements TapeStudy, GraphClipRenderer
	{
	public static final String STUDY_NAME="RSf";
	public String getName() { return STUDY_NAME; }
	public static final int DEFAULT_PERIOD=100;

	/**
	* This is a lazily loaded list of SPY bars which should
	* correspond to fBars (by dates). Note that it is static
	* since the programs will always use the same backfill
	* and dates for all symbols being followed. Needs to be
	* a BarList (as opposed to BarList because we need a
	* search method to find bars of a given date.
	*/
	private static BarList _spBars;

	private BarList fBars;
	private Color       fColor = Color.WHITE;
	private int         fPeriod = DEFAULT_PERIOD; // pretty arbitrary

	/**
	* prevIndex and prevValue are cached, because we will usually
	* plot values in order by connecting the prevValue to the newly
	* calculated value. This saves having to recalculate the prev
	* value every time.
	*/
	private int prevIndex = -1;
	private int prevValue;

	public RSf( BarList bars ) { this ( bars, "" ); }
	public RSf( BarList bars, String params )
		{
		fBars = bars;
		setParams(params);
		}

	public String getParams() { return ""; }
	public void setParams( String params )
		{
		dataChanged(0);
		if ((params == null) || params.isEmpty())
			return;
		System.err.println( getClass().getName() + ".setParams: NOT IMPLEMENTED");
		}

	public void   setColor(Color c) { fColor = c; }

	public String getRangeString(int value) { return "RSf: " + value; }
	public String getClipLabel() { return "RSf"; }
	public String getToolTipText(int x) { return ""; }
	public int    getRangeMaximum() { return 100; }
	public int    getRangeMinimum() { return 0; }

	public void   dataChanged(int index)
		{
		// We always compute on the fly, but cache the prev values for plotting
		if (index == 0)
			{
			prevIndex = -1; // will force a recalc;
			if (_spBars != null // have S&P data, ensure goes back enough
			&& fBars.size() > 0 &&  _spBars.size() > 0
			&& fBars.get(0).getTime() > _spBars.get(0).getTime())
				_spBars = null;
			}
		if ( _spBars == null && fBars.size() > 0)
			{
			long[] dateRange = { fBars.get(0).getTime(), SBDate.timeNow() };
			_spBars = new BarList();
			_spBars.fetch("SPY", BarSize.ONE_DAY, dateRange);
		System.out.println("RSf reloaded SPY from " + SBDate.yyyymmdd(dateRange[0]));
			}
		}

	/**
	* Get the S&P bar for the specified time. The index is really a hint,
	* but we expect that _spBars corresponds to fBars. So we read the S&P
	* bar at the given index, then verify that it's time matches the expected
	* time. If not, we got the wrong bar, and are forced to do a search to
	* find the right one.
	*/
	private Bar spBar(int aIndex, long aTime)
		{
		Bar sp = _spBars.get(aIndex);
		if ( sp.getTime() != aTime )
			{
		System.out.println("RSf time mismatch: want " + aTime + " got " + sp.getTime());
			sp = _spBars.find(aTime); // does the binary search
			}
		return sp;
		}

	/**
	* Formula given: (((meNow ÷ meThen) ÷ (spNow ÷ spThen)) -1) × 100
	*  which simplifies to ((meNow * spThen / (meThen * spNow)) - 1) * 100;
	*/
	private int compute(int index)
		{
		int then = index - fPeriod;
		if (then < 0) return 0; // insufficient data

		try
			{
			Bar meNow = fBars.get(index);
			Bar spNow = spBar(index, meNow.getTime());
			Bar meThen = fBars.get(then);
			Bar spThen = spBar(then, meThen.getTime());
double it = ( (double)meNow.getClose() / meThen.getClose())
 / ( (double)spNow.getClose()/ spThen.getClose());
//			double it = ((double)spThen.getClose() * meNow.getClose())
//			          / ((double)meThen.getClose() * spNow.getClose());
System.out.println(meNow.yyyymmdd() + " RSf " + it);
			return (int)((it - 1.0) * 100);
			}
		catch (Exception e)
			{
			System.out.println("RSf exception at index: " + index + ": " + e);
			return 0;
			}
		}

	public void plot ( TGGraph graph, byte clip, int index )
		{
		if (index < fPeriod+1) return;
		if (prevIndex != index - 1)
			prevValue = compute(index-1);
		int thisValue = compute(index);
		graph.setColor( fColor );
		graph.connect( clip, prevValue, thisValue);
		prevIndex = index;
		prevValue = thisValue;
		}
	public String getMetrics(int index) { return ""; }
	public int getValue(int index) { return compute(index); }
	}
