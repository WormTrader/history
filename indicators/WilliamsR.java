package com.wormtrader.history.indicators;
/********************************************************************
* @(#)WilliamsR.java 1.00 ????????
* Copyright © 2007-2013 by Richard T. Salamone, Jr. All rights reserved.
*
* WilliamsR: Calculates Williams %R as developed by Larry Williams.
*
* The following formula is used to calculate the value of the Williams %R: 
* %R = (high_over_period - close) / (high_over_period - low_over_period) 
* The typical number of periods is 14. 
* The range is from 0 to -100, where values above -20 are considered
* overbought and values below -80 are considered oversold.
*
* @author Rick Salamone
* @version 1.00
* 20120710 rts created
* 20130206 rts use BarList for bars
*******************************************************/
import com.wormtrader.bars.Bar;
import com.wormtrader.bars.BarList;
import com.wormtrader.history.TapeStudy;
import com.shanebow.ui.graph.TGGraph;
import com.shanebow.ui.graph.GraphClipRenderer;
import com.shanebow.util.SBFormat;
import com.shanebow.util.SBMisc;
import java.awt.Color;

final public class WilliamsR
	implements TapeStudy, GraphClipRenderer
	{
	public static final String STUDY_NAME="Williams %R";
	public String getName() { return STUDY_NAME; }

	private static final int DEFAULT_PERIOD = 14;
	private static final int SCALE_FACTOR = 100;

	private final BarList m_bars;
	private int         m_maxValue = 0;
	private Color[]     m_color = { Color.RED, Color.BLUE };
	private int         m_period;
	private int[]       m_values;      // %R * SCALE_FACTOR as int
	private int         m_index = -1;  // index of last valid data
	private static int  _capacityIncrement = 40;

	public WilliamsR( BarList bars ) { this ( bars, "" ); }
	public WilliamsR( BarList bars, String params )
		{
		m_bars = bars;
		int length = bars.size();
		if ( length < _capacityIncrement )
			length = _capacityIncrement;
		m_values = new int[length];
		setParams(params);
		}

	private void enlargeArrays()
		{
		m_values = SBMisc.arrayExpand ( m_values, _capacityIncrement );
		}

	public String getParams() { return "" + m_period; }
	public void setParams( String params )
		{
		int period = DEFAULT_PERIOD;
		try { period = Integer.parseInt(params.trim()); }
		catch (Exception e) {System.out.println(STUDY_NAME + " params: '" + params + "' " + e);  }
		if (m_period == period) return; // unchanged
		m_period = period;
		dataChanged(0);
		}

	public String getRangeString(int value)
		{
		return STUDY_NAME + ": " + SBFormat.toDollarString(value);
		}
	public String getClipLabel() { return "%R(" + m_period + ")"; }
	public String getToolTipText(int x) { return getRangeString(get(x)); }

	public void setColor(Color c) { m_color[0] = c; }
	public int getRangeMaximum() { return 0; }
	public int getRangeMinimum() { return -100 * SCALE_FACTOR; }

	public void dataChanged(int index)
		{
		int numBars = m_bars.size();
		for ( int i = Math.max(m_period,index); i < numBars; i++ )
			compute(i);
		}

	private void compute(int index)
		{
//		if (index < m_period) return; // this cannot happen!

		Bar bar = m_bars.get(index);
		int close = bar.getClose();
		int periodH = bar.getHigh();
		int periodL = bar.getLow();
		for ( int i = 1; i < m_period; i++ )
			{
			bar = m_bars.get(index-i);
			int barH = bar.getHigh();
			int barL = bar.getLow();
			if (barH > periodH) periodH = barH;
			if (barL < periodL) periodL = barL;
			}
		set(index, (-100 * SCALE_FACTOR * (periodH - close)) / (periodH - periodL));
		}

	public void plot ( TGGraph graph, byte clip, int index )
		{
		graph.setColor( m_color[0] ); // plot overbought/oversold
		graph.dash( clip, -20 * SCALE_FACTOR);
		graph.dash( clip, -80 * SCALE_FACTOR);
		if ( index < m_period + 1) return;
		graph.setColor( m_color[1] ); // plot smoothed temp
		graph.connect ( clip, get(index-1), get(index));
		}

	public String getMetrics( int index ) { return "" + get(index); }

	private void set ( int index, int scaledValue )
		{
		int slot = index - m_period;
		if ( m_values.length <= slot )
			enlargeArrays();
		m_values[slot] = scaledValue;
		}

	/**
	* @return scaled %R (i.e. %R * SCALE_FACTOR
	*/
	private int get ( int index )
		{
		if (index < m_period) return 0;
		int slot = index - m_period;
		return m_values[slot];
		}

	/**
	* @return %R truncated to nearest integral value
	*/
	public int getValue( int index ) { return m_values[index] / SCALE_FACTOR; }
	}
