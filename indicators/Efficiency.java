package com.wormtrader.history.indicators;
/********************************************************************
* @(#)Efficiency.java 1.00 20120416
* Copyright © 2012-2013 by Richard T. Salamone, Jr. All rights reserved.
*
* Efficiency - Developed by Perry Kaufman, in his book, "Smarter Trading"
* and explained in Van Tharp's "Trade Your Way To Financial Freedom" pg 191.
*
* @author Rick Salamone
* @version 1.0
* 20120416 rts created
* 20130302 rts uses BarList
*******************************************************/
import com.wormtrader.bars.Bar;
import com.wormtrader.bars.BarList;
import com.wormtrader.history.TapeStudy;
import com.shanebow.ui.graph.TGGraph;
import com.shanebow.ui.graph.GraphClipRenderer;
import com.shanebow.util.SBMath;
import com.shanebow.util.SBMisc;
import java.awt.Color;

public final class Efficiency
	implements TapeStudy, GraphClipRenderer
	{
	public static final String STUDY_NAME="Efficiency";
	public String getName() { return STUDY_NAME; }

	private static final int DEFAULT_PERIOD = 10;
	private static int  _capacityIncrement = 80;
	private BarList m_bars;
	private Color[]   m_color = { Color.YELLOW };
	private int       m_period = DEFAULT_PERIOD;
	private int[]     m_efficiency;   // smoothed force index
	private int       m_index = -1; // index of last valid data

	public Efficiency( BarList bars )
		{
		this ( bars, "" );
		}
	public Efficiency( BarList bars, String params )
		{
		m_bars = bars;
		int length = bars.size();
		if ( length < _capacityIncrement )
			length = _capacityIncrement;
		m_efficiency = new int[length];
		setParams(params);
		}

	private void enlargeArrays()
		{
		m_efficiency = SBMisc.arrayExpand ( m_efficiency, _capacityIncrement );
		}

	public String getParams() { return ""; }
	public void setParams( String params )
		{
		dataChanged(0);
		if ((params == null) || params.isEmpty())
			return;
		System.err.println( getClass().getName() + ".setParams: NOT IMPLEMENTED");
		}

	public String getRangeString(int value)
		{
		return "Efficiency: " + value;
		}
	public String getClipLabel() { return "Efficiency"; }
	public String getToolTipText(int x)
		{
		return "<font color=" + ((m_efficiency[x] >= 0)? "BLUE>" : "RED>")
		      + m_efficiency[x] + "%";
		}

	public void setColor(Color c) { m_color[0] = c; }
	public int getRangeMaximum() { return 100; }
	public int getRangeMinimum() { return -100; }

	public void dataChanged(int index)
		{
		int numBars = m_bars.size();
		for ( int i = index; i < numBars; i++ )
			compute(i);
		}
	public void plot ( TGGraph graph, byte clip, int index )
		{
		if ( index < m_period ) return;
		graph.setColor( Color.GRAY ); // plot zero line
		graph.connect ( clip, 0, 0 );
		graph.setColor( m_color[0] ); // plot indicator
		graph.connect ( clip, m_efficiency[index-1], m_efficiency[index] );
		}

	public String getMetrics ( int index ) { return "" + m_efficiency[index]; }
	public int getValue ( int index ) { return m_efficiency[index]; }

	private void compute(int index)
		{
		if ( m_efficiency.length <= index )
			enlargeArrays();
		m_index = index;
		if ( index < m_period )
			return;
		m_efficiency[index] = efficiency(index);
		}

	public int efficiency ( int index )
		{
		int i = index - m_period;
		int firstClose = m_bars.get(i).getClose();
		int prevClose = firstClose;
		int volatility = 0; // sum abs value of close today - close yest over period
		while ( ++i <= index )
			{
			int thisClose = m_bars.get(i).getClose();
			volatility += Math.abs(thisClose - prevClose);
			prevClose = thisClose;
			}
		int speed = prevClose - firstClose;
		return 100 * speed / volatility;
		}
	}
