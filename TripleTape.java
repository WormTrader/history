package com.wormtrader.history;
/********************************************************************
* @(#)TripleTape.java	1.00 2009????
* Copyright © 2010-2013 by Richard T. Salamone, Jr. All rights reserved.
*
* TripleTape: A collection of three Tape objects for a given symbol:
* Each tape holds a different bar size (i.e. 5 min, 60 min, 1 day).
* Supports simulations (back testing and paper trading) as well as
* live data.
*
* @author Rick Salamone
* @version 1.00
* 2009???? rts created
* 20120325 rts set go back on 5 minutes to 3 days - was 2 days
* 20120608 rts removed go back from here, moved to Tape user property
* 20121109 rts gets bar sizes from properties - can be any number
* 20130218 rts modified for changes to BarSize names
* 20130227 rts removed hardcoded time frames
* 20130304 rts removed special handling for ChartTrainer
*******************************************************/
import com.wormtrader.bars.BarSize;
import com.wormtrader.history.Tape;
import com.wormtrader.history.event.*;
import com.shanebow.util.SBDate;
import com.shanebow.util.SBProperties;

public class TripleTape
	{
	public static BarSize[] BAR_SIZES;
	protected static int TIME_FRAMES;
	static
		{
		int[] sizeParams = SBProperties.getInstance().getIntArray("app.tape.sizes",11,9,7);
		TIME_FRAMES = sizeParams.length;
		BAR_SIZES = new BarSize[TIME_FRAMES];
		for (int i= 0; i < TIME_FRAMES; i++)
			BAR_SIZES[i] = BarSize.find(sizeParams[i]);
		}
	public static int numTimeFrames() { return TIME_FRAMES; }
	public static int shortestDuration()
		{
		return BAR_SIZES[TIME_FRAMES-1].duration();
		}

	public static byte indexOf(BarSize aBarSize)
		{
		for (byte i = 0; i < TIME_FRAMES; i++)
			if (BAR_SIZES[i].equals(aBarSize))
				return i;
		return (byte)-1;
		}

	private Tape[] m_tapes;

	public TripleTape( String symbol ) { this( symbol, 0 ); }
	public TripleTape( String symbol, long time )
		{
		m_tapes = new Tape[TIME_FRAMES];

		for ( int i = 0; i < TIME_FRAMES; i++ )
			m_tapes[i] = new Tape( symbol, BAR_SIZES[i], time );
		// All other tapes listen to the smallest bar size to build their bars
		for ( int i = 0; i < TIME_FRAMES-1; i++ )
			m_tapes[TIME_FRAMES-1].addTapeListener ( m_tapes[i] );
		}

	public final Tape getTape( byte timeFrame ) { return m_tapes[timeFrame]; }
	public final Tape getTape( BarSize bs )
		{
		for ( Tape tape : m_tapes )
			if ( tape.getBarSize().equals(bs))
				return tape;
		return null;
		}

	public final Tape[] getTapes() { return m_tapes; }
	public final String getSymbol(){ return m_tapes[0].getSymbol(); }
	public final String toString()
		{
		return getClass().getSimpleName() + "(" + getSymbol() + ")";
		}

	public final void disableBackfill()
		{
		for ( Tape tape : getTapes())
			tape.setGoBack(null);
		}
	}
