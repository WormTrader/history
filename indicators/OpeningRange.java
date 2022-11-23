package com.wormtrader.history.indicators;
/********************************************************************
* @(#)OpeningRange.java	1.00 20120420
* Copyright © 2012-2013 by Richard T. Salamone, Jr. All rights reserved.
*
* OpeningRange: This study assumes that it is being displayed on a
* chart of 5-minute bars
*
* @author Rick Salamone
* @version 1.00
* 20120420 rts created
* 20120603 rts rewrite to fix bugs
* 20120615 rts using BarList
* 20130407 rts uses bar hhmm() method
*******************************************************/
import com.wormtrader.bars.Bar;
import com.wormtrader.bars.BarList;
import com.wormtrader.history.TapeStudy;
import com.shanebow.ui.graph.TGGraph;
import com.shanebow.ui.graph.GraphClipRenderer;
import java.awt.Color;

final public class OpeningRange
	implements TapeStudy, GraphClipRenderer
	{
	public static final String STUDY_NAME="Opening Range";
	public String getName() { return STUDY_NAME; }
	private static final String START_BAR_TIME = "09:30";
	private static final String END_BAR_TIME = "09:55";

	private BarList   m_bars;
	private Color[]       fColors = { Color.PINK };
	private int[]         fRange = { Integer.MAX_VALUE, 0 };
	private int           fStartOfDayIndex = -1;
	private int           fFirstVisibleIndex = -1;

	public OpeningRange( BarList bars )
		{
		this ( bars, "" );
		}
	public OpeningRange( BarList bars, String params )
		{
		m_bars = bars;
		setParams(params);
		reset();
		}

	public String getParams() { return ""; }
	public void setParams( String params )
		{
		if ((params == null) || params.isEmpty())
			return;
		System.err.println( getClass().getName() + ".setParams: NOT IMPLEMENTED");
		}

	public String getRangeString(int value)
		{
		return (fFirstVisibleIndex == -1)? ""
		     : ("OR: " + fRange[0] + " - " + fRange[1]);
		}
	public String getMetrics(int index) { return ""; }
	public String getClipLabel() { return ""; }
	public String getToolTipText(int x) { return STUDY_NAME; }

	public void setColor(Color c) { fColors[0] = c; }
	public int getRangeMaximum() { return fRange[1]; }
	public int getRangeMinimum() { return fRange[0]; }

	private void reset()
		{
		fRange[0] = Integer.MAX_VALUE; // lo range
		fRange[1] = 0;
		fFirstVisibleIndex = -1;
		fStartOfDayIndex = -1;
		int size = m_bars.size();
		for (int i = 0; i < size; i++)
			process(i);
		}

	public void dataChanged(int index)
		{
		if (index == 0)
			reset();
		else process(index);
		}

	private void process(int index)
		{
		Bar bar = m_bars.get(index);
		String hhmm = bar.hhmm();
		if ( hhmm.charAt(0) == '0' )  // OR bars all start with "09:"
			{
			if ( fRange[0] == Integer.MAX_VALUE // no first bar yet or missing 9:30 bar
			||   hhmm.equals(START_BAR_TIME))
				{
				initRange(bar);
				fFirstVisibleIndex = -1;
				fStartOfDayIndex = index;
				}
			else expandRange(bar);
			if ( hhmm.equals(END_BAR_TIME))
				fFirstVisibleIndex = index;
			}
		else if ( index >= fStartOfDayIndex // this should only happen when
		     && index <= fFirstVisibleIndex ) // a bar is edited
			{
			reset();
			}
		}

	private void initRange(Bar bar)
		{
		fRange[0] = bar.getLow(); // lo range
		fRange[1] = bar.getHigh();
		}

	private void expandRange(Bar bar)
		{
		int lo = bar.getLow();
		if ( lo < fRange[0] ) fRange[0] = lo;
		int hi = bar.getHigh();
		if ( hi > fRange[1] ) fRange[1] = hi;
		}

	public void plot ( TGGraph graph, byte clip, int index )
		{
		if ( index < fFirstVisibleIndex ) return;
		graph.setColor( fColors[0] );
		graph.dash(clip, fRange[0]);
		graph.dash(clip, fRange[1]);
		}
	}
