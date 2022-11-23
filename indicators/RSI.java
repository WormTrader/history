package com.wormtrader.history.indicators;
/********************************************************************
* @(#)RSI.java 1.00 2009
* Copyright © 2009-2013 by Richard T. Salamone, Jr. All rights reserved.
*
* RSI: Relative Strength Index - Developed by Welles Wilder
*
* @author Rick Salamone
* @version 1.00
* 2009     rts created
* 20130206 rts using BarList
* 20130510 rts bug fix - not calling dataChanged(0) when default params
*******************************************************/
import com.wormtrader.bars.Bar;
import com.wormtrader.bars.BarList;
import com.wormtrader.history.TapeStudy;
import com.shanebow.ui.graph.TGGraph;
import com.shanebow.ui.graph.GraphClipRenderer;
import com.shanebow.util.SBMisc;
import java.awt.Color;

public final class RSI
	implements TapeStudy, GraphClipRenderer
	{
	public static final String STUDY_NAME="RSI";
	public String getName() { return STUDY_NAME; }

	public static final int OVERBOUGHT = 70;
	public static final int OVERSOLD = 30;
	public static final int DEFAULT_PERIOD = 14;

	private final BarList m_bars;
	private Color[]       m_color = { Color.ORANGE };
	private int           m_period = DEFAULT_PERIOD;
	private int[]         m_rsi;
	private int           m_index = -1; // index of last valid data
	private static int    _capacityIncrement = 80;

	public RSI( BarList bars ) { this ( bars, "" ); }
	public RSI( BarList bars, String params )
		{
		m_bars = bars;
		int length = bars.size();
		if ( length < _capacityIncrement )
			length = _capacityIncrement;
		m_rsi = new int[length];
		setParams(params);
		}

	private void enlargeArrays()
		{
		m_rsi = SBMisc.arrayExpand ( m_rsi, _capacityIncrement );
		}

	public String getParams() { return "" + m_period; }

	public void setParams( String params )
		{
		int period = DEFAULT_PERIOD;
		try { period = Integer.parseInt(params.trim()); }
		catch (Exception e) {}
		m_period = period;
		dataChanged(0);
		}

	public String getRangeString(int value)
		{
		return "RSI: " + value;
		}
	public String getClipLabel() { return "RSI(" + m_period + ")"; }
	public String getToolTipText(int x)
		{
		return "RSI(" + m_period + "): " + m_rsi[x];
		}

	public void setColor(Color c) { m_color[0] = c; }
	public int getRangeMaximum() { return 100; }
	public int getRangeMinimum() { return   0; }

	public void dataChanged(int index)
		{
		int numBars = m_bars.size();
		for ( int i = index; i < numBars; i++ )
			compute(i);
		}

	private void compute(int index)
		{
		if ( m_rsi.length <= index )
			enlargeArrays();
		m_index = index;
		int sumUps = 0;   // sum of changes for up days
		int sumDowns = 0; //  and down days over the period
		int count = (++index < m_period)? index : m_period;
		for ( int i = 0; i < count; i++ )
			{
			int change = getChange( --index );
			if ( change < 0 ) sumDowns -= change;
			else sumUps += change;
			}
		int denominator = sumDowns + sumUps;
		if ( denominator == 0 ) denominator = 1;
		m_rsi[m_index] = 100 - ((100 * sumDowns) / denominator);
		}

	public void plot ( TGGraph graph, byte clip, int index )
		{
		if ( index < 1 ) return;
		graph.setColor( Color.GRAY ); // plot overbought
		graph.connect ( clip, OVERSOLD, OVERSOLD );
		graph.connect ( clip, OVERBOUGHT, OVERBOUGHT );
		graph.setColor( Color.LIGHT_GRAY ); // plot center line
		graph.connect ( clip, 50, 50 );
		graph.setColor( m_color[0] ); // plot smoothed value
		graph.connect ( clip, m_rsi[index-1], m_rsi[index] );
		}

	public String getMetrics ( int index ) { return "" + m_rsi[index]; }
	public int getValue ( int index ) { return m_rsi[index]; }
	public int getChange ( int index )
		{
		Bar bar = m_bars.get(index);
		int close = bar.getClose();
		int prevClose = (index == 0)? bar.getOpen()
		                            : m_bars.get(index-1).getClose();
		return (close - prevClose);
		}
	}
