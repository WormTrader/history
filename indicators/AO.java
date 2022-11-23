package com.wormtrader.history.indicators;
/********************************************************************
* @(#)AO.java 1.01 20120722
* Copyright © 2007 - 2012 by Richard T. Salamone, Jr. All rights reserved.
*
* AO: Implements Bill Williams Awesome Oscillator as described on
* page 126 of Trading Chaos, 2nd Edition:
*          "The Awesome Oscillator provides us with the
*                         keys to the kingdom.
*   The AO measures the immediate momentum of the past 5 price bars,
*   compared to the momentum of the last 34 bars.
*   It is a 34-bar simple moving average of the bar’s midpoints (H–L)/2
*   subtracted from a 5-bar simple moving average of the midpoints (H–L)/2,
*   plotted in a histogram form.
*   The AO tells us exactly what is happening with the current momentum."
* 
* @author Rick Salamone
* @version 1.0
* 20120722 rts created
*******************************************************/
import com.wormtrader.bars.Bar;
import com.wormtrader.bars.BarList;
import com.wormtrader.history.TapeStudy;
import com.shanebow.ui.graph.TGGraph;
import com.shanebow.ui.graph.GraphClipRenderer;
import java.awt.Color;

final public class AO
	implements TapeStudy, GraphClipRenderer
	{
	public static final String STUDY_NAME="AO";
	public String getName() { return STUDY_NAME; }

	private final BarList m_bars;
	private int           m_minValue = 0;
	private int           m_maxValue = 0;
	private Color         m_color = Color.WHITE;
	private int           m_slowPeriod = 34;
	private int           m_fastPeriod = 5;
	private MovingAverage m_maSlow; // 34-bar SMA of the bar's midpoints (H–L)/2
	private MovingAverage m_maFast; // 5-bar SMA of the midpoints (H–L)/2

	public AO( BarList bars, String params )
		{
		m_bars = bars;
		setParams(params);
		}

	public String getParams() { return ""; }
	public void setParams( String params )
		{
		m_maSlow = new SMA(m_bars, "" + m_slowPeriod + ",M");
		m_maFast = new SMA(m_bars, "" + m_fastPeriod + ",M");
		dataChanged(0);
		if ((params == null) || params.isEmpty())
			return;
		System.err.println( getClass().getName() + ".setParams: NOT IMPLEMENTED");
		}

	public String getRangeString(int value)
		{
		return "" + value;
		}
	public String getClipLabel() { return "AO"; }
	public String getToolTipText(int x)
		{
		return "" + getValue(x);
		}

	public void setColor(Color c) { m_color = c; }
	public int  getRangeMaximum() { return m_maxValue + 2; }
	public int  getRangeMinimum() { return m_minValue - 2; }
	public void dataChanged(int index)
		{
		if ( index == 0 )
			m_minValue = m_maxValue = 0;

		m_maSlow.dataChanged(index);
		m_maFast.dataChanged(index);
		int numBars = m_bars.size();
		for ( int i = index; i < numBars; i++ )
			{
			int value = getValue(i);
			if ( value > m_maxValue ) m_maxValue = value;
			if ( value < m_minValue ) m_minValue = value;
			}
		}

	public void plot( TGGraph graph, byte clip, int index )
		{
		int value = getValue(index);
		int prev = (index == 0)? 0 : getValue(index-1);
		int diff = value - prev;
		graph.setColor((diff < 0)? Color.RED : (diff > 0)? Color.GREEN : Color.YELLOW);
		graph.vLine ( clip, 0, value);
		}

	public String getMetrics( int index )
		{ return "" + m_maSlow.getValue(index) + "," + m_maFast.getValue(index); }
	public int getValue( int index )
		{
		return m_maFast.getValue(index) - m_maSlow.getValue(index);
		}

	public int position(int index)
		{
		int value = getValue(index);
		return (value > 5)? 1
		     : (value < -5)? -1
		     : 0;
		}

	public int trend(int index)
		{
		if (index < 2) return 0;
		int now = getValue(index);
		int then = getValue(index-1);
		return (now > then)? 1
		     : (then > now)? -1
		     : 0;
		}
	}
